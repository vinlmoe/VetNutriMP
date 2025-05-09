package fr.vetbrain.vetnutri_mp.Data

/**
 * Classe représentant une référence bibliographique utilisée pour citer les sources des valeurs
 * nutritionnelles et équations.
 *
 * @property uuid Identifiant unique de la référence
 * @property nom Nom court de la référence
 * @property authors Auteurs de la référence
 * @property title Titre complet de la référence
 * @property journal Journal ou source de publication
 * @property date Date de publication
 * @property doi Identifiant DOI (Digital Object Identifier)
 * @property url URL vers la référence
 * @property comments Commentaires additionnels
 */
data class BiblioRef(
        val uuid: String = java.util.UUID.randomUUID().toString(),
        var nom: String = "",
        var authors: String = "",
        var title: String = "",
        var journal: String = "",
        var date: String = "",
        var doi: String = "",
        var url: String = "",
        var comments: String = "",
        var bibtex: String = "",
        var consistent: Int = 1
) {
    // Propriétés calculées pour faciliter la compatibilité avec BiblioRefEntity
    val firstAuthor: String
        get() = authors.split(",").firstOrNull()?.trim() ?: ""

    val year: Int
        get() = date.substring(0, 4).toIntOrNull() ?: 0

    val completeRef: String
        get() = "$authors. $title. $journal, $date. ${if (doi.isNotBlank()) "doi:$doi" else ""}"

    override fun toString(): String {
        return if (nom.isNotBlank()) {
            nom
        } else if (authors.isNotBlank() && date.isNotBlank()) {
            "$authors, $date"
        } else if (authors.isNotBlank()) {
            authors
        } else if (title.isNotBlank()) {
            title
        } else {
            "Référence sans nom"
        }
    }
}
