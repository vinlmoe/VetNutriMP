package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Troupeau : un animal (AnimalEv) peut représenter un groupe d'animaux décrit par types (effectif,
 * poids moyen). Chaque consultation fixe, pour chaque type, l'effectif, le poids, le référentiel et
 * K ; ses rations sont saisies en quantités pour l'ensemble du groupe. La ration est répartie entre
 * les types au prorata de leur besoin énergétique, puis la ration d'un animal de chaque type est
 * analysée avec son référentiel, par l'analyse de ration habituelle de la fiche animal.
 */

/**
 * Groupe d'individus du troupeau. Le troupeau n'a ni espèce, ni race, ni sexe : ce sont ceux de
 * ses individus, décrits par groupe (un troupeau peut mêler plusieurs espèces). Effectif et poids
 * servent de valeurs par défaut aux consultations.
 */
@Serializable
data class TypeAnimalTroupeau(
        val id: String = genUUID(),
        val nom: String = "",
        val nombre: Int = 1,
        /** Poids moyen d'un individu (kg). */
        val poids: Double = 0.0,
        /** Espèce des individus (nom de l'enum Espece) ; null = non précisée (anciens dossiers). */
        val espece: String? = null,
        val race: String = "",
        /** Sexe des individus (Sex.id) ; null = non précisé ou mixte. */
        val sexId: Int? = null
) {
    fun especeEnum(): Espece? = espece?.let { nom -> Espece.entries.firstOrNull { it.name == nom } }

    fun sexe(): Sex? = sexId?.let { id -> Sex.entries.firstOrNull { it.id == id } }
}

/** Espèce d'un groupe : la sienne, sinon celle enregistrée sur la fiche (anciens dossiers). */
fun AnimalEv.especeDuGroupe(type: TypeAnimalTroupeau): Espece = type.especeEnum() ?: getEspece()

/** Espèces présentes dans le troupeau, dans l'ordre des groupes. */
fun AnimalEv.especesTroupeau(): List<Espece> = typesTroupeau.orEmpty().map { especeDuGroupe(it) }.distinct()

/**
 * Le troupeau n'a pas d'espèce propre : la fiche garde celle du premier groupe, utilisée par les
 * écrans communs (filtres de la liste, aliments proposés).
 */
fun AnimalEv.avecEspeceDuTroupeau(): AnimalEv {
    val premiere = typesTroupeau?.firstNotNullOfOrNull { it.especeEnum() } ?: return this
    return copy(specieId = premiere.label)
}

/** Paramètres d'un type pour une consultation (les effectifs et poids peuvent évoluer). */
@Serializable
data class ParametresTypeTroupeau(
        val typeId: String,
        val nombre: Int = 1,
        val poids: Double = 0.0,
        val referenceId: String? = null,
        /** K propre au type (coefficient d'ajustement) ; les K1…K5 de la consultation s'appliquent à tous. */
        val k: Double = 1.0,
        /** Variables des équations du référentiel (AW, L, wG...), par label de VariableKind. */
        val variables: Map<String, Double> = emptyMap(),
        /** Note d'état corporel moyenne du groupe (échelle de l'espèce) ; null = non évaluée. */
        val bcs: Double? = null
)

object TroupeauJson {
    private val format = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val typesSerializer = ListSerializer(TypeAnimalTroupeau.serializer())
    private val parametresSerializer = ListSerializer(ParametresTypeTroupeau.serializer())

    /** null = individu. */
    fun typesVersJson(types: List<TypeAnimalTroupeau>?): String? =
            types?.let { format.encodeToString(typesSerializer, it) }

    fun typesDepuisJson(json: String?): MutableList<TypeAnimalTroupeau>? =
            if (json.isNullOrBlank()) null
            else runCatching { format.decodeFromString(typesSerializer, json).toMutableList() }.getOrElse { mutableListOf() }

    fun parametresVersJson(parametres: List<ParametresTypeTroupeau>): String? =
            if (parametres.isEmpty()) null else format.encodeToString(parametresSerializer, parametres)

    fun parametresDepuisJson(json: String?): MutableList<ParametresTypeTroupeau> =
            if (json.isNullOrBlank()) mutableListOf()
            else runCatching { format.decodeFromString(parametresSerializer, json).toMutableList() }.getOrElse { mutableListOf() }
}

/** Paramètres du type pour la consultation : ceux enregistrés, sinon les valeurs du troupeau. */
fun ConsultationEv.parametresType(type: TypeAnimalTroupeau): ParametresTypeTroupeau =
        parametresTroupeau.firstOrNull { it.typeId == type.id }
                ?: ParametresTypeTroupeau(typeId = type.id, nombre = type.nombre, poids = type.poids)

/** Copie de la consultation dont les paramètres de ce type sont remplacés. */
fun ConsultationEv.avecParametresType(parametres: ParametresTypeTroupeau): ConsultationEv =
        copy(parametresTroupeau = (parametresTroupeau.filterNot { it.typeId == parametres.typeId } + parametres).toMutableList())

/** Produit des K1…K5 de la consultation (communs à tous les types). */
fun ConsultationEv.kCommunTroupeau(): Double =
        listOfNotNull(k1Value, k2Value, k3Value, k4Value, k5Value).fold(1.0) { acc, k -> acc * k }

/** Variables saisies converties en variables supplémentaires (seules les VariableKind connues). */
fun ParametresTypeTroupeau.variablesSupplementaires(): List<SupplementalvariableP> =
        variables.mapNotNull { (label, valeur) ->
            VariableKind.entries.firstOrNull { it.label == label }?.let { SupplementalvariableP(it, valeur) }
        }

/**
 * Consultation « vue depuis un animal du type » : poids, référentiel, K et variables du type, pour
 * réutiliser tels quels les calculs et l'affichage écrits pour une consultation individuelle. Les
 * K1…K5, références maladies et rations restent ceux de la consultation du troupeau.
 */
fun ConsultationEv.pourTypeTroupeau(parametres: ParametresTypeTroupeau): ConsultationEv {
    val variablesType = parametres.variablesSupplementaires()
    val autres = suppVarp.filter { sv -> variablesType.none { it.variable == sv.variable } }
    return copy(
            weight = parametres.poids.takeIf { it > 0.0 },
            idealWeight = null,
            referenceGeneraleId = parametres.referenceId,
            coefficientAjustement = parametres.k,
            suppVarp = (autres + variablesType).toMutableList()
    )
}

/**
 * Ration d'un animal : quantités du groupe × [facteur] (= Bᵢ / Σ nⱼ·Bⱼ). L'UUID est conservé pour
 * que la ration du groupe reste sélectionnée dans la liste ; la copie n'est jamais enregistrée.
 */
fun rationParAnimalTroupeau(rationGroupe: Ration, facteur: Double, nomType: String): Ration =
        rationGroupe.copy(
                name = "${rationGroupe.name} — par individu ($nomType)",
                alimentMutableList =
                        rationGroupe.alimentMutableList
                                .map { it.copy(quantite = it.quantite.coerceAtLeast(0.0) * facteur) }
                                .toMutableList(),
                suppVarp = rationGroupe.suppVarp.toMutableList()
        )

/** Besoin énergétique d'un animal du type : BEE standard × K1…K5 × K du type ; null si incalculable. */
fun besoinAnimalTroupeau(parametres: ParametresTypeTroupeau, reference: ReferenceEv?, kCommun: Double): Double? {
    if (reference == null || parametres.poids <= 0.0 || parametres.k <= 0.0) return null
    val bee = CalculMetabolique.besoinEnergetiqueStandard(parametres.poids, reference, parametres.variablesSupplementaires())
    return bee?.takeIf { it.isFinite() && it > 0.0 }?.let { it * kCommun * parametres.k }
}

/**
 * Facteur de répartition de chaque type : quantité d'un animal / quantité du groupe = Bᵢ / Σ nⱼ·Bⱼ.
 * null si le besoin d'un type nourri (effectif > 0) n'est pas calculable. Les K1…K5 communs se
 * simplifient : seuls poids, référentiel, variables et K du type comptent.
 */
fun facteursRepartitionTroupeau(
        types: List<TypeAnimalTroupeau>,
        consultation: ConsultationEv,
        references: Map<String, ReferenceEv>
): Map<String, Double>? {
    val kCommun = consultation.kCommunTroupeau()
    val besoins =
            types.associate { type ->
                val p = consultation.parametresType(type)
                type.id to (p.nombre to if (p.nombre > 0) besoinAnimalTroupeau(p, p.referenceId?.let { references[it] }, kCommun) else 0.0)
            }
    if (besoins.values.any { (nombre, besoin) -> nombre > 0 && besoin == null }) return null
    val total = besoins.values.sumOf { (nombre, besoin) -> if (nombre > 0) nombre * besoin!! else 0.0 }
    if (total <= 0.0) return null
    return besoins.mapValues { (_, nb) -> if (nb.first > 0) nb.second!! / total else 0.0 }
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

// --- Répartition et analyse ---------------------------------------------------------------------

/** Apport d'un nutriment pour un animal d'un type, avec les seuils absolus de son référentiel. */
data class LigneAnalyseTroupeau(
        val nutriment: Nutrient,
        val valeur: ValeurNutritionnelle,
        /** Seuils dans l'unité de l'apport (ratios : valeur brute). */
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
        /** Besoin énergétique d'un animal = standard × K1…K5 × K du type (kcal). */
        val besoinEnergetique: Double?,
        /** Quantité d'un animal / quantité du groupe = Bᵢ / Σ nⱼ·Bⱼ ; null si non répartissable. */
        val facteur: Double?,
        val rationParAnimal: Ration?,
        val energieParAnimal: Double?,
        val lignes: List<LigneAnalyseTroupeau> = emptyList(),
        val message: String? = null
) {
    /** Part de la ration du groupe attribuée à ce type (0..1). */
    val part: Double?
        get() = if (parametres.nombre <= 0) 0.0 else facteur?.let { it * parametres.nombre }

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

    fun type(id: String?): AnalyseTypeTroupeau? = types.firstOrNull { it.type.id == id }
}

/**
 * Apport d'un nutriment à l'ensemble du troupeau, comparé à la somme des besoins de ses individus.
 * [seuils] = Σ nᵢ × seuil d'un individu du groupe i, pour les niveaux présents dans les
 * référentiels de tous les groupes nourris (sinon absents). Les ratios (Ca/P...) sont identiques
 * pour tous les groupes : pas de somme, seuls les groupes hors normes sont signalés.
 */
data class LigneApportTroupeau(
        val nutriment: Nutrient,
        val unite: fr.vetbrain.vetnutri_mp.Enumer.UnitEnum,
        /** Apport total au troupeau (valeur commune pour un ratio). */
        val apport: Double,
        val complet: Boolean,
        val seuils: Map<Reflevel, Double>,
        /** Groupes dont les individus sont hors normes pour ce nutriment, avec le niveau manqué. */
        val groupesHorsNormes: List<Pair<String, ConformiteResult>>,
        val ratio: Boolean
) {
    /** Position de l'apport total par rapport aux seuils sommés, null si dans les normes. */
    val statutTroupeau: ConformiteStatus?
        get() =
                when {
                    ratio -> null
                    seuils[Reflevel.MIN]?.let { apport < it } == true || seuils[Reflevel.OPTIMIN]?.let { apport < it } == true ->
                            ConformiteStatus.CARENCE
                    seuils[Reflevel.MAX]?.let { apport > it } == true || seuils[Reflevel.OPTIMAX]?.let { apport > it } == true ->
                            ConformiteStatus.EXCES
                    else -> null
                }

    /** Vrai si un seuil critique (MIN/MAX) du troupeau n'est pas respecté. */
    val critique: Boolean
        get() =
                !ratio &&
                        (seuils[Reflevel.MIN]?.let { apport < it } == true || seuils[Reflevel.MAX]?.let { apport > it } == true)
}

/**
 * Apports de l'ensemble du troupeau : pour chaque nutriment ayant un seuil dans le référentiel
 * d'au moins un groupe, apport total (Σ effectif × apport d'un individu) et besoins sommés. Les
 * groupes hors normes sont listés : un total conforme peut masquer la carence d'un groupe.
 * Vide tant que la répartition n'est pas calculable.
 */
fun AnalyseTroupeau.apportsTroupeauEntier(): List<LigneApportTroupeau> {
    val nourris = types.filter { it.parametres.nombre > 0 }
    if (nourris.isEmpty() || nourris.any { it.rationParAnimal == null }) return emptyList()
    val nutriments = nourris.flatMap { t -> t.lignes.map { it.nutriment } }.distinctBy { it.label }
    return nutriments.mapNotNull { nutriment ->
        val parGroupe = nourris.map { t -> t to t.lignes.firstOrNull { it.nutriment.label == nutriment.label } }
        val connues = parGroupe.mapNotNull { (t, l) -> l?.let { t to it } }
        val premiere = connues.firstOrNull()?.second ?: return@mapNotNull null
        val ratio = estNutrimentAnalysisRatio(nutriment)
        val apport =
                if (ratio) premiere.valeur.valeur
                else connues.sumOf { (t, l) -> t.parametres.nombre * l.valeur.valeur }
        val seuils =
                if (ratio) emptyMap()
                else
                        listOf(Reflevel.MIN, Reflevel.OPTIMIN, Reflevel.OPTIMAX, Reflevel.MAX)
                                .mapNotNull { niveau ->
                                    // Somme seulement si chaque groupe nourri a ce seuil
                                    if (parGroupe.any { (_, l) -> l?.seuils?.get(niveau) == null }) null
                                    else niveau to parGroupe.sumOf { (t, l) -> t.parametres.nombre * l!!.seuils.getValue(niveau) }
                                }
                                .toMap()
        LigneApportTroupeau(
                nutriment = nutriment,
                unite = premiere.valeur.unite,
                apport = apport,
                complet = connues.all { (_, l) -> l.valeur.complete },
                seuils = seuils,
                groupesHorsNormes =
                        connues.mapNotNull { (t, l) ->
                            l.conformite?.let { c -> t.type.nom.ifBlank { "Groupe sans nom" } to c }
                        },
                ratio = ratio
        )
    }
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
 * Répartition d'une ration de troupeau et analyse de synthèse par type.
 *
 * Bᵢ = BEE standard du référentiel du type × K1…K5 de la consultation × K du type. Un animal du
 * type i reçoit la quantité du groupe × Bᵢ / Σ nⱼ·Bⱼ de chaque aliment ; ses apports sont comparés
 * aux seuils de son référentiel (mêmes fonctions que l'analyse de ration).
 */
class AnalyseurTroupeau(private val equationRepository: EquationRepository?) {

    suspend fun analyser(
            types: List<TypeAnimalTroupeau>,
            consultation: ConsultationEv,
            rationGroupe: Ration?,
            references: Map<String, ReferenceEv>
    ): AnalyseTroupeau {
        val problemes = mutableListOf<String>()
        val lignesRation = rationGroupe?.alimentMutableList.orEmpty()
        val quantiteTotale = lignesRation.sumOf { it.quantite.coerceAtLeast(0.0) }
        val kCommun = consultation.kCommunTroupeau()

        class Besoin(
                val type: TypeAnimalTroupeau,
                val parametres: ParametresTypeTroupeau,
                val reference: ReferenceEv?,
                val poidsMetabolique: Double?,
                val besoinStandard: Double?,
                val message: String?
        ) {
            val besoin: Double? = besoinStandard?.let { it * kCommun * parametres.k }
        }

        val besoins =
                types.map { type ->
                    val p = consultation.parametresType(type)
                    val reference = p.referenceId?.let { references[it] }
                    val nom = type.nom.ifBlank { "Groupe sans nom" }
                    when {
                        p.nombre <= 0 -> Besoin(type, p, reference, null, null, "Effectif nul : ce groupe ne reçoit rien.")
                        p.referenceId == null -> Besoin(type, p, null, null, null, "Choisir un référentiel pour « $nom ».")
                        reference == null -> Besoin(type, p, null, null, null, "Référentiel de « $nom » introuvable.")
                        p.poids <= 0.0 -> Besoin(type, p, reference, null, null, "Poids de « $nom » à renseigner.")
                        p.k <= 0.0 -> Besoin(type, p, reference, null, null, "K de « $nom » doit être positif.")
                        else -> {
                            val vars = p.variablesSupplementaires()
                            val bee = CalculMetabolique.besoinEnergetiqueStandard(p.poids, reference, vars)
                            val mw = CalculMetabolique.poidsMetabolique(p.poids, reference, vars)
                            if (bee == null || !bee.isFinite() || bee <= 0.0)
                                    Besoin(type, p, reference, mw, null, "Équation BEE absente ou invalide dans « ${reference.nom} ».")
                            else Besoin(type, p, reference, mw, bee, null)
                        }
                    }
                }
        val actifs = besoins.filter { it.parametres.nombre > 0 }
        problemes += actifs.mapNotNull { it.message }
        val besoinGroupe =
                if (actifs.isNotEmpty() && actifs.all { it.besoin != null }) actifs.sumOf { it.parametres.nombre * it.besoin!! }
                else null
        if (types.isEmpty()) problemes += "Ajouter au moins un groupe d'individus au troupeau (modifier la fiche)."
        else if (actifs.isEmpty()) problemes += "Aucun animal : tous les effectifs sont nuls."
        if (rationGroupe == null) problemes += "Sélectionner une ration."

        // Nutriments évalués : ceux des référentiels de tous les groupes, pour que l'apport au
        // troupeau entier compte chaque groupe même si son référentiel n'a pas ce seuil
        val nutrimentsTroupeau =
                besoins.filter { it.parametres.nombre > 0 }.mapNotNull { it.reference }.flatMap { nutrimentsAvecSeuil(it) }
                        .distinctBy { it.label }
        val analyses =
                besoins.map { b ->
                    val besoin = b.besoin
                    val reference = b.reference
                    if (besoinGroupe == null || besoinGroupe <= 0.0 || besoin == null || reference == null || b.parametres.nombre <= 0) {
                        AnalyseTypeTroupeau(
                                b.type, b.parametres, reference, b.poidsMetabolique, b.besoinStandard, besoin,
                                facteur = if (b.parametres.nombre <= 0) 0.0 else null,
                                rationParAnimal = null,
                                energieParAnimal = null,
                                message = b.message ?: "Répartition impossible tant que les besoins de tous les groupes ne sont pas calculables."
                        )
                    } else {
                        val facteur = besoin / besoinGroupe
                        val ration = rationGroupe?.let { rationParAnimalTroupeau(it, facteur, b.type.nom) }
                        analyserType(b.type, b.parametres, reference, b.poidsMetabolique, b.besoinStandard, besoin, facteur, ration, nutrimentsTroupeau)
                    }
                }

        val nourris = analyses.filter { it.parametres.nombre > 0 }
        val energieGroupe =
                if (nourris.isNotEmpty() && nourris.all { it.energieParAnimal != null })
                        nourris.sumOf { it.parametres.nombre * it.energieParAnimal!! }
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

    private suspend fun analyserType(
            type: TypeAnimalTroupeau,
            parametres: ParametresTypeTroupeau,
            reference: ReferenceEv,
            poidsMetabolique: Double?,
            besoinStandard: Double?,
            besoin: Double,
            facteur: Double,
            ration: Ration?,
            nutrimentsTroupeau: List<Nutrient>
    ): AnalyseTypeTroupeau {
        val distribues = ration?.alimentMutableList.orEmpty().filter { it.quantite > 0.0 && it.aliment != null }
        if (ration == null || distribues.isEmpty()) {
            return AnalyseTypeTroupeau(
                    type, parametres, reference, poidsMetabolique, besoinStandard, besoin, facteur, ration,
                    energieParAnimal = 0.0, message = "Ration vide."
            )
        }
        val nutriments = (nutrimentsAvecSeuil(reference) + nutrimentsTroupeau).distinctBy { it.label }
        val labels = nutriments.map { it.label } + NutrientMain.ENERGIE.label
        // Seuls les aliments distribués entrent dans l'analyse (un aliment à 0 g ne rend pas un
        // nutriment « incomplet »)
        val analysee = ration.copy(uuid = genUUID(), alimentMutableList = distribues.toMutableList())
        val valeurs = analyserValeursNutritionnellesRationSelective(analysee, labels, equationRepository, reference)
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
                type, parametres, reference, poidsMetabolique, besoinStandard, besoin, facteur, ration, energie, lignes
        )
    }
}
