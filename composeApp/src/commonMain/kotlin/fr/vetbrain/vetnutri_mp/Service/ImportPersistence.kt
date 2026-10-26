package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.RaceCodeMapper
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import fr.vetbrain.vetnutri_mp.Utils.ImportCommun
import fr.vetbrain.vetnutri_mp.Utils.NutritionalRequirementImporter
import fr.vetbrain.vetnutri_mp.Utils.SauvegardeResult

/** Persistance des données importées dans les repositories (extrait de ImportUtils). */
object ImportPersistence {
    /** Sauvegarde automatiquement tous les éléments importés dans la base de données */
    suspend fun sauvegarderDonneesImportees(
            referencesImportees: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository?,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository?
    ): SauvegardeResult {
        var referencesAjoutees = 0
        var referencesMisesAJour = 0
        var equationsAjoutees = 0
        var equationsMisesAJour = 0
        var bibliographiesAjoutees = 0
        var bibliographiesMisesAJour = 0
        val erreurs = mutableListOf<String>()

        try {
            // 1. Sauvegarder toutes les bibliographies d'abord (références)
            if (biblioRefRepository != null) {
                val toutesLesBibliographies = mutableSetOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef>()

                for (reference in referencesImportees) {
                    // Collecter toutes les bibliographies de la référence
                    reference.getAllBiblioRefs().forEach {
                            biblio: fr.vetbrain.vetnutri_mp.Data.BiblioRef ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    // Collecter les bibliographies des équations assignées aux propriétés
                    // individuelles
                    reference.equationBEE?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationBW?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationDEraw?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    reference.equationDEcom?.bib?.let { biblio ->
                        if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                            toutesLesBibliographies.add(biblio)
                        }
                    }

                    // Bibliographies des équations nutritionnelles
                    reference.equationsNut.forEach { equation ->
                        equation.bib?.let { biblio ->
                            if (biblio.uuid.isNotBlank() && biblio.firstAuthor.isNotBlank()) {
                                toutesLesBibliographies.add(biblio)
                            }
                        }
                    }
                }

                for (bibliographie in toutesLesBibliographies) {
                    try {
                        val existante = biblioRefRepository.getBiblioRefById(bibliographie.uuid)
                        if (existante != null) {
                            if (existante != bibliographie) {
                                biblioRefRepository.updateBiblioRef(bibliographie)
                                bibliographiesMisesAJour++
                            }
                        } else {
                            biblioRefRepository.insertBiblioRef(bibliographie)
                            bibliographiesAjoutees++
                        }
                    } catch (e: Exception) {
                        erreurs.add(
                                "Erreur bibliographie ${bibliographie.firstAuthor}: ${e.message}"
                        )
                    }
                }
            }

            // 2. Sauvegarder toutes les équations ensuite
            if (equationRepository != null) {
                val toutesLesEquations = mutableSetOf<fr.vetbrain.vetnutri_mp.Data.Equation>()

                for (reference in referencesImportees) {
                    // Collecter toutes les équations de la référence
                    // Équations des listes existantes
                    reference.getAllEquations().forEach {
                            equation: fr.vetbrain.vetnutri_mp.Data.Equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    // Équations assignées aux propriétés individuelles (nouvellement importées)
                    reference.equationBEE?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationBW?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationDEraw?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    reference.equationDEcom?.let { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }

                    // Équations dans la liste equationsNut
                    reference.equationsNut.forEach { equation ->
                        if (equation.uuid.isNotBlank() && equation.name.isNotBlank()) {
                            toutesLesEquations.add(equation)
                        }
                    }
                }

                for (equation in toutesLesEquations) {
                    try {
                        val existante = equationRepository.getEquationById(equation.uuid)
                        if (existante != null) {
                            if (existante != equation) {
                                equationRepository.updateEquation(equation)
                                equationsMisesAJour++
                            }
                        } else {
                            equationRepository.saveEquation(equation)
                            equationsAjoutees++
                        }
                    } catch (e: Exception) {
                        erreurs.add("Erreur équation ${equation.name}: ${e.message}")
                    }
                }
            }

            // 3. Sauvegarder les références nutritionnelles enfin
            if (databaseReferenceEvRepository != null) {
                for (reference in referencesImportees) {
                    try {
                        val existante =
                                databaseReferenceEvRepository.getReferenceEvById(reference.uuid)
                        if (existante != null) {
                            if (existante != reference) {
                                databaseReferenceEvRepository.updateReferenceEv(reference)
                                referencesMisesAJour++
                            }
                        } else {
                            databaseReferenceEvRepository.saveReferenceEv(reference)
                            referencesAjoutees++
                            
                        }
                    } catch (e: Exception) {
                        erreurs.add("Erreur référence ${reference.nom}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            erreurs.add("Erreur générale de sauvegarde: ${e.message}")
        }

        return SauvegardeResult(
                referencesAjoutees = referencesAjoutees,
                referencesMisesAJour = referencesMisesAJour,
                equationsAjoutees = equationsAjoutees,
                equationsMisesAJour = equationsMisesAJour,
                bibliographiesAjoutees = bibliographiesAjoutees,
                bibliographiesMisesAJour = bibliographiesMisesAJour,
                erreurs = erreurs
        )
    }

    /**
     * Importe des références nutritionnelles depuis un fichier JSON .vbnr avec sauvegarde
     * automatique en base de données
     *
     * @param jsonContent Le contenu JSON du fichier .vbnr.json à importer
     * @param databaseReferenceEvRepository Repository pour sauvegarder les références (optionnel)
     * @param equationRepository Repository pour sauvegarder les équations (optionnel)
     * @param biblioRefRepository Repository pour sauvegarder les bibliographies (optionnel)
     * @param sauvegarderEnBase Si true, sauvegarde automatiquement en base de données
     * @return La liste des références nutritionnelles importées
     */
    suspend fun importNutritionalRequirementsFromJson(
            jsonContent: String,
            databaseReferenceEvRepository:
                    fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository? =
                    null,
            equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null,
            biblioRefRepository: fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository? = null,
            sauvegarderEnBase: Boolean = true
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {

        try {
            // Vérifier si le contenu est un JSON valide
            if (!ImportCommun.isValidJson(jsonContent)) {
                return emptyList()
            }

            // Vérifier si le contenu correspond à un fichier de références nutritionnelles
            if (!NutritionalRequirementImporter.isNutritionalRequirementJsonContent(jsonContent)) {
                return emptyList()
            }

            val references = mutableListOf<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>()
            val jsonElement = ImportCommun.json.parseToJsonElement(jsonContent)

            when (jsonElement) {
                is kotlinx.serialization.json.JsonArray -> {

                    jsonElement.forEachIndexed { index, element ->
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val ref = NutritionalRequirementImporter.creerReferenceDepuisNutritionalRequirementData(element, index)
                            if (ref != null) {
                                references.add(ref)
                            } else {}
                        }
                    }
                }
                is kotlinx.serialization.json.JsonObject -> {
                    val ref = NutritionalRequirementImporter.creerReferenceDepuisNutritionalRequirementData(jsonElement, 0)
                    if (ref != null) {
                        references.add(ref)
                    } else {}
                }
                else -> {
                    return emptyList()
                }
            }

            // Afficher un résumé
            if (references.isNotEmpty()) {
                references.forEach { ref ->
                    
                    if (ref.maladie) {}
                }
            }

            if (sauvegarderEnBase) {
                val result =
                        sauvegarderDonneesImportees(
                                references,
                                databaseReferenceEvRepository,
                                equationRepository,
                                biblioRefRepository
                        )
            }

            return references
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

}
