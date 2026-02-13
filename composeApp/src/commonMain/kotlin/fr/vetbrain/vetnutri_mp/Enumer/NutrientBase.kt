package fr.vetbrain.vetnutri_mp.Enumer

/**
 * Classe de transition pour assurer la compatibilité. Cette classe est obsolète, veuillez utiliser
 * NutrientMain à la place. Cette classe remplace l'énumération NutrientBaseExt originale du projet
 * Java.
 */
@Deprecated("Utilisez NutrientMain à la place", ReplaceWith("NutrientMain"))
object NutrientBaseExt {
    /** Obsolète, utilisez NutrientMain.entries à la place */
    val entries: List<NutrientMain>
        get() = NutrientMain.entries

    /** Obsolète, utilisez NutrientMain.getByLabel() à la place */
    fun getByLabel(label: String): NutrientMain = NutrientMain.getByLabel(label) ?: NutrientMain.PROTEINE

    /** Obsolète, utilisez NutrientMain.getByCoef() à la place */
    fun getByCoef(coef: Int): NutrientMain? = NutrientMain.getByCoef(coef)

    /** Constantes compatibles avec l'ancienne énumération */
    val PROTEINE
        get() = NutrientMain.PROTEINE
    val LIPIDE
        get() = NutrientMain.LIPIDE
    val GLUCIDE
        get() = NutrientMain.GLUCIDE
    val FIBRE
        get() = NutrientMain.CELLULOSE
    val CENDRE
        get() = NutrientMain.CENDRE
    val HUMIDITE
        get() = NutrientMain.HUMIDITE
    val ENERGIE
        get() = NutrientMain.ENERGIE
    val ENA
        get() = NutrientMain.ENA
}
