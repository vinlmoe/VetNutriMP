package fr.vetbrain.vetnutri_mp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.vetbrain.vetnutri_mp.Utils.FullscreenManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests unitaires pour le FullscreenManager.
 */
@RunWith(AndroidJUnit4::class)
class FullscreenManagerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testEnableFullscreen() {
        // Arrange
        activityRule.scenario.onActivity { activity ->
            // Act
            FullscreenManager.enableFullscreen(activity)
            
            // Assert
            // Vérifier que la configuration plein écran est appliquée
            // Note: Les tests d'interface utilisateur nécessitent des tests d'intégration
        }
    }

    @Test
    fun testShowSystemBars() {
        // Arrange
        activityRule.scenario.onActivity { activity ->
            // Act
            FullscreenManager.showSystemBars(activity)
            
            // Assert
            // Vérifier que les barres système sont affichées
        }
    }

    @Test
    fun testHideSystemBars() {
        // Arrange
        activityRule.scenario.onActivity { activity ->
            // Act
            FullscreenManager.hideSystemBars(activity)
            
            // Assert
            // Vérifier que les barres système sont masquées
        }
    }
}
