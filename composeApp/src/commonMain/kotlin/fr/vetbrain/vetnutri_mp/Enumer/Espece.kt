package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Énumération des espèces animales supportées par l'application.
 *
 * @property displayName Le nom internationalisé de l'espèce
 * @property categorie L'identifiant numérique de la catégorie d'espèce
 * @property label Le libellé utilisé pour identifier l'espèce
 */
enum class Espece(override val label: String, val categorie: Int, val id: String) : Labelable {
    CH("ALL", 2, "ALL"),
    CHIEN("DOG", 0, "0"),
    CHAT("CAT", 1, "1"),
    PRIMATE("PRIMATE", 3, "3"),
    RAT("RAT", 4, "4"),
    SOURIS("SOURIS", 5, "5"),
    FURET("FURET", 6, "6"),
    LAPIN("LAPIN", 7, "7"),
    CHEVAL("CHEVAL", 8, "8"),
    FELIN("FELIN", 9, "9"),
    CANIN("CANIN", 10, "10"),
    HERBIVORE("HERBIVORE", 11, "11"),
    FOLIVORE("FOLIVORE", 12, "12");

    /**
     * Retourne le nom de l'espèce.
     * @return Le nom de l'espèce
     */
    fun nameToString(): String {
        return label
    }

    companion object {
        // Regex et maps pré-calculées une seule fois (évite recompilation/reconstruction à chaque appel)
        private val NETTOYAGE_REGEX: Regex by lazy { "[\\[\\]\"]".toRegex() }
        private val LABEL_MAP: Map<String, Espece> by lazy { entries.associateBy { it.label.lowercase() } }
        private val ID_MAP: Map<String, Espece> by lazy { entries.associateBy { it.id } }
        private val CATEGORY_MAP: Map<Int, Espece> by lazy { entries.associateBy { it.categorie } }

        /**
         * Récupère une espèce à partir d'une chaîne, en essayant plusieurs formats possibles.
         * Version optimisée avec maps pré-calculées pour accès O(1).
         *
         * @param value La chaîne à convertir en espèce
         * @return L'espèce correspondante ou null si aucune correspondance n'est trouvée
         */
        fun getFromString(value: String): Espece? {
            if (value.isBlank()) return null

            // Nettoyer la chaîne d'entrée
            val cleanedValue = value.trim().replace(NETTOYAGE_REGEX, "")

            // Maps pré-calculées pour accès O(1)
            return LABEL_MAP[cleanedValue.lowercase()]
                    ?: ID_MAP[cleanedValue] ?: cleanedValue.toIntOrNull()?.let { getEnumFromInt(it) }
                            ?: run {
                        // Fallback : essayer de matcher par nom d'énumération
                        try {
                            valueOf(cleanedValue.uppercase())
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
        }

        /**
         * Retourne le nom de l'espèce correspondant à l'identifiant numérique. Version optimisée
         * avec map O(1).
         * @param id L'identifiant numérique de l'espèce
         * @return Le nom de l'espèce
         */
        fun getStringFromInt(id: Int): String {
            return CATEGORY_MAP[id]?.label ?: CHIEN.label
        }

        /**
         * Retourne le nom de l'espèce correspondant à l'ID. Version optimisée avec map O(1).
         * @param id L'ID de l'espèce
         * @return Le nom de l'espèce
         */
        fun getStringFromId(id: String): String {
            return ID_MAP[id]?.label ?: CHIEN.label
        }

        /**
         * Retourne l'énumération correspondant à l'identifiant numérique. Version optimisée avec
         * maps O(1).
         * @param id L'identifiant numérique de l'espèce
         * @return L'énumération correspondante
         */
        fun getEnumFromInt(id: Int): Espece {
            return CATEGORY_MAP[id] ?: ID_MAP[id.toString()] ?: CHIEN
        }

        /**
         * Retourne l'énumération correspondant au nom. Version optimisée avec map O(1).
         * @param id Le nom de l'espèce
         * @return L'énumération correspondante
         */
        fun getEnumFromString(id: String): Espece {
            return getFromString(id) ?: CHIEN
        }

        /**
         * Retourne toutes les valeurs de l'énumération sauf CH. Version optimisée sans boucles.
         * @return Une liste immuable contenant toutes les valeurs sauf CH
         */
        fun valuesExcept(): List<Espece> {
            return entries.filter { it != CH }
        }

        /**
         * Variante de valuesExcept qui prend plusieurs exceptions en paramètre. Version optimisée
         * avec Set pour O(1) lookup.
         * @param exceptions Les énumérations à exclure
         * @return Une liste sans les énumérations spécifiées
         */
        fun valuesExcept(vararg exceptions: Espece): List<Espece> {
            val exceptionSet = exceptions.toSet()
            return entries.filter { it !in exceptionSet }
        }

        /**
         * Retourne l'énumération correspondant à l'identifiant de label. Version optimisée avec map
         * O(1).
         * @param label L'identifiant de label
         * @return L'énumération correspondante
         */
        fun getEnumFromStringId(label: String): Espece? {
            val labelMap = entries.associateBy { it.label }
            return labelMap[label]
        }

        /**
         * Retourne l'énumération correspondant au label. Version optimisée avec map O(1).
         * @param label Le label à rechercher
         * @return L'énumération correspondante ou null si non trouvée
         */
        fun getByLabel(label: String): Espece? {
            val labelMap = entries.associateBy { it.label }
            return labelMap[label]
        }
    }
}
