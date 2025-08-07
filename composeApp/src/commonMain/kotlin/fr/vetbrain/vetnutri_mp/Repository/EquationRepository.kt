package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.EquationDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

/** Interface définissant les opérations disponibles pour la gestion des équations */
interface EquationRepository {
    /**
     * Récupère toutes les équations
     * @return Une liste de toutes les équations
     */
    suspend fun getAllEquations(): List<Equation>

    /**
     * Récupère toutes les équations sous forme de Flow
     * @return Un Flow de liste d'équations
     */
    fun observeAllEquations(): Flow<List<Equation>>

    /**
     * Récupère une équation par son UUID
     * @param uuid L'UUID de l'équation à récupérer
     * @return L'équation si trouvée, null sinon
     */
    suspend fun getEquationById(uuid: String): Equation?

    /**
     * Sauvegarde une équation
     * @param equation L'équation à sauvegarder
     */
    suspend fun saveEquation(equation: Equation)

    /**
     * Met à jour une équation existante
     * @param equation L'équation à mettre à jour
     */
    suspend fun updateEquation(equation: Equation)

    /**
     * Supprime une équation
     * @param uuid L'UUID de l'équation à supprimer
     */
    suspend fun deleteEquation(uuid: String)

    /**
     * Supprime toutes les équations
     * @return Le nombre d'équations supprimées
     */
    suspend fun clearAllEquations(): Int
}

/** Implémentation en mémoire du repository des équations */
class InMemoryEquationRepository : EquationRepository {
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())

    override suspend fun getAllEquations(): List<Equation> {
        return _equations.value
    }

    override fun observeAllEquations(): Flow<List<Equation>> {
        return _equations.asStateFlow()
    }

    override suspend fun getEquationById(uuid: String): Equation? {
        return _equations.value.find { it.uuid == uuid }
    }

    override suspend fun saveEquation(equation: Equation) {
        // Création d'une nouvelle liste pour déclencher la réactivité
        val newList = _equations.value.toMutableList()
        newList.add(equation)
        _equations.value = newList
    }

    override suspend fun updateEquation(equation: Equation) {
        _equations.update { currentList ->
            currentList.map { if (it.uuid == equation.uuid) equation else it }
        }
    }

    override suspend fun deleteEquation(uuid: String) {
        _equations.update { currentList -> currentList.filter { it.uuid != uuid } }
    }

    override suspend fun clearAllEquations(): Int {
        val count = _equations.value.size
        _equations.value = emptyList()
        return count
    }
}

/** Implémentation qui utilise la base de données pour le repository des équations */
class DatabaseEquationRepository(
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) : EquationRepository {

    private val equationsFlow = MutableStateFlow<List<Equation>>(emptyList())

    init {
        // Ajouter quelques équations d'exemple si la base est vide
        runBlocking {
            val equations = equationDao.getAllEquations()
            // Vérifier si nos équations de test existent déjà
            val hasTestEquations =
                    equations.any {
                        it.uuid in listOf("equation-7", "equation-8", "equation-9", "equation-10")
                    }

            if (equations.isEmpty() || !hasTestEquations) {
                // Récupérer une référence bibliographique existante ou créer une référence par
                // défaut
                val biblioRefFromDb = biblioRefDao.getAllBiblioRefs().firstOrNull()?.toDomain()
                val biblioRef =
                        biblioRefFromDb
                                ?: BiblioRef(
                                        uuid = "default-biblio",
                                        firstAuthor = "Système",
                                        year = 2024,
                                        completeRef = "Référence générée automatiquement",
                                        comments = "Référence par défaut",
                                        bibtex = "",
                                        consistent = 1
                                )

                // Équation 1 : Besoin énergétique pour chien
                val equation1 =
                        Equation(
                                uuid = "equation-1",
                                name = "BEE Chien",
                                description = "Besoin énergétique d'entretien pour chien",
                                equationScript = "70 * (BW^0.75)",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.ENERGYNEED,
                                consistent = true
                        )

                // Équation 2 : Besoin énergétique pour chat
                val equation2 =
                        Equation(
                                uuid = "equation-2",
                                name = "BEE Chat",
                                description = "Besoin énergétique d'entretien pour chat",
                                equationScript = "100 * (BW^0.67)",
                                bib = biblioRef,
                                specie = Espece.CHAT,
                                kind = EquationKind.ENERGYNEED,
                                consistent = true
                        )

                // Équation 3 : Poids métabolique pour chien
                val equation3 =
                        Equation(
                                uuid = "equation-3",
                                name = "Poids métabolique chien",
                                description = "Calcul du poids métabolique pour chien",
                                equationScript = "BW^0.75",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.MW,
                                consistent = true
                        )

                // Équation 4 : Poids métabolique pour chat
                val equation4 =
                        Equation(
                                uuid = "equation-4",
                                name = "Poids métabolique chat",
                                description = "Calcul du poids métabolique pour chat",
                                equationScript = "BW^0.67",
                                bib = biblioRef,
                                specie = Espece.CHAT,
                                kind = EquationKind.MW,
                                consistent = true
                        )

                // Équation 5 : Énergie digestible pour aliments commerciaux (chien)
                val equation5 =
                        Equation(
                                uuid = "equation-5",
                                name = "Énergie digestible commerciale chien",
                                description =
                                        "Calcul de l'énergie digestible des aliments commerciaux pour chien",
                                equationScript = "(PB * 3.5) + (MG * 8.5) + (ENA * 3.5)",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.ENERGYDENSITY,
                                consistent = true
                        )

                // Équation 6 : Énergie digestible pour aliments bruts (chien)
                val equation6 =
                        Equation(
                                uuid = "equation-6",
                                name = "Énergie digestible brute chien",
                                description =
                                        "Calcul de l'énergie digestible des aliments bruts pour chien",
                                equationScript = "(PB * 3.0) + (MG * 8.0) + (ENA * 3.0)",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.ENERGYDENSITY,
                                consistent = true
                        )

                // Équation 7 : Vitamine A complémentaire pour chien (global)
                val equation7 =
                        Equation(
                                uuid = "equation-7",
                                name = "Vitamine A complémentaire",
                                description = "Calcul de la vitamine A complémentaire pour chien",
                                equationScript = "BEE * 0.001",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                                nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITA,
                                consistent = true
                        )

                // Équation 8 : Calcium complémentaire pour chien (global)
                val equation8 =
                        Equation(
                                uuid = "equation-8",
                                name = "Calcium complémentaire",
                                description = "Calcul du calcium complémentaire pour chien",
                                equationScript = "BW * 0.02",
                                bib = biblioRef,
                                specie = Espece.CHIEN,
                                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                                nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL,
                                consistent = true
                        )

                // Équation 9 : Vitamine D complémentaire pour chat (spécifique)
                val equation9 =
                        Equation(
                                uuid = "equation-9",
                                name = "Vitamine D complémentaire chat",
                                description = "Calcul de la vitamine D complémentaire pour chat",
                                equationScript = "BEE * 0.0005",
                                bib = biblioRef,
                                specie = Espece.CHAT,
                                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                                nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITD,
                                consistent = true
                        )

                // Équation 10 : Phosphore complémentaire pour chat (spécifique)
                val equation10 =
                        Equation(
                                uuid = "equation-10",
                                name = "Phosphore complémentaire chat",
                                description = "Calcul du phosphore complémentaire pour chat",
                                equationScript = "BW * 0.015",
                                bib = biblioRef,
                                specie = Espece.CHAT,
                                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                                nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS,
                                consistent = true
                        )

                // Sauvegarder les équations
                equationDao.insertEquation(equation1.toEntity())
                equationDao.insertEquation(equation2.toEntity())
                equationDao.insertEquation(equation3.toEntity())
                equationDao.insertEquation(equation4.toEntity())
                equationDao.insertEquation(equation5.toEntity())
                equationDao.insertEquation(equation6.toEntity())
                equationDao.insertEquation(equation7.toEntity())
                equationDao.insertEquation(equation8.toEntity())
                equationDao.insertEquation(equation9.toEntity())
                equationDao.insertEquation(equation10.toEntity())
            }

            // Charger les équations initiales
            loadEquations()
        }
    }

    private suspend fun loadEquations() {
        val equations =
                equationDao.getAllEquations().mapNotNull { entity ->
                    val biblioRef =
                            entity.bibRef?.let { biblioRefDao.getBiblioRefById(it)?.toDomain() }
                    entity.toDomain(biblioRef)
                }
        equationsFlow.value = equations
    }

    override suspend fun getAllEquations(): List<Equation> {
        loadEquations()
        return equationsFlow.value
    }

    override fun observeAllEquations(): Flow<List<Equation>> {
        return equationsFlow.asStateFlow()
    }

    override suspend fun getEquationById(uuid: String): Equation? {
        val entity = equationDao.getEquationById(uuid) ?: return null
        val biblioRef = entity.bibRef?.let { biblioRefDao.getBiblioRefById(it)?.toDomain() }
        return entity.toDomain(biblioRef)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveEquation(equation: Equation) {

        try {
            // Si l'UUID est vide, créer une nouvelle équation avec un UUID généré
            val equationToSave =
                    if (equation.uuid.isEmpty()) {
                        equation.copy(uuid = kotlin.uuid.Uuid.random().toString())
                    } else {
                        equation
                    }

            val entity = equationToSave.toEntity()

            // Vérifier si l'équation existe déjà
            val existingEquation = equationDao.getEquationById(equationToSave.uuid)
            if (existingEquation != null) {
                // Équation existante : utiliser updateEquation
                equationDao.updateEquation(entity)
            } else {
                // Nouvelle équation : utiliser insertEquation
                equationDao.insertEquation(entity)
            }

            loadEquations()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun updateEquation(equation: Equation) {
        equationDao.updateEquation(equation.toEntity())
        loadEquations()
    }

    override suspend fun deleteEquation(uuid: String) {
        equationDao.deleteEquation(uuid)
    }

    override suspend fun clearAllEquations(): Int {
        println("DEBUG DatabaseEquationRepository: clearAllEquations() démarrée")

        return try {
            // Obtenir le nombre total d'équations avant suppression
            val allEquations = getAllEquations()
            val count = allEquations.size

            if (count > 0) {
                // Supprimer toutes les équations
                equationDao.deleteAllEquations()
            }

            count
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
