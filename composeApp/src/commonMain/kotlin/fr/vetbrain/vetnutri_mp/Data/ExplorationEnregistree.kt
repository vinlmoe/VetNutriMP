package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Configuration d'exploration multiration enregistrée en base (table MULTI_RATION_EXPLORATIONS). */
data class ExplorationEnregistree(
        val uuid: String,
        val nom: String,
        /** Nom de l'enum Espece (CHIEN, CHAT...). */
        val espece: String,
        val configurationJson: String,
        val updatedAt: Long
)

/** Cible d'un rôle, par label de nutriment et nom de niveau (Reflevel). */
@Serializable
data class CibleExplorationJson(val nutriment: String, val niveau: String, val facteur: Double)

/**
 * Contenu JSON d'une configuration : identifiants seulement (référentiels, aliments), pour rester
 * valable quand le catalogue évolue. Les rôles sont indexés par le nom de RoleExploration.
 */
@Serializable
data class ConfigurationExplorationJson(
        val version: Int = 1,
        val espece: String = "CHIEN",
        val referenceIds: List<String> = emptyList(),
        val poidsDe: String = "5",
        val poidsA: String = "30",
        val poidsPas: String = "5",
        val kDe: String = "0.8",
        val kA: String = "1.2",
        val kPas: String = "0.1",
        val listes: Map<String, List<String>> = emptyMap(),
        val cibles: Map<String, CibleExplorationJson> = emptyMap(),
        val doseMinimale: Map<String, Double> = emptyMap(),
        val pasArrondiManuel: Map<String, Double> = emptyMap(),
        val arrondir: Boolean = true,
        val ignorerOptimax: Boolean = true,
        val maxScenarios: String = "2000",
        val besoinsIgnores: Set<String> = emptySet(),
        val arrondirAuSuperieur: Boolean = false,
        val corrections: List<CorrectionRationExploration> = emptyList()
) {
    fun versJson(): String = format.encodeToString(serializer(), this)

    companion object {
        private val format = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun depuisJson(json: String): ConfigurationExplorationJson = format.decodeFromString(serializer(), json)
    }
}

/** Paramètres saisis par l'utilisateur (texte pour les champs numériques). */
data class ParametresExploration(
        /** Espèce explorée : seuls ses référentiels généraux sont proposés. */
        val espece: Espece = Espece.CHIEN,
        val referenceIds: Set<String> = emptySet(),
        val poidsDe: String = "5",
        val poidsA: String = "30",
        val poidsPas: String = "5",
        val kDe: String = "0.8",
        val kA: String = "1.2",
        val kPas: String = "0.1",
        val listes: Map<RoleExploration, List<AlimentEv>> = RoleExploration.entries.associateWith { emptyList() },
        val cibles: Map<RoleExploration, CibleExploration> =
                RoleExploration.entries.filter { !it.estEnergie }.associateWith {
                    CibleExploration(it.nutrimentParDefaut)
                },
        val doseMinimale: Map<RoleExploration, Double> = RoleExploration.entries.associateWith { it.doseParDefaut },
        val pasArrondiManuel: Map<RoleExploration, Double> =
                RoleExploration.entries.filter { it == RoleExploration.SODIUM }.associateWith { 0.1 },
        val arrondir: Boolean = true,
        val ignorerOptimax: Boolean = true,
        val maxScenarios: String = "2000",
        val besoinsIgnores: Set<String> = emptySet(),
        val arrondirAuSuperieur: Boolean = false
)

/** Paramètres → JSON enregistré (identifiants seulement). */
fun ParametresExploration.versJson(): ConfigurationExplorationJson =
        ConfigurationExplorationJson(
                espece = espece.name,
                referenceIds = referenceIds.toList(),
                poidsDe = poidsDe,
                poidsA = poidsA,
                poidsPas = poidsPas,
                kDe = kDe,
                kA = kA,
                kPas = kPas,
                listes = listes.entries.associate { (role, liste) -> role.name to liste.map { it.uuid } },
                cibles =
                        cibles.entries.associate { (role, cible) ->
                            role.name to CibleExplorationJson(cible.nutriment.label, cible.niveau.name, cible.facteur)
                        },
                doseMinimale = doseMinimale.entries.associate { (role, dose) -> role.name to dose },
                pasArrondiManuel = pasArrondiManuel.entries.associate { (role, pas) -> role.name to pas },
                arrondir = arrondir,
                ignorerOptimax = ignorerOptimax,
                maxScenarios = maxScenarios,
                besoinsIgnores = besoinsIgnores,
                arrondirAuSuperieur = arrondirAuSuperieur
        )

/**
 * JSON enregistré → paramètres. Les référentiels et aliments absents du catalogue actuel
 * sont ignorés et renvoyés dans la liste des éléments introuvables.
 */
fun ConfigurationExplorationJson.versParametres(
        aliments: Map<String, AlimentEv>,
        referencesConnues: Set<String>
): Pair<ParametresExploration, List<String>> {
    val json = this
    val manquants = mutableListOf<String>()
    val roles = RoleExploration.entries + json.listes.keys.filter { it.startsWith("SUPPLEMENT_") }.map { RoleExploration.supplement(it) }
    val defaut = ParametresExploration(
            cibles = roles.filterNot { it.estEnergie }.associateWith { CibleExploration(it.nutrimentParDefaut) },
            doseMinimale = roles.associateWith { it.doseParDefaut },
            pasArrondiManuel = roles.filter { it == RoleExploration.SODIUM || it.supplementaire }
                    .associateWith { 0.1 }
    )
    val espece = Espece.entries.firstOrNull { it.name == json.espece } ?: Espece.CHIEN
    val references = json.referenceIds.filter { it in referencesConnues }
    if (references.size < json.referenceIds.size) manquants += "${json.referenceIds.size - references.size} référentiel(s)"
    val listes =
            roles.associateWith { role ->
                val ids = json.listes[role.name].orEmpty()
                val trouves = ids.mapNotNull { aliments[it] }
                if (trouves.size < ids.size) manquants += "${ids.size - trouves.size} aliment(s) ${role.libelle}"
                trouves
            }
    val cibles =
            defaut.cibles.mapValues { (role, parDefaut) ->
                val c = json.cibles[role.name] ?: return@mapValues parDefaut
                CibleExploration(
                        nutriment = NutrientResolver.AllNutrientResolver(c.nutriment) ?: parDefaut.nutriment,
                        niveau = Reflevel.entries.firstOrNull { it.name == c.niveau } ?: parDefaut.niveau,
                        facteur = c.facteur
                )
            }
    val doses = defaut.doseMinimale.mapValues { (role, d) -> json.doseMinimale[role.name] ?: d }
    val pas = roles.mapNotNull { role ->
        (json.pasArrondiManuel[role.name] ?: defaut.pasArrondiManuel[role])?.let { role to it }
    }.toMap()
    return ParametresExploration(
            espece = espece,
            referenceIds = references.toSet(),
            poidsDe = json.poidsDe,
            poidsA = json.poidsA,
            poidsPas = json.poidsPas,
            kDe = json.kDe,
            kA = json.kA,
            kPas = json.kPas,
            listes = listes,
            cibles = cibles,
            doseMinimale = doses,
            pasArrondiManuel = pas,
            arrondir = json.arrondir,
            ignorerOptimax = json.ignorerOptimax,
            maxScenarios = json.maxScenarios,
            besoinsIgnores = json.besoinsIgnores,
            arrondirAuSuperieur = json.arrondirAuSuperieur
    ) to manquants
}

/** Corrections propres à une configuration et un profil, jamais à un animal/une consultation. */
@Serializable
data class LigneCorrectionExploration(val alimentId: String, val quantite: Double)

@Serializable
data class CorrectionRationExploration(
        val configuration: String,
        val cleScenario: String,
        val aliments: List<LigneCorrectionExploration>
)

fun cleScenarioExploration(s: ScenarioExploration): String =
        listOf(s.reference.uuid, s.poids.toString(), s.k.toString(), s.combinaison.toString()).joinToString("|")

suspend fun appliquerCorrectionsExploration(
        resultat: ResultatExploration,
        configuration: String,
        corrections: List<CorrectionRationExploration>,
        aliments: Map<String, AlimentEv>,
        explorateur: ExplorateurMultiration
): ResultatExploration {
    val parCle = corrections.filter { it.configuration == configuration }.associateBy { it.cleScenario }
    val scenarios = resultat.scenarios.map { scenario ->
        val correction = parCle[cleScenarioExploration(scenario)] ?: return@map scenario
        val ration = scenario.ration.copy(alimentMutableList = correction.aliments.map { ligne ->
            val aliment = aliments[ligne.alimentId]
                    ?: throw ExplorationException("Aliment corrigé introuvable : ${ligne.alimentId}")
            if (!ligne.quantite.isFinite() || ligne.quantite < 0.0)
                throw ExplorationException("Quantité corrigée invalide pour ${aliment.nom}")
            AlimentRation(aliment = aliment, quantite = ligne.quantite,
                    refRation = scenario.ration.uuid, refAlimUnif = aliment.uuid)
        }.toMutableList())
        explorateur.reevaluerScenario(scenario, ration, resultat.configuration)
    }
    return resultat.copy(scenarios = scenarios, cases = ExplorateurMultiration.construireCases(scenarios))
}
