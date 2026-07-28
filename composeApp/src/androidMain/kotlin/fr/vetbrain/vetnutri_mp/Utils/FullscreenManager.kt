package fr.vetbrain.vetnutri_mp.Utils

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Gestionnaire pour la configuration plein écran de l'application Android.
 * Masque les barres de statut et de navigation pour une expérience immersive,
 * sauf sur Chrome OS où les apps tournent en fenêtres gérées par l'OS.
 */
object FullscreenManager {

    private var isCurrentlyFullscreen: Boolean = false

    /** Renvoie true si l'app tourne dans l'ARC de Chrome OS. */
    private fun isRunningOnChromeOS(activity: Activity): Boolean =
        activity.packageManager.hasSystemFeature("org.chromium.arc")

    /**
     * Configure l'application en mode plein écran sans barres système.
     * Sur Chrome OS, cette méthode est un no-op : la gestion des barres
     * appartient au window manager de Chrome OS.
     */
    fun enableFullscreen(activity: Activity): Unit {
        // Chrome OS gère ses propres barres système — forcer le fullscreen
        // cacherait la barre de tâches et rendrait la fenêtre non déplaçable.
        if (isRunningOnChromeOS(activity)) return

        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        windowInsetsController.hide(
            WindowInsetsCompat.Type.statusBars() or
            WindowInsetsCompat.Type.navigationBars()
        )

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        isCurrentlyFullscreen = true
    }
    
    fun toggleFullscreen(activity: Activity, isFullscreen: Boolean): Unit {
        if (isFullscreen) {
            enableFullscreen(activity)
        } else {
            showSystemBars(activity)
            isCurrentlyFullscreen = false
        }
    }

    fun isFullscreen(activity: Activity): Boolean = isCurrentlyFullscreen

    fun showSystemBars(activity: Activity): Unit {
        if (isRunningOnChromeOS(activity)) return
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(
            WindowInsetsCompat.Type.statusBars() or
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    fun hideSystemBars(activity: Activity): Unit {
        if (isRunningOnChromeOS(activity)) return
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(
            WindowInsetsCompat.Type.statusBars() or
            WindowInsetsCompat.Type.navigationBars()
        )
    }
}
