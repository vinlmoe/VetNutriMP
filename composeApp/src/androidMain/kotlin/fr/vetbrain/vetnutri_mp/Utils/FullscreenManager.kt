package fr.vetbrain.vetnutri_mp.Utils

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Gestionnaire pour la configuration plein écran de l'application Android.
 * Masque les barres de statut et de navigation pour une expérience immersive.
 */
object FullscreenManager {
    
    private var isCurrentlyFullscreen: Boolean = false
    
    /**
     * Configure l'application en mode plein écran sans barres système.
     * 
     * @param activity L'activité à configurer
     */
    fun enableFullscreen(activity: Activity): Unit {
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        // Désactive l'ajustement automatique des fenêtres aux barres système
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Masque les barres de statut et de navigation
        windowInsetsController.hide(
            android.view.WindowInsets.Type.statusBars() or
            android.view.WindowInsets.Type.navigationBars()
        )
        
        // Configure le comportement des barres système pour qu'elles apparaissent temporairement au swipe
        windowInsetsController.systemBarsBehavior = 
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
        isCurrentlyFullscreen = true
    }
    
    /**
     * Active ou désactive le mode plein écran.
     * 
     * @param activity L'activité à configurer
     * @param isFullscreen true pour activer, false pour désactiver
     */
    fun toggleFullscreen(activity: Activity, isFullscreen: Boolean): Unit {
        if (isFullscreen) {
            enableFullscreen(activity)
        } else {
            showSystemBars(activity)
            isCurrentlyFullscreen = false
        }
    }
    
    /**
     * Vérifie si l'application est actuellement en mode plein écran.
     * 
     * @param activity L'activité à vérifier
     * @return true si en mode plein écran, false sinon
     */
    fun isFullscreen(activity: Activity): Boolean {
        return isCurrentlyFullscreen
    }
    
    /**
     * Affiche temporairement les barres système.
     * 
     * @param activity L'activité concernée
     */
    fun showSystemBars(activity: Activity): Unit {
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(
            android.view.WindowInsets.Type.statusBars() or
            android.view.WindowInsets.Type.navigationBars()
        )
    }
    
    /**
     * Masque les barres système.
     * 
     * @param activity L'activité concernée
     */
    fun hideSystemBars(activity: Activity): Unit {
        val window = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(
            android.view.WindowInsets.Type.statusBars() or
            android.view.WindowInsets.Type.navigationBars()
        )
    }
}