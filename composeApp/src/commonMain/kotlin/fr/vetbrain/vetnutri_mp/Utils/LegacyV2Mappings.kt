package fr.vetbrain.vetnutri_mp.Utils

internal object LegacyV2Mappings {
    val nutrientTableLabels: Map<String, Array<String>> = mapOf(
        "VALUEBASE" to arrayOf(
            "HUMIDITE", "PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE",
            "SUCRE", "AMIDON", "FIBRESOL", "FIBRETOT", "NDF", "ADF"
        ),
        "VALUEAA" to arrayOf(
            "ALANINE", "ARGININE", "ASPARAGINE", "ASPARATE", "CYSTEINE",
            "GLUTAMATE", "GLUTAMINE", "GLYCINE", "HISTIDINE", "ISOLEUCINE",
            "LEUCINE", "LYSINE", "METHIONINE", "PHENYLALANINE", "PROLINE",
            "PYRROLYSINE", "SELENOCYSTEINE", "SERINE", "THREONINE", "TRYPTOPHANE",
            "TYROSINE", "VALINE"
        ),
        "VALUEMACRO" to arrayOf("CAL", "PHOS", "MG", "NA", "K", "CHL"),
        "VALUEMIN" to arrayOf("FE", "CU", "ZN", "MN", "I", "SE"),
        "VALUEVITAM" to arrayOf(
            "VITA", "VITC", "VITD", "VITE", "VITK", "VITB1", "VITB2", "VITB3",
            "VITB5", "VITB6", "VITB8", "VITB9", "VITB12", "CHOLINE", "RETINOL",
            "BETACAR"
        ),
        "VALUELIPID" to arrayOf(
            "AGSATURE", "AGMONO", "AGPOLY", "AG40", "AG60", "AG80", "AG100",
            "AG120", "AG140", "AG160", "AG180", "AG181", "AG182", "AG183",
            "AG204", "AG205", "AG226", "CHOL", "O3", "O6", "EPADHA"
        ),
        "VALUEOTHER" to arrayOf(
            "TAURINE", "CARNITINE", "FOS", "MOS", "SUCR", "FRUCT", "LACT",
            "MALT", "AcOx", "GAL", "GLUCOSE", "DEXTROSE"
        )
    )

    val stageLabels = mapOf(
        0 to "ADULTE",
        1 to "CROISSANCE",
        2 to "LACTATION",
        3 to "GESTATION",
        4 to "HOSPIT"
    )

    val equationKindNames = mapOf(
        0 to "ENERGYNEED",
        1 to "ENERGYDENSITY",
        2 to "MW",
        3 to "INDICATOR",
        4 to "NEED",
        5 to "COMPLEMENTARY_NUTRIENT",
        6 to "ENERCOMP"
    )

    val referenceLevelNames = mapOf(
        0 to "MIN",
        1 to "MAX",
        2 to "OPTIMIN",
        3 to "OPTIMAX"
    )

    val unitRequirementIds = mapOf(
        0 to 1,
        1 to 0,
        2 to 2,
        3 to 6,
        4 to 5
    )

    val speciesLabels = mapOf(
        0 to "DOG",
        1 to "CAT",
        2 to "ALL",
        3 to "PRIMATE",
        4 to "RAT",
        5 to "SOURIS",
        6 to "FURET",
        7 to "LAPIN",
        8 to "CHEVAL",
        9 to "FELIN",
        10 to "CANIN",
        11 to "HERBIVORE",
        12 to "FOLIVORE"
    )

    val speciesEnumNames: Map<Int, String?> = mapOf(
        0 to "CHIEN",
        1 to "CHAT",
        2 to null,
        3 to "PRIMATE",
        4 to "RAT",
        5 to "SOURIS",
        6 to "FURET",
        7 to "LAPIN",
        8 to "CHEVAL",
        9 to "FELIN",
        10 to "CANIN",
        11 to "HERBIVORE",
        12 to "FOLIVORE"
    )

    fun speciesEnumName(rawValue: String): String? = when (rawValue.trim().uppercase()) {
        "ALL", "CH" -> null
        "DOG", "CHIEN" -> "CHIEN"
        "CAT", "CHAT" -> "CHAT"
        "PRIMATE" -> "PRIMATE"
        "RAT" -> "RAT"
        "SOURIS" -> "SOURIS"
        "FURET" -> "FURET"
        "LAPIN" -> "LAPIN"
        "CHEVAL" -> "CHEVAL"
        "FELIN" -> "FELIN"
        "CANIN" -> "CANIN"
        "HERBIVORE" -> "HERBIVORE"
        "FOLIVORE" -> "FOLIVORE"
        else -> null
    }

    fun transpileScript(script: String): String {
        var result = script.trim()
            .replace(Regex("\\bMath\\."), "")
            .replace("**", "^")
            .replace("|", "+")

        if (result.contains('\n') || result.contains(';')) {
            result = foldValueScript(result) ?: result
        }

        return result.replace(Regex("[ \t]+"), " ").trim()
    }

    private fun foldValueScript(script: String): String? {
        val terms = mutableListOf<String>()
        val ifTerms = mutableListOf<String>()
        val elseTerms = mutableListOf<String>()
        var condition = ""
        var phase = 0

        val initPattern = Regex("^value\\s*=\\s*(?!value\\b)(.+)$")
        val incrementPattern = Regex("^value\\s*=\\s*value\\s*\\+\\s*(.+)$")
        val ifPattern = Regex("^if\\s*\\((.+)\\)\\s*\\{?\\s*$")
        val elsePattern = Regex("^[}]?\\s*else\\s*\\{?\\s*$")

        val lines = script
            .replace(Regex("[ \t]+"), " ")
            .split(Regex("[;\n]"))
            .map { it.trim().trimEnd('{').trim() }
            .filter { it.isNotBlank() && it != "value" && it != "}" }

        for (line in lines) {
            when {
                elsePattern.matches(line) -> phase = 2
                ifPattern.matches(line) -> {
                    condition = ifPattern.find(line)!!.groupValues[1].trim()
                    phase = 1
                    ifTerms.clear()
                    elseTerms.clear()
                }
                incrementPattern.matches(line) -> {
                    val term = incrementPattern.find(line)!!.groupValues[1].trim()
                    when (phase) {
                        0 -> terms.add(term)
                        1 -> ifTerms.add(term)
                        2 -> elseTerms.add(term)
                    }
                }
                initPattern.matches(line) && phase == 0 ->
                    terms.add(initPattern.find(line)!!.groupValues[1].trim())
            }
        }

        if (condition.isNotBlank()) {
            val ifExpression = ifTerms.joinToString("+")
            val elseExpression = elseTerms.joinToString("+").ifBlank { "0" }
            terms.add("if($condition,$ifExpression,$elseExpression)")
        }

        return terms.joinToString("+").takeIf { it.isNotBlank() }
    }
}
