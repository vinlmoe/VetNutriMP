package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum

/**
 * Classe représentant une référence de nutriment avec ses propriétés. Basée sur la classe
 * NutrientRefP du projet Java original.
 */
class NutrientRefP(
        val mne: MainNutrientEnum,
        var nom: String,
        var kind: Int,
        var relativekind: Int,
        var quantity: String,
        var present: Boolean,
        var unit: UnitP,
        var unitReq: UnitReqEnum,
        var biblio: BiblioRef
) {
    // Variable interne pour mémoriser l'unité principale
    private val unitMain: UnitP = unit

    // Variable maladie
    var disease: Boolean = false

    // Niveau relatif
    var reflevel: Reflevel? = null

    /** Constructeur secondaire qui utilise UnitEnum au lieu de UnitP */
    constructor(
            mne: MainNutrientEnum,
            nom: String,
            kind: Int,
            relativekind: Int,
            quantity: String,
            present: Boolean,
            unit: UnitEnum,
            unitReq: UnitReqEnum,
            biblio: BiblioRef
    ) : this(
            mne = mne,
            nom = nom,
            kind = kind,
            relativekind = relativekind,
            quantity = quantity,
            present = present,
            unit = UnitP(unit),
            unitReq = unitReq,
            biblio = biblio
    )

    /** Calcule la conversion entre l'unité courante et l'unité principale */
    fun getConverter(): Float {
        return unit.getUnit().conv / unitMain.getUnit().conv
    }

    /** Obtient la valeur convertie (utilisé dans ReferenceEv) */
    fun getQuantityConverted(): Float {
        // Si la quantité est vide, retourner 0
        if (quantity.isBlank()) return 0f

        // Sinon, convertir la valeur
        val value = quantity.replace(",", ".").toFloat()
        return value * getConverter()
    }
}
