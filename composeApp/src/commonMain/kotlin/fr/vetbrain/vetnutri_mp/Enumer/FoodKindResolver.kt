package fr.vetbrain.vetnutri_mp.Enumer

/**
 * Résolution robuste du type d'aliment (FoodKind) depuis des valeurs legacy/variées.
 * Gère:
 * - les noms d'énum (ex: COMPLEMENTAIRE)
 * - les labels (ex: complementary, household)
 * - les synonymes usuels (ex: ménager, home cooked, cru, raw)
 * - les codes numériques historiques (coef)
 */
object FoodKindResolver {
    private val synonymes: Map<String, FoodKind> = buildMap {
        // COMPLET
        put("complet", FoodKind.COMPLET)
        put("complete", FoodKind.COMPLET)
        put("complète", FoodKind.COMPLET)
        put("complete food", FoodKind.COMPLET)
        put("COMPLET", FoodKind.COMPLET)

        // COMPLEMENTAIRE
        put("complementaire", FoodKind.COMPLEMENTAIRE)
        put("complémentaire", FoodKind.COMPLEMENTAIRE)
        put("complement", FoodKind.COMPLEMENTAIRE)
        put("complément", FoodKind.COMPLEMENTAIRE)
        put("complementary", FoodKind.COMPLEMENTAIRE)
        put("supplement", FoodKind.COMPLEMENTAIRE)
        put("supplementary", FoodKind.COMPLEMENTAIRE)   
        put("COMPLEMENTAIRE", FoodKind.COMPLEMENTAIRE)
        // MEN (ménager / fait maison)
        put("men", FoodKind.MEN)
        put("menager", FoodKind.MEN)
        put("ménager", FoodKind.MEN)
        put("menagere", FoodKind.MEN)
        put("ménagère", FoodKind.MEN)
        put("maison", FoodKind.MEN)
        put("fait maison", FoodKind.MEN)
        put("home cooked", FoodKind.MEN)
        put("home-cooked", FoodKind.MEN)
        put("homemade", FoodKind.MEN)
        put("household", FoodKind.MEN)
        put("MEN", FoodKind.MEN)
        // BARF
        put("barf", FoodKind.BARF)
        put("raw", FoodKind.BARF)
        put("cru", FoodKind.BARF)
        put("BARF", FoodKind.BARF)
        // ALL / divers
        put("all", FoodKind.ALL)
        put("tous", FoodKind.ALL)
        put("toutes", FoodKind.ALL)
    }

    /**
     * Résout un FoodKind depuis une chaîne brute hétérogène.
     * @param valeurBrute Chaîne potentiellement issue d'un ancien JSON (noms, labels, synonymes, chiffres)
     * @return FoodKind résolu, ou null si non résolu
     */
    fun resoudreFoodKindBrut(valeurBrute: String?): FoodKind? {
        if (valeurBrute.isNullOrBlank()) return null
        val s: String = valeurBrute.trim().replace('-', ' ').replace('_', ' ').lowercase()
        // 1) Nom d'énum exact (insensible à la casse)
        FoodKind.entries.firstOrNull { it.name.equals(s, ignoreCase = true) }?.let { return it }
        // 2) Label exact (insensible à la casse)
        FoodKind.entries.firstOrNull { it.label.equals(s, ignoreCase = true) }?.let { return it }
        // 3) Synonymes usuels
        synonymes[s]?.let { return it }
        // 4) Ancien code numérique (coef)
        s.toIntOrNull()?.let { code -> return FoodKind.byCoef(code) }
        // 5) Heuristiques simples
        if (s.contains("complement", true) || s.contains("complément", true)) return FoodKind.COMPLEMENTAIRE
        if (s.contains("complete", true) || s.contains("complet", true)) return FoodKind.COMPLET
        if (s.contains("home", true) || s.contains("maison", true) || s.contains("ménag", true) || s.contains("menag", true)) return FoodKind.MEN
        if (s.contains("raw", true) || s.contains("cru", true) || s.contains("barf", true)) return FoodKind.BARF
        return null
    }
}


