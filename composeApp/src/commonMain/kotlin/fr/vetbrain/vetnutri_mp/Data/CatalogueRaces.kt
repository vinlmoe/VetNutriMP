package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.Recherche

/** Une race du catalogue, nommée dans les trois langues de l'application. */
data class RaceCatalogue(
        val fr: String,
        val en: String,
        val zh: String,
        val synonymes: List<String> = emptyList()
) {
    /** Nom dans la langue de l'interface ; l'anglais pour une langue sans traduction. */
    fun nom(langue: String): String =
            when (langue) {
                "fr" -> fr
                "zh" -> zh
                else -> en
            }
}

/**
 * Race proposée à la saisie : [libelle] est le texte inséré, [index] le texte normalisé
 * (voir [Recherche]) de tous ses noms, toutes langues et synonymes confondus.
 */
class SuggestionRace internal constructor(
        val libelle: String,
        internal val index: String,
        internal val libelleNormalise: String,
        /** Tous les noms normalisés (libellé, autres langues, synonymes), pour le classement. */
        internal val noms: List<String> = listOf(libelleNormalise)
)

/**
 * Catalogue hors ligne des races de chiens et de chats ([DONNEES_RACES_CHIENS],
 * [DONNEES_RACES_CHATS]) : il pré-remplit le champ race sans le restreindre, toute saisie libre
 * reste acceptée.
 */
object CatalogueRaces {

    val chiens: List<RaceCatalogue> by lazy { lire(DONNEES_RACES_CHIENS) }
    val chats: List<RaceCatalogue> by lazy { lire(DONNEES_RACES_CHATS) }

    /** Races connues de l'espèce ; vide pour une espèce sans catalogue. */
    fun races(espece: Espece?): List<RaceCatalogue> =
            when (espece) {
                Espece.CHIEN -> chiens
                Espece.CHAT -> chats
                else -> emptyList()
            }

    /**
     * Suggestions pour l'espèce, libellées dans [langue] et triées par ordre alphabétique, suivies
     * des [racesSaisies] (races déjà enregistrées dans la base) qui ne sont pas au catalogue.
     */
    fun suggestions(
            espece: Espece?,
            langue: String,
            racesSaisies: List<String> = emptyList()
    ): List<SuggestionRace> {
        val catalogue =
                races(espece)
                        .map { race ->
                            val libelle = race.nom(langue)
                            val noms = listOf(libelle, race.fr, race.en, race.zh) + race.synonymes
                            SuggestionRace(
                                    libelle,
                                    Recherche.indexer(*noms.toTypedArray()),
                                    Recherche.normaliser(libelle),
                                    noms.map { Recherche.normaliser(it) }.distinct()
                            )
                        }
                        .sortedBy { it.libelleNormalise }
        val nomsConnus =
                races(espece).flatMapTo(HashSet()) { race ->
                    listOf(race.fr, race.en, race.zh).map { Recherche.normaliser(it) }
                }
        val saisies =
                racesSaisies
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinctBy { Recherche.normaliser(it) }
                        .filterNot { Recherche.normaliser(it) in nomsConnus }
                        .map { SuggestionRace(it, Recherche.indexer(it), Recherche.normaliser(it)) }
        return catalogue + saisies
    }

    /**
     * Suggestions correspondant à [requete] (chaque mot dans un des noms, voir [Recherche]) : d'abord
     * celles dont un nom est exactement la requête, puis celles dont le libellé, puis un autre nom,
     * commence par la requête, puis celles dont un mot du libellé commence par un mot de la requête,
     * puis les autres. Vide si la requête est vide ou si elle est déjà
     * exactement une race proposée (la liste ne reste pas ouverte après un choix).
     */
    fun filtrer(requete: String, suggestions: List<SuggestionRace>, max: Int = 8): List<SuggestionRace> {
        val mots = Recherche.mots(requete)
        if (mots.isEmpty()) return emptyList()
        val requeteNormalisee = mots.joinToString(" ")
        if (suggestions.any { it.libelle == requete.trim() }) return emptyList()
        return suggestions
                .filter { Recherche.correspond(mots, it.index) }
                .sortedBy { suggestion ->
                    val libelle = suggestion.libelleNormalise
                    when {
                        suggestion.noms.any { it == requeteNormalisee } -> 0
                        libelle.startsWith(requeteNormalisee) -> 1
                        suggestion.noms.any { it.startsWith(requeteNormalisee) } -> 2
                        libelle.split(' ').any { mot -> mots.any { mot.startsWith(it) } } -> 3
                        else -> 4
                    }
                }
                .take(max)
    }

    internal fun lire(donnees: String): List<RaceCatalogue> =
            donnees.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .mapNotNull { ligne ->
                        val champs = ligne.split('|').map { it.trim() }
                        if (champs.size < 3 || champs.take(3).any { it.isEmpty() }) return@mapNotNull null
                        RaceCatalogue(
                                fr = champs[0],
                                en = champs[1],
                                zh = champs[2],
                                synonymes =
                                        champs.getOrNull(3)
                                                ?.split(';')
                                                ?.map { it.trim() }
                                                ?.filter { it.isNotEmpty() }
                                                .orEmpty()
                        )
                    }
                    .toList()
}
