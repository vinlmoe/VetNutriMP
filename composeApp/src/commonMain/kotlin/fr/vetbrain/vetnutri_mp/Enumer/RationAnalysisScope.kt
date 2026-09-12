package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys

/**
 * Périmètre d'analyse des rations.
 *
 * - [RATION_UNIQUE] : mode historique, une seule ration sélectionnée est analysée.
 * - [GROUPE_ACTUELLES] : toutes les rations actuelles de la consultation sont agrégées en une
 *   ration virtuelle unique, moyenne pondérée par le coefficient de chaque ration.
 * - [GROUPE_PROPOSEES] : idem pour toutes les rations proposées.
 */
enum class RationAnalysisScope(val labelKey: String) {
    RATION_UNIQUE(LocalizationKeys.Ration.SCOPE_SINGLE),
    GROUPE_ACTUELLES(LocalizationKeys.Ration.SCOPE_GROUP_ACTUAL),
    GROUPE_PROPOSEES(LocalizationKeys.Ration.SCOPE_GROUP_PROPOSED);

    /** Indique si ce périmètre agrège plusieurs rations en une ration virtuelle. */
    val estGroupe: Boolean
        get() = this != RATION_UNIQUE

    /** Vrai si ce périmètre regroupe les rations actuelles, faux pour les rations proposées. */
    val cibleRationsActuelles: Boolean
        get() = this == GROUPE_ACTUELLES

    companion object {
        /** Périmètre de groupe correspondant au drapeau `actual` d'une ration. */
        fun pourActual(actual: Boolean): RationAnalysisScope =
                if (actual) GROUPE_ACTUELLES else GROUPE_PROPOSEES
    }
}
