package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Version optimisée de l'énumération Espece avec companion objects optimisés et suppression des
 * boucles for inefficaces.
 */
enum class OptimizedEspece(override val label: String, val categorie: Int, val id: String) :
        Labelable {
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

    fun nameToString(): String = label

    companion object {
        // Maps pré-calculées pour accès O(1)
        private val labelMap by lazy { entries.associateBy { it.label.lowercase() } }
        private val idMap by lazy { entries.associateBy { it.id } }
        private val categoryMap by lazy { entries.associateBy { it.categorie } }

        /** Version optimisée de getFromString - O(1) au lieu de boucles */
        fun getFromString(value: String): OptimizedEspece? {
            if (value.isBlank()) return null

            val cleanedValue = value.trim().replace("[\\[\\]\"]".toRegex(), "")

            return labelMap[cleanedValue.lowercase()]
                    ?: idMap[cleanedValue] ?: cleanedValue.toIntOrNull()?.let { categoryMap[it] }
                            ?: run {
                        // Fallback : essayer de matcher par nom d'énumération
                        try {
                            valueOf(cleanedValue.uppercase())
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
        }

        /** Version optimisée de getStringFromInt - O(1) */
        fun getStringFromInt(id: Int): String {
            return categoryMap[id]?.label ?: CHIEN.label
        }

        /** Version optimisée de getStringFromId - O(1) */
        fun getStringFromId(id: String): String {
            return idMap[id]?.label ?: CHIEN.label
        }

        /** Version optimisée de getEnumFromInt - O(1) */
        fun getEnumFromInt(id: Int): OptimizedEspece {
            return categoryMap[id] ?: idMap[id.toString()] ?: CHIEN
        }

        /** Version optimisée de getEnumFromString - O(1) */
        fun getEnumFromString(id: String): OptimizedEspece {
            return getFromString(id) ?: CHIEN
        }

        /** Version optimisée de valuesExcept - O(n) mais avec une seule passe */
        fun valuesExcept(vararg exceptions: OptimizedEspece): List<OptimizedEspece> {
            val exceptionSet = exceptions.toSet()
            return entries.filter { it !in exceptionSet }
        }

        /** Version optimisée de valuesExcept() - retourne une liste immuable */
        fun valuesExcept(): List<OptimizedEspece> {
            return entries.filter { it != CH }
        }

        /** Recherche par label exact - O(1) */
        fun getByLabel(label: String): OptimizedEspece? {
            return labelMap[label.lowercase()]
        }

        /** Recherche par ID - O(1) */
        fun getById(id: String): OptimizedEspece? {
            return idMap[id]
        }
    }
}
