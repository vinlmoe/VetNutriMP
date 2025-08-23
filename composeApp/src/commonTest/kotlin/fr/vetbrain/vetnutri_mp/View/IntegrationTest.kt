package fr.vetbrain.vetnutri_mp.View

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Tests d'intégration pour valider que toutes les vues utilisent correctement
 * le composant FoodSearchComponent partagé
 */
class IntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Données de test communes
    private val testFoods = listOf(
        AlimentEv(
            uuid = "1",
            nom = "Croquettes Premium",
            brand = "VetFood",
            typeAliment = FoodKind.CROQUETTES,
            group = GroupAlim.CEREALES,
            especes = listOf("CH", "CHAT"),
            indicat = listOf(AlimIndic.SENIOR, AlimIndic.SENSIBLE)
        ),
        AlimentEv(
            uuid = "2",
            nom = "Pâtée Gourmet",
            brand = "PetDeluxe",
            typeAliment = FoodKind.PATEE,
            group = GroupAlim.VIANDE,
            especes = listOf("CHAT"),
            indicat = listOf(AlimIndic.JUNIOR)
        )
    )

    private val testRation = Ration(
        uuid = "ration1",
        name = "Ration Test",
        alimentMutableList = mutableListOf()
    )

    @Test
    fun testAddAlimentView_Integration() {
        // Simuler un ViewModel avec des données
        val viewModel = AnimalDetailViewModel()
        
        composeTestRule.setContent {
            AddAlimentView(
                viewModel = viewModel,
                ration = testRation,
                onNavigateBack = { },
                onAddAliment = { _, _ -> }
            )
        }

        // Vérifier que le composant de recherche est présent
        composeTestRule.onNodeWithText("Filtres de recherche").assertExists()
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Groupe").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        composeTestRule.onNodeWithText("Indications").assertExists()
    }

    @Test
    fun testFoodListView_Integration() {
        // Simuler un ViewModel avec des données
        val viewModel = FoodListViewModel()
        
        composeTestRule.setContent {
            FoodListView(
                viewModel = viewModel,
                onNavigateBack = { },
                onOpenSettings = { },
                onEditFood = { },
                onCreateFood = { }
            )
        }

        // Vérifier que le composant de recherche est présent
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Groupe").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        composeTestRule.onNodeWithText("Indications").assertExists()
    }

    @Test
    fun testAnalyseSelectionAlimentsView_Integration() {
        composeTestRule.setContent {
            AnalyseSelectionAlimentsView(
                aliments = testFoods,
                onClose = { },
                onAlimentSelected = { }
            )
        }

        // Vérifier que le composant de recherche est présent
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        
        // Vérifier que les actions d'analyse sont présentes
        composeTestRule.onNodeWithText("Analyser").assertExists()
        composeTestRule.onNodeWithText("Comparer").assertExists()
    }

    @Test
    fun testConsistentFiltering_AcrossAllViews() {
        // Test pour vérifier que le filtrage est cohérent entre toutes les vues
        
        // Test 1: Filtrage par type d'aliment
        composeTestRule.setContent {
            AddAlimentView(
                viewModel = AnimalDetailViewModel(),
                ration = testRation,
                onNavigateBack = { },
                onAddAliment = { _, _ -> }
            )
        }

        // Vérifier que les mêmes options de filtrage sont disponibles
        composeTestRule.onNodeWithText("CROQUETTES").assertExists()
        composeTestRule.onNodeWithText("PATEE").assertExists()
        composeTestRule.onNodeWithText("BISCUITS").assertExists()
    }

    @Test
    fun testSearchBar_Consistency() {
        // Test pour vérifier que la barre de recherche est cohérente
        
        composeTestRule.setContent {
            FoodListView(
                viewModel = FoodListViewModel(),
                onNavigateBack = { },
                onOpenSettings = { },
                onEditFood = { },
                onCreateFood = { }
            )
        }

        // Vérifier que la barre de recherche est présente avec le bon placeholder
        composeTestRule.onNodeWithText("Nom, marque, ingrédients...").assertExists()
    }

    @Test
    fun testFilterOptions_Consistency() {
        // Test pour vérifier que les options de filtrage sont cohérentes
        
        composeTestRule.setContent {
            AnalyseSelectionAlimentsView(
                aliments = testFoods,
                onClose = { },
                onAlimentSelected = { }
            )
        }

        // Vérifier que les filtres compacts sont présents
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
    }

    @Test
    fun testActions_Consistency() {
        // Test pour vérifier que les actions sont cohérentes selon le contexte
        
        // Test AddAlimentView (pas d'actions sur les aliments)
        composeTestRule.setContent {
            AddAlimentView(
                viewModel = AnimalDetailViewModel(),
                ration = testRation,
                onNavigateBack = { },
                onAddAliment = { _, _ -> }
            )
        }

        // Vérifier qu'il n'y a pas de boutons d'action sur les aliments
        composeTestRule.onNodeWithText("Éditer").assertDoesNotExist()
        composeTestRule.onNodeWithText("Supprimer").assertDoesNotExist()

        // Test FoodListView (actions d'édition/suppression)
        composeTestRule.setContent {
            FoodListView(
                viewModel = FoodListViewModel(),
                onNavigateBack = { },
                onOpenSettings = { },
                onEditFood = { },
                onCreateFood = { }
            )
        }

        // Vérifier que les actions sont présentes
        composeTestRule.onNodeWithText("Éditer").assertExists()
        composeTestRule.onNodeWithText("Supprimer").assertExists()
    }

    @Test
    fun testLayout_Adaptation() {
        // Test pour vérifier que les layouts s'adaptent correctement
        
        // Test layout horizontal (AddAlimentView)
        composeTestRule.setContent {
            AddAlimentView(
                viewModel = AnimalDetailViewModel(),
                ration = testRation,
                onNavigateBack = { },
                onAddAliment = { _, _ -> }
            )
        }

        // Vérifier que le layout horizontal est utilisé (filtres dans une carte)
        composeTestRule.onNodeWithText("Filtres de recherche").assertExists()

        // Test layout vertical (FoodListView)
        composeTestRule.setContent {
            FoodListView(
                viewModel = FoodListViewModel(),
                onNavigateBack = { },
                onOpenSettings = { },
                onEditFood = { },
                onCreateFood = { }
            )
        }

        // Vérifier que le layout vertical est utilisé (filtres en grille)
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Groupe").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        composeTestRule.onNodeWithText("Indications").assertExists()

        // Test layout compact (AnalyseSelectionAlimentsView)
        composeTestRule.setContent {
            AnalyseSelectionAlimentsView(
                aliments = testFoods,
                onClose = { },
                onAlimentSelected = { }
            )
        }

        // Vérifier que le layout compact est utilisé (filtres en ligne)
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        composeTestRule.onNodeWithText("Groupe").assertDoesNotExist() // Pas dans le layout compact
    }
}
