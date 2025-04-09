package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum

/**
 * Représente un besoin nutritionnel pour une référence
 *
 * @property id Identifiant unique du besoin nutritionnel
 * @property referenceEvId Identifiant de la référence associée
 * @property name Nom du nutriment
 * @property value Valeur du besoin nutritionnel
 * @property nutrientType Type principal du nutriment (BASE, MACRO, MIN, etc.)
 * @property nutrientCode Code spécifique du nutriment dans son type
 * @property unitReq Unité de mesure pour la valeur du besoin
 * @property biblioRef Référence bibliographique associée (facultatif)
 */
data class NutrientRef(
        val id: String = "",
        val referenceEvId: String = "",
        val name: String = "",
        val value: String = "",
        val nutrientType: MainNutrientEnum,
        val nutrientCode: Int = 0,
        val unitReq: UnitReqEnum = UnitReqEnum.PERKG,
        val biblioRef: BiblioRef? = null
)
