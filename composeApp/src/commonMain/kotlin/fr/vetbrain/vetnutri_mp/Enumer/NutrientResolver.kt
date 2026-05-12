package fr.vetbrain.vetnutri_mp.Enumer

// DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
// Description: Classe critique pour la résolution des nutriments à partir de leurs labels.
// Cette classe est utilisée intensivement dans le processus d'import pour associer
// les valeurs nutritionnelles aux bonnes énumérations de nutriments.
/** Classe utilitaire permettant de résoudre les nutriments à partir de leur label. */
object NutrientResolver {

    // Lookup map O(1) construit une seule fois depuis toutes les énumérations
    private val labelToNutrient: Map<String, Nutrient> by lazy {
        buildMap {
            NutrientMain.entries.forEach { put(it.label.uppercase(), it) }
            NutrientMacro.entries.forEach { put(it.label.uppercase(), it) }
            NutrientMin.entries.forEach { put(it.label.uppercase(), it) }
            NutrientLipid.entries.forEach { put(it.label.uppercase(), it) }
            NutrientVitam.entries.forEach { n ->
                put(n.label.uppercase(), n)
                n.altLabels.forEach { alt -> 
                    val key = alt.uppercase()
                    if (!containsKey(key)) {
                        put(key, n)
                    }
                }
            }
            NutrientOther.entries.forEach { put(it.label.uppercase(), it) }
            AAEnum.entries.forEach { put(it.label.uppercase(), it) }
            NutrientEnergy.entries.forEach { put(it.label.uppercase(), it) }
            NutrientAnalysis.entries.forEach { put(it.label.uppercase(), it) }
            CustomNutrientRegistry.all().forEach { put(it.label.uppercase(), it) }
        }
    }

    // Cache des résultats de résolution (normalizedLabel → Nutrient?) pour éviter les répétitions
    private val resolvedCache = HashMap<String, Nutrient?>(512)

    /**
     * Résout un nutriment à partir de son label. Cette fonction cherche dans toutes les classes
     * d'énumération de nutriments pour trouver celle qui correspond au label donné.
     *
     * @param label Le label du nutriment à rechercher
     * @return Le nutriment correspondant au label, ou null si aucun nutriment ne correspond
     */
    fun AllNutrientResolver(label: String): Nutrient? {
        val cleanedLabel = normalizeLabel(label)

        // Cache hit — évite tout recalcul pour les labels déjà résolus
        resolvedCache[cleanedLabel]?.let { return it }
        if (resolvedCache.containsKey(cleanedLabel)) return null

        val result = resolveNutrient(cleanedLabel)
        resolvedCache[cleanedLabel] = result
        return result
    }

    /**
     * Résolution stricte pour les labels déjà persistés en base.
     * Évite volontairement le fuzzy matching pour ne pas remapper un label custom
     * vers un nutriment standard (ex: BENAZPRIN -> K).
     */
    fun resolveStoredLabel(label: String): Nutrient {
        val raw = label.trim().replace("[", "").replace("]", "").replace("\"", "")
        val normalized = normalizeLabel(raw)

        // 0) prioriser le registre live des nutriments personnalisés
        // pour éviter d'utiliser une entrée obsolète du cache interne.
        CustomNutrientRegistry.getByLabel(raw)?.let { return it }
        CustomNutrientRegistry.getByLabel(normalized)?.let { return it }

        // 1) match exact sur le label stocké (insensible casse)
        labelToNutrient[raw.uppercase()]?.let { return it }

        // 2) match exact sur la forme normalisée
        labelToNutrient[normalized.uppercase()]?.let { return it }

        // 3) fallback custom exact
        val custom = CustomNutrientRegistry.getByLabel(raw) ?: CustomNutrient.fromLabel(raw)
        return CustomNutrientRegistry.register(custom)
    }

    private fun resolveNutrient(cleanedLabel: String): Nutrient? {
        // Lookup O(1) dans la map pré-construite
        labelToNutrient[cleanedLabel.uppercase()]?.let { return it }

        // Traitement de cas spéciaux pour éviter les confusions connues ou gérer des alias
        when (cleanedLabel) {
            "CHOL" -> {
                // Essayer d'abord comme cholestérol
                val nutrient =
                        NutrientLipid.entries.find { it.label.equals("CHOLES", ignoreCase = true) }
                if (nutrient != null) {
                    return nutrient
                }
                // Ensuite essayer comme chlore
                val nutrientCHL =
                        NutrientMin.entries.find { it.label.equals("CHL", ignoreCase = true) }
                if (nutrientCHL != null) {
                    return nutrientCHL
                }
            }
            "FIBRETOT" -> {
                // Essayer d'abord avec le terme exact
                val nutrientExact =
                        NutrientMain.entries.find {
                            it.label.equals(cleanedLabel, ignoreCase = true)
                        }
                if (nutrientExact != null) {
                    return nutrientExact
                }

                // Si échec, essayer avec FIBRTOT (sans le E)
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("FIBRTOT", ignoreCase = true) }
                return nutrient
            }
            "FIBRESOL" -> {
                // Essayer d'abord avec le terme exact
                val nutrientExact =
                        NutrientMain.entries.find {
                            it.label.equals(cleanedLabel, ignoreCase = true)
                        }
                if (nutrientExact != null) {
                    return nutrientExact
                }

                // Si échec, essayer avec FIBRSOL (sans le E)
                val nutrient =
                        NutrientMain.entries.find { it.label.equals("FIBRSOL", ignoreCase = true) }
                return nutrient
            }
        }

        // Fallback : recherche floue si le lookup exact a échoué

        // Essayer de trouver une correspondance avec une distance de Levenshtein (similarité de
        // chaîne)
        val fuzzyMatch = findBestFuzzyMatch(cleanedLabel)
        if (fuzzyMatch != null) {
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
            return matchingNutrient
        }

        // Fallback final: créer/réutiliser un nutriment personnalisé
        val custom = CustomNutrientRegistry.getByLabel(cleanedLabel) ?: CustomNutrient.fromLabel(cleanedLabel)
        return CustomNutrientRegistry.register(custom)
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
            "LIPIDE", "LIPIDES", "FAT", "FATS", "EE", "MATIERE_GRASSE", "MATIÈRES_GRASSES" ->
                    "LIPIDE"
            "GLUCIDE", "GLUCIDES", "CARBOHYDRATE", "CARBOHYDRATES", "CARBS", "CHO" -> "GLUCIDE"
            "CENDRE", "CENDRES", "ASH", "ASHES", "MM", "MINERALS" -> "CENDRE"
            "ENERGIE", "ÉNERGIE", "ENERGY", "CALORIES", "EB", "ED", "EM", "ME", "DE", "GE" ->
                    "ENERGIE"
            "FIBRE", "FIBRES", "FIBER", "FIBERS", "FB", "TDF", "FIBRA" -> "CELLULOSE"
            "CELLULOSE", "CELLULOSES", "CRUDE_FIBER", "FIBRE_BRUTE", "FIBRA_BRUTA" -> "CELLULOSE"
            "AMIDON", "STARCH", "STARCHES", "ALMIDON" -> "AMIDON"
            "SUCRE", "SUCRES", "SUGAR", "SUGARS", "AZUCAR", "AZUCARES" -> "SUCRE"
            "ENA", "NFE", "EXTRACTIF_NON_AZOTE", "NITROGEN_FREE_EXTRACT", "CARBOHIDRATOS" -> "ENA"
            "FIBRETOT", "FIBRE_TOTALE", "TOTAL_FIBER", "DIETARY_FIBER", "FIBRETOTALE" -> "FIBRETOT"
            "FIBRSOL", "FIBRE_SOLUBLE", "SOLUBLE_FIBER", "FIBRESOLUBLE" -> "FIBRSOL"
            "FIBRINSO", "FIBRE_INSOLUBLE", "INSOLUBLE_FIBER", "FIBREINSOLUBLE" -> "FIBRINSO"
            "DM", "MATIERE_SECHE", "MATIÈRE_SÈCHE", "DRY_MATTER", "MATERIA_SECA", "MS" -> "DM"

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

            // Minéraux - Corriger pour utiliser les vrais labels des énumérations
            "CA",
            "CALCIUM",
            "CALCIO" -> "CAL" // NutrientMacro.CAL a le label "CAL"
            "CAL" -> "CAL"
            "P", "PHOSPHORE", "PHOSPHORUS", "FOSFORO" ->
                    "PHOS" // NutrientMacro.PHOS a le label "PHOS"
            "PHOS" -> "PHOS"
            "NA", "SODIUM", "SODIO", "NATRIUM" -> "NA"
            "K", "POTASSIUM", "POTASIO", "KALIUM" -> "K"
            "CL", "CHLORURE", "CHLORIDE", "CHLORE", "CHLORO" ->
                    "CHL" // NutrientMacro.CHL a le label "CHL"
            "CHL" -> "CHL"
            "MG", "MAGNESIUM", "MAGNESIO", "MAGNÉSIUM" -> "MG" // NutrientMacro.MG a le label "MG"
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
            // Mapper EPA/DHA vers les labels réels des lipides (AG205/AG226)
            "EPA", "EICOSAPENTAENOIC", "EICOSAPENTAENOIQUE", "C20_5", "C20:5" -> "AG205"
            "DHA", "DOCOSAHEXAENOIC", "DOCOSAHEXAENOIQUE", "C22_6", "C22:6" -> "AG226"
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

            // Acides aminés - Corriger pour utiliser les vrais labels de AAEnum
            "ALANINE",
            "ALA",
            "A" -> "ALANINE"
            "ARGININE", "ARG", "R" -> "ARGININE"
            "ASPARTIC_ACID", "ASPARTATE", "ASP", "D", "ACIDE_ASPARTIQUE" ->
                    "ASPARATE" // AAEnum.ASPARATE
            "CYSTEINE", "CYS", "C" -> "CYSTEINE"
            "GLUTAMIC_ACID", "GLUTAMATE", "GLU", "E", "ACIDE_GLUTAMIQUE" ->
                    "GLUTAMATE" // AAEnum.GLUTAMATE
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
            "TRYPTOPHAN", "TRP", "W", "TRYPTOPHANE" ->
                    "TRYPTOPHANE" // AAEnum.TRYPTOPHANE a le label "TRYPTOPHANE"
            "TYROSINE", "TYR", "Y" -> "TYROSINE"
            "VALINE", "VAL", "V" -> "VALINE"

            // Ratios et analyses - Corriger pour utiliser les vrais labels de NutrientAnalysis
            "PCA",
            "P/CA",
            "PHOSPHOCALCIUM",
            "RAPPORT_PHOSPHOCALCIQUE" -> "CAP" // NutrientAnalysis.PCa a le label "CAP"
            "ZNCU", "ZN/CU", "ZINCCOPPER", "RAPPORT_ZINC_CUIVRE" ->
                    "ZNCU" // NutrientAnalysis.ZnCu a le label "ZNCU"
            "O6O3", "O6/O3", "OMEGA6OMEGA3", "RAPPORT_OMEGA6_OMEGA3" ->
                    "O6O3" // NutrientAnalysis.o6o3 a le label "O6O3"
            "KNA", "K/NA", "POTASSIUMSODIUM", "RAPPORT_K_NA" ->
                    "KNA" // NutrientAnalysis.NaK a le label "KNA"
            "PROTP", "PROT/P", "PROTEINPHOSPHORE", "RAPPORT_PROT_P" ->
                    "PROTP" // NutrientAnalysis.PhosphProt a le label "PROTP"
            "METHCYS", "METH+CYS", "METHIONINE_CYSTEINE", "METH_CYS" ->
                    "METHCYS" // NutrientAnalysis.MethCys a le label "METHCYS"
            "PHENTYR", "PHEN+TYR", "PHENYLALANINE_TYROSINE", "PHEN_TYR" ->
                    "PHENTYR" // NutrientAnalysis.PhenTyr a le label "PHENTYR"

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
    private fun isKnownNutrient(normalizedLabel: String): Boolean =
        labelToNutrient.containsKey(normalizedLabel.uppercase())

    /** Obtient tous les labels de nutriments disponibles de toutes les énumérations */
    fun getAllNutrientLabels(): Set<String> {
        val nutrientsMain = NutrientMain.entries.map { it.label }
        val nutrientsLipides = NutrientLipid.entries.map { it.label }
        val nutrientsVitamines = NutrientVitam.entries.map { it.label }
        val nutrientsMacro = NutrientMacro.entries.map { it.label }
        val nutrientsMin = NutrientMin.entries.map { it.label }
        val nutrientsOther = NutrientOther.entries.map { it.label }
        val acideAmines = AAEnum.entries.map { it.label }

        return (nutrientsMain +
                        nutrientsLipides +
                        nutrientsVitamines +
                        nutrientsMacro +
                        nutrientsMin +
                        nutrientsOther +
                        acideAmines)
                .toSet()
    }

    /** Obtient tous les labels de variables système (VariableKind.variable) */
    fun getAllSystemVariableLabels(): Set<String> {
        return VariableKind.entries.map { it.variable }.toSet()
    }

    /** Obtient tous les labels de VariableKind (VariableKind.label) */
    fun getAllVariableKindLabels(): Set<String> {
        return VariableKind.entries.map { it.label }.toSet()
    }

    /** Obtient tous les labels reconnus (nutriments + variables système + VariableKind labels) */
    fun getAllRecognizedLabels(): Set<String> {
        return getAllNutrientLabels() + getAllSystemVariableLabels() + getAllVariableKindLabels()
    }

    /** Vérifie si un label correspond à un nutriment */
    fun isNutrientLabel(label: String): Boolean {
        return label in getAllNutrientLabels()
    }

    /** Vérifie si un label correspond à une variable système (VariableKind.variable) */
    fun isSystemVariableLabel(label: String): Boolean {
        return label in getAllSystemVariableLabels()
    }

    /** Vérifie si un label correspond à un VariableKind.label */
    fun isVariableKindLabel(label: String): Boolean {
        return label in getAllVariableKindLabels()
    }

    /** Vérifie si un label est reconnu (nutriment, variable système ou VariableKind) */
    fun isRecognizedLabel(label: String): Boolean {
        return isNutrientLabel(label) || isSystemVariableLabel(label) || isVariableKindLabel(label)
    }

    /** Trouve l'énumération de nutriment correspondant à un label */
    fun findNutrientByLabel(label: String): Nutrient? {
        return when {
            NutrientMain.entries.any { it.label == label } ->
                    NutrientMain.entries.find { it.label == label }
            NutrientLipid.entries.any { it.label == label } ->
                    NutrientLipid.entries.find { it.label == label }
            NutrientVitam.entries.any { it.label == label } ->
                    NutrientVitam.entries.find { it.label == label }
            NutrientMacro.entries.any { it.label == label } ->
                    NutrientMacro.entries.find { it.label == label }
            NutrientMin.entries.any { it.label == label } ->
                    NutrientMin.entries.find { it.label == label }
            NutrientOther.entries.any { it.label == label } ->
                    NutrientOther.entries.find { it.label == label }
            AAEnum.entries.any { it.label == label } -> AAEnum.entries.find { it.label == label }
            else -> null
        }
    }

    /** Trouve le VariableKind correspondant à un label (par variable ou label) */
    fun findVariableKindByLabel(label: String): VariableKind? {
        return VariableKind.entries.find { it.variable == label || it.label == label }
    }

    /**
     * Obtient une valeur de test par défaut pour une variable donnée Utilisé pour valider les
     * équations
     */
    fun getDefaultTestValue(variable: String): Double {
        return when {
            // Variables système avec valeurs spécifiques basées sur VariableKind
            isSystemVariableLabel(variable) -> {
                when (VariableKind.entries.find { it.variable == variable }) {
                    VariableKind.BW -> 25.0
                    VariableKind.BEE -> 400.0
                    VariableKind.MW -> 15.0
                    VariableKind.iBW -> 20.0
                    VariableKind.AdultWeight -> 30.0
                    VariableKind.LitterSize -> 6.0
                    VariableKind.WeekGestation -> 8.0
                    VariableKind.WeekLactation -> 4.0
                    VariableKind.BE -> 350.0
                    else -> 1.0
                }
            }
            // Variables de nutriments
            isNutrientLabel(variable) -> 10.0
            // Variables VariableKind par label
            isVariableKindLabel(variable) -> 1.0
            // Valeur par défaut
            else -> 1.0
        }
    }

    /** Obtient des informations détaillées sur un label */
    fun getLabelInfo(label: String): LabelInfo {
        return when {
            isNutrientLabel(label) -> {
                val nutrient = findNutrientByLabel(label)
                LabelInfo(
                        label = label,
                        type = LabelType.NUTRIENT,
                        nutrient = nutrient,
                        variableKind = null,
                        defaultValue = getDefaultTestValue(label)
                )
            }
            isSystemVariableLabel(label) -> {
                val variableKind = findVariableKindByLabel(label)
                LabelInfo(
                        label = label,
                        type = LabelType.SYSTEM_VARIABLE,
                        nutrient = null,
                        variableKind = variableKind,
                        defaultValue = getDefaultTestValue(label)
                )
            }
            isVariableKindLabel(label) -> {
                val variableKind = findVariableKindByLabel(label)
                LabelInfo(
                        label = label,
                        type = LabelType.VARIABLE_KIND_LABEL,
                        nutrient = null,
                        variableKind = variableKind,
                        defaultValue = getDefaultTestValue(label)
                )
            }
            else -> {
                LabelInfo(
                        label = label,
                        type = LabelType.UNKNOWN,
                        nutrient = null,
                        variableKind = null,
                        defaultValue = 1.0
                )
            }
        }
    }
}

/** Types de labels reconnus */
enum class LabelType {
    NUTRIENT,
    SYSTEM_VARIABLE,
    VARIABLE_KIND_LABEL,
    UNKNOWN
}

/** Informations détaillées sur un label */
data class LabelInfo(
        val label: String,
        val type: LabelType,
        val nutrient: Nutrient?,
        val variableKind: VariableKind?,
        val defaultValue: Double
)

// FIN ZONE PROTÉGÉE
