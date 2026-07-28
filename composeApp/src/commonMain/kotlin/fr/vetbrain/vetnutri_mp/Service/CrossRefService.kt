package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Utils.PlatformHttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class CrossRefResponse(val message: CrossRefMessage)

@Serializable
private data class CrossRefMessage(
        val author: List<CrossRefAuthor>? = null,
        val title: List<String>? = null,
        @SerialName("container-title") val containerTitle: List<String>? = null,
        val published: CrossRefDate? = null,
        val issued: CrossRefDate? = null,
        val volume: String? = null,
        val issue: String? = null,
        val page: String? = null,
        @SerialName("DOI") val doi: String? = null
)

@Serializable
private data class CrossRefAuthor(val family: String = "", val given: String = "")

@Serializable
private data class CrossRefDate(
        @SerialName("date-parts") val dateParts: List<List<Int>>? = null
)

data class DoiImportResult(
        val firstAuthor: String,
        val year: Int,
        val completeRef: String
)

object CrossRefService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importDoi(doi: String): DoiImportResult {
        val url = "https://api.crossref.org/works/${doi.trim()}"
        val raw = PlatformHttpClient.fetchJson(url)
        val root = json.decodeFromString<CrossRefResponse>(raw)
        val msg = root.message

        val family = msg.author?.firstOrNull()?.family ?: ""
        val year =
                msg.published?.dateParts?.firstOrNull()?.firstOrNull()
                        ?: msg.issued?.dateParts?.firstOrNull()?.firstOrNull()
                        ?: 0
        val title = msg.title?.firstOrNull() ?: ""
        val journal = msg.containerTitle?.firstOrNull() ?: ""
        val volume = msg.volume?.trim().orEmpty()
        val issue = msg.issue?.trim().orEmpty()
        val pages = msg.page?.trim().orEmpty()
        val doiValue = msg.doi?.trim().takeUnless { it.isNullOrBlank() } ?: doi.trim()
        val authors = formatAuthors(msg.author.orEmpty())

        val completeRef = buildString {
            if (authors.isNotBlank()) append("$authors ")
            if (year > 0) append("($year). ")
            if (title.isNotBlank()) append("$title. ")
            if (journal.isNotBlank()) append(journal)
            if (volume.isNotBlank()) {
                append(", ")
                append(volume)
                if (issue.isNotBlank()) append("($issue)")
            } else if (issue.isNotBlank()) {
                append(", ($issue)")
            }
            if (pages.isNotBlank()) append(", $pages")
            if (doiValue.isNotBlank()) append(". DOI: $doiValue")
        }.trim()

        return DoiImportResult(firstAuthor = family, year = year, completeRef = completeRef)
    }

    private fun formatAuthors(authors: List<CrossRefAuthor>): String {
        return authors
                .mapNotNull { author ->
                    val family = author.family.trim()
                    val given = author.given.trim()
                    when {
                        family.isBlank() && given.isBlank() -> null
                        family.isBlank() -> given
                        given.isBlank() -> family
                        else -> "$family $given"
                    }
                }
                .joinToString(", ")
    }
}
