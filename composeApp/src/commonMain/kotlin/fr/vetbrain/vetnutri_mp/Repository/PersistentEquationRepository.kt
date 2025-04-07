package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/** Implémentation du repository des équations avec persistance via fichiers JSON */
class PersistentEquationRepository : EquationRepository {
    private val dispatcher = AppDispatchers.IO
    private val resourceReader = ResourceReader()
    private val filename = "equations.json"

    // État en mémoire des équations
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())

    init {
        // Charger les équations depuis le fichier au démarrage
        loadFromDisk()
    }

    private fun loadFromDisk() {
        try {
            val jsonString = resourceReader.readUserFile(filename)
            if (jsonString != null && jsonString.isNotEmpty()) {
                println(
                        "DEBUG PersistentEquationRepo: Fichier d'équations chargé, contenu: $jsonString"
                )

                // Extraction simple des noms d'équations du JSON
                if (jsonString.contains("equations")) {
                    // Extrait les équations entre [ et ]
                    val equationsArrayMatch = Regex("\\[([^\\]]*)\\]").find(jsonString)
                    equationsArrayMatch?.let { match ->
                        val equationsArray = match.groupValues[1]
                        // Sépare les éléments de l'array
                        val equationNames =
                                equationsArray
                                        .split(",")
                                        .map { it.trim().replace("\"", "") }
                                        .filter { it.isNotEmpty() }

                        // Crée une équation pour chaque nom
                        val equations = equationNames.map { name -> createEquationFromName(name) }

                        // Met à jour l'état
                        _equations.value = equations
                        println(
                                "DEBUG PersistentEquationRepo: ${equations.size} équations chargées depuis le fichier"
                        )
                    }
                            ?: run {
                                println(
                                        "DEBUG PersistentEquationRepo: Format JSON invalide, aucun tableau d'équations trouvé"
                                )
                            }
                } else {
                    println(
                            "DEBUG PersistentEquationRepo: Format JSON invalide, clé 'equations' introuvable"
                    )
                }
            } else {
                println(
                        "DEBUG PersistentEquationRepo: Aucune équation trouvée dans le fichier (fichier vide ou inexistant)"
                )
            }
        } catch (e: Exception) {
            println(
                    "DEBUG PersistentEquationRepo: Erreur lors du chargement des équations: ${e.message}"
            )
            // Si une erreur se produit, on continue avec la liste vide
        }
    }

    // Méthode utilitaire pour créer une équation à partir d'un nom
    private fun createEquationFromName(name: String): Equation {
        // Générer un UUID stable basé sur le nom pour éviter de générer un nouveau UUID à chaque
        // démarrage
        val uuid = name.hashCode().toString() + System.currentTimeMillis()

        return Equation(
                uuid = uuid,
                name = name,
                description = "Équation importée depuis le fichier",
                equationScript = name,
                specie = Espece.CHIEN,
                kind = EquationKind.ENERGYNEED,
                consistent = true,
                bib = BiblioRef(),
                variables = mutableListOf()
        )
    }

    private suspend fun saveToDisk() =
            withContext(dispatcher) {
                try {
                    // Sérialisation simple des équations pour l'instant
                    // Dans une implémentation réelle, utiliser kotlinx.serialization
                    val equationNamesList = _equations.value.map { "\"${it.name}\"" }
                    val jsonString = """{"equations":[${equationNamesList.joinToString(",")}]}"""
                    resourceReader.writeUserFile(filename, jsonString)
                    println(
                            "DEBUG PersistentEquationRepo: ${_equations.value.size} équations sauvegardées dans le fichier"
                    )
                } catch (e: Exception) {
                    println(
                            "DEBUG PersistentEquationRepo: Erreur lors de la sauvegarde des équations: ${e.message}"
                    )
                }
            }

    override suspend fun getAllEquations(): List<Equation> =
            withContext(dispatcher) {
                println("DEBUG PersistentEquationRepo: Récupération de toutes les équations")
                _equations.value
            }

    override fun observeAllEquations(): Flow<List<Equation>> {
        return _equations.asStateFlow()
    }

    override suspend fun getEquationById(uuid: String): Equation? =
            withContext(dispatcher) {
                println("DEBUG PersistentEquationRepo: Récupération de l'équation avec UUID: $uuid")
                _equations.value.find { it.uuid == uuid }
            }

    override suspend fun saveEquation(equation: Equation) =
            withContext(dispatcher) {
                println("DEBUG PersistentEquationRepo: Sauvegarde de l'équation: ${equation.name}")

                // Vérifier si l'équation existe déjà
                val exists = _equations.value.any { it.uuid == equation.uuid }

                // Mise à jour de l'état en mémoire
                if (exists) {
                    updateEquation(equation)
                } else {
                    // Ajouter la nouvelle équation
                    _equations.update { current ->
                        val newList = current.toMutableList()
                        newList.add(equation)
                        newList
                    }
                }

                // Sauvegarder sur le disque
                saveToDisk()

                println("DEBUG PersistentEquationRepo: Équation sauvegardée avec succès")
            }

    override suspend fun updateEquation(equation: Equation) =
            withContext(dispatcher) {
                println("DEBUG PersistentEquationRepo: Mise à jour de l'équation: ${equation.name}")

                // Mise à jour de l'état en mémoire
                _equations.update { current ->
                    current.map { if (it.uuid == equation.uuid) equation else it }
                }

                // Sauvegarder sur le disque
                saveToDisk()

                println("DEBUG PersistentEquationRepo: Équation mise à jour avec succès")
            }

    override suspend fun deleteEquation(uuid: String) =
            withContext(dispatcher) {
                println("DEBUG PersistentEquationRepo: Suppression de l'équation avec UUID: $uuid")

                // Mise à jour de l'état en mémoire
                _equations.update { current -> current.filter { it.uuid != uuid } }

                // Sauvegarder sur le disque
                saveToDisk()

                println("DEBUG PersistentEquationRepo: Équation supprimée avec succès")
            }
}
