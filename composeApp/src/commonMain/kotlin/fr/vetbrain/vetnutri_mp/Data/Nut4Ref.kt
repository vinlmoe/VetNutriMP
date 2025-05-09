package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum

/**
 * Classe représentant une référence nutritionnelle Extraite de la classe interne
 * ReferenceEv.Nut4Ref pour faciliter la persistance
 *
 * @property uuid Identifiant unique de la référence nutritionnelle
 * @property nutrient Le nutriment concerné
 * @property niveauRef Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
 * @property quantite La valeur numérique du nutriment
 * @property unite L'unité du nutriment
 * @property uniteReq L'unité requise pour le nutriment
 * @property citation La référence bibliographique associée (peut être null)
 */
data class Nut4Ref(
        val uuid: String = java.util.UUID.randomUUID().toString(),
        val nutrient: Nutrient,
        val niveauRef: Reflevel,
        val quantite: Float,
        val unite: UnitEnum,
        val uniteReq: UnitReqEnum,
        val citation: BiblioRef? = null
) {
    /** Constructeur secondaire pour compatibilité avec l'ancien code qui utilisait 'biblio' */
    constructor(
            nutrient: Nutrient,
            niveauRelatif: Reflevel,
            quantite: Float,
            unite: UnitEnum,
            uniteReq: UnitReqEnum,
            biblio: BiblioRef?
    ) : this(
            uuid = java.util.UUID.randomUUID().toString(),
            nutrient = nutrient,
            niveauRef = niveauRelatif,
            quantite = quantite,
            unite = unite,
            uniteReq = uniteReq,
            citation = biblio
    )
}
