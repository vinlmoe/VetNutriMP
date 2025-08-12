package fr.vetbrain.vetnutri_mp.Localization

import android.app.Activity
import android.content.Context
import java.lang.ref.WeakReference

object AndroidContext {
    lateinit var appContext: Context
    private var currentActivityRef: WeakReference<Activity>? = null
    fun setCurrentActivity(activity: Activity): Unit {
        currentActivityRef = WeakReference(activity)
    }
    fun clearCurrentActivity(): Unit {
        currentActivityRef = null
    }
    fun getCurrentActivityOrNull(): Activity? {
        return currentActivityRef?.get()
    }
}
