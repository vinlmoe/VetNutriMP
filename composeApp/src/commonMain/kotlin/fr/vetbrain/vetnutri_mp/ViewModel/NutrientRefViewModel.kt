package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.NutrientRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.ReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel pour la gestion des besoins nutritionnels d'une référence */
class NutrientRefViewModel(
        private val referenceEvRepository: ReferenceEvRepository,
        private val biblioRefRepository: BiblioRefRepository,
        private val platformDispatcher: PlatformDispatcher = PlatformDispatcher(),
        private val coroutineContext: CoroutineContext = platformDispatcher.provideMainDispatcher()
) {
    private val scope = CoroutineScope(coroutineContext)

    // Référence en cours d'édition
    private val _currentReference = MutableStateFlow<ReferenceEv?>(null)
    val currentReference: StateFlow<ReferenceEv?> = _currentReference.asStateFlow()

    // Liste des besoins nutritionnels pour la référence
    private val _nutrientRefs = MutableStateFlow<List<NutrientRef>>(emptyList())
    val nutrientRefs: StateFlow<List<NutrientRef>> = _nutrientRefs.asStateFlow()

    // Liste des références bibliographiques disponibles
    private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs.asStateFlow()

    // État de chargement
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // Message d'erreur
    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    /** Définit la référence à éditer */
    fun setReference(reference: ReferenceEv) {
        _currentReference.value = reference
        loadNutrients()
    }

    /** Charge tous les nutriments pour la référence courante */
    private fun loadNutrients() {
        scope.launch {
            _loading.value = true
            try {
                _error.value = ""
                val reference = _currentReference.value ?: return@launch

                // TODO: Implémenter le chargement réel des nutriments depuis le repository
                // Pour l'instant, nous générons des données de test
                val nutrients = generateTestNutrients(reference.uuid)
                _nutrientRefs.value = nutrients
            } catch (e: Exception) {
                _error.value =
                        "Erreur lors du chargement des nutriments: ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Charge les nutriments d'un type spécifique pour la référence courante */
    fun loadNutrientsForType(nutrientType: MainNutrientEnum) {
        // Si nous avons déjà chargé tous les nutriments, nous filtrons simplement
        // Sinon, nous chargeons juste ce type spécifique
        if (_nutrientRefs.value.isEmpty()) {
            scope.launch {
                _loading.value = true
                try {
                    _error.value = ""
                    val reference = _currentReference.value
                    if (reference == null) {
                        _nutrientRefs.value = emptyList()
                        return@launch
                    }

                    // TODO: Implémenter le chargement réel depuis le repository
                    // Pour l'instant, nous générons des données de test
                    val nutrients = generateTestNutrientsForType(reference.uuid, nutrientType)
                    _nutrientRefs.value = nutrients
                } catch (e: Exception) {
                    _error.value =
                            "Erreur lors du chargement des nutriments: ${e.message ?: "Erreur inconnue"}"
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    /** Réinitialise les nutriments d'un type spécifique */
    fun resetNutrientsForType(nutrientType: MainNutrientEnum) {
        scope.launch {
            _loading.value = true
            try {
                _error.value = ""
                val reference = _currentReference.value
                if (reference == null) {
                    return@launch
                }

                // Conserver les nutriments des autres types
                val otherNutrients = _nutrientRefs.value.filter { it.nutrientType != nutrientType }

                // Générer de nouveaux nutriments pour le type spécifié
                val newNutrients = generateTestNutrientsForType(reference.uuid, nutrientType)

                // Mettre à jour la liste complète
                _nutrientRefs.value = otherNutrients + newNutrients
            } catch (e: Exception) {
                _error.value =
                        "Erreur lors de la réinitialisation: ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Sauvegarde les nutriments d'un type spécifique */
    fun saveNutrientsForType(nutrientType: MainNutrientEnum) {
        scope.launch {
            _loading.value = true
            try {
                _error.value = ""
                val reference = _currentReference.value
                if (reference == null) {
                    return@launch
                }

                // TODO: Implémenter la sauvegarde réelle dans le repository
                // Pour l'instant, nous faisons juste semblant de sauvegarder
                println(
                        "Sauvegarde des nutriments de type $nutrientType pour la référence ${reference.uuid}"
                )
                _nutrientRefs.value.filter { it.nutrientType == nutrientType }.forEach {
                    println("- ${it.name}: ${it.value} ${it.unitReq}")
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la sauvegarde: ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Met à jour la valeur d'un nutriment */
    fun updateNutrientValue(nutrientId: String, newValue: String) {
        val currentList = _nutrientRefs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == nutrientId }
        if (index >= 0) {
            val nutrient = currentList[index]
            currentList[index] = nutrient.copy(value = newValue)
            _nutrientRefs.value = currentList
        }
    }

    /** Met à jour l'unité d'un nutriment */
    fun updateNutrientUnit(nutrientId: String, newUnit: UnitReqEnum) {
        val currentList = _nutrientRefs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == nutrientId }
        if (index >= 0) {
            val nutrient = currentList[index]
            currentList[index] = nutrient.copy(unitReq = newUnit)
            _nutrientRefs.value = currentList
        }
    }

    /** Met à jour la référence bibliographique d'un nutriment */
    fun updateNutrientBiblioRef(nutrientId: String, newBiblioRef: BiblioRef?) {
        val currentList = _nutrientRefs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == nutrientId }
        if (index >= 0) {
            val nutrient = currentList[index]
            currentList[index] = nutrient.copy(biblioRef = newBiblioRef)
            _nutrientRefs.value = currentList
        }
    }

    /** Charge les références bibliographiques disponibles */
    fun loadAvailableBiblioRefs() {
        scope.launch {
            try {
                // TODO: Implémenter le chargement réel depuis le repository
                // Pour l'instant, nous utilisons des données de test
                val biblioRefs =
                        listOf(
                                BiblioRef(
                                        uuid = "biblio1",
                                        firstAuthor = "Dupont",
                                        year = 2020,
                                        completeRef =
                                                "Dupont et al., Etude sur les nutriments, 2020",
                                        comments = "Étude importante"
                                ),
                                BiblioRef(
                                        uuid = "biblio2",
                                        firstAuthor = "Martin",
                                        year = 2021,
                                        completeRef = "Martin J., Nutrition canine, 2021",
                                        comments = "À vérifier"
                                )
                        )
                _availableBiblioRefs.value = biblioRefs
            } catch (e: Exception) {
                _error.value =
                        "Erreur lors du chargement des références biblio: ${e.message ?: "Erreur inconnue"}"
            }
        }
    }

    // Méthodes utilitaires pour générer des données de test

    /** Génère des données de test pour tous les nutriments */
    private fun generateTestNutrients(referenceId: String): List<NutrientRef> {
        val result = mutableListOf<NutrientRef>()

        MainNutrientEnum.values().forEach { type ->
            result.addAll(generateTestNutrientsForType(referenceId, type))
        }

        return result
    }

    /** Génère des données de test pour un type spécifique de nutriments */
    private fun generateTestNutrientsForType(
            referenceId: String,
            type: MainNutrientEnum
    ): List<NutrientRef> {
        val random = Random(type.name.hashCode())
        val result = mutableListOf<NutrientRef>()

        val names =
                when (type) {
                    MainNutrientEnum.BASE -> listOf("Énergie", "Eau", "Matière sèche")
                    MainNutrientEnum.MACRO -> listOf("Protéines", "Lipides", "Glucides", "Fibres")
                    MainNutrientEnum.MIN ->
                            listOf("Calcium", "Phosphore", "Sodium", "Potassium", "Magnésium")
                    MainNutrientEnum.VITAM ->
                            listOf(
                                    "Vitamine A",
                                    "Vitamine D",
                                    "Vitamine E",
                                    "Vitamine B1",
                                    "Vitamine B2"
                            )
                    MainNutrientEnum.LIPID -> listOf("Oméga 3", "Oméga 6", "EPA", "DHA")
                    MainNutrientEnum.AMA -> listOf("Arginine", "Lysine", "Méthionine", "Cystéine")
                    MainNutrientEnum.OTHER -> listOf("Taurine", "Carnitine", "Créatine")
                    MainNutrientEnum.ANA ->
                            listOf("Protéines brutes", "Lipides bruts", "Cellulose brute")
                    MainNutrientEnum.ENERGIE -> listOf("Énergie métabolisable", "Énergie nette")
                    MainNutrientEnum.NO -> listOf("Autres")
                    MainNutrientEnum.INGREDIENT -> listOf("Ingrédient")
                    MainNutrientEnum.INDICAT -> listOf("Indication")
                }

        names.forEachIndexed { index, name ->
            val value =
                    when (type) {
                        MainNutrientEnum.BASE ->
                                if (name == "Énergie") "95-130" else "${random.nextInt(50, 95)}"
                        MainNutrientEnum.MACRO -> "${random.nextInt(10, 40)}"
                        MainNutrientEnum.MIN -> "${random.nextInt(1, 15) / 10.0}"
                        MainNutrientEnum.VITAM -> "${random.nextInt(100, 1000)}"
                        MainNutrientEnum.LIPID -> "${random.nextInt(5, 25) / 10.0}"
                        MainNutrientEnum.AMA -> "${random.nextInt(5, 20) / 10.0}"
                        MainNutrientEnum.OTHER -> "${random.nextInt(1, 10) / 10.0}"
                        MainNutrientEnum.ANA -> "${random.nextInt(20, 50)}"
                        MainNutrientEnum.ENERGIE -> "${random.nextInt(300, 500)}"
                        MainNutrientEnum.NO -> "${random.nextInt(1, 10)}"
                        MainNutrientEnum.INGREDIENT -> "${random.nextInt(1, 100)}"
                        MainNutrientEnum.INDICAT -> "${random.nextInt(1, 5)}"
                    }

            val unitReq =
                    when (type) {
                        MainNutrientEnum.BASE ->
                                if (name == "Énergie") UnitReqEnum.PERKG else UnitReqEnum.PERMS
                        MainNutrientEnum.MACRO -> UnitReqEnum.PERMS
                        MainNutrientEnum.MIN -> UnitReqEnum.PERKG
                        MainNutrientEnum.VITAM -> UnitReqEnum.PERKG
                        MainNutrientEnum.LIPID -> UnitReqEnum.PERKG
                        MainNutrientEnum.AMA -> UnitReqEnum.PERKG
                        MainNutrientEnum.OTHER -> UnitReqEnum.ABSOLUTE
                        MainNutrientEnum.ANA -> UnitReqEnum.PERMS
                        MainNutrientEnum.ENERGIE -> UnitReqEnum.PERKG
                        MainNutrientEnum.NO -> UnitReqEnum.ABSOLUTE
                        MainNutrientEnum.INGREDIENT -> UnitReqEnum.RATIO
                        MainNutrientEnum.INDICAT -> UnitReqEnum.ABSOLUTE
                    }

            // Attribuer une référence bibliographique à certains nutriments
            val biblioRef =
                    if (random.nextBoolean()) {
                        BiblioRef(
                                uuid = "test-${random.nextInt(1, 3)}",
                                firstAuthor = if (random.nextBoolean()) "Dupont" else "Martin",
                                year = 2020 + random.nextInt(0, 3),
                                completeRef = "Référence test"
                        )
                    } else null

            result.add(
                    NutrientRef(
                            id = "nutrient-${type.name}-$index",
                            referenceEvId = referenceId,
                            name = name,
                            value = value,
                            nutrientType = type,
                            nutrientCode = index,
                            unitReq = unitReq,
                            biblioRef = biblioRef
                    )
            )
        }

        return result
    }

    // Extension function pour arrondir les doubles
    private fun Double.round(decimals: Int): String {
        val factor = 10.0.pow(decimals)
        return (kotlin.math.round(this * factor) / factor).toString()
    }

    // Puissance pour les doubles
    private fun Double.pow(n: Int): Double {
        var result = 1.0
        repeat(n) { result *= this }
        return result
    }
}
