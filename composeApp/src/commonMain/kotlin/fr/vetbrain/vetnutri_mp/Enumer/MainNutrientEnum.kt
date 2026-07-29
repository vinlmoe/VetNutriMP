package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/** Énumération des catégories principales de nutriments */
enum class MainNutrientEnum(val id: Int, override val label: String, val coef: Double) : Labelable {
    MIN(0, "Minéraux", 1.0),
    ANA(1, "Acides aminés", 1.0),
    MACRO(2, "Macronutriments", 1.0),
    VITAM(3, "Vitamines", 1.0),
    BASE(4, "Base de données", 1.0),
    LIPID(5, "Acides gras", 1.0),
    OTHER(6, "Autres", 1.0),
    ENERGIE(7, "Énergie", 1.0),
    NO(8, "Aucun", 1.0),
    AMA(9, "Acides aminés", 1.0),
    INGREDIENT(10, "Ingrédients", 1.0),
    INDICAT(11, "Indicateurs", 1.0);

    fun getNutrient(i: Int): String {
        val result =
                when (this) {
                    MIN -> getMinNutrient(i)
                    ANA, AMA -> getAmaNutrient(i)
                    MACRO -> getMacroNutrient(i)
                    VITAM -> getVitamNutrient(i)
                    LIPID -> getLipidNutrient(i)
                    else ->
                            when (i) {
                                0 -> "NONE"
                                else -> "Rien"
                            }
                }
        return result
    }

    /** @return liste des sous-nutriments */
    fun getSousNutrients(): List<String> {
        val result =
                when (this) {
                    MIN ->
                            listOf(
                                    "Phosphore",
                                    "Calcium",
                                    "Sodium",
                                    "Potassium",
                                    "Chlorure",
                                    "Magnésium",
                                    "Cuivre",
                                    "Zinc",
                                    "Manganèse",
                                    "Sélénium",
                                    "Iode",
                                    "Fer",
                                    "Soufre"
                            )
                    ANA, AMA ->
                            listOf(
                                    "Arginine",
                                    "Alanine",
                                    "Histidine",
                                    "Isoleucine",
                                    "Leucine",
                                    "Lysine",
                                    "Méthionine",
                                    "Méthionine+Cystine",
                                    "Phénylalanine",
                                    "Phénylalanine+Tyrosine",
                                    "Thréonine",
                                    "Tryptophane",
                                    "Valine"
                            )
                    MACRO -> listOf("Protéines", "Matières grasses", "Glucides", "ENA", "Cellulose")
                    VITAM ->
                            listOf(
                                    "Vitamine A",
                                    "Vitamine D",
                                    "Vitamine E",
                                    "Vitamine K",
                                    "Vitamine C",
                                    "Thiamine",
                                    "Riboflavine",
                                    "Vitamine B6",
                                    "Vitamine B12",
                                    "Niacine",
                                    "Acide pantothénique",
                                    "Acide folique",
                                    "Biotine",
                                    "Choline"
                            )
                    LIPID ->
                            listOf(
                                    "Acide arachidonique",
                                    "Acide linoléique",
                                    "Oméga 3",
                                    "Oméga 6",
                                    "EPA+DHA"
                            )
                    else -> listOf("NONE")
                }
        return result
    }

    /**
     * @param i indice du nutriment
     * @return nom du minéral
     */
    private fun getMinNutrient(i: Int): String {
        return when (i) {
            0 -> "Phosphore"
            1 -> "Calcium"
            2 -> "Sodium"
            3 -> "Potassium"
            4 -> "Chlorure"
            5 -> "Magnésium"
            6 -> "Cuivre"
            7 -> "Zinc"
            8 -> "Manganèse"
            9 -> "Sélénium"
            10 -> "Iode"
            11 -> "Fer"
            12 -> "Soufre"
            else -> "Minéral Inconnu"
        }
    }

    /**
     * @param i indice du nutriment
     * @return nom de l'acide aminé
     */
    private fun getAmaNutrient(i: Int): String {
        return when (i) {
            0 -> "Arginine"
            1 -> "Alanine"
            2 -> "Histidine"
            3 -> "Isoleucine"
            4 -> "Leucine"
            5 -> "Lysine"
            6 -> "Méthionine"
            7 -> "Méthionine+Cystine"
            8 -> "Phénylalanine"
            9 -> "Phénylalanine+Tyrosine"
            10 -> "Thréonine"
            11 -> "Tryptophane"
            12 -> "Valine"
            else -> "Acide aminé Inconnu"
        }
    }

    /**
     * @param i indice du nutriment
     * @return nom du macronutriment
     */
    private fun getMacroNutrient(i: Int): String {
        return when (i) {
            0 -> "Protéines"
            1 -> "Matières grasses"
            2 -> "Glucides"
            3 -> "ENA"
            4 -> "Cellulose"
            else -> "Macronutriment Inconnu"
        }
    }

    /**
     * @param i indice du nutriment
     * @return nom de la vitamine
     */
    private fun getVitamNutrient(i: Int): String {
        return when (i) {
            0 -> "Vitamine A"
            1 -> "Vitamine D"
            2 -> "Vitamine E"
            3 -> "Vitamine K"
            4 -> "Vitamine C"
            5 -> "Thiamine"
            6 -> "Riboflavine"
            7 -> "Vitamine B6"
            8 -> "Vitamine B12"
            9 -> "Niacine"
            10 -> "Acide pantothénique"
            11 -> "Acide folique"
            12 -> "Biotine"
            13 -> "Choline"
            else -> "Vitamine Inconnue"
        }
    }

    /**
     * @param i indice du nutriment
     * @return nom du lipide
     */
    private fun getLipidNutrient(i: Int): String {
        return when (i) {
            0 -> "Acide arachidonique"
            1 -> "Acide linoléique"
            2 -> "Oméga 3"
            3 -> "Oméga 6"
            4 -> "EPA+DHA"
            else -> "Lipide Inconnu"
        }
    }

    override fun toString(): String {
        return label
    }

    companion object {
        /**
         * Trouve un type de nutriment principal par son coefficient
         *
         * @param coef Le coefficient à rechercher
         * @return Le type de nutriment principal ou null si non trouvé
         */
        fun getByCoef(coef: Int): MainNutrientEnum? {
            return entries.find { it.coef.toInt() == coef }
        }
    }
}
