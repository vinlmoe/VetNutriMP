package fr.vetbrain.vetnutri_mp.View

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.NutrientQuantity
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import org.junit.Rule
import org.junit.Test

/**
 * Tests pour AnalyseGraphiqueAlimentsView
 */
class AnalyseGraphiqueAlimentsViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Données de test
    private val testAliments = listOf(
        AlimentEv(
            uuid = "1",
            nom = "Aliment Test 1",
            gamme = "Premium",
            brand = "Marque A",
            valMap = mutableMapOf(
                NutrientMain.PROTEINE to NutrientQuantity(25.0, "g"),
                NutrientMain.LIPIDE to NutrientQuantity(15.0, "g"),
                NutrientMain.GLUCIDE to NutrientQuantity(45.0, "g")
            )
        ),
        AlimentEv(
            uuid = "2",
            nom = "Aliment Test 2",
            gamme = "Standard",
            brand = "Marque B",
            valMap = mutableMapOf(
                NutrientMain.PROTEINE to NutrientQuantity(20.0, "g"),
                NutrientMain.LIPIDE to NutrientQuantity(10.0, "g"),
                NutrientMain.GLUCIDE to NutrientQuantity(50.0, "g")
            )
        )
    )

    @Test
    fun testAnalyseGraphiqueAlimentsView_Display() {
        composeTestRule.setContent {
            AnalyseGraphiqueAlimentsView(
                aliments = testAliments,
                referenceEv = null,
                equationRepository = null,
                onClose = { }
            )
        }

        // Vérifier que le titre est affiché
        composeTestRule.onNodeWithText("Analyse graphique de 2 aliment(s)").assertExists()
        
        // Vérifier que la liste des aliments est affichée
        composeTestRule.onNodeWithText("Liste des aliments (triés par densité énergétique décroissante)").assertExists()
        
        // Vérifier que les aliments sont listés
        composeTestRule.onNodeWithText("Aliment Test 1").assertExists()
        composeTestRule.onNodeWithText("Aliment Test 2").assertExists()
        
        // Vérifier que les numéros sont affichés
        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
    }

    @Test
    fun testAnalyseGraphiqueAlimentsView_EmptyList() {
        composeTestRule.setContent {
            AnalyseGraphiqueAlimentsView(
                aliments = emptyList(),
                referenceEv = null,
                equationRepository = null,
                onClose = { }
            )
        }

        // Vérifier que le titre est affiché
        composeTestRule.onNodeWithText("Analyse graphique de 0 aliment(s)").assertExists()
        
        // Vérifier que le message pour liste vide est affiché
        composeTestRule.onNodeWithText("Aucun aliment à analyser").assertExists()
    }

    @Test
    fun testAnalyseGraphiqueAlimentsView_BackButton() {
        var backClicked = false
        
        composeTestRule.setContent {
            AnalyseGraphiqueAlimentsView(
                aliments = testAliments,
                referenceEv = null,
                equationRepository = null,
                onClose = { backClicked = true }
            )
        }

        // Cliquer sur le bouton retour
        composeTestRule.onNodeWithContentDescription("Retour").performClick()
        
        // Vérifier que la fonction onClose a été appelée
        assert(backClicked)
    }
}
