package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class AlimIndic(val coef: Int, override val label: String) : Labelable {
    ALL(999, "All"),
    PED(0, "Pédiatrique"),
    NEUT(1, "Stérilisé"),
    PHYS(2, "Physiologique"),
    SEN(3, "Sénior"),
    CALM(4, "Stress félin"),
    OBES(5, "Obésité"),
    GESTATION(6, "Gestation"),
    SONDE(7, "Sonde"),
    LACT(8, "Lactation"),
    CROISSANCE(9, "Croissance"),
    DENT(11, "Hygiéne Buccodentaire"),
    DIAB(12, "Diabète"),
    INSHEP(25, "Insuffisance Hépatique"),
    HYPO(26, "Hypoallergénique"),
    ART(27, "Soutien Articulaire"),
    MRC(28, "Soutien de la fonction rénale"),
    CONV(30, "Convalescence"),
    MBAUF(32, "MBAUF"),
    URO(33, "Urolithiase"),
    DERM(34, "Affections cutanées"),
    GI(35, "Affections gastro-intestinales"),
    CAR(36, "Affections cardiaques"),
    END(37, "Affections endocriniennes"),
    IPE(38, "Insufisance pancréatique"),
    DISTRU(39, "Dissolution struvites"),
    REDSTRU(40, "Réduction struvites"),
    REDURA(41, "Réduction urates"),
    REDOXA(42, "Réduction oxalates"),
    REDCYST(43, "Réduction cystines"),
    ACT(45, "Sport"),
    AUTRE(44, "");

    companion object {
        fun byCoef(coef: Int): AlimIndic = entries.find { it.coef == coef } ?: PHYS

        fun isPresent(indic: AlimIndic): Boolean = entries.contains(indic)

        fun byName(name: String): AlimIndic =
                entries.find { it.label.equals(name, ignoreCase = true) } ?: AUTRE

        fun valuesExcept(): List<AlimIndic> = entries.filter { it != ALL }

        /**
         * Récupère une indication à partir d'une chaîne, en essayant plusieurs formats possibles.
         * Cette méthode nettoie automatiquement la chaîne d'entrée (crochets, guillemets, espaces)
         * et gère l'insensibilité à la casse.
         *
         * @param value La chaîne à convertir en indication
         * @return L'indication correspondante (AUTRE si aucune correspondance n'est trouvée)
         */
        fun getFromString(value: String): AlimIndic {
            // Nettoyer la chaîne d'entrée
            val cleanedValue = value.replace("[", "").replace("]", "").replace("\"", "").trim()

            if (cleanedValue.isEmpty()) {
                return AUTRE
            }

            // Vérifier si c'est un nom d'énumération (OBES, URO, etc.) - insensible à la casse
            try {
                val upperCaseValue = cleanedValue.uppercase()
                return valueOf(upperCaseValue)
            } catch (e: Exception) {
                // Pas un nom d'énumération valide
            }

            // Vérifier si c'est un label par nom - déjà insensible à la casse via byName
            val byLabel = byName(cleanedValue)
            if (byLabel != AUTRE) {
                return byLabel
            }

            // Vérifier si c'est un coefficient numérique
            val coef = cleanedValue.toIntOrNull()
            if (coef != null) {
                return byCoef(coef)
            }

            // Aucune correspondance trouvée
            return AUTRE
        }
    }

    override fun toString() = label
}
