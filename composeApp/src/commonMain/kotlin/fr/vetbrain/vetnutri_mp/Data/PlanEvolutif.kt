package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/** Une ligne du tableau de synthèse d'un plan évolutif : un ingrédient et sa masse par étape. */
data class LigneSynthese(
        val refAlimUnif: String,
        val nom: String,
        val aliment: AlimentEv?,
        /** Masse (g/j) par étape, dans l'ordre de [PlanEvolutif.etapesTriees] ; null = absent. */
        val quantites: List<Double?>
)

/**
 * Logique pure du plan évolutif d'une consultation (une ration par étape). Ne touche ni la base
 * ni l'état : les fonctions renvoient de nouvelles rations/listes à persister par le ViewModel.
 */
object PlanEvolutif {

    /** Variables dont la valeur diffère d'une étape à l'autre (servent au tri et aux libellés). */
    fun variablesDistinctives(consultation: ConsultationEv): List<VariableKind> {
        val etapes = consultation.etapesEvolutives
        if (etapes.size < 2) return emptyList()
        val valeursParEtape =
                etapes.map { etape ->
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

    /** Étapes triées par poids croissant, puis par valeurs des variables distinctives. */
    fun etapesTriees(consultation: ConsultationEv): List<Ration> {
        val distinctives = variablesDistinctives(consultation)
        val cle: (Ration) -> List<Double> = { etape ->
            val vars =
                    VariablesEtape.variablesFusionnees(consultation, etape).associate {
                        it.variable to (it.varue ?: 0.0)
                    }
            listOf(VariablesEtape.poidsEtape(consultation, etape) ?: 0.0) +
                    distinctives.map { vars[it] ?: 0.0 }
        }
        return consultation.etapesEvolutives.sortedWith { a, b ->
            val ka = cle(a)
            val kb = cle(b)
            ka.zip(kb).map { (x, y) -> x.compareTo(y) }.firstOrNull { it != 0 } ?: 0
        }
    }

    /** Libellé d'étape : « 8 kg · D 12 » (sans mention « actuel », ajoutée par l'appelant). */
    fun libelleEtape(
            consultation: ConsultationEv,
            etape: Ration,
            distinctives: List<VariableKind> = variablesDistinctives(consultation)
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

    /** Vrai si l'étape est au poids réel de la consultation. */
    fun estPoidsReel(etape: Ration): Boolean = etape.poids == null

    /** L'étape au poids réel doit toujours exister : la dernière ne peut pas être supprimée. */
    fun peutSupprimer(consultation: ConsultationEv, etape: Ration): Boolean =
            !estPoidsReel(etape) || consultation.etapesEvolutives.count { estPoidsReel(it) } > 1

    /** Copie d'une ration en étape (nouveaux UUID pour la ration et ses aliments). */
    fun copierEnEtape(
            modele: Ration?,
            idConsult: String,
            poids: Double?,
            suppVarp: List<SupplementalvariableP> = modele?.suppVarp ?: emptyList()
    ): Ration {
        val uuid = genUUID()
        return Ration(
                uuid = uuid,
                idConsult = idConsult,
                name = modele?.name ?: "",
                coef = modele?.coef ?: 1.0,
                actual = false,
                number = modele?.number ?: 1,
                espece = modele?.espece,
                recette = false,
                description = modele?.description ?: "",
                alimentMutableList =
                        modele?.alimentMutableList
                                ?.map { it.copy(uuid = genUUID(), refRation = uuid) }
                                ?.toMutableList()
                                ?: mutableListOf(),
                etapeEvolutive = true,
                poids = poids,
                suppVarp = suppVarp.toMutableList()
        )
    }

    /** Première étape du plan : au poids réel, copiée depuis la ration de départ (ou vide). */
    fun creerPlan(consultation: ConsultationEv, rationDepart: Ration?): Ration =
            copierEnEtape(rationDepart, consultation.uuid, poids = null, suppVarp = emptyList())

    /**
     * Ajoute à chaque autre étape les aliments de [source] qui lui manquent, à 0 g.
     *
     * @return les étapes mises à jour (toutes les rations de la consultation) et le nombre
     * d'aliments ajoutés.
     */
    fun propagerAliments(consultation: ConsultationEv, source: Ration): Pair<List<Ration>, Int> {
        var ajouts = 0
        val rations =
                consultation.rations.map { ration ->
                    if (!ration.etapeEvolutive || ration.uuid == source.uuid) return@map ration
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

    /** Tableau ingrédients × étapes (étapes dans l'ordre de [etapesTriees]). */
    fun matriceSynthese(consultation: ConsultationEv): List<LigneSynthese> {
        val etapes = etapesTriees(consultation)
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

    /** Remplace la masse d'un ingrédient dans une étape (première occurrence de l'aliment). */
    fun avecQuantite(etape: Ration, refAlimUnif: String, quantite: Double): Ration {
        val index = etape.alimentMutableList.indexOfFirst { it.refAlimUnif == refAlimUnif }
        if (index < 0) return etape
        val aliments = etape.alimentMutableList.toMutableList()
        aliments[index] = aliments[index].copy(quantite = quantite)
        return etape.copy(alimentMutableList = aliments)
    }
}
