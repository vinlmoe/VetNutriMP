package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/** Énumération des unités pour les besoins nutritionnels */
enum class UnitReqEnum(val id: Int, override val label: String) : Labelable {
    PERKG(0, "par kg"),
    PERKCAL(1, "par 1000 kcal"),
    PERMS(2, "par % MS"),
    PERG(3, "par g"),
    PERKJ(4, "par 1000 kJ"),
    RATIO(5, "ratio"),
    ABSOLUTE(6, "valeur absolue");

    override fun toString(): String {
        return label
    }

    /** Retourne l'identifiant de l'unité */
    fun getID(): Int {
        return id
    }

    companion object {
        /**
         * Obtient l'unité correspondant à l'identifiant
         *
         * @param id Identifiant de l'unité
         * @return L'unité correspondante ou PERKG par défaut
         */
        fun getById(id: Int): UnitReqEnum {
            return values().find { it.id == id } ?: PERKG
        }
    }
}
