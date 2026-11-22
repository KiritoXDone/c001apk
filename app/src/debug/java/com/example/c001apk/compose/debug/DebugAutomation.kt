package com.example.c001apk.compose.debug

import android.app.Activity
import androidx.activity.ComponentActivity
import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.example.c001apk.compose.logic.update.AppUpdates
import com.example.c001apk.compose.ui.main.AppDestination
import com.example.c001apk.compose.ui.main.AppNavigator
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject

/** Executes debug requests against the resumed activity and its Compose semantics tree. */
internal class DebugAutomation(
    private val app: Application,
    private val activity: Activity?,
) {

    fun execute(request: JSONObject): JSONObject =
        when (val command = request.optString("command")) {
            "status" -> status()
            "controls" -> listControls(request)
            "pointer" -> handlePointer(request)
            "text" -> handleText(request)
            "page" -> handlePage(request)
            "screenshot" -> captureWindow(request)
            "business" -> handleBusiness(request)
            "quit" -> quit()
            else -> throw IllegalArgumentException("Unknown debug command '$command'")
        }

    private fun status(): JSONObject {
        val packageInfo: PackageInfo =
            app.packageManager.getPackageInfo(app.packageName, 0)
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode
            else @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
        return JSONObject()
            .put("ok", true)
            .put("package", app.packageName)
            .put("version", packageInfo.versionName.orEmpty())
            .put("versionCode", versionCode)
            .put("pid", Process.myPid())
            .put("sdk", Build.VERSION.SDK_INT)
            .put("device", Build.MODEL)
            .put("activity", activity?.javaClass?.name ?: JSONObject.NULL)
            .put("resumed", activity != null)
            .put("animationScale", animationScale())
    }

    private fun animationScale(): Float =
        Settings.Global.getFloat(
            app.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )

    private fun listControls(request: JSONObject): JSONObject {
        val owner = requireComposeOwner()
        val limit = request.optInt("limit", 400)
        val nodes = onMain { collectNodes(owner) }
        val items = JSONArray()
        nodes.take(limit.coerceAtLeast(1)).forEach { items.put(describeNode(it)) }
        return JSONObject()
            .put("ok", true)
            .put("count", nodes.size)
            .put("items", items)
    }

    private fun handlePointer(request: JSONObject): JSONObject {
        val action = request.optString("action")
        val view = requireComposeView()
        var target =
            request.optJSONObject("target")?.let { resolveTarget(requireComposeOwner(), it) }
        if (target == null && action == "scroll") target = largestScrollable()
        val bounds = target?.boundsInRoot
        val x = request.optDouble("x", Double.NaN).takeUnless { it.isNaN() }?.toFloat()
            ?: bounds?.center?.x
            ?: throw IllegalArgumentException("Pointer action requires a target or x/y")
        val y = request.optDouble("y", Double.NaN).takeUnless { it.isNaN() }?.toFloat()
            ?: bounds?.center?.y
            ?: throw IllegalArgumentException("Pointer action requires a target or x/y")
        when (action) {
            "click" -> onMain { tap(view, x, y) }
            "longClick" -> onMain { longPress(view, x, y) }
            "scroll" -> {
                val dy = request.optInt("dy", 0)
                val dx = request.optInt("dx", 0)
                require(dx != 0 || dy != 0) { "Scroll requires dx or dy" }
                onMain { swipe(view, x, y, dx, dy) }
                // 手势事件按 16ms 节奏排程，等待其先完成再进入帧/稳定等待。
                Thread.sleep(240)
            }
            else -> throw IllegalArgumentException("Unsupported pointer action '$action'")
        }
        waitAfter(request)
        return JSONObject()
            .put("ok", true)
            .put("action", action)
            .put("x", x)
            .put("y", y)
            .put("target", target?.let { describeNode(it) } ?: JSONObject.NULL)
    }

    private fun handleText(request: JSONObject): JSONObject {
        val value = request.optString("text")
        require(value.isNotEmpty()) { "Text input requires a non-empty 'text'" }
        val view = requireComposeView()
        request.optJSONObject("target")?.let { selector ->
            val target = resolveTarget(requireComposeOwner(), selector)
            val center = target.boundsInRoot.center
            onMain { tap(view, center.x, center.y) }
            awaitFrames(2)
        }
        onMain {
            val connection = view.onCreateInputConnection(EditorInfo())
            requireNotNull(connection) { "No focused text field accepts input" }
            connection.commitText(value, 1)
            connection.finishComposingText()
        }
        waitAfter(request)
        return JSONObject().put("ok", true).put("text", value)
    }

    private fun handlePage(request: JSONObject): JSONObject {
        val current = requireActivity()
        val navigator = navigatorOf(current)
        return when (val action = request.optString("action")) {
            "open" -> {
                val name = request.optString("destination")
                val destination = destinationOf(name)
                onMain { navigator.navigate(destination) }
                waitAfter(request)
                JSONObject().put("ok", true).put("action", action).put("destination", name)
            }
            "back" -> {
                val popped = onMain { navigator.popBackStack() }
                if (!popped) onMain { (current as ComponentActivity).onBackPressedDispatcher.onBackPressed() }
                waitAfter(request)
                JSONObject().put("ok", true).put("action", action).put("popped", popped)
            }
            else -> throw IllegalArgumentException("Unsupported page action '$action'")
        }
    }

    private fun quit(): JSONObject {
        activity?.let { current -> onMain { current.finishAffinity() } }
        return JSONObject().put("ok", true).put("finished", activity != null)
    }

    private fun handleBusiness(request: JSONObject): JSONObject {
        val updates =
            EntryPointAccessors.fromApplication(app, AppDebugEntryPoint::class.java).appUpdates()
        return when (request.optString("action")) {
            "updateState" -> describeUpdateState(updates)
            "updateCheck" -> {
                updates.check(manual = true)
                val timeoutMs = request.optLong("timeoutMs", 25_000L).coerceIn(1_000L, 60_000L)
                val deadline = SystemClock.uptimeMillis() + timeoutMs
                while (updates.state.value.checking && SystemClock.uptimeMillis() < deadline) {
                    SystemClock.sleep(120L)
                }
                describeUpdateState(updates).put("completed", !updates.state.value.checking)
            }
            else -> throw IllegalArgumentException(
                "Unknown business action '" + request.optString("action") + "'",
            )
        }
    }

    private fun describeUpdateState(updates: AppUpdates): JSONObject {
        val state = updates.state.value
        val release = state.release
        return JSONObject()
            .put("ok", true)
            .put("checking", state.checking)
            .put("manual", state.manual)
            .put("hasUpdate", release != null)
            .put("version", release?.version?.toString() ?: JSONObject.NULL)
            .put("tag", release?.tag ?: JSONObject.NULL)
            .put("page", release?.page ?: JSONObject.NULL)
            .put("message", state.message ?: JSONObject.NULL)
    }

    private fun captureWindow(request: JSONObject): JSONObject {
        val current = requireActivity()
        return captureScreenshot(app, current, request)
    }

    private fun requireActivity(): Activity =
        activity ?: throw IllegalStateException("No resumed activity; start the app first")

    private fun requireComposeView(): View {
        val current = requireActivity()
        return onMain { findComposeView(current.window.decorView) }
            ?: throw IllegalStateException("Compose view is not attached yet")
    }

    private fun requireComposeOwner(): SemanticsOwner {
        val view = requireComposeView()
        val method = view.javaClass.getMethod("getSemanticsOwner")
        return method.invoke(view) as? SemanticsOwner
            ?: throw IllegalStateException("Compose owner is not available")
    }

    private fun findComposeView(view: View): View? {
        if (view.javaClass.methods.any { it.name == "getSemanticsOwner" && it.parameterCount == 0 }) {
            return view
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findComposeView(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun collectNodes(owner: SemanticsOwner): List<SemanticsNode> {
        val result = ArrayList<SemanticsNode>()
        val root = owner.rootSemanticsNode
        fun visit(node: SemanticsNode) {
            result += node
            node.children.forEach(::visit)
        }
        visit(root)
        return result.filter {
            it.isInteractive() || it.config.contains(SemanticsActions.OnClick) ||
                it.config.contains(SemanticsActions.ScrollBy)
        }.filter { !it.boundsInRoot.isEmpty }
    }

    private fun SemanticsNode.isInteractive(): Boolean =
        !testTag().isNullOrEmpty() || !description().isEmpty() || !text().isEmpty()

    private fun SemanticsNode.testTag(): String? =
        config.getOrNull(SemanticsProperties.TestTag)

    private fun SemanticsNode.description(): String =
        config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().joinToString(" ")

    private fun SemanticsNode.text(): String =
        config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString(" ")

    private fun describeNode(node: SemanticsNode): JSONObject {
        val bounds = node.boundsInRoot
        val role = node.config.getOrNull(SemanticsProperties.Role)
        return JSONObject()
            .put("id", node.id)
            .put("tag", node.testTag() ?: JSONObject.NULL)
            .put("desc", node.description())
            .put("text", node.text())
            .put("role", role?.toString() ?: JSONObject.NULL)
            .put("clickable", node.config.contains(SemanticsActions.OnClick))
            .put("enabled", !node.config.contains(SemanticsProperties.Disabled))
            .put("scrollable", node.config.contains(SemanticsActions.ScrollBy))
            .put(
                "bounds",
                JSONArray().put(bounds.left).put(bounds.top).put(bounds.right).put(bounds.bottom),
            )
    }

    private fun resolveTarget(owner: SemanticsOwner, selector: JSONObject): SemanticsNode {
        val tag = selector.optString("tag").takeIf { it.isNotEmpty() }
        val desc = selector.optString("desc").takeIf { it.isNotEmpty() }
        val text = selector.optString("text").takeIf { it.isNotEmpty() }
        require(tag != null || desc != null || text != null) {
            "Target requires tag, desc or text"
        }
        val contains = selector.optBoolean("contains", false)
        val nodes = onMain { collectNodes(owner) }
        val matches =
            nodes.filter { node ->
                (tag == null || node.testTag() == tag) &&
                    (desc == null || node.description().matchesText(desc, contains)) &&
                    (text == null || node.text().matchesText(text, contains))
            }
        require(matches.isNotEmpty()) {
            "No control matches ${selector.toString()}"
        }
        val index = selector.optInt("index", 0)
        require(index in matches.indices) {
            "Control index $index is outside ${matches.size} matches"
        }
        return matches[index]
    }

    private fun largestScrollable(): SemanticsNode? =
        onMain {
            collectNodes(requireComposeOwner())
                .filter { it.config.contains(SemanticsActions.ScrollBy) }
                .maxByOrNull { node ->
                    val bounds = node.boundsInRoot
                    bounds.width * bounds.height
                }
        }

    private fun String.matchesText(expected: String, contains: Boolean): Boolean =
        if (contains) contains(expected) else this == expected

    private fun tap(view: View, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        val up = MotionEvent.obtain(downTime, downTime + 60, MotionEvent.ACTION_UP, x, y, 0)
        try {
            view.dispatchTouchEvent(down)
            view.dispatchTouchEvent(up)
        } finally {
            down.recycle()
            up.recycle()
        }
    }

    private fun longPress(view: View, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        view.dispatchTouchEvent(down)
        down.recycle()
        view.postDelayed(
            {
                val upTime = SystemClock.uptimeMillis()
                val up = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, x, y, 0)
                view.dispatchTouchEvent(up)
                up.recycle()
            },
            700,
        )
    }

    private fun swipe(view: View, x: Float, y: Float, dx: Int, dy: Int) {
        val downTime = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        view.dispatchTouchEvent(down)
        down.recycle()
        val steps = 12
        for (step in 1..steps) {
            val fraction = step.toFloat() / steps
            view.postDelayed(
                {
                    val eventTime = SystemClock.uptimeMillis()
                    val action =
                        if (step == steps) MotionEvent.ACTION_UP else MotionEvent.ACTION_MOVE
                    val event =
                        MotionEvent.obtain(
                            downTime,
                            eventTime,
                            action,
                            x + dx * fraction,
                            y + dy * fraction,
                            0,
                        )
                    view.dispatchTouchEvent(event)
                    event.recycle()
                },
                step * 16L,
            )
        }
    }

    private fun waitAfter(request: JSONObject) {
        when (val wait = request.optString("wait", "settled")) {
            "none" -> Unit
            "frame" -> awaitFrames(2)
            "settled" -> awaitSettled()
            else -> throw IllegalArgumentException("Unsupported wait '$wait'")
        }
    }

    private fun awaitFrames(count: Int) {
        val latch = CountDownLatch(count)
        onMain {
            val choreographer = Choreographer.getInstance()
            val callback =
                object : Choreographer.FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        latch.countDown()
                        if (latch.count > 0) choreographer.postFrameCallback(this)
                    }
                }
            choreographer.postFrameCallback(callback)
        }
        check(latch.await(5, TimeUnit.SECONDS)) { "Frame await timed out" }
    }

    private fun awaitSettled() {
        val deadline = SystemClock.uptimeMillis() + 5_000
        var lastSignature: String? = null
        var stableSamples = 0
        while (SystemClock.uptimeMillis() < deadline) {
            Thread.sleep(120)
            val signature = onMain { semanticsSignature() }
            stableSamples = if (signature == lastSignature) stableSamples + 1 else 0
            lastSignature = signature
            if (stableSamples >= 2) return
        }
    }

    private fun semanticsSignature(): String {
        activity ?: return "detached"
        val owner = runCatching { requireComposeOwner() }.getOrElse { return "detached" }
        val nodes = collectNodes(owner)
        val builder = StringBuilder(nodes.size.toString())
        nodes.forEach { node ->
            val bounds = node.boundsInRoot
            builder.append('|').append(node.id).append(',')
                .append(bounds.left).append(',').append(bounds.top)
                .append(',').append(bounds.right).append(',').append(bounds.bottom)
        }
        return builder.toString()
    }

    private fun navigatorOf(current: Activity): AppNavigator {
        val field =
            current.javaClass.declaredFields.firstOrNull { it.name == "navigator" }
                ?: throw IllegalStateException("Activity has no navigator field")
        field.isAccessible = true
        return field.get(current) as? AppNavigator
            ?: throw IllegalStateException("Navigator is not ready yet")
    }

    private fun destinationOf(name: String): AppDestination =
        when (name) {
            "main" -> AppDestination.Main
            "settings" -> AppDestination.Settings()
            "about" -> AppDestination.About
            "license" -> AppDestination.License
            "accounts" -> AppDestination.Accounts
            "local-follows" -> AppDestination.LocalFollows
            "tab-editor" -> AppDestination.TabEditor
            "navigation-order" -> AppDestination.NavigationOrder
            "params" -> AppDestination.Params
            "content-filters" -> AppDestination.ContentFilters
            "twilight-rules" -> AppDestination.TwilightRules
            "device-import" -> AppDestination.DeviceImport
            "login" -> AppDestination.Login
            else -> throw IllegalArgumentException(
                "Unsupported destination '$name'; use one of " +
                    "main, settings, about, license, accounts, local-follows, tab-editor, " +
                    "navigation-order, params, content-filters, twilight-rules, " +
                    "device-import, login",
            )
        }
}
