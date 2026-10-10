package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Mode troupeau : un groupe d'animaux décrit par types (effectif, poids), suivi par des
 * consultations de groupe. Pour chaque consultation, chaque type a son référentiel (et son K) ; la
 * ration est saisie en quantités pour l'ensemble du groupe et répartie entre les types au prorata
 * de leur besoin énergétique (effectif × besoin d'un animal).
 */

/** Type d'animaux du groupe ; effectif et poids servent de valeurs par défaut aux consultations. */
@Serializable
data class TypeAnimalTroupeau(
        val id: String = genUUID(),
        val nom: String = "",
        val nombre: Int = 1,
        /** Poids moyen d'un animal (kg). */
        val poids: Double = 0.0
)

/** Paramètres d'un type pour une consultation (les effectifs et poids peuvent évoluer). */
@Serializable
data class ParametresTypeTroupeau(
        val typeId: String,
        val nombre: Int = 1,
        val poids: Double = 0.0,
        val referenceId: String? = null,
        /** Besoin énergétique = besoin standard du référentiel × K. */
        val k: Double = 1.0,
        /** Variables des équations du référentiel (AW, L, wG...), par label de VariableKind. */
        val variables: Map<String, Double> = emptyMap()
)

/** Ligne de la ration du groupe : quantité journalière pour l'ensemble des animaux (g). */
@Serializable
data class LigneRationTroupeau(
        val id: String = genUUID(),
        val alimentId: String,
        /** Nom au moment de l'ajout, affiché si l'aliment a disparu du catalogue. */
        val nom: String = "",
        val quantite: Double = 0.0
)

@Serializable
data class ConsultationTroupeau(
        val id: String = genUUID(),
        val date: Long = 0L,
        val titre: String = "",
        val notes: String = "",
        val types: List<ParametresTypeTroupeau> = emptyList(),
        val ration: List<LigneRationTroupeau> = emptyList()
) {
    /** Paramètres du type : ceux de la consultation, sinon les valeurs par défaut du troupeau. */
    fun parametres(type: TypeAnimalTroupeau): ParametresTypeTroupeau =
            types.firstOrNull { it.typeId == type.id }
                    ?: ParametresTypeTroupeau(typeId = type.id, nombre = type.nombre, poids = type.poids)

    fun avecParametres(parametres: ParametresTypeTroupeau): ConsultationTroupeau =
            copy(types = types.filterNot { it.typeId == parametres.typeId } + parametres)
}

/** Contenu JSON d'un troupeau (table HERDS). Identifiants seulement pour référentiels et aliments. */
@Serializable
data class ContenuTroupeau(
        val version: Int = 1,
        val types: List<TypeAnimalTroupeau> = emptyList(),
        val consultations: List<ConsultationTroupeau> = emptyList()
) {
    fun versJson(): String = format.encodeToString(serializer(), this)

    companion object {
        private val format = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun depuisJson(json: String): ContenuTroupeau =
                if (json.isBlank()) ContenuTroupeau() else format.decodeFromString(serializer(), json)
    }
}

data class Troupeau(
        val uuid: String = genUUID(),
        val nom: String,
        /** Nom de l'enum Espece (BOVIN, CHIEN...). */
        val espece: String,
        val contenu: ContenuTroupeau = ContenuTroupeau(),
        val updatedAt: Long = 0L
) {
    val effectifParDefaut: Int get() = contenu.types.sumOf { it.nombre }

    /**
     * Nouvelle consultation : reprend les paramètres de la plus récente (référentiels, K, ration),
     * effectifs et poids pris dans la définition du troupeau.
     */
    fun nouvelleConsultation(date: Long, titre: String): ConsultationTroupeau {
        val precedente = contenu.consultations.maxByOrNull { it.date }
        return ConsultationTroupeau(
                date = date,
                titre = titre,
                types =
                        contenu.types.map { type ->
                            val avant = precedente?.types?.firstOrNull { it.typeId == type.id }
                            ParametresTypeTroupeau(
                                    typeId = type.id,
                                    nombre = type.nombre,
                                    poids = type.poids,
                                    referenceId = avant?.referenceId,
                                    k = avant?.k ?: 1.0,
                                    variables = avant?.variables ?: emptyMap()
                            )
                        },
                ration = precedente?.ration?.map { it.copy(id = genUUID()) } ?: emptyList()
        )
    }

    /** Retirer un type le retire aussi des consultations. */
    fun sansType(typeId: String): Troupeau =
            copy(
                    contenu =
                            contenu.copy(
                                    types = contenu.types.filterNot { it.id == typeId },
                                    consultations =
                                            contenu.consultations.map { c ->
                                                c.copy(types = c.types.filterNot { it.typeId == typeId })
                                            }
                            )
            )
}

// --- Analyse ----------------------------------------------------------------------------------

/** Apport d'un nutriment pour un animal d'un type, avec les seuils absolus de son référentiel. */
data class LigneAnalyseTroupeau(
        val nutriment: Nutrient,
        val valeur: ValeurNutritionnelle,
        /** Seuils dans l'unité de l'apport (ratios : valeur brute), null si absents. */
        val seuils: Map<Reflevel, Double>,
        /** null = conforme (ou pas de seuil). */
        val conformite: ConformiteResult?
)

data class AnalyseTypeTroupeau(
        val type: TypeAnimalTroupeau,
        val parametres: ParametresTypeTroupeau,
        val reference: ReferenceEv?,
        val poidsMetabolique: Double?,
        /** Besoin énergétique standard d'un animal (kcal). */
        val besoinStandard: Double?,
        /** Besoin énergétique d'un animal = standard × K (kcal). */
        val besoinEnergetique: Double?,
        /** Part de la ration du groupe attribuée à ce type (0..1), null si non répartissable. */
        val part: Double?,
        /** Ration d'un animal du type (quantités du groupe × besoin de l'animal / besoin du groupe). */
        val rationParAnimal: Ration?,
        val energieParAnimal: Double?,
        val lignes: List<LigneAnalyseTroupeau> = emptyList(),
        val message: String? = null
) {
    /** Couverture du besoin énergétique d'un animal (%). */
    val couvertureEnergie: Double?
        get() =
                if (energieParAnimal != null && besoinEnergetique != null && besoinEnergetique > 0.0)
                        energieParAnimal / besoinEnergetique * 100.0
                else null

    val nonConformes: List<LigneAnalyseTroupeau>
        get() = lignes.filter { it.conformite != null && it.nutriment != NutrientMain.ENERGIE }
}

data class AnalyseTroupeau(
        val types: List<AnalyseTypeTroupeau>,
        val effectif: Int,
        /** Somme des besoins énergétiques de tous les animaux (kcal/j), null si incomplet. */
        val besoinGroupe: Double?,
        /** Énergie apportée au groupe (somme effectif × énergie par animal), null si incomplet. */
        val energieGroupe: Double?,
        /** Quantité totale de la ration du groupe (g). */
        val quantiteTotale: Double,
        /** Ce qui empêche la répartition (référentiel manquant, BEE incalculable...). */
        val problemes: List<String>
) {
    /** Facteur à appliquer aux quantités pour couvrir exactement le besoin énergétique du groupe. */
    val facteurAjustementEnergie: Double?
        get() =
                if (besoinGroupe != null && energieGroupe != null && energieGroupe > 0.0) besoinGroupe / energieGroupe
                else null
}

/** Variables utilisées par les équations BW et BEE du référentiel, hors variables calculées. */
fun variablesEquationsTroupeau(reference: ReferenceEv?): List<String> {
    if (reference == null) return emptyList()
    val calculees =
            setOf(VariableKind.BW.label, VariableKind.MW.label, VariableKind.BEE.label, VariableKind.BE.label)
    return listOfNotNull(reference.equationBW, reference.equationBEE)
            .flatMap { eq -> eq.equationScript.takeIf { it.isNotBlank() }?.let { ExpressionEvaluator.extraireVariables(it) } ?: emptyList() }
            .filter { it !in calculees }
            .distinct()
}

/** Variables saisies converties pour CalculMetabolique (seules les VariableKind connues). */
private fun variablesSupplementaires(parametres: ParametresTypeTroupeau): List<SupplementalvariableP> =
        parametres.variables.mapNotNull { (label, valeur) ->
            VariableKind.entries.firstOrNull { it.label == label }?.let { SupplementalvariableP(it, valeur) }
        }

/** Seuils absolus d'un nutriment dans le référentiel (même unité que l'apport). */
fun seuilsTroupeau(
        reference: ReferenceEv,
        nutriment: Nutrient,
        besoinStandard: Double?,
        poids: Double,
        poidsMetabolique: Double?
): Map<Reflevel, Double> {
    val ratio = estNutrimentAnalysisRatio(nutriment)
    return listOf(Reflevel.MIN, Reflevel.OPTIMIN, Reflevel.OPTIMAX, Reflevel.MAX)
            .mapNotNull { niveau ->
                if (!reference.contientNutriment(nutriment, niveau)) return@mapNotNull null
                val valeur = reference.obtenirNutriment(nutriment, niveau)
                val unite = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutriment, niveau))
                val absolu =
                        if (ratio || unite == UnitReqEnum.RATIO) valeur
                        else calculerBesoinAbsolu(valeur, unite, besoinStandard, poids, poidsMetabolique)
                absolu?.takeIf { it.isFinite() }?.let { niveau to it }
            }
            .toMap()
}

/** Nutriments ayant au moins un seuil dans le référentiel (hors énergie). */
private fun nutrimentsAvecSeuil(reference: ReferenceEv): List<Nutrient> =
        (reference.getRefMapMin().keys + reference.getRefMapOMin().keys +
                        reference.getRefMapMax().keys + reference.getRefMapOMax().keys)
                .filter { it != NutrientMain.ENERGIE }
                .distinctBy { it.label }

/**
 * Analyse d'une consultation de troupeau.
 *
 * Répartition : la part du type i est n_i × B_i / Σ n_j × B_j (B = besoin énergétique d'un
 * animal) ; un animal du type i reçoit donc la quantité du groupe × B_i / Σ n_j × B_j de chaque
 * aliment. Chaque ration individuelle est analysée avec le référentiel de son type, avec les mêmes
 * fonctions que l'analyse de ration d'une consultation (valeurs nutritionnelles, conformité).
 */
class AnalyseurTroupeau(private val equationRepository: EquationRepository) {

    suspend fun analyser(
            troupeau: Troupeau,
            consultation: ConsultationTroupeau,
            references: Map<String, ReferenceEv>,
            aliments: Map<String, AlimentEv>
    ): AnalyseTroupeau {
        val problemes = mutableListOf<String>()
        val quantiteTotale = consultation.ration.sumOf { it.quantite.coerceAtLeast(0.0) }
        val manquants = consultation.ration.filter { it.alimentId !in aliments }
        if (manquants.isNotEmpty())
                problemes += "Aliments introuvables (ignorés) : ${manquants.joinToString { it.nom.ifBlank { it.alimentId } }}."

        // 1. Besoin énergétique d'un animal de chaque type
        data class Besoin(
                val type: TypeAnimalTroupeau,
                val parametres: ParametresTypeTroupeau,
                val reference: ReferenceEv?,
                val poidsMetabolique: Double?,
                val besoinStandard: Double?,
                val message: String?
        ) {
            val besoin: Double? get() = besoinStandard?.let { it * parametres.k }
        }
        val besoins =
                troupeau.contenu.types.map { type ->
                    val p = consultation.parametres(type)
                    val reference = p.referenceId?.let { references[it] }
                    val nom = type.nom.ifBlank { "Type sans nom" }
                    when {
                        p.nombre <= 0 -> Besoin(type, p, reference, null, null, "Effectif nul : ce type ne reçoit rien.")
                        p.referenceId == null -> Besoin(type, p, null, null, null, "Choisir un référentiel pour « $nom ».")
                        reference == null -> Besoin(type, p, null, null, null, "Référentiel de « $nom » introuvable.")
                        p.poids <= 0.0 -> Besoin(type, p, reference, null, null, "Poids de « $nom » à renseigner.")
                        p.k <= 0.0 -> Besoin(type, p, reference, null, null, "K de « $nom » doit être positif.")
                        else -> {
                            val vars = variablesSupplementaires(p)
                            val bee = CalculMetabolique.besoinEnergetiqueStandard(p.poids, reference, vars)
                            val mw = CalculMetabolique.poidsMetabolique(p.poids, reference, vars)
                            if (bee == null || !bee.isFinite() || bee <= 0.0)
                                    Besoin(type, p, reference, mw, null, "Équation BEE absente ou invalide dans « ${reference.nom} ».")
                            else Besoin(type, p, reference, mw, bee, null)
                        }
                    }
                }
        val actifs = besoins.filter { it.parametres.nombre > 0 }
        actifs.mapNotNull { it.message }.let { problemes += it }
        val besoinGroupe =
                if (actifs.isNotEmpty() && actifs.all { it.besoin != null }) actifs.sumOf { it.parametres.nombre * it.besoin!! }
                else null
        if (troupeau.contenu.types.isEmpty()) problemes += "Ajouter au moins un type d'animaux au troupeau."
        else if (actifs.isEmpty()) problemes += "Aucun animal : tous les effectifs sont nuls."

        // 2. Ration d'un animal de chaque type et analyse avec son référentiel
        val analyses =
                besoins.map { b ->
                    val besoin = b.besoin
                    if (besoinGroupe == null || besoinGroupe <= 0.0 || besoin == null || b.reference == null || b.parametres.nombre <= 0) {
                        AnalyseTypeTroupeau(
                                b.type, b.parametres, b.reference, b.poidsMetabolique, b.besoinStandard, besoin,
                                part = if (b.parametres.nombre <= 0) 0.0 else null,
                                rationParAnimal = null,
                                energieParAnimal = null,
                                message = b.message ?: "Répartition impossible tant que les besoins de tous les types ne sont pas calculables."
                        )
                    } else {
                        val facteur = besoin / besoinGroupe
                        val ration = rationParAnimal(b.type, consultation, aliments, facteur, b.reference)
                        analyserType(b.type, b.parametres, b.reference, b.poidsMetabolique, b.besoinStandard, besoin,
                                b.parametres.nombre * facteur, ration)
                    }
                }

        val energieGroupe =
                if (analyses.any { it.parametres.nombre > 0 } && analyses.filter { it.parametres.nombre > 0 }.all { it.energieParAnimal != null })
                        analyses.filter { it.parametres.nombre > 0 }.sumOf { it.parametres.nombre * it.energieParAnimal!! }
                else null

        return AnalyseTroupeau(
                types = analyses,
                effectif = besoins.sumOf { it.parametres.nombre.coerceAtLeast(0) },
                besoinGroupe = besoinGroupe,
                energieGroupe = energieGroupe,
                quantiteTotale = quantiteTotale,
                problemes = problemes.distinct()
        )
    }

    private fun rationParAnimal(
            type: TypeAnimalTroupeau,
            consultation: ConsultationTroupeau,
            aliments: Map<String, AlimentEv>,
            facteur: Double,
            reference: ReferenceEv
    ): Ration {
        val id = genUUID()
        return Ration(
                uuid = id,
                name = "${type.nom} — par animal",
                espece = reference.espece.label,
                alimentMutableList =
                        consultation.ration
                                .mapNotNull { ligne ->
                                    val aliment = aliments[ligne.alimentId] ?: return@mapNotNull null
                                    AlimentRation(
                                            uuid = genUUID(),
                                            aliment = aliment,
                                            quantite = ligne.quantite.coerceAtLeast(0.0) * facteur,
                                            refRation = id,
                                            refAlimUnif = aliment.uuid
                                    )
                                }
                                .toMutableList()
        )
    }

    private suspend fun analyserType(
            type: TypeAnimalTroupeau,
            parametres: ParametresTypeTroupeau,
            reference: ReferenceEv,
            poidsMetabolique: Double?,
            besoinStandard: Double?,
            besoin: Double,
            part: Double,
            ration: Ration
    ): AnalyseTypeTroupeau {
        val nutriments = nutrimentsAvecSeuil(reference)
        val labels = nutriments.map { it.label } + NutrientMain.ENERGIE.label
        // Seuls les aliments distribués entrent dans l'analyse (un aliment à 0 g ne rend pas un
        // nutriment « incomplet »)
        val analysee = ration.copy(alimentMutableList = ration.alimentMutableList.filter { it.quantite > 0.0 }.toMutableList())
        val valeurs =
                if (analysee.alimentMutableList.isEmpty()) emptyMap()
                else analyserValeursNutritionnellesRationSelective(analysee, labels, equationRepository, reference)
        val energie = valeurs[NutrientMain.ENERGIE.label]?.valeur ?: 0.0
        val lignes =
                (listOf<Nutrient>(NutrientMain.ENERGIE) + nutriments).mapNotNull { nutriment ->
                    val valeur = valeurs[nutriment.label] ?: return@mapNotNull null
                    val seuils =
                            // Énergie : bande ±10 % autour du besoin, comme l'analyse de ration
                            if (nutriment == NutrientMain.ENERGIE) mapOf(Reflevel.MIN to 0.9 * besoin, Reflevel.MAX to 1.1 * besoin)
                            else seuilsTroupeau(reference, nutriment, besoinStandard, parametres.poids, poidsMetabolique)
                    LigneAnalyseTroupeau(
                            nutriment,
                            valeur,
                            seuils,
                            calculerConformite(
                                    valeur, reference, besoinStandard, parametres.poids, poidsMetabolique,
                                    besoinEnergetiqueCible = besoin
                            )
                    )
                }
        return AnalyseTypeTroupeau(
                type, parametres, reference, poidsMetabolique, besoinStandard, besoin, part, ration, energie, lignes,
                message = if (analysee.alimentMutableList.isEmpty()) "Ration vide." else null
        )
    }
}
