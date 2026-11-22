package com.example.c001apk.compose.debug

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** No-op lifecycle adapter so the provider only overrides the callbacks it needs. */
internal open class ActivityCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}

/** Runs the block on the main thread and propagates its failure to the caller thread. */
internal fun <T> onMain(block: () -> T): T {
    if (Looper.myLooper() == Looper.getMainLooper()) return block()
    val latch = CountDownLatch(1)
    var outcome: Result<T>? = null
    Handler(Looper.getMainLooper()).post {
        outcome = runCatching(block)
        latch.countDown()
    }
    check(latch.await(15, TimeUnit.SECONDS)) { "Main thread did not respond within 15s" }
    return checkNotNull(outcome).getOrThrow()
}
