package fr.vetbrain.vetnutri_mp.Enumer


enum class NutrientAnalysis(
    val displayName: String,
    override val coef: Int,
    override val unite: String,
    override val label: String

) : Nutrient {
    NaK("Rapport K/NA", 0, "", "KNA"),
    PCa("Rapport Ca/P", 1, "", "CAP"),
    o6o3("Rapport O6/O3", 2, "", "O6O3"),
    ZnCu("Rapport Zn/Cu", 3, "", "ZNCU"),
    nonOsPhos("Phosphore non osseux", 7, "%", "nonOsPhos"),
    nonOsProt("Proteine non osseuse", 8, "%", "nonOsProt"),
    nonOsPP("Ratio Prot/phos non osseux", 9, "", "nonOsPP"),
    PhosphProt("Rapport Prot/Phos", 4, "", "PROTP"),
    MethCys("Méthionine+cystéine", 5, "g", "METHCYS"),
    PhenTyr("Phénylalanine+tyrosine", 6, "g", "PHENTYR");

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) = coefMap[coef]
        fun getByLabel(label: String) = labelMap[label]
        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }


    override fun getMNE() = MainNutrientEnum.ANA
    override val ue = UnitEnum.NO
    // Redondance supprimée grâce aux propriétés Kotlin
}