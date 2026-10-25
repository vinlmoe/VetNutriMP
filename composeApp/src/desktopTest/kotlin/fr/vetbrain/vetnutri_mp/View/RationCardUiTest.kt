package fr.vetbrain.vetnutri_mp.View

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Carte de ration ([RationItem]) rendue avec Compose UI Test (desktop, sans écran) : textes
 * affichés (nom, statut actuelle/proposée, coefficient, sous-titre d'étape) et rappels des
 * actions (sélection, édition, suppression, duplication).
 */
@OptIn(ExperimentalTestApi::class)
class RationCardUiTest {

    private val clesGeneral = LocalizationKeys.General
    private val clesRation = LocalizationKeys.Ration
    private val clesEvolutive = LocalizationKeys.Evolutive

    @Test
    fun rationActuelle_afficheNomStatutCoefficientEtDeclencheLesActions() = runDesktopComposeUiTest(width = 600, height = 200) {
        LocalizationManager.initialize("fr")
        var selections = 0
        var editions = 0
        var suppressions = 0
        var duplications = 0
        val ration = Ration(uuid = "r1", name = "Ration test", actual = true, coef = 1.5)

        setContent {
            MaterialTheme {
                RationItem(
                        ration = ration,
                        isSelected = false,
                        onClick = { selections++ },
                        onEdit = { editions++ },
                        onDelete = { suppressions++ },
                        onDuplicate = { duplications++ }
                )
            }
        }

        onNodeWithText("Ration test").assertIsDisplayed()
        onNodeWithText(translate(clesRation.ACTUAL)).assertIsDisplayed()
        onNodeWithText(translate("ration.coefficientFormat", "1.50")).assertIsDisplayed()

        onNodeWithText("Ration test").performClick()
        assertEquals(1, selections, "cliquer la carte sélectionne la ration")

        onNodeWithContentDescription(translate(clesGeneral.EDIT)).performClick()
        assertEquals(1, editions)
        onNodeWithContentDescription(translate(clesGeneral.DELETE)).performClick()
        assertEquals(1, suppressions)
        onNodeWithContentDescription(translate("ration.duplicateAction")).performClick()
        assertEquals(1, duplications)
        assertEquals(1, selections, "les boutons d'action ne sélectionnent pas la carte")
    }

    @Test
    fun rationProposee_etEtapeDePlan_affichentLeBonSousTitre() = runDesktopComposeUiTest(width = 600, height = 300) {
        LocalizationManager.initialize("fr")
        var etapesAjoutees = 0
        setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column {
                    RationItem(
                            ration = Ration(uuid = "p", name = "Proposition", actual = false, coef = 1.0),
                            isSelected = true,
                            onClick = {},
                            onEdit = {},
                            onDelete = {},
                            onAjouterEtape = { etapesAjoutees++ }
                    )
                    RationItem(
                            ration = Ration(uuid = "e", name = "Étape 8 kg", etapeEvolutive = true, poids = 8.0),
                            isSelected = false,
                            onClick = {},
                            onEdit = {},
                            onDelete = {},
                            estEtape = true
                    )
                }
            }
        }

        onNodeWithText("Proposition").assertIsDisplayed()
        onNodeWithText(translate(clesRation.PROPOSED)).assertIsDisplayed()
        onNodeWithText("Étape 8 kg").assertIsDisplayed()
        onNodeWithText(translate(clesEvolutive.STEP_SUBTITLE)).assertIsDisplayed()

        onNodeWithContentDescription(translate(clesEvolutive.ADD_STEP_TO)).performClick()
        assertEquals(1, etapesAjoutees)
    }
}
