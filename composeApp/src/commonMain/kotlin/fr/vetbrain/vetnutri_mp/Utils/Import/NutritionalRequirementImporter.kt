package fr.vetbrain.vetnutri_mp.Utils

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

/** Importation JSON des références nutritionnelles, équations et bibliographies (extrait de ImportUtils). */
object NutritionalRequirementImporter {
    /**
     * Importe une liste de références nutritionnelles à partir d'une chaîne JSON Format attendu:
     * .vbnr.json (VetBrain Nutritional Requirements) Structure: Array de NutritionalRequirementData
     * avec reference, allEquations, allBibliographicReferences, nutrientRequirements
     *
     * @param jsonContent Le contenu JSON à désérialiser (.vbnr.json format)
     * @return La liste des références nutritionnelles importées
     */
    fun importNutritionalRequirementsFromJson(
            jsonContent: String
    ): List<fr.vetbrain.vetnutri_mp.Data.ReferenceEv> {

        try {
            // Vérifier si le contenu est un JSON valide
            if (!ImportCommun.isValidJson(jsonContent)) {
                return emptyList()
            }

            // Vérifier si le contenu correspond à un fichier de références nutritionnelles
            if (!isNutritionalRequirementJsonContent(jsonContent)) {
                return emptyList()
            }

            val references = mutableListOf<fr.vetbrain.vetnutri_mp.Data.ReferenceEv>()
            val jsonElement = ImportCommun.json.parseToJsonElement(jsonContent)

            when (jsonElement) {
                is kotlinx.serialization.json.JsonArray -> {

                    jsonElement.forEachIndexed { index, element ->
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val ref = creerReferenceDepuisNutritionalRequirementData(element, index)
                            if (ref != null) {
                                references.add(ref)
                            } else {}
                        }
                    }
                }
                is kotlinx.serialization.json.JsonObject -> {
                    val ref = creerReferenceDepuisNutritionalRequirementData(jsonElement, 0)
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

            return references
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Crée une ReferenceEv à partir d'un objet NutritionalRequirementData JSON Structure attendue:
     * { reference: {...}, allEquations: [...], allBibliographicReferences: [...],
     * nutrientRequirements: [...] }
     */
    internal fun creerReferenceDepuisNutritionalRequirementData(
            dataObj: kotlinx.serialization.json.JsonObject,
            index: Int
    ): fr.vetbrain.vetnutri_mp.Data.ReferenceEv? {
        return try {
            // Extraire l'objet reference du NutritionalRequirementData
            val referenceObj = dataObj["reference"] as? kotlinx.serialization.json.JsonObject

            if (referenceObj == null) {
                return null
            }

            // Extraire les propriétés de base de la référence
            val uuid =
                    extraireStringDepuisJson(referenceObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()

            val nom =
                    extraireStringDepuisJson(referenceObj, "nom")
                            ?: "Référence importée ${index + 1}"

            val description =
                    extraireStringDepuisJson(referenceObj, "description")
                            ?: "Référence nutritionnelle importée automatiquement"

            val disease = extraireBooleanDepuisJson(referenceObj, "disease") ?: false
            val nameDisease = extraireStringDepuisJson(referenceObj, "nameDisease") ?: ""
            val nameEnergy =
                    extraireStringDepuisJson(referenceObj, "nameEnergy") ?: "Énergie métabolisable"
            val consistent = extraireIntDepuisJson(referenceObj, "consistent") ?: 1

            // Convertir les espèces et stades physiologiques
            val speciesStr = extraireStringDepuisJson(referenceObj, "species") ?: "CHIEN"
            val sPhysioStr = extraireStringDepuisJson(referenceObj, "sPhysio") ?: "ADULTE"

            val espece = convertirStringVersEspece(speciesStr)

            // Utiliser la détection intelligente pour le stade physiologique
            val stadePhysio = detecterStadePhysioDepuisNom(nom, sPhysioStr)

            // Utiliser la détection intelligente pour les maladies
            val (estMaladie, nomMaladieDetecte) = detecterMaladie(nom, disease, nameDisease)

            // Créer la référence de base
            val reference =
                    fr.vetbrain.vetnutri_mp.Data.ReferenceEv(
                            uuid = uuid,
                            nom = nom,
                            description = description,
                            maladie = estMaladie,
                            nomMaladie = nomMaladieDetecte,
                            nomEnergie = nameEnergy,
                            consistent = consistent,
                            espece = espece,
                            stadePhysio = stadePhysio
                    )

            // Traiter les équations si présentes
            val allEquations = dataObj["allEquations"] as? kotlinx.serialization.json.JsonArray
            if (allEquations != null) {
                val equations = traiterEquations(allEquations)

                // Assigner les équations selon leur type
                equations.forEach { equation ->
                    when (equation.kind) {
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED -> {
                            if (equation.name.contains("BEE", ignoreCase = true) ||
                                            equation.name.contains("energy", ignoreCase = true)
                            ) {
                                reference.equationBEE = equation
                            } else {
                                reference.equationsNut.add(equation)
                            }
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW -> {
                            reference.equationBW = equation
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY -> {
                            if (equation.name.contains("commercial", ignoreCase = true)) {
                                reference.equationDEcom = equation
                            } else {
                                reference.equationDEraw = equation
                            }
                        }
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED -> {
                            reference.equationsNut.add(equation)
                        }
                        else -> {
                            reference.equationsNut.add(equation)
                        }
                    }
                }

                // Vérification finale des équations assignées
                reference.equationsNut.forEach { eq ->
                    
                }
            } else {}

            // Traiter les références bibliographiques
            val allBibliographicReferences =
                    dataObj["allBibliographicReferences"] as? kotlinx.serialization.json.JsonArray
            if (allBibliographicReferences != null) {
                // Les références biblio sont utilisées dans les nutriments, pas stockées
                // directement dans la référence
            }

            // Traiter les besoins nutritionnels
            val nutrientRequirements =
                    dataObj["nutrientRequirements"] as? kotlinx.serialization.json.JsonArray
            if (nutrientRequirements != null) {
                traiterBesoinsNutritionnels(
                        reference,
                        nutrientRequirements,
                        allBibliographicReferences
                )
            }

            // Traiter les coefficients de modification si présents
            traiterCoefficientsModification(reference, referenceObj)

            

            return reference
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /** Traite les équations d'un tableau JSON et les convertit en objets Equation */
    private fun traiterEquations(
            equationsArray: kotlinx.serialization.json.JsonArray
    ): List<fr.vetbrain.vetnutri_mp.Data.Equation> {
        val equations = mutableListOf<fr.vetbrain.vetnutri_mp.Data.Equation>()

        equationsArray.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                try {
                    val equation = creerEquationDepuisJson(element)
                    if (equation != null) {
                        equations.add(equation)
                    }
                } catch (e: Exception) {}
            }
        }

        return equations
    }

    /** Crée un objet Equation à partir d'un objet JSON */
    private fun creerEquationDepuisJson(
            equationObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.Equation? {
        try {
            val uuid =
                    extraireStringDepuisJson(equationObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val name = extraireStringDepuisJson(equationObj, "name") ?: "Équation importée"
            val description =
                    extraireStringDepuisJson(equationObj, "Description")
                            ?: extraireStringDepuisJson(equationObj, "description") ?: ""
            val equationScript = extraireStringDepuisJson(equationObj, "equationScript") ?: "0"

            // Gestion du kind - peut être une string ou un int selon le format JSON
            val kindValue: Int =
                    when (val kindRaw = equationObj["kind"]) {
                        is kotlinx.serialization.json.JsonPrimitive -> {
                            if (kindRaw.isString) {
                                // C'est une string comme "ENERGYNEED", convertir en int
                                when (kindRaw.content.uppercase()) {
                                    "ENERGYNEED" -> 0
                                    "ENERGYDENSITY" -> 1
                                    "MW" -> 2
                                    "INDICATOR" -> 3
                                    "NEED" -> 4
                                    else -> 0
                                }
                            } else {
                                // C'est déjà un int
                                when (kindRaw) {
                                    is kotlinx.serialization.json.JsonPrimitive -> {
                                        if (kindRaw.isString) {
                                            kindRaw.content.toIntOrNull() ?: 0
                                        } else {
                                            try {
                                                kindRaw.content.toIntOrNull() ?: 0
                                            } catch (e: Exception) {
                                                0
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> 0
                    }

            val speciesStr =
                    extraireStringDepuisJson(equationObj, "Specie")
                            ?: extraireStringDepuisJson(equationObj, "specie") ?: "CHIEN"
            val consistent = extraireBooleanDepuisJson(equationObj, "consistent") ?: true

            // Convertir le kind en énumération
            val kind =
                    when (kindValue) {
                        0 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
                        1 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY
                        2 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW
                        3 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.INDICATOR
                        4 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED
                        5 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.COMPLEMENTARY_NUTRIENT
                        6 -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERCOMP
                        else -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
                    }

            val espece = convertirStringVersEspece(speciesStr)

            // Traiter la référence bibliographique si présente
            val bibRef =
                    equationObj["bib"]?.let { bibElement ->
                        if (bibElement is kotlinx.serialization.json.JsonObject) {
                            creerBiblioRefDepuisJsonComplet(bibElement)
                        } else null
                    }
                            ?: creerBiblioRefParDefaut(
                                    "Aucune référence bibliographique fournie pour l'équation"
                            )

            val equation =
                    fr.vetbrain.vetnutri_mp.Data.Equation(
                            uuid = uuid,
                            name = name,
                            description = description,
                            equationScript = equationScript,
                            kind = kind,
                            specie = espece,
                            bib = bibRef,
                            consistent = consistent
                    )

            
            return equation
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Crée un objet BiblioRef à partir d'un objet JSON complet (avec firstAuthor, year,
     * completeRef)
     */
    private fun creerBiblioRefDepuisJsonComplet(
            biblioObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.BiblioRef? {
        try {
            val uuid = extraireStringDepuisJson(biblioObj, "UUID")
            val firstAuthor = extraireStringDepuisJson(biblioObj, "firstAuthor") ?: ""
            val year = extraireIntDepuisJson(biblioObj, "year") ?: 0
            val completeRef = extraireStringDepuisJson(biblioObj, "completeRef") ?: ""
            val comment = extraireStringDepuisJson(biblioObj, "comment") ?: ""
            val consistent = extraireIntDepuisJson(biblioObj, "consistent") ?: 1

            // Vérifier si les données sont valides et utilisables
            if (uuid.isNullOrBlank() || firstAuthor.isBlank() || year < 1900 || consistent == 0) {

                return creerBiblioRefParDefaut("Données incomplètes dans JSON")
            }

            val biblioRef =
                    fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                            uuid = uuid,
                            firstAuthor = firstAuthor,
                            year = year,
                            completeRef = completeRef,
                            comments = comment,
                            consistent = consistent
                    )

            
            return biblioRef
        } catch (e: Exception) {
            e.printStackTrace()
            return creerBiblioRefParDefaut("Erreur lors de la création")
        }
    }

    /** Crée une référence bibliographique par défaut pour éviter les erreurs de clé étrangère */
    private fun creerBiblioRefParDefaut(motif: String): fr.vetbrain.vetnutri_mp.Data.BiblioRef {
        // Utiliser un UUID fixe pour éviter les duplicatas et garantir la cohérence
        val defaultUuid = "default-biblio"

        return fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                uuid = defaultUuid,
                firstAuthor = "Système VetNutri",
                year = 2024,
                completeRef = "Référence par défaut générée automatiquement lors de l'import",
                comments = "Créée automatiquement - $motif",
                consistent = 1
        )
    }

    /** Crée un objet BiblioRef à partir d'un objet JSON (ancienne version pour compatibilité) */
    private fun creerBiblioRefDepuisJson(
            biblioObj: kotlinx.serialization.json.JsonObject
    ): fr.vetbrain.vetnutri_mp.Data.BiblioRef? {
        // Essayer d'abord la version complète
        val completeVersion = creerBiblioRefDepuisJsonComplet(biblioObj)
        if (completeVersion != null && completeVersion.firstAuthor.isNotBlank()) {
            return completeVersion
        }

        // Sinon, essayer l'ancienne méthode
        try {
            val uuid =
                    extraireStringDepuisJson(biblioObj, "UUID")
                            ?: fr.vetbrain.vetnutri_mp.Utils.genUUID()
            val author = extraireStringDepuisJson(biblioObj, "author") ?: ""
            val title = extraireStringDepuisJson(biblioObj, "title") ?: ""
            val year = extraireIntDepuisJson(biblioObj, "year") ?: 0
            val completeRef = "$author. $title. $year"

            return fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                    uuid = uuid,
                    firstAuthor = author,
                    year = year,
                    completeRef = completeRef
            )
        } catch (e: Exception) {
            return null
        }
    }

    /** Traite les besoins nutritionnels et les ajoute aux maps de la référence */
    private fun traiterBesoinsNutritionnels(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            nutrientRequirements: kotlinx.serialization.json.JsonArray,
            allBibliographicReferences: kotlinx.serialization.json.JsonArray?
    ) {
        // Créer un index des références bibliographiques pour la recherche rapide
        val biblioIndex = mutableMapOf<String, fr.vetbrain.vetnutri_mp.Data.BiblioRef>()
        allBibliographicReferences?.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                val biblioRef = creerBiblioRefDepuisJsonComplet(element)
                if (biblioRef != null && biblioRef.uuid.isNotBlank()) {
                    biblioIndex[biblioRef.uuid] = biblioRef
                }
            }
        }

        nutrientRequirements.forEach { element ->
            if (element is kotlinx.serialization.json.JsonObject) {
                try {
                    val nutrientInfo = creerNutrientRequirementInfoDepuisJson(element, biblioIndex)
                    if (nutrientInfo != null) {
                        ajouterNutrientALaReference(reference, nutrientInfo)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    /** Crée un objet représentant un besoin nutritionnel à partir du JSON */
    private fun creerNutrientRequirementInfoDepuisJson(
            nutrientObj: kotlinx.serialization.json.JsonObject,
            biblioIndex: Map<String, fr.vetbrain.vetnutri_mp.Data.BiblioRef>
    ): NutrientRequirementInfo? {
        try {
            // Extraire le nutriment - le JSON a directement "nutrient": "K"
            val nutrientString = extraireStringDepuisJson(nutrientObj, "nutrient")
            if (nutrientString == null) {
                return null
            }

            // Normaliser le nom du nutriment pour la résolution
            val normalizedNutrientString = normaliserNomNutrient(nutrientString)
            
            // 🔍 LOG DIAGNOSTIC : Tracer la résolution des nutriments
            

            var finalNutrient =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                            normalizedNutrientString
                    )

            if (finalNutrient != null) {
                // 🔍 LOG DIAGNOSTIC : Nutriment résolu avec succès
                
            } else {
                // 🔍 LOG DIAGNOSTIC : Échec de la résolution
                
                ImportUtils.resolutionsProblematiques.add(
                        "ECHEC_RESOLUTION_EXACTE: '$nutrientString' → '$normalizedNutrientString'"
                )

                // Essayer avec d'autres variantes courantes
                val variantes = genererVariantesNutrient(nutrientString)
                for (variante in variantes) {
                    finalNutrient =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                    variante
                            )
                    if (finalNutrient != null) {
                        ImportUtils.resolutionsProblematiques.add(
                                "RESOLUTION_PAR_VARIANTE: '$nutrientString' → '$variante' (${finalNutrient::class.simpleName})"
                        )
                        break
                    }
                }

                if (finalNutrient == null) {
                    ImportUtils.resolutionsProblematiques.add(
                            "ECHEC_TOTAL: '$nutrientString' - Aucune correspondance trouvée"
                    )
                    return null
                }
            }

            // Extraire le niveau de référence
            val referenceLevelStr = extraireStringDepuisJson(nutrientObj, "referenceLevel") ?: "MIN"
            val referenceLevel = convertirStringVersReflevel(referenceLevelStr)

            // Extraire la quantité et les unités
            val quantity = extraireDoubleDepuisJson(nutrientObj, "quantity") ?: 0.0
            val unitStr = extraireStringDepuisJson(nutrientObj, "unit") ?: "BUg"
            val unitRequirementStr =
                    extraireStringDepuisJson(nutrientObj, "unitRequirement") ?: "MCAL"

            val unit = convertirStringVersUnitEnum(unitStr)
            val unitRequirement = convertirStringVersUnitReqEnum(unitRequirementStr)

            // Extraire la référence bibliographique
            val biblioRefElement =
                    nutrientObj["bibliographicReference"] as? kotlinx.serialization.json.JsonObject
            val biblioRef =
                    if (biblioRefElement != null) {
                        // Chercher d'abord par UUID si présent
                        val biblioUuid = extraireStringDepuisJson(biblioRefElement, "UUID")
                        if (biblioUuid != null && biblioIndex.containsKey(biblioUuid)) {
                            biblioIndex[biblioUuid]
                        } else {
                            // Sinon créer directement depuis le JSON
                            creerBiblioRefDepuisJson(biblioRefElement)
                        }
                    } else null

            return NutrientRequirementInfo(
                    nutrient = finalNutrient,
                    referenceLevel = referenceLevel,
                    quantity = quantity,
                    unit = unit,
                    unitRequirement = unitRequirement,
                    bibliographicReference = biblioRef
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /** Normalise le nom d'un nutriment pour une meilleure résolution */
    private fun normaliserNomNutrient(nutrient: String): String {
        return when (nutrient.uppercase().trim()) {
            // Corrections des minéraux essentiels - Utiliser les labels EXACTS des énumérations
            "MG",
            "MAGNESIUM" -> "MG" // NutrientMacro.MG a le label "MG"
            "CA", "CALCIUM" -> "CA" // NutrientMacro.CA a le label "CA" (pas "CAL")
            "P", "PHOSPHORE", "PHOSPHORUS" -> "P" // NutrientMacro.P a le label "P" (pas "PHOS")
            "NA", "SODIUM" -> "NA" // NutrientMacro.NA a le label "NA"
            "K", "POTASSIUM" -> "K" // NutrientMacro.K a le label "K"
            "CL", "CHLORE", "CHLORIDE" -> "CL" // NutrientMacro.CL a le label "CL" (pas "CHL")

            // Corrections des oligo-éléments
            "FE",
            "IRON",
            "FER" -> "FE"
            "ZN", "ZINC" -> "ZN"
            "CU", "COPPER", "CUIVRE" -> "CU"
            "MN", "MANGANESE" -> "MN"
            "I", "IODE", "IODINE" -> "I"
            "SE", "SELENIUM" -> "SE"

            // Corrections d'acides aminés - Utiliser les vrais labels de AAEnum
            "METHCYS",
            "METH+CYS",
            "MET+CYS",
            "METHIONINE_CYSTEINE" -> "METHCYS" // NutrientAnalysis.MethCys a le label "METHCYS"
            "PHENTYR", "PHEN+TYR", "PHE+TYR", "PHENYLALANINE_TYROSINE" ->
                    "PHENTYR" // NutrientAnalysis.PhenTyr a le label "PHENTYR"
            "TRYPTOPHAN", "TRYPTOPHANE", "TRY" ->
                    "TRYPTOPHANE" // AAEnum.TRYPTOPHANE a le label "TRYPTOPHANE"
            "LYSINE", "LYS" -> "LYSINE"
            "METHIONINE", "MET" -> "METHIONINE"
            "CYSTEINE", "CYS" -> "CYSTEINE"
            "PHENYLALANINE", "PHE" -> "PHENYLALANINE"
            "TYROSINE", "TYR" -> "TYROSINE"
            "LEUCINE", "LEU" -> "LEUCINE"
            "ISOLEUCINE", "ILE" -> "ISOLEUCINE"
            "VALINE", "VAL" -> "VALINE"
            "THREONINE", "THR" -> "THREONINE"
            "HISTIDINE", "HIS" -> "HISTIDINE"
            "ARGININE", "ARG" -> "ARGININE"

            // Corrections de vitamines
            "VITB1",
            "THIAMINE",
            "THIAMIN" -> "VITB1"
            "VITB2", "RIBOFLAVINE", "RIBOFLAVIN" -> "VITB2"
            "VITB3", "NIACINE", "NIACIN" -> "VITB3"
            "VITB5", "PANTOTHENIC", "PANTOTHENIC_ACID" -> "VITB5"
            "VITB6", "PYRIDOXINE", "PYRIDOXIN" -> "VITB6"
            "VITB9", "FOLATE", "FOLIC_ACID" -> "VITB9"
            "VITB12", "COBALAMIN", "CYANOCOBALAMIN" -> "VITB12" // Correction importante !
            "VITA", "RETINOL", "VITAMIN_A" -> "VITA"
            "VITD", "CHOLECALCIFEROL", "VITAMIN_D" -> "VITD"
            "VITE", "TOCOPHEROL", "VITAMIN_E" -> "VITE"
            "VITK", "PHYLLOQUINONE", "VITAMIN_K" -> "VITK"
            "VITC", "ASCORBIC", "VITAMIN_C" -> "VITC"
            "CHOLINE" -> "CHOLINE"

            // Corrections de ratios et analyses - Utiliser les vrais labels de NutrientAnalysis
            "PCA",
            "P/CA",
            "PHOSPHOCALCIUM",
            "PCa" -> "CAP" // NutrientAnalysis.PCa a le label "CAP" !
            "O6O3", "O6/O3", "OMEGA6OMEGA3" -> "O6O3"
            "ZNCU", "ZN/CU", "ZINCCOPPER", "ZnCu" -> "ZNCU"

            // Corrections d'acides gras
            "AG204",
            "DHA" -> "AG204"
            "AG183", "ALA" -> "AG183"
            "AG205", "EPA" -> "AG205"
            "AG182", "LINOLEIC", "LINOLEIQUE" -> "AG182"
            "O3", "OMEGA3" -> "O3"
            "O6", "OMEGA6" -> "O6"
            "EPADHA", "EPA+DHA" -> "EPADHA"

            // Corrections des macronutriments
            "PROTEINE",
            "PROTEIN" -> "PROTEINE"
            "LIPIDE", "LIPIDES", "FAT" -> "LIPIDE"
            "GLUCIDE", "GLUCIDES", "CARBOHYDRATE" -> "GLUCIDE"
            "FIBRE", "FIBRES", "FIBER" -> "CELLULOSE"
            "CELLULOSE" -> "CELLULOSE"
            "AMIDON", "STARCH" -> "AMIDON"
            "SUCRE", "SUGAR" -> "SUCRE"
            "HUMIDITE", "MOISTURE" -> "HUMIDITE"
            "CENDRE", "ASH" -> "CENDRE"
            "ENA", "NFE" -> "ENA"

            // Garder tel quel pour les autres
            else -> nutrient.uppercase().trim()
        }
    }

    /** Génère des variantes possibles pour un nom de nutriment */
    private fun genererVariantesNutrient(nutrient: String): List<String> {
        val base = nutrient.uppercase().trim()
        val variantes = mutableListOf<String>()

        // Ajouter la forme normalisée en premier
        variantes.add(normaliserNomNutrient(base))

        // Ajouter la forme originale
        variantes.add(base)

        // Variantes spécifiques pour les minéraux problématiques
        when (base) {
            "MG" -> variantes.addAll(listOf("MG", "MAGNESIUM"))
            "CA" -> variantes.addAll(listOf("CA", "CALCIUM"))
            "P" -> variantes.addAll(listOf("P", "PHOSPHORE", "PHOSPHORUS"))
            "NA" -> variantes.addAll(listOf("NA", "SODIUM"))
            "K" -> variantes.addAll(listOf("K", "POTASSIUM"))
            "FE" -> variantes.addAll(listOf("FE", "IRON", "FER"))
            "ZN" -> variantes.addAll(listOf("ZN", "ZINC"))
            "CU" -> variantes.addAll(listOf("CU", "COPPER", "CUIVRE"))
            "MN" -> variantes.addAll(listOf("MN", "MANGANESE"))
            "I" -> variantes.addAll(listOf("I", "IODE", "IODINE"))
            "SE" -> variantes.addAll(listOf("SE", "SELENIUM"))
        }

        // Variantes pour les acides aminés
        when (base) {
            "TRYPTOPHAN", "TRYPTOPHANE" ->
                    variantes.addAll(listOf("TRY", "TRYPTOPHAN", "TRYPTOPHANE"))
            "METHIONINE_CYSTEINE", "METH+CYS" ->
                    variantes.addAll(listOf("METH_CYS", "METHCYS", "MET+CYS"))
            "PHENYLALANINE_TYROSINE", "PHEN+TYR" ->
                    variantes.addAll(listOf("PHEN_TYR", "PHENTYR", "PHE+TYR"))
            "LYSINE" -> variantes.addAll(listOf("LYSINE", "LYS"))
            "METHIONINE" -> variantes.addAll(listOf("METHIONINE", "MET"))
            "LEUCINE" -> variantes.addAll(listOf("LEUCINE", "LEU"))
            "ISOLEUCINE" -> variantes.addAll(listOf("ISOLEUCINE", "ILE"))
            "VALINE" -> variantes.addAll(listOf("VALINE", "VAL"))
            "THREONINE" -> variantes.addAll(listOf("THREONINE", "THR"))
            "HISTIDINE" -> variantes.addAll(listOf("HISTIDINE", "HIS"))
            "ARGININE" -> variantes.addAll(listOf("ARGININE", "ARG"))
            "PHENYLALANINE" -> variantes.addAll(listOf("PHENYLALANINE", "PHE"))
            "TYROSINE" -> variantes.addAll(listOf("TYROSINE", "TYR"))
            "CYSTEINE" -> variantes.addAll(listOf("CYSTEINE", "CYS"))
        }

        // Variantes pour les vitamines
        when (base) {
            "CYANOCOBALAMIN", "COBALAMIN" ->
                    variantes.addAll(listOf("VITB12", "B12", "CYANOCOBALAMIN"))
            "THIAMINE", "THIAMIN" -> variantes.addAll(listOf("VITB1", "B1", "THIAMIN"))
            "RIBOFLAVIN", "RIBOFLAVINE" -> variantes.addAll(listOf("VITB2", "B2", "RIBOFLAVIN"))
            "NIACIN", "NIACINE" -> variantes.addAll(listOf("VITB3", "B3", "NIACIN"))
            "PANTOTHENIC_ACID", "PANTOTHENIC" -> variantes.addAll(listOf("VITB5", "B5"))
            "PYRIDOXIN", "PYRIDOXINE" -> variantes.addAll(listOf("VITB6", "B6", "PYRIDOXIN"))
            "FOLIC_ACID", "FOLATE" -> variantes.addAll(listOf("VITB9", "B9", "FOLIC_ACID"))
            "VITAMIN_A", "RETINOL" -> variantes.addAll(listOf("VITA", "VITAMIN_A"))
            "VITAMIN_D", "CHOLECALCIFEROL" -> variantes.addAll(listOf("VITD", "VITAMIN_D"))
            "VITAMIN_E", "TOCOPHEROL" -> variantes.addAll(listOf("VITE", "VITAMIN_E"))
            "VITAMIN_K", "PHYLLOQUINONE" -> variantes.addAll(listOf("VITK", "VITAMIN_K"))
            "CHOLINE" -> variantes.addAll(listOf("CHOLINE"))
        }

        // Variantes pour les ratios
        when (base) {
            "PCA", "P/CA", "PHOSPHOCALCIUM" -> variantes.addAll(listOf("PCa", "PCA", "P/CA"))
            "ZNCU", "ZN/CU", "ZINCCOPPER" -> variantes.addAll(listOf("ZnCu", "ZNCU", "ZN/CU"))
            "O6O3", "O6/O3", "OMEGA6OMEGA3" -> variantes.addAll(listOf("o6o3", "O6O3", "O6/O3"))
        }

        // Ajouter des variantes avec/sans préfixes pour les vitamines
        if (base.startsWith("VIT")) {
            variantes.add(base.removePrefix("VIT"))
            variantes.add("VITAMIN_" + base.removePrefix("VIT"))
        }

        return variantes.distinct()
    }

    /** Ajoute un nutriment à la référence dans la map appropriée selon le niveau de référence */
    private fun ajouterNutrientALaReference(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            nutrientInfo: NutrientRequirementInfo
    ) {
        try {
            // 🔍 LOG DIAGNOSTIC : Tentative d'ajout du nutriment
            
            
            // Créer la référence bibliographique si elle n'existe pas
            val biblio =
                    nutrientInfo.bibliographicReference ?: fr.vetbrain.vetnutri_mp.Data.BiblioRef()

            // Utiliser la méthode publique pour définir le nutriment
            reference.definirNutriment(
                    valeur = nutrientInfo.quantity,
                    nutrient = nutrientInfo.nutrient,
                    niveauRef = nutrientInfo.referenceLevel,
                    uniteReq = nutrientInfo.unitRequirement,
                    biblio = biblio
            )
            
            // 🔍 LOG DIAGNOSTIC : Vérification après ajout
            val valeurVerifiee = reference.obtenirNutriment(nutrientInfo.nutrient, nutrientInfo.referenceLevel)
            
            
        } catch (e: Exception) {
        }
    }

    /** Traite les coefficients de modification (modk1-5) si présents */
    private fun traiterCoefficientsModification(
            reference: fr.vetbrain.vetnutri_mp.Data.ReferenceEv,
            referenceObj: kotlinx.serialization.json.JsonObject
    ) {
        try {
            // Traiter les noms des coefficients
            reference.nomk1 = extraireStringDepuisJson(referenceObj, "namek1") ?: ""
            reference.nomk2 = extraireStringDepuisJson(referenceObj, "namek2") ?: ""
            reference.nomk3 = extraireStringDepuisJson(referenceObj, "namek3") ?: ""
            reference.nomk4 = extraireStringDepuisJson(referenceObj, "namek4") ?: ""
            reference.nomk5 = extraireStringDepuisJson(referenceObj, "namek5") ?: ""

            // Traiter les tableaux de coefficients
            val coefArrays = listOf("modk1", "modk2", "modk3", "modk4", "modk5")
            val referenceLists =
                    listOf(
                            reference.modk1,
                            reference.modk2,
                            reference.modk3,
                            reference.modk4,
                            reference.modk5
                    )

            coefArrays.forEachIndexed { index, arrayName ->
                val coefArray = referenceObj[arrayName] as? kotlinx.serialization.json.JsonArray
                if (coefArray != null) {
                    val coefList = referenceLists[index]
                    coefArray.forEach { element ->
                        if (element is kotlinx.serialization.json.JsonObject) {
                            val coefP = creerCoefPDepuisJson(element, index)
                            if (coefP != null) {
                                coefList.add(coefP)
                            }
                        }
                    }
                }
            }

            // Déduplication des coefficients par groupe (même nom et même valeur)
            reference.deduplicateCoefficients()
        } catch (e: Exception) {}
    }

    /** Crée un objet CoefP à partir d'un objet JSON */
    private fun creerCoefPDepuisJson(
            coefObj: kotlinx.serialization.json.JsonObject,
            groupUUID: Int
    ): fr.vetbrain.vetnutri_mp.Data.CoefP? {
        try {
            val description =
                    extraireStringDepuisJson(coefObj, "description")
                            ?: extraireStringDepuisJson(coefObj, "name") ?: ""
            val coef =
                    extraireDoubleDepuisJson(coefObj, "coef")
                            ?: extraireDoubleDepuisJson(coefObj, "value") ?: 0.0

            return fr.vetbrain.vetnutri_mp.Data.CoefP(
                    description = description,
                    coef = coef,
                    groupUUID = groupUUID
            )
        } catch (e: Exception) {
            return null
        }
    }

    /** Extrait une chaîne d'un objet JSON */
    private fun extraireStringDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): String? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content
    }

    /** Extrait un booléen d'un objet JSON */
    private fun extraireBooleanDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Boolean? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toBoolean()
    }

    /** Extrait un entier d'un objet JSON */
    private fun extraireIntDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Int? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
    }

    /** Convertit une chaîne en énumération Espece */
    private fun convertirStringVersEspece(species: String): fr.vetbrain.vetnutri_mp.Enumer.Espece {
        return when (species.uppercase()) {
            "CHIEN", "DOG", "CANINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN
            "CHAT", "CAT", "FELINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHAT
            "LAPIN", "RABBIT", "LAPINE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.LAPIN
            "PRIMATE", "PRIMATES", "MONKEY", "APE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.PRIMATE
            "RAT", "RATS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.RAT
            "SOURIS", "MOUSE", "MICE" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.SOURIS
            "FURET", "FERRET", "FURETS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FURET
            "CHEVAL", "HORSE", "EQUINE", "CHEVAUX" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CHEVAL
            "FELIN", "FELINS", "FELINES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FELIN
            "CANIN", "CANINS", "CANINES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CANIN
            "HERBIVORE", "HERBIVORES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.HERBIVORE
            "FOLIVORE", "FOLIVORES" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.FOLIVORE
            "ALL", "CH", "TOUTES", "TOUS" -> fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
            else -> {
                // Essayer d'utiliser la méthode getFromString qui gère de nombreux cas
                val especeDetectee = fr.vetbrain.vetnutri_mp.Enumer.Espece.getFromString(species)
                if (especeDetectee != null) {
                    especeDetectee
                } else {
                    fr.vetbrain.vetnutri_mp.Enumer.Espece.CHIEN
                }
            }
        }
    }

    /**
     * Convertit une chaîne en énumération StadePhysio Inclut une logique intelligente pour détecter
     * le stade selon le contexte
     */
    private fun convertirStringVersStadePhysio(
            sPhysio: String
    ): fr.vetbrain.vetnutri_mp.Enumer.StadePhysio {
        return when (sPhysio.uppercase()) {
            "ADULTE", "ADULT" -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
            "CROISSANCE", "GROWTH" -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.CROISSANCE
            "GESTATION", "GESTANTE", "PREGNANT" ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.GESTATION
            "LACTATION", "LACTANTE", "LACTATING" ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.LACTATION
            else -> {
                fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
            }
        }
    }

    /** Convertit une chaîne en énumération Reflevel */
    private fun convertirStringVersReflevel(
            reflevel: String
    ): fr.vetbrain.vetnutri_mp.Enumer.Reflevel {
        return when (reflevel.uppercase()) {
            "MIN" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
            "MAX" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
            "OPTIMIN" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
            "OPTIMAX" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
            else -> {
                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
            }
        }
    }

    /** Convertit une chaîne en énumération UnitEnum - amélioration pour les unités JSON réelles */
    private fun convertirStringVersUnitEnum(unit: String): fr.vetbrain.vetnutri_mp.Enumer.UnitEnum {
        return when (unit.uppercase()) {
            // Unités de base existantes
            "G",
            "BUG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUg
            "MG", "BUMG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUmg
            "UG", "µG", "BUMU" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUmu
            "IU", "UI", "AUUI" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.AUui
            "DUI", "DUUI" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.DUui
            "KCAL" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.KCAL
            "NO" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUg // Par défaut pour "pas d'unité"
            "PERCENT", "%" -> fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUg
            else -> {
                fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.BUg
            }
        }
    }

    /**
     * Convertit une chaîne en énumération UnitReqEnum - amélioration pour les unités de besoin JSON
     * réelles
     */
    private fun convertirStringVersUnitReqEnum(
            unitReq: String
    ): fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum {
        return when (unitReq.uppercase()) {
            "NO" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.ABSOLUTE
            "KGBW", "PERKG" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
            "KGMW" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
            "MCAL", "PERKCAL" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKCAL
            "DM", "PERMS" -> fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERMS
            else -> {
                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.ABSOLUTE
            }
        }
    }

    /**
     * Détecte le stade physiologique basé sur le nom de la référence si le champ sPhysio n'est pas
     * informatif
     */
    private fun detecterStadePhysioDepuisNom(
            nom: String,
            sPhysio: String
    ): fr.vetbrain.vetnutri_mp.Enumer.StadePhysio {
        val nomLower = nom.lowercase()

        // Si sPhysio est déjà informatif, l'utiliser
        if (sPhysio.uppercase() != "ADULTE") {
            return convertirStringVersStadePhysio(sPhysio)
        }

        // Sinon, détecter basé sur le nom
        return when {
            nomLower.contains("croissance") ||
                    nomLower.contains("growth") ||
                    nomLower.contains("chiot") ||
                    nomLower.contains("chaton") ||
                    nomLower.contains("jeune") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.CROISSANCE
            nomLower.contains("gestation") ||
                    nomLower.contains("gestante") ||
                    nomLower.contains("pregnant") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.GESTATION
            nomLower.contains("lactation") ||
                    nomLower.contains("lactante") ||
                    nomLower.contains("lactating") ||
                    nomLower.contains("allaitement") ->
                    fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.LACTATION
            else -> fr.vetbrain.vetnutri_mp.Enumer.StadePhysio.ADULTE
        }
    }

    /**
     * Détecte si une référence correspond à une maladie basé sur son nom et d'autres indicateurs
     */
    private fun detecterMaladie(
            nom: String,
            disease: Boolean,
            nameDisease: String
    ): Pair<Boolean, String> {
        val nomLower = nom.lowercase()

        // Si disease est explicitement défini et vrai
        if (disease && nameDisease.isNotBlank()) {
            return Pair(true, nameDisease)
        }

        // Si disease est défini mais nameDisease est vide, utiliser le nom comme nom de maladie
        if (disease) {
            return Pair(true, nom)
        }

        // Détection basée sur le nom
        val indicateursMaladie =
                listOf(
                        "cardio",
                        "cardiac",
                        "heart",
                        "cardiaque",
                        "renal",
                        "kidney",
                        "rénal",
                        "rein",
                        "hepatic",
                        "liver",
                        "hépatique",
                        "foie",
                        "diabete",
                        "diabetes",
                        "diabétique",
                        "obese",
                        "obesity",
                        "obésité",
                        "cancer",
                        "oncol",
                        "tumeur",
                        "allergi",
                        "allergy",
                        "allergique",
                        "gastro",
                        "digestif",
                        "intestinal",
                        "arthro",
                        "joint",
                        "articulaire"
                )

        val estMaladie = indicateursMaladie.any { nomLower.contains(it) }
        val nomMaladie = if (estMaladie) nom else ""

        return Pair(estMaladie, nomMaladie)
    }

    /** Vérifie si le contenu correspond à un fichier de références nutritionnelles (.vbnr.json) */
    internal fun isNutritionalRequirementJsonContent(content: String): Boolean {
        return try {
            val jsonElement = ImportCommun.json.parseToJsonElement(content)

            // Vérifier si c'est un tableau d'objets avec les clés requises
            if (jsonElement is kotlinx.serialization.json.JsonArray && jsonElement.isNotEmpty()) {
                val firstElement = jsonElement.first()
                if (firstElement is kotlinx.serialization.json.JsonObject) {
                    val requiredKeys =
                            listOf(
                                    "reference",
                                    "allEquations",
                                    "allBibliographicReferences",
                                    "nutrientRequirements"
                            )
                    val hasAllRequiredKeys =
                            requiredKeys.all { key -> firstElement.containsKey(key) }

                    if (hasAllRequiredKeys) {
                        
                        return true
                    }

                    // Fallback: vérifier au moins 3 des 4 clés requises
                    val keyCount = requiredKeys.count { key -> firstElement.containsKey(key) }
                    if (keyCount >= 3) {
                        return true
                    }
                }
            } else if (jsonElement is kotlinx.serialization.json.JsonObject) {
                // Vérifier si c'est un objet unique avec la structure NutritionalRequirementData
                val requiredKeys =
                        listOf(
                                "reference",
                                "allEquations",
                                "allBibliographicReferences",
                                "nutrientRequirements"
                        )
                val hasAllRequiredKeys = requiredKeys.all { key -> jsonElement.containsKey(key) }

                if (hasAllRequiredKeys) {
                    
                    return true
                }

                // Fallback: vérifier au moins 3 des 4 clés requises
                val keyCount = requiredKeys.count { key -> jsonElement.containsKey(key) }
                if (keyCount >= 3) {
                    return true
                }
            }

            // Fallback final: chercher des mots-clés caractéristiques dans le contenu
            val contentLower = content.lowercase()
            val nutritionalKeywords =
                    listOf(
                            "reference",
                            "nutrient",
                            "equation",
                            "bibliographic",
                            "species",
                            "sphysio",
                            "disease",
                            "allequations",
                            "nutritionalrequirement",
                            "reflevel"
                    )
            val keywordCount = nutritionalKeywords.count { contentLower.contains(it) }

            if (keywordCount >= 4) {
                return true
            }

            return false
        } catch (e: Exception) {
            return false
        }
    }

    /** Extrait un double d'un objet JSON */
    private fun extraireDoubleDepuisJson(
            obj: kotlinx.serialization.json.JsonObject,
            key: String
    ): Double? {
        return (obj[key] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toDoubleOrNull()
    }

}
