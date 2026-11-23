package com.example.c001apk.compose.ui.feed.reply

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentDialog
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.example.c001apk.compose.BuildConfig
import com.example.c001apk.compose.R
import com.example.c001apk.compose.logic.settings.AppLanguage
import com.example.c001apk.compose.databinding.ActivityReplyBinding
import com.example.c001apk.compose.databinding.ItemCaptchaBinding
import com.example.c001apk.compose.logic.datastore.ReplyDraftImage
import com.example.c001apk.compose.logic.datastore.ReplyDraftStore
import com.example.c001apk.compose.logic.datastore.ReplyEditorDraft
import com.example.c001apk.compose.logic.model.VOTE_COMMENT_TYPE
import com.example.c001apk.compose.ui.component.createMiuixLoadingDialog
import com.example.c001apk.compose.ui.component.MiuixViewDialogBuilder
import com.example.c001apk.compose.ui.feed.reply.emoji.EmojiPagerAdapter
import com.example.c001apk.compose.ui.feed.reply.emoji.updateEmojiCategoryStyle
import com.example.c001apk.compose.ui.theme.DefaultFontScale
import com.example.c001apk.compose.ui.theme.miuixViewColors
import com.example.c001apk.compose.util.CookieUtil
import com.example.c001apk.compose.util.EmojiTextWatcher
import com.example.c001apk.compose.util.EmojiUtils
import com.example.c001apk.compose.util.EmojiUtils.pickerCategories
import com.example.c001apk.compose.util.FastDeleteAtUserKeyListener
import com.example.c001apk.compose.util.OssUploadUtil.ossUpload
import com.example.c001apk.compose.util.dp
import com.example.c001apk.compose.util.makeToast
import com.example.c001apk.compose.util.performConfiguredHapticFeedback
import com.example.c001apk.compose.view.SmoothInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Copyright (C) 2018 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/** 输入面板 */
@AndroidEntryPoint
class ReplyActivity :
    AppCompatActivity(),
    View.OnClickListener,
    SmoothInputLayout.OnVisibilityChangeListener {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    private lateinit var binding: ActivityReplyBinding
    private val viewModel by viewModels<ReplyViewModel>()
    private val type: String? by lazy { intent.getStringExtra("type") }
    private val voteCommentTarget by lazy {
        if (type == VOTE_COMMENT_TYPE) VoteCommentTarget.fromIntent(intent) else null
    }
    private val rid: String? by lazy { intent.getStringExtra("rid") }
    private val username: String? by lazy { intent.getStringExtra("username") }

    private val targetType: String? by lazy { intent.getStringExtra("targetType") }
    private val targetId: String? by lazy { intent.getStringExtra("targetId") }
    private val prefilledTopic by lazy {
        intent.getStringExtra("title")?.let { "#$it#" }.orEmpty()
    }
    private val draftStore by lazy { ReplyDraftStore(this) }
    private val draftKey by lazy {
        val editorType = type ?: return@lazy null
        val target = when (editorType) {
            "createFeed" -> listOf(editorType, targetType.orEmpty(), targetId.orEmpty())
            VOTE_COMMENT_TYPE -> voteCommentTarget?.let {
                listOf(editorType, it.voteId, it.anonymous.toString())
            }
            else -> rid?.takeIf { it.isNotBlank() }?.let { listOf(editorType, it) }
        }
        val account = CookieUtil.accountSession
        if (account.isLogin && account.uid.isNotBlank() && target != null) {
            draftStore.key(account.uid, target)
        } else {
            null
        }
    }
    private var draftReady = false
    private val saveDraftRunnable = Runnable { saveDraft() }

    private val imm by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val color by lazy { resolveSurfaceContainerColor() }
    private val imeScrimDrawable by lazy { ColorDrawable(color) }
    private val recentList = ArrayList<List<Pair<String, Int>>>()
    private val list = listOf(recentList) + pickerCategories.map { it.pages }
    private lateinit var pickContent: ActivityResultLauncher<String>
    private lateinit var pickDocument: ActivityResultLauncher<Array<String>>
    private val attachments = mutableListOf<ReplyDraftImage>()
    private var importingImages = false
    private var dialog: ComponentDialog? = null
    private lateinit var atTopicResultLauncher: ActivityResultLauncher<Intent>
    private var isFromAt = false
    private var pendingShowKeyboard = true
    private val imeRetryHandler = Handler(Looper.getMainLooper())
    private var imeRetryCount = 0
    private val maxImeRetry = 8
    private val imeRetryDelayMs = 120L
    private val imeRetryRunnable = Runnable { retryShowIme() }
    private var isEmojiPanelVisible = false
    private var isEmojiPanelRequested = false
    private var animateEmojiInputDescent = false
    private var emojiInputDescentStart = 0f
    private var baseRootPaddingBottom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode =
            if (com.example.c001apk.compose.util.CookieUtil.isDarkMode)
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        super.onCreate(savedInstanceState)
        if (type == VOTE_COMMENT_TYPE && voteCommentTarget?.isCurrentAccount() != true) {
            pendingShowKeyboard = false
            makeToast(getString(R.string.editor_reopen_vote))
            finish()
            return
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityReplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
        )
        binding.main.isFocusable = false
        binding.main.isFocusableInTouchMode = false
        baseRootPaddingBottom = binding.main.paddingBottom

        var imeAnimating = false
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.main,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        imeAnimating = true
                    }
                    if (
                        (animation.typeMask and WindowInsetsCompat.Type.ime()) != 0 &&
                            isEmojiPanelRequested &&
                            isEmojiPanelVisible
                    ) {
                        animateEmojiInputDescent = true
                        emojiInputDescentStart = binding.inputLayout.translationY
                    }
                    super.onPrepare(animation)
                }

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>,
                ): WindowInsetsCompat {
                    val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val useImeInset =
                        insets.isVisible(WindowInsetsCompat.Type.ime()) || imeInset > 0
                    val imeAnimation = runningAnimations.lastOrNull {
                        (it.typeMask and WindowInsetsCompat.Type.ime()) != 0
                    }
                    val translation =
                        if (animateEmojiInputDescent && imeAnimation != null) {
                            val fraction = imeAnimation.fraction.coerceIn(0f, 1f)
                            emojiInputDescentStart * (1f - smoothStep(fraction))
                        } else {
                            calculateInputTranslation(
                                imeInset = if (useImeInset) imeInset else 0,
                            )
                        }
                    updateReplyPanelTranslation(translation)
                    if (
                        useImeInset &&
                            imeInset > 0 &&
                            binding.bottomLayout == null &&
                            !isEmojiPanelVisible
                    ) {
                        // 遮罩盖住键盘背后的整段区域：原来顶端留了导航栏高度的缺口，
                        // 半透明键盘透过来就是一截透明条。
                        imeScrimDrawable.setBounds(
                            0,
                            binding.main.height - imeInset,
                            binding.main.width,
                            binding.main.height,
                        )
                        binding.main.overlay.remove(imeScrimDrawable)
                        binding.main.overlay.add(imeScrimDrawable)
                    } else {
                        binding.main.overlay.remove(imeScrimDrawable)
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    if ((animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
                        imeAnimating = false
                        if (animateEmojiInputDescent && isEmojiPanelRequested) {
                            updateReplyPanelTranslation(0f)
                        }
                        animateEmojiInputDescent = false
                    }
                    val isImeVisible =
                        ViewCompat.getRootWindowInsets(binding.editText)
                            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                    if (isImeVisible && isEmojiPanelVisible && !isEmojiPanelRequested) {
                        closeEmojiPanelForIme()
                    }
                    ViewCompat.requestApplyInsets(binding.main)
                    super.onEnd(animation)
                }
            },
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val sysInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val useImeInset = isImeVisible || imeInset > 0
            if (isImeVisible) {
                if (!imeAnimating && !isEmojiPanelRequested && binding.bottomLayout == null) {
                    closeEmojiPanelForIme(imeInset)
                }
            }
            if (!imeAnimating) {
                val translation =
                    calculateInputTranslation(
                        imeInset = if (useImeInset) imeInset else 0,
                    )
                updateReplyPanelTranslation(translation)
                if (
                    useImeInset &&
                        imeInset > 0 &&
                        binding.bottomLayout == null &&
                        !isEmojiPanelVisible
                ) {
                    imeScrimDrawable.setBounds(
                        0,
                        binding.main.height - imeInset,
                        binding.main.width,
                        binding.main.height,
                    )
                    binding.main.overlay.remove(imeScrimDrawable)
                    binding.main.overlay.add(imeScrimDrawable)
                } else {
                    binding.main.overlay.remove(imeScrimDrawable)
                }
            }
            // 竖屏：导航栏高度不再留在根布局底部，改由输入面板自身的底边距消化，
            // 面板背景随之铺到底边，手势条悬浮在面板上；横屏沿用原来的留白。
            binding.main.updatePadding(
                bottom =
                    baseRootPaddingBottom +
                        if (binding.bottomLayout == null) 0 else navInset,
                top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
            )
            if (binding.bottomLayout == null) {
                binding.inputLayout.updatePadding(bottom = navInset)
            }
            insets
        }
        ViewCompat.requestApplyInsets(binding.main)

        viewModel.type = type
        viewModel.rid = rid
        viewModel.voteCommentTarget = voteCommentTarget

        initView()
        initEditText()
        initPage()
        restoreDraft()
        draftReady = true
        initEmojiPanel()
        initObserve()
        initPhotoPick()
        initAtUser()
    }

    private fun resolvePrimaryColor(): Int =
        com.example.c001apk.compose.ui.theme.miuixViewColors(this).primary.toArgb()

    private fun resolveSurfaceContainerColor(): Int =
        com.example.c001apk.compose.ui.theme.miuixViewColors(this).surfaceContainer.toArgb()

    private fun resolveSurfaceColor(): Int =
        com.example.c001apk.compose.ui.theme.miuixViewColors(this).surface.toArgb()

    private fun resolvePrimaryDarkColor(primary: Int): Int =
        ColorUtils.blendARGB(primary, Color.BLACK, 0.2f)

    private fun initAtUser() {
        atTopicResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val list = result.data?.getStringExtra("data")
                    if (isFromAt) {
                        isFromAt = false
                        with(binding.editText.selectionStart) {
                            binding.editText.editableText.replace(this - 1, this, list)
                        }
                    } else {
                        binding.editText.editableText.append(list)
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (pendingShowKeyboard) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(120)
                showInput()
            }
        }
    }

    override fun onPause() {
        if (draftReady) {
            binding.editText.removeCallbacks(saveDraftRunnable)
            saveDraft()
        }
        pendingShowKeyboard = false
        imeRetryHandler.removeCallbacks(imeRetryRunnable)
        super.onPause()
    }

    override fun onPostResume() {
        super.onPostResume()
        if (pendingShowKeyboard) {
            pendingShowKeyboard = false
            binding.editText.postDelayed({ showInput() }, 200)
            startImeRetry()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && pendingShowKeyboard) {
            pendingShowKeyboard = false
            binding.editText.postDelayed({ showInput() }, 200)
            startImeRetry()
        }
    }

    private fun initPhotoPick() {
        pickContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let { importAttachments(listOf(it)) }
            }

        pickDocument =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                importAttachments(uris)
            }
    }

    private fun importAttachments(uris: List<Uri>) {
        if (uris.isEmpty() || importingImages) return
        val remaining = 9 - attachments.size
        if (uris.size > remaining) makeToast(getString(R.string.editor_image_limit))
        if (remaining <= 0) return

        importingImages = true
        updateEditorState()
        showDialog()
        lifecycleScope.launch {
            try {
                for (uri in uris.take(remaining)) {
                    addAttachment(draftStore.importImage(uri))
                    saveDraft()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                makeToast(getString(R.string.editor_image_info_failed))
            } finally {
                importingImages = false
                updateEditorState()
                closeDialog()
            }
        }
    }

    private fun addAttachment(image: ReplyDraftImage) {
        attachments.add(image)
        binding.imageLayout.addView(createAttachmentThumbnail(image))
        updateAttachmentVisibility()
    }

    private fun createAttachmentThumbnail(image: ReplyDraftImage): FrameLayout {
        val thumbnail = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ReplyEditorDefaults.ThumbnailSize.dp,
                ReplyEditorDefaults.ThumbnailSize.dp,
            ).apply { marginEnd = 8.dp }
        }
        thumbnail.addView(ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = getString(R.string.image_selected)
            background = GradientDrawable().apply {
                setColor(resolveSurfaceContainerColor())
                cornerRadius = ReplyEditorDefaults.ThumbnailRadius.dp.toFloat()
            }
            clipToOutline = true
            load(draftStore.imageUri(image)) { crossfade(true) }
        })
        thumbnail.addView(ReplyAttachmentDeleteView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
            ).apply { setMargins(4.dp, 4.dp, 4.dp, 4.dp) }
            onRemove = {
                binding.imageLayout.removeView(thumbnail)
                attachments.remove(image)
                if (draftKey == null) draftStore.deleteImages(listOf(image))
                updateAttachmentVisibility()
                saveDraft()
            }
        })
        return thumbnail
    }

    private fun updateAttachmentVisibility() {
        binding.imageLayout.isVisible = attachments.isNotEmpty()
        binding.imageScrollLayout.isVisible = attachments.isNotEmpty()
    }

    private fun restoreDraft() {
        val draft = draftKey?.let(draftStore::load) ?: return
        binding.editText.setText(draft.text)
        binding.editText.setSelection(binding.editText.length())
        binding.checkBox.isChecked = draft.checked
        draft.images.forEach(::addAttachment)
    }

    private fun saveDraft() {
        val key = draftKey ?: return
        if (viewModel.postFinished.value) return
        val text = binding.editText.text.toString()
        val hasText = text.isNotBlank() && text.trim() != prefilledTopic
        val draft = if (hasText || attachments.isNotEmpty()) {
            ReplyEditorDraft(text, binding.checkBox.isChecked, attachments.toList())
        } else {
            null
        }
        draftStore.save(key, draft)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.emojiBtn.setOnClickListener(this)
        binding.imageBtn.setOnClickListener(this)
        binding.otherImageBtn.setOnClickListener(this)
        binding.atBtn.setOnClickListener(this)
        binding.tagBtn.setOnClickListener(this)
        binding.checkBox.setOnClickListener(this)
        binding.publish.setOnClickListener(this)
        binding.editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isEmojiPanelVisible) {
                showInput()
            }
            false
        }
        binding.out.setOnClickListener { finish() }
        (binding.main as? SmoothInputLayout)?.setOnVisibilityChangeListener(this)
        (binding.main as? SmoothInputLayout)?.setOnKeyboardChangeListener(null)
        val radius = listOf(16.dp.toFloat(), 16.dp.toFloat(), 0f, 0f)
        val radiusBg =
            GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(this@ReplyActivity.color)
                cornerRadii =
                    floatArrayOf(
                        radius[0],
                        radius[0],
                        radius[1],
                        radius[1],
                        radius[2],
                        radius[2],
                        radius[3],
                        radius[3],
                    )
            }
        if (binding.main is SmoothInputLayout) {
            binding.inputLayout.background = radiusBg
            binding.emojiLayout.setBackgroundColor(color)
        } else binding.bottomLayout?.background = radiusBg
        val colors = miuixViewColors(this)
        binding.title.setTextColor(colors.onSurfaceContainer.toArgb())
        binding.title.textSize = 18f * DefaultFontScale
        binding.characterCount.setTextColor(colors.onSurfaceVariantSummary.toArgb())
        binding.characterCount.textSize = 12f * DefaultFontScale
        binding.dragHandle.background = GradientDrawable().apply {
            setColor(colors.outline.toArgb())
            cornerRadius = 2.dp.toFloat()
        }
    }

    private fun initObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uploadImage.collect { responseData ->
                    responseData?.let {
                        viewModel.replyAndFeedData["pic"] =
                            responseData.fileInfo.joinToString(separator = ",") {
                                responseData.uploadPrepareInfo.uploadImagePrefix +
                                    "/" +
                                    it.uploadFileName
                            }
                        val images = attachments.toList()
                        val remainingUploads = AtomicInteger(images.size)
                        val uploadFailed = AtomicBoolean(false)
                        ossUpload(
                            this@ReplyActivity,
                            responseData,
                            images.map(draftStore::imageUri),
                            images.map { it.mimeType },
                            images.map(ReplyDraftImage::checksumBytes),
                            iOnSuccess = {
                                if (BuildConfig.DEBUG) {
                                    Log.i("OSSUpload", "uploadSuccess")
                                }
                                if (remainingUploads.decrementAndGet() == 0 && !uploadFailed.get()) {
                                    viewModel.postCurrentDraft()
                                }
                            },
                            iOnFailure = {
                                uploadFailed.set(true)
                                if (BuildConfig.DEBUG) {
                                    Log.i("OSSUpload", "uploadFailed")
                                }
                                runOnUiThread {
                                    closeDialog()
                                    Toast.makeText(
                                            this@ReplyActivity,
                                            getString(R.string.image_upload_failed),
                                            Toast.LENGTH_SHORT,
                                        )
                                        .show()
                                }
                            },
                            closeDialog = {
                                closeDialog()
                            },
                        )
                        viewModel.resetUpload()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.recentList.collect {
                    viewModel.size = it.size
                    viewModel.last = it.lastOrNull()?.data
                    if (binding.emojiPanel.currentItem == 0 && recentList.isNotEmpty())
                        return@collect
                    recentList.clear()
                    if (it.isEmpty()) {
                        if (viewModel.isInit) {
                            viewModel.isInit = false
                            binding.emojiPanel.setCurrentItem(1, false)
                        }
                        recentList.add(0, emptyList())
                    } else {
                        recentList.add(
                            0,
                            it.map { item ->
                                Pair(
                                    item.data,
                                    EmojiUtils.emojiMap[item.data] ?: R.mipmap.ic_launcher,
                                )
                            },
                        )
                    }
                    binding.emojiPanel.adapter?.notifyItemChanged(0)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.postFinished.collect {
                    if (it) {
                        draftKey?.let { key -> draftStore.save(key, null) }
                        closeDialog()
                        val intent = voteCommentTarget?.resultIntent() ?: Intent().apply {
                            if (type == "createFeed") {
                                this@ReplyActivity.makeToast(getString(R.string.editor_published))
                            } else {
                                putExtra("response_data", viewModel.responseData)
                            }
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.captchaImg.collect { img ->
                    img?.let {
                        closeDialog()
                        val binding =
                            ItemCaptchaBinding.inflate(
                                LayoutInflater.from(this@ReplyActivity),
                                null,
                                false,
                            )
                        binding.captchaImg.setImageBitmap(img)
                        binding.captchaText.highlightColor =
                            ColorUtils.setAlphaComponent(
                                resolvePrimaryDarkColor(resolvePrimaryColor()),
                                128,
                            )
                        MiuixViewDialogBuilder(this@ReplyActivity)
                            .apply {
                                setView(binding.root)
                                setTitle(getString(R.string.captcha_title))
                                setNegativeButton(android.R.string.cancel, null)
                                setPositiveButton(R.string.captcha_continue) { _, _ ->
                                    viewModel.requestValidateData = HashMap()
                                    viewModel.requestValidateData["type"] = "err_request_captcha"
                                    viewModel.requestValidateData["code"] =
                                        binding.captchaText.text.toString()
                                    viewModel.requestValidateData["mobile"] = ""
                                    viewModel.requestValidateData["idcard"] = ""
                                    viewModel.requestValidateData["name"] = ""
                                    viewModel.onPostRequestValidate()
                                }
                            }
                            .create()
                            .apply {
                                window?.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE,
                                )
                                binding.captchaText.requestFocus()
                            }
                            .show()
                        viewModel.resetCaptcha()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.toastText.collect { text ->
                    text?.let {
                        this@ReplyActivity.makeToast(it)
                        viewModel.resetToast()
                        closeDialog()
                    }
                }
            }
        }
    }

    private fun closeDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun initPage() {
        voteCommentTarget?.let { target ->
            binding.checkBox.isVisible = false
            binding.title.text = getString(if (target.anonymous) R.string.editor_anonymous_comment else R.string.feed_write_comment)
            binding.publish.actionLabel = getString(R.string.editor_publish_comment)
            binding.editText.hint = getString(R.string.editor_comment_hint)
            updateEditorState()
            return
        }
        binding.checkBox.text = getString(if (type == "createFeed") R.string.label_private else R.string.editor_reply_forward)
        binding.checkBox.isReply = type != "createFeed"
        binding.title.text = getString(if (type == "createFeed") R.string.feed_publish else R.string.feed_reply)
        binding.publish.actionLabel = getString(if (type == "createFeed") R.string.feed_publish else R.string.editor_publish_reply)
        if (type != "createFeed" && !username.isNullOrEmpty())
            binding.editText.hint = getString(R.string.editor_reply_hint, username)
        if (prefilledTopic.isNotEmpty()) {
            binding.editText.editableText.append(prefilledTopic).append(' ')
        }
        updateEditorState()
    }

    private fun initEmojiPanel() {
        val labels = listOf(getString(R.string.label_recent)) + pickerCategories.map { getString(it.titleRes) }
        for (i in labels.indices) {
            binding.indicator.addView(
                TextView(this).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                            .apply {
                                weight = 1f
                            }
                    gravity = Gravity.CENTER
                    text = labels[i]
                    updateEmojiCategoryStyle(i == binding.emojiPanel.currentItem)
                    setOnClickListener {
                        binding.emojiPanel.setCurrentItem(i, false)
                    }
                    if (i == 0 && BuildConfig.DEBUG) {
                        setOnLongClickListener {
                            viewModel.deleteAll()
                            true
                        }
                    }
                },
            )
            if (i != labels.lastIndex) {
                binding.indicator.addView(
                    View(this).apply {
                        layoutParams =
                            LinearLayout.LayoutParams(
                                1.dp,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            )
                        setBackgroundColor(
                            resolveSurfaceContainerColor(),
                        )
                    },
                )
            }
        }
        binding.emojiPanel.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    for (i in 0 until binding.indicator.childCount) {
                        with(binding.indicator.getChildAt(i)) {
                            if (this is TextView) {
                                updateEmojiCategoryStyle(i / 2 == position)
                            }
                        }
                    }
                }
            },
        )

        binding.emojiPanel.adapter =
            EmojiPagerAdapter(
                list,
                onClickEmoji = {
                    with(binding.editText) {
                        if (it == "[c001apk]") {
                            onBackSpace()
                        } else {
                            editableText.replace(selectionStart, selectionEnd, it)
                            viewModel.updateRecentEmoji(it)
                        }
                    }
                },
                onCountStart = {
                    countDownTimer.start()
                },
                onCountStop = {
                    countDownTimer.cancel()
                },
            )
    }

    private val countDownTimer: CountDownTimer =
        object : CountDownTimer(100000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                onBackSpace()
            }

            override fun onFinish() {}
        }

    private fun onBackSpace() {
        dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        ViewCompat.performHapticFeedback(binding.editText, HapticFeedbackConstantsCompat.CONFIRM)
    }

    private fun initEditText() {
        val primaryColor = resolvePrimaryColor()
        val primaryDarkColor = resolvePrimaryDarkColor(primaryColor)
        val boxColor = miuixViewColors(this).surfaceContainerHighest.toArgb()
        binding.editText.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            highlightColor =
                ColorUtils.setAlphaComponent(
                    primaryDarkColor,
                    128,
                )
            post { setCursorColor(this, primaryColor) }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setCursorColor(this, primaryColor)
                }
            }
            addTextChangedListener(
                EmojiTextWatcher(
                    this@ReplyActivity,
                    binding.editText.textSize,
                    primaryColor,
                ) {
                    updateEditorState()
                    if (draftReady) {
                        removeCallbacks(saveDraftRunnable)
                        postDelayed(saveDraftRunnable, 400L)
                    }
                },
            )
            setOnKeyListener(FastDeleteAtUserKeyListener)
        }
        binding.textInputLayout.setCardColor(boxColor)
        binding.editText.setHintTextColor(miuixViewColors(this).onSurfaceVariantSummary.toArgb())
        binding.editText.setTextColor(
            com.example.c001apk.compose.ui.theme.miuixViewColors(this).onSurface.toArgb(),
        )
    }

    private fun updateEditorState() {
        val text = binding.editText.text.toString()
        binding.characterCount.text = "${text.length} / 500"
        val canPublish = text.isNotBlank() && !importingImages
        binding.publish.canPublish = canPublish
        binding.publish.isEnabled = canPublish
        binding.publish.isClickable = canPublish
    }

    private fun setCursorColor(editText: TextView, color: Int) {
        if (SDK_INT >= 29) {
            val width = max(2.dp, 2)
            val height = max(editText.textSize.toInt(), 1)
            val cursorDrawable =
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(color)
                    setSize(width, height)
                }
            editText.textCursorDrawable = cursorDrawable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDialog()
        if (draftReady) binding.editText.removeCallbacks(saveDraftRunnable)
        if (attachments.isNotEmpty() && draftKey == null) draftStore.deleteImages(attachments)
        countDownTimer.cancel()
        imeRetryHandler.removeCallbacks(imeRetryRunnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 输入面板背景已铺到底边，手势条悬浮在面板上；导航栏颜色与面板同色，
        // 只是让仍按旧规则画导航栏的版本没有接缝，对比度强制保持关闭。
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = resolveSurfaceContainerColor()
        if (SDK_INT >= 29) {
            @Suppress("DEPRECATION")
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars =
            !com.example.c001apk.compose.util.CookieUtil.isDarkMode
    }

    private fun showInput() {
        if (isFinishing || isDestroyed || !hasWindowFocus() ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        isEmojiPanelRequested = false
        animateEmojiInputDescent = false
        if (binding.main is SmoothInputLayout) (binding.main as? SmoothInputLayout)?.showKeyboard()
        else
            binding.editText.let {
                it.requestFocus()
                it.requestFocusFromTouch()
                imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
                WindowCompat.getInsetsController(window, it).show(WindowInsetsCompat.Type.ime())
            }
        ViewCompat.requestApplyInsets(binding.main)
    }

    private fun startImeRetry() {
        imeRetryCount = 0
        imeRetryHandler.removeCallbacks(imeRetryRunnable)
        imeRetryHandler.postDelayed(imeRetryRunnable, imeRetryDelayMs)
    }

    private fun retryShowIme() {
        if (isFinishing || isDestroyed || !hasWindowFocus() ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val isImeVisible =
            ViewCompat.getRootWindowInsets(binding.editText)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        if (isImeVisible) {
            imeRetryHandler.removeCallbacks(imeRetryRunnable)
            return
        }
        showInput()
        if (imeRetryCount++ >= maxImeRetry) {
            imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
            imeRetryHandler.removeCallbacks(imeRetryRunnable)
            return
        }
        imeRetryHandler.postDelayed(imeRetryRunnable, imeRetryDelayMs)
    }

    private fun showEmoji() {
        isEmojiPanelRequested = true
        (binding.main as? SmoothInputLayout)?.showEmojiPanelBehindKeyboard(true)
        binding.main.doOnPreDraw {
            if (isEmojiPanelRequested) {
                val insets = ViewCompat.getRootWindowInsets(binding.editText)
                if (insets != null) {
                    updateReplyPanelTranslation(
                        calculateInputTranslation(
                            imeInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom,
                        ),
                    )
                }
                hideImeForEmoji()
                ViewCompat.requestApplyInsets(binding.main)
            }
        }
    }

    private fun hideImeForEmoji() {
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
        WindowCompat.getInsetsController(window, binding.editText)
            .hide(WindowInsetsCompat.Type.ime())
    }

    private fun closeEmojiPanelForIme(
        imeInset: Int =
            ViewCompat.getRootWindowInsets(binding.editText)
                ?.getInsets(WindowInsetsCompat.Type.ime())
                ?.bottom ?: 0,
    ) {
        (binding.main as? SmoothInputLayout)?.closeEmojiPanel()
        updateReplyPanelTranslation(-max(0, imeInset).toFloat())
    }

    private fun calculateInputTranslation(imeInset: Int): Float {
        if (binding.bottomLayout != null) return 0f
        // 面板底部已贴到底边，上移量按整段键盘高度算，不再扣导航栏高度。
        val imeHeight = max(0, imeInset)
        val emojiHeight =
            if (isEmojiPanelVisible) {
                max(binding.emojiLayout.measuredHeight, binding.emojiLayout.layoutParams.height)
            } else {
                0
            }
        return -max(0, imeHeight - emojiHeight).toFloat()
    }

    private fun smoothStep(fraction: Float): Float {
        return fraction * fraction * (3f - 2f * fraction)
    }

    @SuppressLint("InflateParams")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.atBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchAtTopic("user")
            }

            R.id.tagBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchAtTopic("topic")
            }

            R.id.imageBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchPick()
            }

            R.id.otherImageBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                launchDocumentPick()
            }

            R.id.emojiBtn -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                showEmoji()
            }

            R.id.checkBox -> {
                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)
                saveDraft()
            }

            R.id.publish -> {
                performConfiguredHapticFeedback(
                    fallback = {
                        ViewCompat.performHapticFeedback(
                            view,
                            HapticFeedbackConstantsCompat.CONFIRM,
                        )
                    },
                )
                publishDraft()
            }
        }
    }

    private fun publishDraft() {
        if (importingImages) return
        if (voteCommentTarget?.isCurrentAccount() == false) {
            makeToast(getString(R.string.editor_account_changed))
            return
        }
        saveDraft()
        val draft = viewModel.replyAndFeedData
        draft["message"] = binding.editText.text.toString()
        draft.remove("pic")
        when (type) {
            "createFeed" -> {
                draft["id"] = ""
                draft["type"] = if (targetType == "apk") "comment" else "feed"
                draft["status"] = "1"
                draft["publish_status"] = if (binding.checkBox.isChecked) "1" else "0"
                targetType?.let { draft["targetType"] = it }
                targetId?.let { draft["targetId"] = it }
            }
            VOTE_COMMENT_TYPE -> Unit
            else -> draft["replyAndForward"] = if (binding.checkBox.isChecked) "1" else "0"
        }
        showDialog()
        if (attachments.isNotEmpty()) {
            viewModel.onPostOSSUploadPrepare(
                attachments.map(ReplyDraftImage::toUploadModel),
            )
        } else {
            viewModel.postCurrentDraft()
        }
    }

    private fun showDialog() {
        closeDialog()
        dialog = createMiuixLoadingDialog(this).also { it.show() }
    }

    private fun launchAtTopic(type: String) {
        val intent = Intent(this, AtTopicActivity::class.java)
        intent.putExtra("type", type)
        atTopicResultLauncher.launch(intent)
    }

    private fun launchPick() {
        if (importingImages) return
        (binding.main as? SmoothInputLayout)?.closeKeyboard(false)
        try {
            pickContent.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            makeToast("Activity Not Found")
            e.printStackTrace()
        }
    }

    private fun launchDocumentPick() {
        if (importingImages) return
        (binding.main as? SmoothInputLayout)?.closeKeyboard(false)
        try {
            pickDocument.launch(arrayOf("image/*"))
        } catch (e: ActivityNotFoundException) {
            makeToast("Activity Not Found")
            e.printStackTrace()
        }
    }

    private fun updateReplyPanelTranslation(translation: Float) {
        // In the landscape layout, the editor stays beside the split IME instead of above it.
        binding.bottomLayout?.translationY = 0f
        if (binding.bottomLayout == null) {
            val alignedTranslation = translation.roundToInt().toFloat()
            val seamOverlap = if (isEmojiPanelVisible && alignedTranslation != 0f) 1f else 0f
            binding.inputLayout.translationY = alignedTranslation
            binding.emojiLayout.translationY =
                if (isEmojiPanelVisible) {
                    alignedTranslation - seamOverlap
                } else {
                    0f
                }
        }
    }

    override fun onVisibilityChange(visibility: Int) { // 0->visible, 8->gone
        isEmojiPanelVisible = visibility == VISIBLE
        ViewCompat.requestApplyInsets(binding.main)
    }

    override fun finish() {
        pendingShowKeyboard = false
        imeRetryHandler.removeCallbacks(imeRetryRunnable)
        super.finish()
        if (SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.anim_bottom_sheet_slide_up,
                R.anim.anim_bottom_sheet_slide_down,
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                R.anim.anim_bottom_sheet_slide_up,
                R.anim.anim_bottom_sheet_slide_down,
            )
        }
    }
}
