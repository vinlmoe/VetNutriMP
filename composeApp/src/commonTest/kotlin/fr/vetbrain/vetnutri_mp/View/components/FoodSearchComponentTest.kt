package fr.vetbrain.vetnutri_mp.View.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import org.junit.Rule
import org.junit.Test

/**
 * Tests unitaires pour le composant FoodSearchComponent
 */
class FoodSearchComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Données de test
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
        ),
        AlimentEv(
            uuid = "3",
            nom = "Biscuits Santé",
            brand = "HealthyPaws",
            typeAliment = FoodKind.BISCUITS,
            group = GroupAlim.CEREALES,
            especes = listOf("CH"),
            indicat = listOf(AlimIndic.ALL)
        )
    )

    @Test
    fun testFoodSearchComponent_DisplaysAllFoods_WhenNoFilters() {
        val filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = filters,
                onFiltersChange = { },
                config = config
            )
        }

        // Vérifier que tous les aliments sont affichés
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertExists()
        composeTestRule.onNodeWithText("Biscuits Santé").assertExists()
    }

    @Test
    fun testFoodSearchComponent_FiltersBySearchQuery() {
        var currentFilters = FoodSearchFilters(searchQuery = "Premium")
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seul l'aliment contenant "Premium" est affiché
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Biscuits Santé").assertDoesNotExist()
    }

    @Test
    fun testFoodSearchComponent_FiltersByFoodType() {
        var currentFilters = FoodSearchFilters(selectedFoodType = FoodKind.CROQUETTES)
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seuls les croquettes sont affichés
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Biscuits Santé").assertDoesNotExist()
    }

    @Test
    fun testFoodSearchComponent_FiltersByFoodGroup() {
        var currentFilters = FoodSearchFilters(selectedFoodGroup = GroupAlim.CEREALES)
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seuls les aliments du groupe céréales sont affichés
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Biscuits Santé").assertExists()
    }

    @Test
    fun testFoodSearchComponent_FiltersByEspece() {
        var currentFilters = FoodSearchFilters(selectedEspece = Espece.CHAT)
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seuls les aliments pour chats sont affichés
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists() // CH + CHAT
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertExists() // CHAT
        composeTestRule.onNodeWithText("Biscuits Santé").assertDoesNotExist() // CH seulement
    }

    @Test
    fun testFoodSearchComponent_FiltersByIndications() {
        var currentFilters = FoodSearchFilters(selectedIndications = setOf(AlimIndic.SENIOR))
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seul l'aliment avec l'indication SENIOR est affiché
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Biscuits Santé").assertDoesNotExist()
    }

    @Test
    fun testFoodSearchComponent_CombinedFilters() {
        var currentFilters = FoodSearchFilters(
            searchQuery = "Croquettes",
            selectedFoodType = FoodKind.CROQUETTES,
            selectedEspece = Espece.CH
        )
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que seul l'aliment correspondant à tous les critères est affiché
        composeTestRule.onNodeWithText("Croquettes Premium").assertExists()
        composeTestRule.onNodeWithText("Pâtée Gourmet").assertDoesNotExist()
        composeTestRule.onNodeWithText("Biscuits Santé").assertDoesNotExist()
    }

    @Test
    fun testFoodSearchComponent_EmptyResults() {
        var currentFilters = FoodSearchFilters(
            searchQuery = "AlimentInexistant",
            selectedFoodType = FoodKind.PATEE
        )
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que le message "Aucun aliment trouvé" est affiché
        composeTestRule.onNodeWithText("Aucun aliment trouvé avec ces critères").assertExists()
    }

    @Test
    fun testFoodSearchComponent_WithActions() {
        var currentFilters = FoodSearchFilters()
        val config = FoodSearchConfig(
            availableActions = listOf("Éditer", "Supprimer"),
            onFoodAction = { _, _ -> }
        )

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que les boutons d'action sont affichés
        composeTestRule.onNodeWithText("Éditer").assertExists()
        composeTestRule.onNodeWithText("Supprimer").assertExists()
    }

    @Test
    fun testFoodSearchComponent_CompactLayout() {
        var currentFilters = FoodSearchFilters()
        val config = FoodSearchConfig(
            layout = FoodSearchLayout.COMPACT,
            showFilters = true
        )

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que le layout compact est utilisé (filtres en ligne)
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
    }

    @Test
    fun testFoodSearchComponent_VerticalLayout() {
        var currentFilters = FoodSearchFilters()
        val config = FoodSearchConfig(
            layout = FoodSearchLayout.VERTICAL,
            showFilters = true
        )

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = currentFilters,
                onFiltersChange = { currentFilters = it },
                config = config
            )
        }

        // Vérifier que le layout vertical est utilisé (filtres en grille)
        composeTestRule.onNodeWithText("Type").assertExists()
        composeTestRule.onNodeWithText("Groupe").assertExists()
        composeTestRule.onNodeWithText("Espèce").assertExists()
        composeTestRule.onNodeWithText("Indications").assertExists()
    }
}
