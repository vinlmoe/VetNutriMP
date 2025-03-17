package fr.vetbrain.vetnutri_mp.Enumer

// DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
// Description: Classe critique pour la résolution des nutriments à partir de leurs labels.
// Cette classe est utilisée intensivement dans le processus d'import pour associer
// les valeurs nutritionnelles aux bonnes énumérations de nutriments.
/** Classe utilitaire permettant de résoudre les nutriments à partir de leur label. */
object NutrientResolver {

    /**
     * Résout un nutriment à partir de son label. Cette fonction cherche dans toutes les classes
     * d'énumération de nutriments pour trouver celle qui correspond au label donné.
     *
     * @param label Le label du nutriment à rechercher
     * @return Le nutriment correspondant au label, ou null si aucun nutriment ne correspond
     */
    fun AllNutrientResolver(label: String): Nutrient? {
        // Nettoyage plus approfondi du label
        val cleanedLabel = normalizeLabel(label)

        println("Résolution du nutriment: original='$label', normalisé='$cleanedLabel'")

        // Traitement de cas spéciaux pour éviter les confusions connues
        when (cleanedLabel) {
            "CARNITINE" -> {
                val nutrient = NutrientOther.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientOther: ${nutrient?.label}")
                return nutrient
            }
            "FE" -> {
                val nutrient = NutrientMin.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientMin: ${nutrient?.label}")
                return nutrient
            }
            "VITB9" -> {
                val nutrient = NutrientVitam.getByLabel(cleanedLabel)
                println("  → Résolu comme NutrientVitam: ${nutrient?.label}")
                return nutrient
            }
            "FIBRETOT" -> {
                // Essayer d'abord avec le terme exact
                val nutrientExact =
                        NutrientMain.entries.find {
                            it.label.equals(cleanedLabel, ignoreCase = true)
                        }
                if (nutrientExact != null) {
                    println("  → Résolu comme NutrientMain: ${nutrientExact.label}")
                    return nutrientExact
                }

                // Si échec, essayer avec FIBRTOT (sans le E)
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("FIBRTOT", ignoreCase = true) }
                println(
                        "  → Résolu comme NutrientMain: ${nutrient?.label} (corrigé depuis FIBRETOT)"
                )
                return nutrient
            }
            "FIBRESOL" -> {
                // Essayer d'abord avec le terme exact
                val nutrientExact =
                        NutrientMain.entries.find {
                            it.label.equals(cleanedLabel, ignoreCase = true)
                        }
                if (nutrientExact != null) {
                    println("  → Résolu comme NutrientMain: ${nutrientExact.label}")
                    return nutrientExact
                }

                // Si échec, essayer avec FIBRSOL (sans le E)
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("FIBRSOL", ignoreCase = true) }
                println(
                        "  → Résolu comme NutrientMain: ${nutrient?.label} (corrigé depuis FIBRESOL)"
                )
                return nutrient
            }
            "HUMIDITE" -> {
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("humidité", ignoreCase = true) }
                println("  → Cas spécial: HUMIDITE résolu comme ${nutrient?.label}")
                return nutrient
            }
            "PROTEINE" -> {
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("protéine", ignoreCase = true) }
                println("  → Cas spécial: PROTEINE résolu comme ${nutrient?.label}")
                return nutrient
            }
            "CHOL" -> {
                // Essayer d'abord comme cholestérol
                val nutrient =
                        NutrientLipid.entries.find { it.label.equals("CHOLES", ignoreCase = true) }
                if (nutrient != null) {
                    println("  → Cas spécial: CHOL résolu comme NutrientLipid.${nutrient.label}")
                    return nutrient
                }
                // Ensuite essayer comme chlore
                val nutrientCHL =
                        NutrientMacro.entries.find { it.label.equals("CHL", ignoreCase = true) }
                if (nutrientCHL != null) {
                    println("  → Cas spécial: CHOL résolu comme NutrientMacro.${nutrientCHL.label}")
                    return nutrientCHL
                }
            }
            // Nouveaux cas spéciaux pour gérer des nutriments fréquemment mal résolus
            "CHL" -> {
                val nutrient =
                        NutrientMin.entries.find { it.label.equals("CHL", ignoreCase = true) }
                println("  → Cas spécial: CHL résolu comme ${nutrient?.label}")
                return nutrient
            }
            "LIPIDE" -> {
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("LIPIDE", ignoreCase = true) }
                println("  → Cas spécial: LIPIDE résolu comme ${nutrient?.label}")
                return nutrient
            }
            "ENA" -> {
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("ENA", ignoreCase = true) }
                println("  → Cas spécial: ENA résolu comme ${nutrient?.label}")
                return nutrient
            }
            "EPADHA" -> {
                val nutrient =
                        NutrientLipid.entries.find { it.label.equals("EPADHA", ignoreCase = true) }
                println("  → Cas spécial: EPADHA résolu comme ${nutrient?.label}")
                return nutrient
            }
            "AG180", "AG182", "AG183", "AG204", "AG205", "AG226" -> {
                // Acides gras comme nutriments lipidiques
                val nutrient =
                        NutrientLipid.entries.find {
                            it.label.equals(cleanedLabel, ignoreCase = true)
                        }
                println("  → Cas spécial: $cleanedLabel résolu comme ${nutrient?.label}")
                return nutrient
            }
            "TAURINE" -> {
                val nutrient =
                        NutrientOther.entries.find { it.label.equals("TAURINE", ignoreCase = true) }
                println("  → Cas spécial: TAURINE résolu comme ${nutrient?.label}")
                return nutrient
            }
            "CYSTEINE",
            "METHIONINE",
            "LYSINE",
            "HISTIDINE",
            "ARGININE",
            "TYROSINE",
            "VALINE",
            "LEUCINE",
            "ISOLEUCINE",
            "TRYPTOPHANE" -> {
                // Acides aminés
                val nutrient =
                        AAEnum.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
                println(
                        "  → Cas spécial: $cleanedLabel résolu comme acide aminé: ${nutrient?.label}"
                )
                return nutrient
            }
        }

        // Approche plus robuste avec une recherche insensible à la casse

        // Vérifier dans NutrientMain (insensible à la casse)
        val nutrientMain =
                NutrientMain.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientMain != null) {
            println("  → Résolu comme NutrientMain: ${nutrientMain.label}")
            return nutrientMain
        }

        // Vérifier dans NutrientMacro (insensible à la casse)
        val nutrientMacro =
                NutrientMacro.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientMacro != null) {
            println("  → Résolu comme NutrientMacro: ${nutrientMacro.label}")
            return nutrientMacro
        }

        // Vérifier dans NutrientMin (insensible à la casse)
        val nutrientMin =
                NutrientMin.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientMin != null) {
            println("  → Résolu comme NutrientMin: ${nutrientMin.label}")
            return nutrientMin
        }

        // Vérifier dans NutrientLipid (insensible à la casse)
        val nutrientLipid =
                NutrientLipid.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientLipid != null) {
            println("  → Résolu comme NutrientLipid: ${nutrientLipid.label}")
            return nutrientLipid
        }

        // Vérifier dans NutrientVitam (insensible à la casse + recherche dans altLabels)
        val nutrientVitam =
                NutrientVitam.entries.find {
                    it.label.equals(cleanedLabel, ignoreCase = true) ||
                            it.altLabels.any { altLabel ->
                                altLabel.equals(cleanedLabel, ignoreCase = true)
                            }
                }
        if (nutrientVitam != null) {
            println("  → Résolu comme NutrientVitam: ${nutrientVitam.label}")
            return nutrientVitam
        }

        // Vérifier dans NutrientOther (insensible à la casse)
        val nutrientOther =
                NutrientOther.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientOther != null) {
            println("  → Résolu comme NutrientOther: ${nutrientOther.label}")
            return nutrientOther
        }

        // Vérifier dans AAEnum (insensible à la casse)
        val aaEnum = AAEnum.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (aaEnum != null) {
            println("  → Résolu comme AAEnum: ${aaEnum.label}")
            return aaEnum
        }

        // Vérifier dans NutrientEnergy (insensible à la casse)
        val nutrientEnergy =
                NutrientEnergy.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientEnergy != null) {
            println("  → Résolu comme NutrientEnergy: ${nutrientEnergy.label}")
            return nutrientEnergy
        }

        // Vérifier dans NutrientAnalysis (insensible à la casse)
        val nutrientAnalysis =
                NutrientAnalysis.entries.find { it.label.equals(cleanedLabel, ignoreCase = true) }
        if (nutrientAnalysis != null) {
            println("  → Résolu comme NutrientAnalysis: ${nutrientAnalysis.label}")
            return nutrientAnalysis
        }

        // Si aucun nutriment n'a été trouvé avec une correspondance exacte, essayer une recherche
        // plus flexible
        println(
                "  × Aucune correspondance exacte trouvée pour '$cleanedLabel', essai de correspondance partielle..."
        )

        // Essayer de trouver une correspondance avec une distance de Levenshtein (similarité de
        // chaîne)
        val fuzzyMatch = findBestFuzzyMatch(cleanedLabel)
        if (fuzzyMatch != null) {
            println("  → Résolu par correspondance approximative: ${fuzzyMatch.label}")
            return fuzzyMatch
        }

        // Rechercher un nutriment dont le label contient le terme recherché
        val allEntries =
                (NutrientMain.entries.asSequence() +
                        NutrientMacro.entries.asSequence() +
                        NutrientMin.entries.asSequence() +
                        NutrientLipid.entries.asSequence() +
                        NutrientVitam.entries.asSequence() +
                        NutrientOther.entries.asSequence() +
                        AAEnum.entries.asSequence() +
                        NutrientEnergy.entries.asSequence() +
                        NutrientAnalysis.entries.asSequence())

        val matchingNutrient =
                allEntries.find {
                    it.label.contains(cleanedLabel, ignoreCase = true) ||
                            cleanedLabel.contains(it.label, ignoreCase = true)
                }

        if (matchingNutrient != null) {
            println("  → Résolu par correspondance partielle: ${matchingNutrient.label}")
            return matchingNutrient
        }

        // Aucun nutriment trouvé
        println("  × Non résolu. Labels disponibles dans les énumérations:")
        println("    - NutrientMain: ${NutrientMain.entries.map { it.label }}")
        println("    - NutrientVitam: ${NutrientVitam.entries.map { it.label }}")
        println("    - NutrientMacro: ${NutrientMacro.entries.map { it.label }}")
        println("    - NutrientMin: ${NutrientMin.entries.map { it.label }}")
        println("    - NutrientLipid: ${NutrientLipid.entries.map { it.label }}")
        println("    - NutrientOther: ${NutrientOther.entries.map { it.label }}")

        return null
    }

    /**
     * Normalise le label d'un nutriment pour faciliter sa résolution. Supprime les caractères
     * spéciaux, met en majuscules, et effectue d'autres normalisations pour faciliter la
     * correspondance avec les labels d'énumération.
     *
     * @param label Le label à normaliser
     * @return Le label normalisé
     */
    fun normalizeLabel(label: String): String {
        val trimmed = label.trim().replace("[", "").replace("]", "").replace("\"", "")

        // Gérer certains cas particuliers connus
        return when (trimmed.uppercase()) {
            // Nutriments principaux
            "HUMIDITE",
            "HUMIDITÉ",
            "WATER",
            "H2O",
            "AGUA",
            "EAU",
            "MOISTURE" -> "HUMIDITE"
            "PROTEINE",
            "PROTÉINE",
            "PROTEINES",
            "PROTÉINES",
            "PROTEIN",
            "PROTEINS",
            "MAT",
            "PROT",
            "CP" -> "PROTEINE"
            "LIPIDE", "LIPIDES", "FAT", "FATS", "MG", "EE", "MATIERE_GRASSE", "MATIÈRES_GRASSES" ->
                    "LIPIDE"
            "GLUCIDE", "GLUCIDES", "CARBOHYDRATE", "CARBOHYDRATES", "CARBS", "CHO" -> "GLUCIDE"
            "CENDRE", "CENDRES", "ASH", "ASHES", "MM", "MINERALS" -> "CENDRE"
            "ENERGIE", "ÉNERGIE", "ENERGY", "CALORIES", "EB", "ED", "EM", "ME", "DE", "GE" ->
                    "ENERGIE"
            "FIBRE", "FIBRES", "FIBER", "FIBERS", "FB", "TDF", "FIBRA" -> "FIBRE"
            "CELLULOSE", "CELLULOSES", "CRUDE_FIBER", "FIBRE_BRUTE", "FIBRA_BRUTA" -> "CELLULOSE"
            "AMIDON", "STARCH", "STARCHES", "ALMIDON" -> "AMIDON"
            "SUCRE", "SUCRES", "SUGAR", "SUGARS", "AZUCAR", "AZUCARES" -> "SUCRE"
            "ENA", "NFE", "EXTRACTIF_NON_AZOTE", "NITROGEN_FREE_EXTRACT", "CARBOHIDRATOS" -> "ENA"
            "FIBRETOT", "FIBRE_TOTALE", "TOTAL_FIBER", "DIETARY_FIBER", "FIBRETOTALE" -> "FIBRETOT"
            "FIBRSOL", "FIBRE_SOLUBLE", "SOLUBLE_FIBER", "FIBRESOLUBLE" -> "FIBRSOL"
            "FIBRINSO", "FIBRE_INSOLUBLE", "INSOLUBLE_FIBER", "FIBREINSOLUBLE" -> "FIBRINSO"

            // Vitamines
            "VITAMINEA",
            "VITAMINA",
            "VIT_A",
            "VITA",
            "VITAMIN_A",
            "VITAMINAA",
            "RETINOL",
            "RETINOLA",
            "VITAMIN A" -> "VITA"
            "VITAMINEC",
            "VITAMINC",
            "VIT_C",
            "VITC",
            "VITAMIN_C",
            "VITAMINEC",
            "ASCORBIC_ACID",
            "AC_ASCORBIQUE",
            "VITAMIN C" -> "VITC"
            "VITAMINED",
            "VITAMIND",
            "VIT_D",
            "VITD",
            "VITAMIN_D",
            "VITAMINED",
            "CALCIFEROL",
            "VITAMIN D",
            "CHOLECALCIFEROL" -> "VITD"
            "VITAMINEE",
            "VITAMINE",
            "VIT_E",
            "VITE",
            "VITAMIN_E",
            "VITAMINEE",
            "TOCOPHEROL",
            "VITAMIN E",
            "TOCOFEROL" -> "VITE"
            "VITAMINEK",
            "VITAMINK",
            "VIT_K",
            "VITK",
            "VITAMIN_K",
            "VITAMINEK",
            "PHYLLOQUINONE",
            "VITAMIN K" -> "VITK"
            "VITAMINEB1",
            "VITAMINB1",
            "VIT_B1",
            "VITB1",
            "VITAMIN_B1",
            "VITAMINEB1",
            "THIAMINE",
            "VITAMIN B1" -> "VITB1"
            "VITAMINEB2",
            "VITAMINB2",
            "VIT_B2",
            "VITB2",
            "VITAMIN_B2",
            "VITAMINEB2",
            "RIBOFLAVINE",
            "RIBOFLAVIN",
            "VITAMIN B2" -> "VITB2"
            "VITAMINEB3",
            "VITAMINB3",
            "VIT_B3",
            "VITB3",
            "VITAMIN_B3",
            "VITAMINEB3",
            "NIACINE",
            "NIACIN",
            "PP",
            "VITAMIN B3" -> "VITB3"
            "VITAMINEB5",
            "VITAMINB5",
            "VIT_B5",
            "VITB5",
            "VITAMIN_B5",
            "VITAMINEB5",
            "PANTOTHENIQUE",
            "PANTOTHENIC",
            "PANTOTHENATE",
            "PANTOTHENIC_ACID",
            "ACIDE_PANTOTHENIQUE",
            "VITAMIN B5" -> "VITB5"
            "VITAMINEB6",
            "VITAMINB6",
            "VIT_B6",
            "VITB6",
            "VITAMIN_B6",
            "VITAMINEB6",
            "PYRIDOXINE",
            "PYRIDOXAL",
            "VITAMIN B6" -> "VITB6"
            "VITAMINEB8",
            "VITAMINB8",
            "VIT_B8",
            "VITB8",
            "VITAMIN_B8",
            "VITAMINEB8",
            "BIOTINE",
            "BIOTIN",
            "VITAMIN B8",
            "VITAMIN H" -> "VITB8"
            "VITAMINEB9",
            "VITAMINB9",
            "VIT_B9",
            "VITB9",
            "VITAMIN_B9",
            "VITAMINEB9",
            "FOLATE",
            "FOLIC",
            "ACIDE_FOLIQUE",
            "FOLIC_ACID",
            "VITAMIN B9" -> "VITB9"
            "VITAMINEB12",
            "VITAMINB12",
            "VIT_B12",
            "VITB12",
            "VITAMIN_B12",
            "VITAMINEB12",
            "COBALAMINE",
            "COBALAMIN",
            "VITAMIN B12" -> "VITB12"

            // Minéraux
            "CA",
            "CALCIUM",
            "CALCIO",
            "CAL" -> "CA"
            "P", "PHOSPHORE", "PHOSPHORUS", "FOSFORO", "PHOS" -> "P"
            "NA", "SODIUM", "SODIO", "NATRIUM" -> "NA"
            "K", "POTASSIUM", "POTASIO", "KALIUM" -> "K"
            "CL", "CHLORURE", "CHLORIDE", "CHLORE", "CHLORO", "CHL" -> "CHL"
            "MG", "MAGNESIUM", "MAGNESIO", "MAGNÉSIUM" -> "MG"
            "FE", "FER", "IRON", "HIERRO", "FERRUM" -> "FE"
            "ZN", "ZINC", "ZINK", "ZINCUM" -> "ZN"
            "CU", "CUIVRE", "COPPER", "COBRE", "CUPRUM" -> "CU"
            "MN", "MANGANESE", "MANGANESO", "MANGANUM", "MANGANÈSE" -> "MN"
            "I", "IODE", "IODINE", "YODO", "IODUM" -> "I"
            "SE", "SELENIUM", "SELENIO", "SELEN" -> "SE"

            // Lipides
            "CHOLES",
            "CHOLESTEROL",
            "CHOLESTÉROL",
            "CHOLEST",
            "CHOL" -> "CHOLES"
            "OMEGA3",
            "OMEGA_3",
            "OMEGA-3",
            "N3",
            "O3",
            "OM3",
            "OMEGA_TROIS",
            "OMEGA_3_TOTAL",
            "N-3",
            "OMEGA 3" -> "O3"
            "OMEGA6",
            "OMEGA_6",
            "OMEGA-6",
            "N6",
            "O6",
            "OM6",
            "OMEGA_SIX",
            "OMEGA_6_TOTAL",
            "N-6",
            "OMEGA 6" -> "O6"
            "EPA", "EICOSAPENTAENOIC", "EICOSAPENTAENOIQUE", "C20_5", "C20:5" -> "EPA"
            "DHA", "DOCOSAHEXAENOIC", "DOCOSAHEXAENOIQUE", "C22_6", "C22:6" -> "DHA"
            "EPADHA", "EPA_DHA", "EPA+DHA", "EPA_PLUS_DHA", "EPA_ET_DHA" -> "EPADHA"
            "AG180", "C18_0", "C18:0", "STEARIC", "STEARIQUE" -> "AG180"
            "AG181", "C18_1", "C18:1", "OLEIC", "OLEIQUE" -> "AG181"
            "AG182", "C18_2", "C18:2", "LINOLEIC", "LINOLEIQUE" -> "AG182"
            "AG183", "C18_3", "C18:3", "LINOLENIC", "LINOLENIQUE", "ALA" -> "AG183"
            "AG204", "C20_4", "C20:4", "ARACHIDONIC", "ARACHIDONIQUE", "AA" -> "AG204"
            "AG205", "C20_5", "C20:5", "EICOSAPENTAENOIC", "EICOSAPENTAENOIQUE" -> "AG205"
            "AG226", "C22_6", "C22:6", "DOCOSAHEXAENOIC", "DOCOSAHEXAENOIQUE" -> "AG226"
            "AGS",
            "SATURES",
            "SATURATED",
            "ACIDE_GRAS_SATURES",
            "SATURATED_FAT",
            "SFA",
            "ACIDES_GRAS_SATURES" -> "AGS"
            "AGMI",
            "MONO_INSATURES",
            "MONOUNSATURATED",
            "ACIDE_GRAS_MONO_INSATURES",
            "MUFA",
            "ACIDES_GRAS_MONO_INSATURES" -> "AGMI"
            "AGPI",
            "POLY_INSATURES",
            "POLYUNSATURATED",
            "ACIDE_GRAS_POLY_INSATURES",
            "PUFA",
            "ACIDES_GRAS_POLY_INSATURES" -> "AGPI"

            // Acides aminés
            "ALANINE",
            "ALA",
            "A" -> "ALANINE"
            "ARGININE", "ARG", "R" -> "ARGININE"
            "ASPARTIC_ACID", "ASPARTATE", "ASP", "D", "ACIDE_ASPARTIQUE" -> "ASPARTIC_ACID"
            "CYSTEINE", "CYS", "C" -> "CYSTEINE"
            "GLUTAMIC_ACID", "GLUTAMATE", "GLU", "E", "ACIDE_GLUTAMIQUE" -> "GLUTAMIC_ACID"
            "GLYCINE", "GLY", "G" -> "GLYCINE"
            "HISTIDINE", "HIS", "H" -> "HISTIDINE"
            "ISOLEUCINE", "ILE", "I" -> "ISOLEUCINE"
            "LEUCINE", "LEU", "L" -> "LEUCINE"
            "LYSINE", "LYS", "K" -> "LYSINE"
            "METHIONINE", "MET", "M" -> "METHIONINE"
            "PHENYLALANINE", "PHE", "F", "PHÉNYLALANINE" -> "PHENYLALANINE"
            "PROLINE", "PRO", "P" -> "PROLINE"
            "SERINE", "SER", "S", "SÉRINE" -> "SERINE"
            "THREONINE", "THR", "T", "THRÉONINE" -> "THREONINE"
            "TRYPTOPHAN", "TRP", "W", "TRYPTOPHANE" -> "TRYPTOPHAN"
            "TYROSINE", "TYR", "Y" -> "TYROSINE"
            "VALINE", "VAL", "V" -> "VALINE"

            // Autres
            "CARNITINE",
            "CARNITIN",
            "L-CARNITINE",
            "L_CARNITINE" -> "CARNITINE"
            "TAURINE", "TAURIN", "TAUR" -> "TAURINE"
            "CHOLINE", "CHOLIN", "CHOL" -> "CHOLINE"
            "BETACAR", "BETA_CAROTENE", "BETA-CAROTENE", "CAROTENE", "BETA_CAROTÈNE" -> "BETACAR"

            // Si aucun cas spécial ne correspond, on applique des règles générales de normalisation
            else -> {
                // Nettoyer davantage le label et le mettre en majuscules pour une comparaison plus
                // robuste
                trimmed.replace(Regex("[^A-Za-z0-9]"), "").uppercase()
            }
        }
    }

    /**
     * Trouve la meilleure correspondance approximative pour un label de nutriment en utilisant la
     * distance de Levenshtein
     *
     * @param label Le label à rechercher
     * @return Le nutriment correspondant le mieux, ou null si aucune correspondance n'est assez
     * proche
     */
    private fun findBestFuzzyMatch(label: String): Nutrient? {
        val allEntries =
                (NutrientMain.entries.asSequence() +
                        NutrientMacro.entries.asSequence() +
                        NutrientMin.entries.asSequence() +
                        NutrientLipid.entries.asSequence() +
                        NutrientVitam.entries.asSequence() +
                        NutrientOther.entries.asSequence() +
                        AAEnum.entries.asSequence() +
                        NutrientEnergy.entries.asSequence() +
                        NutrientAnalysis.entries.asSequence())

        // Calculer la similarité pour chaque nutriment
        val candidates =
                allEntries.map { nutrient ->
                    // Calculer la distance de Levenshtein normalisée (0-1)
                    val similarity =
                            calculateSimilarity(label.uppercase(), nutrient.label.uppercase())
                    Pair(nutrient, similarity)
                }

        // Trouver la meilleure correspondance avec une similarité suffisante
        val bestMatch = candidates.maxByOrNull { it.second }

        // On ne retourne une correspondance que si la similarité est suffisamment élevée
        // (seuil arbitraire de 0.7)
        return if (bestMatch != null && bestMatch.second >= 0.7) {
            println(
                    "  → Correspondance approximative trouvée pour '$label': ${bestMatch.first.label} avec similarité ${bestMatch.second}"
            )
            bestMatch.first
        } else {
            null
        }
    }

    /**
     * Calcule la similarité entre deux chaînes basée sur la distance de Levenshtein
     *
     * @param s1 Première chaîne
     * @param s2 Deuxième chaîne
     * @return Un score de similarité entre 0 (totalement différent) et 1 (identique)
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1

        // Si l'une des chaînes est vide, la distance est la longueur de l'autre chaîne
        if (longer.isEmpty()) return 1.0
        if (shorter.isEmpty()) return 0.0

        // Calcul de la distance de Levenshtein
        val distance = levenshteinDistance(longer, shorter)

        // Normaliser la distance en similarité (0-1)
        return 1.0 - (distance.toDouble() / longer.length)
    }

    /**
     * Calcule la distance de Levenshtein entre deux chaînes
     *
     * @param s1 Première chaîne
     * @param s2 Deuxième chaîne
     * @return La distance de Levenshtein
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1)

        for (i in 0..s2.length) {
            costs[i] = i
        }

        for (i in 1..s1.length) {
            var lastValue = i
            for (j in 1..s2.length) {
                val oldValue = costs[j]
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                costs[j] = minOf(costs[j] + 1, costs[j - 1] + 1, lastValue + cost)
                lastValue = oldValue
            }
        }

        return costs[s2.length]
    }

    /**
     * Vérifie si un label normalisé correspond à un nutriment connu
     *
     * @param normalizedLabel Le label normalisé à vérifier
     * @return true si le label correspond à un nutriment connu, false sinon
     */
    private fun isKnownNutrient(normalizedLabel: String): Boolean {
        // Vérifier dans toutes les énumérations de nutriments
        return NutrientMain.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientMacro.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientMin.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientLipid.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientVitam.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientOther.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                AAEnum.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) } ||
                NutrientEnergy.entries.any {
                    it.label.equals(normalizedLabel, ignoreCase = true)
                } ||
                NutrientAnalysis.entries.any { it.label.equals(normalizedLabel, ignoreCase = true) }
    }
}
// FIN ZONE PROTÉGÉE
