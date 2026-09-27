package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/** Une ligne du tableau de synthèse d'un plan évolutif : un ingrédient et sa masse par étape. */
data class LigneSynthese(
        val refAlimUnif: String,
        val nom: String,
        val aliment: AlimentEv?,
        /** Masse (g/j) par ration du plan, dans l'ordre de [PlanEvolutif.planTrie] ; null = absent. */
        val quantites: List<Double?>
)

/**
 * Logique pure des plans évolutifs. Un plan est rangé sous une ration existante (la ration
 * parente, calculée au poids de la consultation) : ses étapes sont des rations enfants, chacune
 * avec son propre poids et ses propres variables d'énergie, et un nom fixe qui en découle.
 * Ne touche ni la base ni l'état : les fonctions renvoient de nouvelles rations à persister.
 */
object PlanEvolutif {

    /** Étapes rangées sous [parent] (ordre de stockage). */
    fun etapesDe(consultation: ConsultationEv, parent: Ration): List<Ration> =
            consultation.rations.filter { it.refRationParente == parent.uuid }

    /** Vrai si la ration porte un plan (au moins une étape rangée dessous). */
    fun aUnPlan(consultation: ConsultationEv, ration: Ration): Boolean =
            consultation.rations.any { it.refRationParente == ration.uuid }

    /** Rations principales (hors étapes), celles qu'on liste et qu'on sélectionne. */
    fun rationsPrincipales(consultation: ConsultationEv): List<Ration> =
            consultation.rations.filter { it.refRationParente == null }

    /** Ration parente d'une étape, ou la ration elle-même si c'est une ration principale. */
    fun parentDe(consultation: ConsultationEv, ration: Ration): Ration =
            ration.refRationParente?.let { id -> consultation.rations.firstOrNull { it.uuid == id } }
                    ?: ration

    /**
     * Statut de couleur d'une ration dans les graphiques : une étape hérite toujours du statut
     * actuel/proposé de sa ration parente, afin que tout le plan ait le même repère visuel.
     */
    fun estActuelle(consultation: ConsultationEv, ration: Ration): Boolean =
            parentDe(consultation, ration).actual

    /** Rations du plan : la ration parente et ses étapes (ordre de stockage). */
    fun membresDuPlan(consultation: ConsultationEv, parent: Ration): List<Ration> =
            listOf(parent) + etapesDe(consultation, parent)

    /** Variables dont la valeur diffère d'une ration du plan à l'autre (tri et libellés). */
    fun variablesDistinctives(consultation: ConsultationEv, parent: Ration): List<VariableKind> {
        val membres = membresDuPlan(consultation, parent)
        if (membres.size < 2) return emptyList()
        val valeursParEtape =
                membres.map { etape ->
                    VariablesEtape.variablesFusionnees(consultation, etape)
                            .mapNotNull { sv -> sv.variable?.let { it to sv.varue } }
                            .toMap()
                }
        return valeursParEtape
                .flatMap { it.keys }
                .distinct()
                .filter { kind -> valeursParEtape.map { it[kind] }.distinct().size > 1 }
                .sortedBy { it.label }
    }

    /**
     * Ration parente et étapes, triées par poids croissant puis par variables distinctives : ce
     * sont les colonnes de la synthèse et de l'ordonnance.
     */
    fun planTrie(consultation: ConsultationEv, parent: Ration): List<Ration> {
        val distinctives = variablesDistinctives(consultation, parent)
        val cle: (Ration) -> List<Double> = { etape ->
            val vars =
                    VariablesEtape.variablesFusionnees(consultation, etape).associate {
                        it.variable to (it.varue ?: 0.0)
                    }
            listOf(VariablesEtape.poidsEtape(consultation, etape) ?: 0.0) +
                    distinctives.map { vars[it] ?: 0.0 }
        }
        return membresDuPlan(consultation, parent)
                .sortedWith(
                        Comparator { a, b ->
                            val ka = cle(a)
                            val kb = cle(b)
                            ka.zip(kb).map { (x, y) -> x.compareTo(y) }.firstOrNull { it != 0 }
                                    ?: 0
                        }
                )
    }

    /** Libellé de colonne : « 8.0 kg · D 12.0 » (mention « actuel » ajoutée par l'appelant). */
    fun libelleEtape(
            consultation: ConsultationEv,
            etape: Ration,
            distinctives: List<VariableKind> =
                    variablesDistinctives(consultation, parentDe(consultation, etape))
    ): String {
        val poids = VariablesEtape.poidsEtape(consultation, etape)
        val base = poids?.let { "${TextUtils.formatDecimal(it, 1)} kg" } ?: "? kg"
        if (distinctives.isEmpty()) return base
        val vars =
                VariablesEtape.variablesFusionnees(consultation, etape).associate {
                    it.variable to it.varue
                }
        val suffixe =
                distinctives.mapNotNull { kind ->
                    vars[kind]?.let { "${kind.label} ${TextUtils.formatDecimal(it, 1)}" }
                }
        return (listOf(base) + suffixe).joinToString(" · ")
    }

    /** Vrai pour la ration parente du plan (calculée au poids de la consultation). */
    fun estRationParente(etape: Ration): Boolean = etape.refRationParente == null

    /**
     * Nom fixe d'une étape, déduit de ses propres données et traduit : « Étape 8.0 kg · AW 30.0 »,
     * ou « Étape poids réel » quand l'étape n'a pas de poids propre.
     */
    fun nomAutomatique(poids: Double?, suppVarp: List<SupplementalvariableP>): String {
        val base =
                poids?.let { "${TextUtils.formatDecimal(it, 1)} kg" }
                        ?: translate(LocalizationKeys.Evolutive.REAL_WEIGHT_NAME)
        val vars =
                suppVarp.mapNotNull { sv ->
                    sv.variable?.let { "${it.label} ${TextUtils.formatDecimal(sv.varue ?: 0.0, 1)}" }
                }
        return (listOf(translate(LocalizationKeys.Evolutive.AUTO_STEP_NAME, base)) + vars)
                .joinToString(" · ")
    }

    /**
     * Nouvelle étape sous [parent] : copie de ses aliments (nouveaux UUID), poids et variables
     * propres, nom fixe.
     */
    fun nouvelleEtape(
            parent: Ration,
            poids: Double?,
            suppVarp: List<SupplementalvariableP>
    ): Ration {
        val uuid = genUUID()
        return Ration(
                uuid = uuid,
                idConsult = parent.idConsult,
                name = nomAutomatique(poids, suppVarp),
                coef = parent.coef,
                actual = false,
                number = parent.number,
                espece = parent.espece,
                recette = false,
                description = "",
                alimentMutableList =
                        parent.alimentMutableList
                                .map { it.copy(uuid = genUUID(), refRation = uuid) }
                                .toMutableList(),
                etapeEvolutive = true,
                poids = poids,
                refRationParente = parent.uuid,
                suppVarp = suppVarp.toMutableList()
        )
    }

    /**
     * Ajoute aux autres rations du même plan (parente comprise) les aliments de [source] qui leur
     * manquent, à 0 g.
     *
     * @return toutes les rations de la consultation mises à jour et le nombre d'aliments ajoutés
     */
    fun propagerAliments(consultation: ConsultationEv, source: Ration): Pair<List<Ration>, Int> {
        val parent = parentDe(consultation, source)
        val membres = membresDuPlan(consultation, parent).map { it.uuid }.toSet()
        var ajouts = 0
        val rations =
                consultation.rations.map { ration ->
                    if (ration.uuid !in membres || ration.uuid == source.uuid) return@map ration
                    val presents = ration.alimentMutableList.mapNotNull { it.refAlimUnif }.toSet()
                    val manquants =
                            source.alimentMutableList.filter {
                                it.refAlimUnif != null && it.refAlimUnif !in presents
                            }
                    if (manquants.isEmpty()) return@map ration
                    ajouts += manquants.size
                    ration.copy(
                            alimentMutableList =
                                    (ration.alimentMutableList +
                                                    manquants.map {
                                                        it.copy(
                                                                uuid = genUUID(),
                                                                refRation = ration.uuid,
                                                                quantite = 0.0,
                                                                proportion = 0.0
                                                        )
                                                    })
                                            .toMutableList()
                    )
                }
        return rations to ajouts
    }

    /** Tableau ingrédients × rations du plan (colonnes dans l'ordre de [planTrie]). */
    fun matriceSynthese(consultation: ConsultationEv, parent: Ration): List<LigneSynthese> {
        val etapes = planTrie(consultation, parent)
        val ordre = LinkedHashMap<String, AlimentRation>()
        etapes.forEach { etape ->
            etape.alimentMutableList.forEach { a ->
                val ref = a.refAlimUnif ?: return@forEach
                if (ref !in ordre) ordre[ref] = a
            }
        }
        return ordre.map { (ref, exemple) ->
            LigneSynthese(
                    refAlimUnif = ref,
                    nom = exemple.aliment?.nom ?: ref,
                    aliment = exemple.aliment,
                    quantites =
                            etapes.map { etape ->
                                etape.alimentMutableList
                                        .filter { it.refAlimUnif == ref }
                                        .takeIf { it.isNotEmpty() }
                                        ?.sumOf { it.quantite }
                            }
            )
        }
    }

    /** Vrai si toutes les étapes de plan de la consultation (au moins une) définissent la variable. */
    fun variableDansToutesLesEtapes(consultation: ConsultationEv, variable: VariableKind): Boolean {
        val etapes = consultation.etapesEvolutives
        return etapes.isNotEmpty() &&
                etapes.all { etape -> etape.suppVarp.any { it.variable == variable } }
    }

    /** Remplace la masse d'un ingrédient dans une étape (première occurrence de l'aliment). */
    fun avecQuantite(etape: Ration, refAlimUnif: String, quantite: Double): Ration {
        val index = etape.alimentMutableList.indexOfFirst { it.refAlimUnif == refAlimUnif }
        if (index < 0) return etape
        val aliments = etape.alimentMutableList.toMutableList()
        aliments[index] = aliments[index].copy(quantite = quantite)
        return etape.copy(alimentMutableList = aliments)
    }
}
