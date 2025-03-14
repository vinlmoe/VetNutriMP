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
    CANIN("LAPIN", 10, "10"),
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
        /**
         * Récupère une espèce à partir d'une chaîne, en essayant plusieurs formats possibles. Cette
         * méthode nettoie automatiquement la chaîne d'entrée (crochets, guillemets, espaces) et
         * gère l'insensibilité à la casse.
         *
         * @param value La chaîne à convertir en espèce
         * @return L'espèce correspondante ou null si aucune correspondance n'est trouvée
         */
        fun getFromString(value: String): Espece? {
            // Nettoyer la chaîne d'entrée
            val cleanedValue = value.replace("[", "").replace("]", "").replace("\"", "").trim()

            if (cleanedValue.isEmpty()) {
                return null
            }

            // Vérifier si c'est un nom d'énumération (CHIEN, CHAT, etc.) - insensible à la casse
            try {
                val upperCaseValue = cleanedValue.uppercase()
                return valueOf(upperCaseValue)
            } catch (e: Exception) {
                // Pas un nom d'énumération valide
            }

            // Vérifier si c'est un label (DOG, CAT, etc.) - insensible à la casse
            val byLabel = entries.find { it.label.equals(cleanedValue, ignoreCase = true) }
            if (byLabel != null) {
                return byLabel
            }

            // Vérifier si c'est un ID sous forme de chaîne
            val byId = entries.find { it.id == cleanedValue }
            if (byId != null) {
                return byId
            }

            // Vérifier si c'est un ID numérique
            val intValue = cleanedValue.toIntOrNull()
            if (intValue != null) {
                return getEnumFromInt(intValue)
            }

            // Aucune correspondance trouvée
            return null
        }

        /**
         * Retourne le nom de l'espèce correspondant à l'identifiant numérique.
         * @param id L'identifiant numérique de l'espèce
         * @return Le nom de l'espèce
         */
        fun getStringFromInt(id: Int): String {
            var str = CHIEN.nameToString()
            for (espe in values()) {
                if (id == espe.categorie) {
                    str = espe.nameToString()
                }
            }
            return str
        }

        fun getStringFromId(id: String): String {
            var str = CHIEN.nameToString()
            for (espe in values()) {
                if (id == espe.id) {
                    str = espe.nameToString()
                }
            }
            return str
        }

        /**
         * Retourne l'énumération correspondant à l'identifiant numérique.
         * @param id L'identifiant numérique de l'espèce
         * @return L'énumération correspondante
         */
        fun getEnumFromInt(id: Int): Espece {
            var esp = CHIEN
            for (espe in values()) {
                if (id == espe.categorie) {
                    esp = espe
                }
            }
            return esp
        }

        /**
         * Retourne l'énumération correspondant au nom.
         * @param id Le nom de l'espèce
         * @return L'énumération correspondante
         */
        fun getEnumFromString(id: String): Espece {
            var esp = CHIEN
            for (espe in values()) {
                if (id == espe.nameToString()) {
                    esp = espe
                }
            }
            return esp
        }

        /**
         * Retourne toutes les valeurs de l'énumération sauf CH.
         * @return Une liste contenant toutes les valeurs sauf CH
         */
        fun valuesExcept(): List<Espece> {
            val es = mutableListOf<Espece>()
            for (e in values()) {
                if (e != CH) {
                    es.add(e)
                }
            }
            return es
        }

        /**
         * Variante de valuesExcept qui prend plusieurs exceptions en paramètre.
         * @param exceptions Les énumérations à exclure
         * @return Une liste sans les énumérations spécifiées
         */
        fun valuesExcept(vararg exceptions: Espece): List<Espece> =
                entries.filter { it !in exceptions }

        /**
         * Retourne l'énumération correspondant à l'identifiant de label.
         * @param i L'identifiant de label
         * @return L'énumération correspondante
         */
        fun getEnumFromStringId(i: String): Espece? {
            return map[i]
        }

        /**
         * Retourne l'énumération correspondant au label.
         * @param label Le label à rechercher
         * @return L'énumération correspondante ou null si non trouvée
         */
        fun getByLabel(label: String): Espece? = entries.find { it.label == label }

        private val map = values().associateBy { it.label }
    }
}
