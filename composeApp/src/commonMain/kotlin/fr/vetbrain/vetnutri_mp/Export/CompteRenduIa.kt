package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlinx.datetime.monthsUntil
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.math.roundToInt

/** Textes du compte rendu proposés par une IA externe ; null = champ laissé inchangé. */
data class ReponseIaCompteRendu(
        val anamnese: String? = null,
        val examenClinique: String? = null,
        val facteurNutritionnelClef: String? = null,
        val texteAdditionnel: String? = null
) {
    val nombreChamps: Int
        get() = listOf(anamnese, examenClinique, facteurNutritionnelClef, texteAdditionnel).count { it != null }
}

/** Données de la consultation transmises au prompt, sans identité de l'animal ni du propriétaire. */
data class DonneesPromptIa(
        val animal: AnimalEv?,
        val consultation: ConsultationEv?,
        val referentielGeneral: String?,
        val referentielsMaladies: List<String>,
        val anamnese: String,
        val examenClinique: String,
        val facteurNutritionnelClef: String,
        val texteAdditionnel: String,
        val conseils: List<String>,
        val nomAliment: (AlimentEv?) -> String,
        /** Extraits du livre de cours (catégorie LIVRE_COURS) joints au prompt. */
        val extraitsLivre: List<HtmlSection> = emptyList()
)

/**
 * Rédaction du compte rendu par une IA externe, sans appel réseau : l'application produit un prompt
 * anonymisé que l'utilisateur colle dans son IA, puis relit la réponse JSON collée en retour.
 */
object CompteRenduIa {
    const val ANIMAL = "[ANIMAL]"
    const val PROPRIETAIRE = "[PROPRIÉTAIRE]"
    const val COORDONNEES = "[COORDONNÉES]"

    private val jsonLisible = Json { prettyPrint = true }
    private val EMAIL = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val TELEPHONE = Regex("""(?:\+\d{2,3}[\s.-]?|0)[1-9](?:[\s.-]?\d{2}){4}""")

    private val CONSIGNES =
            """
            Tu es vétérinaire spécialisé en nutrition. Rédige les textes du compte rendu de la consultation de nutrition décrite par les données JSON ci-dessous.

            Consignes :
            - Rédige en français, dans le style d'un compte rendu vétérinaire adressé à un confrère, en phrases complètes.
            - N'invente aucune valeur (poids, quantités, apports, besoins) absente des données ; si une information utile manque, signale-la.
            - $ANIMAL, $PROPRIETAIRE et $COORDONNEES remplacent des données masquées : garde-les tels quels.
            - Les extraits du livre de cours de l'auteur (« extraitsLivreCours ») font référence : appuie-toi dessus en priorité, sans les contredire, et cite la section utilisée.
            - Si d'autres documents de référence sont joints à cette conversation, utilise-les aussi et cite le chapitre ou la page.
            - Les textes déjà saisis par le vétérinaire (« textesSaisis ») sont à reprendre et à compléter, pas à contredire.
            - Réponds uniquement par un objet JSON valide, sans texte avant ni après, de la forme :
            {
              "anamnese": "synthèse de l'anamnèse et de l'alimentation actuelle",
              "examenClinique": "synthèse de l'examen clinique (poids, NEC, MCS...)",
              "facteurNutritionnelClef": "facteur(s) nutritionnel(s) clef(s) et justification",
              "texteAdditionnel": "conclusion : rations proposées, transition alimentaire, suivi"
            }
            Mets null pour un champ que tu ne modifies pas. Sépare les paragraphes par \n.
            """.trimIndent()

    /** Prompt complet (consignes + données anonymisées) à copier dans une IA. */
    fun construirePrompt(donnees: DonneesPromptIa): String =
            CONSIGNES + "\n\nDonnées de la consultation :\n" +
                    jsonLisible.encodeToString(JsonObject.serializer(), construireDonnees(donnees))

    internal fun construireDonnees(d: DonneesPromptIa): JsonObject {
        val animal = d.animal
        val consultation = d.consultation
        fun anonyme(texte: String?): String? = texte?.takeIf { it.isNotBlank() }?.let { anonymiser(it, animal) }

        return buildJsonObject {
            putJsonObject("animal") {
                if (animal?.estTroupeau == true) {
                    put("type", "troupeau")
                    putJsonArray("groupes") {
                        animal.typesTroupeau.orEmpty().forEach { groupe ->
                            addJsonObject {
                                put("nom", anonymiser(groupe.nom, animal))
                                put("effectif", groupe.nombre)
                                put("poidsMoyenKg", arrondi(groupe.poids))
                                putSiPresent("espece", groupe.especeEnum()?.name?.lowercase())
                                putSiPresent("race", groupe.race)
                            }
                        }
                    }
                } else if (animal != null) {
                    put("espece", animal.getEspece().name.lowercase())
                    putSiPresent("race", animal.race)
                    put("sexe", animal.getSex().displayName)
                    putSiPresent("age", age(animal, consultation))
                }
                putSiPresent("resume", anonyme(animal?.summary))
            }
            if (consultation != null) {
                putJsonObject("consultation") {
                    putSiPresent("date", consultation.date?.toString())
                    putSiPresent("objet", anonyme(consultation.objectConsult))
                    putSiPresent("observation", anonyme(consultation.observation))
                    consultation.weight?.let { put("poidsKg", arrondi(it)) }
                    consultation.idealWeight?.let { put("poidsIdealKg", arrondi(it)) }
                    consultation.BCS?.let { put("nec", it) }
                    consultation.MCS?.let { put("mcs", it) }
                    putSiPresent("referentielGeneral", d.referentielGeneral)
                    if (d.referentielsMaladies.isNotEmpty()) {
                        putJsonArray("referentielsMaladies") { d.referentielsMaladies.forEach { add(JsonPrimitive(it)) } }
                    }
                    if (consultation.coefficientAjustement != 1.0) {
                        put("coefficientAjustementK", consultation.coefficientAjustement)
                    }
                }
                val rations = consultation.rations
                putJsonArray("rationsActuelles") { rations.filter { it.actual }.forEach { add(ration(it, d)) } }
                putJsonArray("rationsProposees") { rations.filter { !it.actual }.forEach { add(ration(it, d)) } }
            }
            if (d.conseils.isNotEmpty()) {
                putJsonArray("conseilsSelectionnes") { d.conseils.forEach { add(JsonPrimitive(it)) } }
            }
            putJsonObject("textesSaisis") {
                putSiPresent("anamnese", anonyme(d.anamnese))
                putSiPresent("examenClinique", anonyme(d.examenClinique))
                putSiPresent("facteurNutritionnelClef", anonyme(d.facteurNutritionnelClef))
                putSiPresent("texteAdditionnel", anonyme(d.texteAdditionnel))
            }
            if (d.extraitsLivre.isNotEmpty()) {
                putJsonArray("extraitsLivreCours") {
                    d.extraitsLivre.forEach { extrait ->
                        addJsonObject {
                            put("source", extrait.title)
                            put("texte", texteBrut(extrait))
                        }
                    }
                }
            }
        }
    }

    /** Contenu d'une section en texte brut : titres, paragraphes, listes à tirets, tableaux « a | b ». */
    fun texteBrut(section: HtmlSection): String =
            section.content.blocks.joinToString("\n\n") { bloc ->
                when (bloc) {
                    is TextBlock.Paragraph -> bloc.text
                    is TextBlock.Heading -> bloc.text.uppercase()
                    is TextBlock.ListBlock -> bloc.items.joinToString("\n") { "- $it" }
                    is TextBlock.TableBlock ->
                            (listOf(bloc.headers) + bloc.rows).filter { it.isNotEmpty() }.joinToString("\n") { it.joinToString(" | ") }
                    is TextBlock.RawHtml -> bloc.html
                }
            }

    private val MOTS_VIDES = setOf("chien", "chiens", "chat", "chats", "maladie", "maladies", "affection", "affections", "chronique", "suivi", "stade")

    private fun racines(texte: String): Set<String> =
            texte.lowercase()
                    .map { c -> SANS_ACCENT[c] ?: c }
                    .joinToString("")
                    .split(Regex("[^a-z0-9]+"))
                    .filter { it.length >= 5 && it !in MOTS_VIDES }
                    .map { it.take(4) }
                    .toSet()

    private val SANS_ACCENT: Map<Char, Char> =
            "àâäáãéèêëíìîïóòôöõúùûüçñ".zip("aaaaaeeeeiiiiooooouuuucn").toMap()

    /**
     * Extraits suggérés pour la consultation : sections « accompagnement » ou « facteurs clefs » des
     * chapitres dont le titre partage une racine (4 premières lettres d'un mot de 5 lettres ou plus,
     * sans accent) avec un mot-clé (référentiels maladies, objet de la consultation).
     */
    fun suggererExtraits(extraits: List<HtmlSection>, motsCles: List<String>): Set<String> {
        val cles = motsCles.flatMap { racines(it) }.toSet()
        if (cles.isEmpty()) return emptySet()
        return extraits
                .filter { extrait ->
                    val chapitre = extrait.title.substringBefore(" — ")
                    val section = extrait.title.substringAfter(" — ", "").lowercase()
                    (section.contains("accompagnement") || section.contains("facteurs clefs")) &&
                            racines(chapitre).any { it in cles }
                }
                .map { it.id }
                .toSet()
    }

    private fun ration(ration: Ration, d: DonneesPromptIa): JsonObject = buildJsonObject {
        putSiPresent("nom", anonymiser(ration.name, d.animal))
        putSiPresent("description", anonymiser(ration.description, d.animal))
        putJsonArray("aliments") {
            ration.alimentMutableList.forEach { ligne ->
                addJsonObject {
                    put("aliment", d.nomAliment(ligne.aliment))
                    put("quantiteG", arrondi(ligne.quantite))
                }
            }
        }
    }

    private fun age(animal: AnimalEv, consultation: ConsultationEv?): String? {
        val naissance = animal.birthdate ?: return null
        val reference = consultation?.date ?: return null
        val mois = naissance.monthsUntil(reference)
        if (mois < 0) return null
        val ans = mois / 12
        return when {
            ans == 0 -> "$mois mois"
            mois % 12 == 0 -> "$ans an(s)"
            else -> "$ans an(s) ${mois % 12} mois"
        }
    }

    private fun arrondi(valeur: Double): Double = (valeur * 10.0).roundToInt() / 10.0

    private fun JsonObjectBuilder.putSiPresent(cle: String, valeur: String?) {
        if (!valeur.isNullOrBlank()) put(cle, valeur)
    }

    /**
     * Masque le nom de l'animal, le nom du propriétaire (complet et chacun de ses mots de 3 lettres
     * ou plus), les adresses e-mail et les numéros de téléphone. Comparaison sans casse, sur mots
     * entiers.
     */
    fun anonymiser(texte: String, animal: AnimalEv?): String {
        val motifs = buildList {
            animal?.nom?.trim()?.takeIf { it.length >= 2 }?.let { add(it to ANIMAL) }
            animal?.ownerName?.trim()?.takeIf { it.isNotEmpty() }?.let { proprietaire ->
                add(proprietaire to PROPRIETAIRE)
                proprietaire.split(' ', ',', ';', '-').filter { it.length >= 3 }.forEach { add(it to PROPRIETAIRE) }
            }
        }.sortedByDescending { it.first.length }

        // Coordonnées d'abord : une adresse e-mail contient souvent le nom du propriétaire
        var resultat = TELEPHONE.replace(EMAIL.replace(texte, COORDONNEES), COORDONNEES)
        for ((motif, jeton) in motifs) resultat = remplacerMotEntier(resultat, motif, jeton)
        return resultat
    }

    private fun remplacerMotEntier(texte: String, motif: String, jeton: String): String {
        val sortie = StringBuilder()
        var debut = 0
        while (true) {
            val i = texte.indexOf(motif, debut, ignoreCase = true)
            if (i < 0) break
            val fin = i + motif.length
            val bordGauche = i == 0 || !texte[i - 1].isLetterOrDigit()
            val bordDroit = fin == texte.length || !texte[fin].isLetterOrDigit()
            if (bordGauche && bordDroit) {
                sortie.appendRange(texte, debut, i).append(jeton)
            } else {
                sortie.appendRange(texte, debut, fin)
            }
            debut = fin
        }
        return sortie.appendRange(texte, debut, texte.length).toString()
    }

    /**
     * Lit la réponse collée : le premier objet JSON trouvé (bloc ```json accepté), champs inconnus
     * ignorés, liste de chaînes jointe par des retours à la ligne. Null si rien d'exploitable.
     */
    fun lireReponse(texte: String): ReponseIaCompteRendu? {
        val debut = texte.indexOf('{')
        val fin = texte.lastIndexOf('}')
        if (debut < 0 || fin <= debut) return null
        val objet = runCatching { Json.parseToJsonElement(texte.substring(debut, fin + 1)).jsonObject }.getOrNull()
                ?: return null

        fun champ(cle: String): String? =
                when (val valeur = objet[cle]) {
                    null, JsonNull -> null
                    is JsonPrimitive -> valeur.contentOrNull
                    is JsonArray -> valeur.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString("\n")
                    else -> null
                }?.trim()?.takeIf { it.isNotEmpty() }

        return ReponseIaCompteRendu(
                anamnese = champ("anamnese"),
                examenClinique = champ("examenClinique"),
                facteurNutritionnelClef = champ("facteurNutritionnelClef"),
                texteAdditionnel = champ("texteAdditionnel")
        ).takeIf { it.nombreChamps > 0 }
    }
}
