package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/** Types d'unités pour les besoins nutritionnels */
enum class UnitType {
    POIDS, // Unités basées sur le poids
    ENERGIE, // Unités basées sur l'énergie
    AUTRE // Autres unités (ratio, valeur absolue)
}

/** Énumération des unités pour les besoins nutritionnels */
enum class UnitReqEnum(val id: Int, override val label: String, val type: UnitType) : Labelable {
    PERKG(0, "par kg poids vif", UnitType.POIDS),
    PERKCAL(1, "par 1000 kcal", UnitType.ENERGIE),
    PERMS(2, "par kg poids Metabolique", UnitType.POIDS),
    PERKJ(4, "par 1000 kJ", UnitType.ENERGIE),
    RATIO(5, "ratio", UnitType.AUTRE),
    ABSOLUTE(6, "valeur absolue", UnitType.AUTRE);

    override fun toString(): String {
        return label
    }

    /** Retourne l'identifiant de l'unité */
    fun getID(): Int {
        return id
    }

    /** Vérifie si l'unité est basée sur l'énergie */
    fun isEnergieBased(): Boolean {
        return type == UnitType.ENERGIE
    }

    /** Vérifie si l'unité est basée sur le poids */
    fun isPoidsBased(): Boolean {
        return type == UnitType.POIDS
    }

    /** Vérifie si l'unité est d'un autre type */
    fun isAutreType(): Boolean {
        return type == UnitType.AUTRE
    }

    companion object {
        /** Facteur de conversion: 1 kcal = 4.184 kJ */
        const val KCAL_TO_KJ_FACTOR: Double = 4.184

        /** Facteur de conversion: 1 cal = 4.184 J */
        const val CAL_TO_J_FACTOR: Double = 4.184

        /**
         * Obtient l'unité correspondant à l'identifiant
         *
         * @param id Identifiant de l'unité
         * @return L'unité correspondante ou PERKG par défaut
         */
        fun getById(id: Int): UnitReqEnum {
            return values().find { it.id == id } ?: PERKG
        }

        /**
         * Convertit des kcal en kJ
         *
         * @param kcal Valeur en kcal
         * @return Valeur en kJ
         */
        fun convertirKcalVersKj(kcal: Double): Double {
            return kcal * KCAL_TO_KJ_FACTOR
        }

        /**
         * Convertit des kJ en kcal
         *
         * @param kj Valeur en kJ
         * @return Valeur en kcal
         */
        fun convertirKjVersKcal(kj: Double): Double {
            return kj / KCAL_TO_KJ_FACTOR
        }

        /**
         * Convertit des calories en joules
         *
         * @param cal Valeur en calories
         * @return Valeur en joules
         */
        fun convertirCalVersJ(cal: Double): Double {
            return cal * CAL_TO_J_FACTOR
        }

        /**
         * Convertit des joules en calories
         *
         * @param j Valeur en joules
         * @return Valeur en calories
         */
        fun convertirJVersCal(j: Double): Double {
            return j / CAL_TO_J_FACTOR
        }

        /**
         * Convertit une valeur d'une unité énergétique vers une autre
         *
         * @param valeur Valeur à convertir
         * @param uniteSource Unité source
         * @param uniteCible Unité cible
         * @return Valeur convertie ou null si les unités ne sont pas énergétiques
         */
        fun convertirEnergieEntreUnites(
                valeur: Double,
                uniteSource: UnitReqEnum,
                uniteCible: UnitReqEnum
        ): Double? {
            if (!uniteSource.isEnergieBased() || !uniteCible.isEnergieBased()) {
                return null
            }

            return when {
                uniteSource == PERKCAL && uniteCible == PERKJ -> convertirKcalVersKj(valeur)
                uniteSource == PERKJ && uniteCible == PERKCAL -> convertirKjVersKcal(valeur)
                uniteSource == uniteCible -> valeur
                else -> null
            }
        }

        /**
         * Obtient toutes les unités d'un type donné
         *
         * @param type Type d'unité recherché
         * @return Liste des unités du type spécifié
         */
        fun getByType(type: UnitType): List<UnitReqEnum> {
            return values().filter { it.type == type }
        }
    }
}
