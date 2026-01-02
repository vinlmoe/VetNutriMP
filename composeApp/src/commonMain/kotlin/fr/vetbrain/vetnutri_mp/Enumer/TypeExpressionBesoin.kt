package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Types d'expression des besoins nutritionnels Basé sur UnitReqEnum mais limité aux types POIDS et
 * ENERGIE (excluant AUTRE)
 */
enum class TypeExpressionBesoin(
        val unitReqEnum: UnitReqEnum,
        val id: Int = unitReqEnum.id,
        val displayName: String = unitReqEnum.label,
        override val label: String = unitReqEnum.label
) : Labelable {
    PAR_KG(UnitReqEnum.PERKG),
    PAR_KCAL(UnitReqEnum.PERKCAL),
    PAR_KG_METABOLIQUE(UnitReqEnum.PERMS),
    PAR_KJ(UnitReqEnum.PERKJ);

    companion object {
        /** Valeur par défaut */
        val DEFAULT = PAR_KG

        /**
         * Obtient le TypeExpressionBesoin correspondant à un ID
         * @param id L'ID de l'unité
         * @return Le TypeExpressionBesoin correspondant ou DEFAULT
         */
        fun getById(id: Int): TypeExpressionBesoin {
            return values().find { it.id == id } ?: DEFAULT
        }

        /**
         * Obtient le TypeExpressionBesoin correspondant à un displayName
         * @param displayName Le nom d'affichage
         * @return Le TypeExpressionBesoin correspondant ou DEFAULT
         */
        fun getByDisplayName(displayName: String): TypeExpressionBesoin {
            return values().find { it.displayName == displayName } ?: DEFAULT
        }

        /**
         * Obtient toutes les unités valides (excluant UnitType.AUTRE)
         * @return Liste des TypeExpressionBesoin disponibles
         */
        fun getValidUnits(): List<TypeExpressionBesoin> {
            return values().toList()
        }
    }
}
