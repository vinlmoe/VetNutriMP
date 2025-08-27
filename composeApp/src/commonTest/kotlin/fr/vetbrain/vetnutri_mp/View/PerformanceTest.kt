package fr.vetbrain.vetnutri_mp.View

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchLayout
import org.junit.Rule
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Tests de performance pour le composant FoodSearchComponent
 */
class PerformanceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Générer une grande liste d'aliments pour les tests de performance
    private fun generateLargeFoodList(size: Int): List<AlimentEv> {
        return (1..size).map { index ->
            AlimentEv(
                uuid = index.toString(),
                nom = "Aliment Test $index",
                brand = "Marque $index",
                typeAliment = FoodKind.values()[index % FoodKind.values().size],
                group = GroupAlim.values()[index % GroupAlim.values().size],
                especes = listOf(
                    Espece.values()[index % Espece.values().size].name
                ),
                indicat = listOf(
                    AlimIndic.values()[index % AlimIndic.values().size]
                )
            )
        }
    }

    @Test
    fun testFilteringPerformance_WithLargeDataset() {
        val largeFoodList = generateLargeFoodList(1000)
        var filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        val setupTime = measureTimeMillis {
            composeTestRule.setContent {
                FoodSearchComponent(
                    foods = largeFoodList,
                    filters = filters,
                    onFiltersChange = { filters = it },
                    config = config
                )
            }
        }

        

        // Test de performance du filtrage
        val filterTime = measureTimeMillis {
            filters = filters.copy(searchQuery = "Test")
        }

        
        
        // Le filtrage devrait être rapide (< 100ms)
        assert(filterTime < 100) { "Le filtrage est trop lent: ${filterTime}ms" }
    }

    @Test
    fun testMemoryUsage_WithLargeDataset() {
        val largeFoodList = generateLargeFoodList(5000)
        var filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        // Mesurer l'utilisation mémoire avant
        val runtime = Runtime.getRuntime()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = largeFoodList,
                filters = filters,
                onFiltersChange = { filters = it },
                config = config
            )
        }

        // Mesurer l'utilisation mémoire après
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore

        
        
        // L'utilisation mémoire devrait être raisonnable (< 100MB)
        assert(memoryUsed < 100 * 1024 * 1024) { "Utilisation mémoire trop élevée: ${memoryUsed / 1024 / 1024}MB" }
    }

    @Test
    fun testRenderingPerformance_DifferentLayouts() {
        val testFoods = generateLargeFoodList(100)
        var filters = FoodSearchFilters()

        // Test layout vertical
        val verticalTime = measureTimeMillis {
            composeTestRule.setContent {
                FoodSearchComponent(
                    foods = testFoods,
                    filters = filters,
                    onFiltersChange = { filters = it },
                    config = FoodSearchConfig(layout = FoodSearchLayout.VERTICAL)
                )
            }
        }

        // Test layout horizontal
        val horizontalTime = measureTimeMillis {
            composeTestRule.setContent {
                FoodSearchComponent(
                    foods = testFoods,
                    filters = filters,
                    onFiltersChange = { filters = it },
                    config = FoodSearchConfig(layout = FoodSearchLayout.HORIZONTAL)
                )
            }
        }

        // Test layout compact
        val compactTime = measureTimeMillis {
            composeTestRule.setContent {
                FoodSearchComponent(
                    foods = testFoods,
                    filters = filters,
                    onFiltersChange = { filters = it },
                    config = FoodSearchConfig(layout = FoodSearchLayout.COMPACT)
                )
            }
        }

        
        
        // Tous les layouts devraient être rapides (< 200ms)
        assert(verticalTime < 200) { "Layout vertical trop lent: ${verticalTime}ms" }
        assert(horizontalTime < 200) { "Layout horizontal trop lent: ${horizontalTime}ms" }
        assert(compactTime < 200) { "Layout compact trop lent: ${compactTime}ms" }
    }

    @Test
    fun testFilterChangePerformance() {
        val testFoods = generateLargeFoodList(500)
        var filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = filters,
                onFiltersChange = { filters = it },
                config = config
            )
        }

        // Test de performance des changements de filtres
        val filterChangeTimes = mutableListOf<Long>()

        // Test 1: Changement de recherche
        val searchTime = measureTimeMillis {
            filters = filters.copy(searchQuery = "Test")
        }
        filterChangeTimes.add(searchTime)

        // Test 2: Changement de type
        val typeTime = measureTimeMillis {
            filters = filters.copy(selectedFoodType = FoodKind.CROQUETTES)
        }
        filterChangeTimes.add(typeTime)

        // Test 3: Changement de groupe
        val groupTime = measureTimeMillis {
            filters = filters.copy(selectedFoodGroup = GroupAlim.CEREALES)
        }
        filterChangeTimes.add(groupTime)

        // Test 4: Changement d'espèce
        val especeTime = measureTimeMillis {
            filters = filters.copy(selectedEspece = Espece.CH)
        }
        filterChangeTimes.add(especeTime)

        // Test 5: Changement d'indications
        val indicationTime = measureTimeMillis {
            filters = filters.copy(selectedIndications = setOf(AlimIndic.SENIOR))
        }
        filterChangeTimes.add(indicationTime)

        val averageTime = filterChangeTimes.average()
        val maxTime = filterChangeTimes.maxOrNull() ?: 0

        
        
        
        // Les changements de filtres devraient être rapides (< 50ms en moyenne)
        assert(averageTime < 50) { "Changements de filtres trop lents: ${averageTime.toLong()}ms en moyenne" }
        assert(maxTime < 100) { "Changement de filtre trop lent: ${maxTime}ms" }
    }

    @Test
    fun testScrollingPerformance_WithLargeList() {
        val largeFoodList = generateLargeFoodList(2000)
        var filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = largeFoodList,
                filters = filters,
                onFiltersChange = { filters = it },
                config = config
            )
        }

        // Test de performance du défilement
        val scrollTime = measureTimeMillis {
            // Simuler un défilement rapide
            repeat(10) {
                composeTestRule.onNodeWithText("Aliment Test ${it * 100}").performScrollTo()
            }
        }

        
        
        // Le défilement devrait être fluide (< 500ms pour 10 éléments)
        assert(scrollTime < 500) { "Le défilement est trop lent: ${scrollTime}ms" }
    }

    @Test
    fun testConcurrentFiltering() {
        val testFoods = generateLargeFoodList(1000)
        var filters = FoodSearchFilters()
        val config = FoodSearchConfig()

        composeTestRule.setContent {
            FoodSearchComponent(
                foods = testFoods,
                filters = filters,
                onFiltersChange = { filters = it },
                config = config
            )
        }

        // Test de performance avec plusieurs changements de filtres simultanés
        val concurrentTime = measureTimeMillis {
            filters = filters.copy(
                searchQuery = "Test",
                selectedFoodType = FoodKind.CROQUETTES,
                selectedFoodGroup = GroupAlim.CEREALES,
                selectedEspece = Espece.CH,
                selectedIndications = setOf(AlimIndic.SENIOR)
            )
        }

        
        
        // Le filtrage concurrent devrait être rapide (< 100ms)
        assert(concurrentTime < 100) { "Filtrage concurrent trop lent: ${concurrentTime}ms" }
    }
}
