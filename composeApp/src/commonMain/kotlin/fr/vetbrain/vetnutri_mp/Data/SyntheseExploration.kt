package fr.vetbrain.vetnutri_mp.Data

/**
 * Bilan d'une combinaison d'aliments sur toute la grille référentiel × poids × K : dans combien
 * de cases elle donne une ration conforme, sur quelle plage de poids et de K, et quels seuils la
 * limitent ailleurs.
 */
data class SyntheseCombinaison(
        val combinaison: Int,
        /** Noms des aliments, dans l'ordre des rôles. */
        val aliments: List<String>,
        val roles: List<RoleExploration>,
        /** Nombre de cases (référentiel × poids × K) où la combinaison a été calculée. */
        val cases: Int,
        /** Cases où la combinaison respecte tous les seuils (y compris sous réserve). */
        val casesConformes: Int,
        /** Dont cases conformes seulement sous réserve de données manquantes. */
        val casesSousReserve: Int,
        /** Poids (kg) où la combinaison est conforme pour au moins un K et un référentiel. */
        val poidsConformes: List<Double>,
        /** K où la combinaison est conforme pour au moins un poids et un référentiel. */
        val kConformes: List<Double>,
        /** Seuils les plus souvent manqués par la combinaison (seuil, nombre de cases). */
        val seuilsLimitants: List<Pair<String, Int>>,
        /** Moyenne des seuils manqués sur les cases évaluables (null si aucune). */
        val moyenneSeuilsManques: Double?
) {
    val tauxConformite: Double get() = if (cases == 0) 0.0 else casesConformes.toDouble() / cases
}

/** Seuil non respecté et nombre de scénarios où il est manqué. */
data class SeuilFrequent(val seuil: String, val scenarios: Int)

/** Synthèse d'un résultat d'exploration, calculée une fois après chaque calcul. */
data class SyntheseExploration(
        val combinaisons: List<SyntheseCombinaison>,
        val seuilsFrequents: List<SeuilFrequent>,
        /** Scénarios non conformes évalués (dénominateur de [seuilsFrequents]). */
        val scenariosNonConformes: Int,
        val scenariosConformes: Int,
        val casesEquilibrables: Int,
        val cases: Int
) {
    companion object {
        val VIDE = SyntheseExploration(emptyList(), emptyList(), 0, 0, 0, 0)
    }
}

private fun ScenarioExploration.estConforme() =
        statut == StatutScenario.CONFORME || statut == StatutScenario.CONFORME_SOUS_RESERVE

private fun ScenarioExploration.estEvaluable() =
        statut != StatutScenario.CIBLE_ABSENTE && statut != StatutScenario.NON_CALCULABLE

/**
 * Classe les combinaisons de la meilleure à la moins bonne : nombre de cases conformes, puis
 * cases conformes sans réserve, puis moyenne des seuils manqués.
 */
fun synthetiserExploration(resultat: ResultatExploration): SyntheseExploration {
    val combinaisons =
            resultat.scenarios.groupBy { it.combinaison }.map { (combinaison, scenarios) ->
                val exemple = scenarios.first()
                val conformes = scenarios.filter { it.estConforme() }
                val evaluables = scenarios.filter { it.estEvaluable() }
                SyntheseCombinaison(
                        combinaison = combinaison,
                        aliments = exemple.ration.alimentMutableList.map { it.aliment?.nom ?: "?" },
                        roles = exemple.roles,
                        cases = scenarios.size,
                        casesConformes = conformes.size,
                        casesSousReserve = conformes.count { it.statut == StatutScenario.CONFORME_SOUS_RESERVE },
                        poidsConformes = conformes.map { it.poids }.distinct().sorted(),
                        kConformes = conformes.map { it.k }.distinct().sorted(),
                        seuilsLimitants = compterSeuils(scenarios).take(3).map { it.seuil to it.scenarios },
                        moyenneSeuilsManques =
                                if (evaluables.isEmpty()) null
                                else evaluables.sumOf { it.seuilsManques.size }.toDouble() / evaluables.size
                )
            }
                    .sortedWith(
                            compareByDescending<SyntheseCombinaison> { it.casesConformes }
                                    .thenBy { it.casesSousReserve }
                                    .thenBy { it.moyenneSeuilsManques ?: Double.MAX_VALUE }
                                    .thenBy { it.combinaison }
                    )
    val nonConformes = resultat.scenarios.filter { it.estEvaluable() && !it.estConforme() }
    return SyntheseExploration(
            combinaisons = combinaisons,
            seuilsFrequents = compterSeuils(nonConformes),
            scenariosNonConformes = nonConformes.size,
            scenariosConformes = resultat.scenarios.count { it.estConforme() },
            casesEquilibrables = resultat.cases.count { it.zone == ZoneEquilibre.EQUILIBRABLE },
            cases = resultat.cases.size
    )
}

/** Seuils manqués, du plus fréquent au moins fréquent (égalité : ordre alphabétique). */
fun compterSeuils(scenarios: List<ScenarioExploration>): List<SeuilFrequent> =
        scenarios.flatMap { it.seuilsManques.distinct() }
                .groupingBy { it }
                .eachCount()
                .map { SeuilFrequent(it.key, it.value) }
                .sortedWith(compareByDescending<SeuilFrequent> { it.scenarios }.thenBy { it.seuil })

/**
 * Cases de la carte limitées à une combinaison ([combinaison] null = toutes les combinaisons).
 * La zone d'une case devient alors celle de cette seule combinaison.
 */
fun casesPourCombinaison(resultat: ResultatExploration, combinaison: Int?): List<CaseEquilibre> =
        if (combinaison == null) resultat.cases
        else ExplorateurMultiration.construireCases(resultat.scenarios.filter { it.combinaison == combinaison })
