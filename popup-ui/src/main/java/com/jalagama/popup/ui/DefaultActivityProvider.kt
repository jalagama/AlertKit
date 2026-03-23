package com.jalagama.popup.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.jalagama.popup.core.ActivityProvider
import java.lang.ref.WeakReference

/**
 * Tracks the last resumed [Activity] using [WeakReference] to avoid retaining destroyed screens.
 */
class DefaultActivityProvider : ActivityProvider, Application.ActivityLifecycleCallbacks {
    private val lock = Any()
    private var resumedRef: WeakReference<Activity>? = null

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun currentActivity(): Activity? {
        synchronized(lock) {
            return resumedRef?.get()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        synchronized(lock) {
            resumedRef = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        synchronized(lock) {
            if (resumedRef?.get() === activity) {
                resumedRef = null
            }
        }
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        synchronized(lock) {
            if (resumedRef?.get() === activity) {
                resumedRef = null
            }
        }
    }
}
