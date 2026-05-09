package fr.vetbrain.vetnutri_mp.Utils

/**
 * Enumération des bases de données pour les aliments. Permet de convertir les codes dataB en noms
 * lisibles de manière type-safe.
 */
enum class DataB(val code: String, val displayName: String) {
    CIQUAL("0", "CIQUAL"),
    FCEN("1", "FCEN"),
    PETFOOD_DIVERS("2", "PetFood Divers"),
    GENERIQUE("4", "Générique"),
    ALIMENT_BARF("5", "Aliment Barf"),
    VETFOOD_2024("VF24", "VetFood 2024"),
    CHEVAL("CHEVAL", "Cheval");

    companion object {
        private const val DEFAULT_DISPLAY_NAME = "Base de données inconnue"

        /**
         * Trouve l'enum DataB correspondant au code fourni
         *
         * @param code Le code dataB à rechercher
         * @return L'enum correspondant ou null si non trouvé
         */
        fun fromCode(code: String?): DataB? {
            if (code.isNullOrBlank()) return null
            return values().find { it.code == code.trim() }
        }

        /**
         * Obtient le nom lisible correspondant à une valeur dataB
         *
         * @param dataBValue La valeur dataB brute
         * @return Le nom lisible correspondant ou la valeur par défaut
         */
        fun getDisplayName(dataBValue: String?): String {
            if (dataBValue.isNullOrBlank()) {
                return DEFAULT_DISPLAY_NAME
            }

            return fromCode(dataBValue)?.displayName ?: dataBValue
        }

        /**
         * Obtient toutes les correspondances disponibles
         *
         * @return Map des correspondances code -> nom lisible
         */
        fun getAllMappings(): Map<String, String> {
            return values().associate { it.code to it.displayName }
        }

        /**
         * Vérifie si une valeur dataB a une correspondance connue
         *
         * @param dataBValue La valeur dataB à vérifier
         * @return true si une correspondance existe, false sinon
         */
        fun hasMapping(dataBValue: String?): Boolean {
            return fromCode(dataBValue) != null
        }
    }
}

/**
 * Utilitaire pour la gestion des correspondances des valeurs dataB Interface de compatibilité avec
 * l'ancienne implémentation
 *
 * @deprecated Utilisez directement l'enum DataB pour une meilleure type-safety
 *
 * Exemples d'utilisation recommandée de l'enum DataB :
 *
 * // 1. Conversion d'un code string vers l'enum val dataB = DataB.fromCode("0") // DataB.CIQUAL
 *
 * // 2. Obtenir le nom affiché val displayName = DataB.CIQUAL.displayName // "CIQUAL" val
 * displayName2 = DataB.getDisplayName("0") // "CIQUAL"
 *
 * // 3. Validation type-safe when (dataB) {
 * ```
 * ```
 * }
 *
 * // 4. Liste de toutes les valeurs disponibles val allDataB = DataB.values() // Array<DataB> val
 * allCodes = DataB.values().map { it.code } // ["0", "1", "2", "4", "5", "VF24"]
 *
 * // 5. Recherche par nom d'enum val vetFood = DataB.valueOf("VETFOOD_2024") // DataB.VETFOOD_2024
 */
object DataBMapping {

    /**
     * Obtient le nom lisible correspondant à une valeur dataB
     *
     * @param dataBValue La valeur dataB brute
     * @return Le nom lisible correspondant ou la valeur par défaut
     */
    fun getDisplayName(dataBValue: String?): String {
        return DataB.getDisplayName(dataBValue)
    }

    /**
     * Obtient toutes les correspondances disponibles
     *
     * @return Map des correspondances dataB -> nom lisible
     */
    fun getAllMappings(): Map<String, String> {
        return DataB.getAllMappings()
    }

    /**
     * Vérifie si une valeur dataB a une correspondance connue
     *
     * @param dataBValue La valeur dataB à vérifier
     * @return true si une correspondance existe, false sinon
     */
    fun hasMapping(dataBValue: String?): Boolean {
        return DataB.hasMapping(dataBValue)
    }

    /** Recharge les correspondances (maintenant inutile avec l'enum) */
    @Deprecated("Plus nécessaire avec l'enum DataB", ReplaceWith("DataB.values()"))
    fun reloadMappings() {
        // Rien à faire avec l'enum
    }
}
