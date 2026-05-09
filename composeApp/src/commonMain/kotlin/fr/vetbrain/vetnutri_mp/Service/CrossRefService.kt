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
        val published: CrossRefDate? = null
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
        val given = msg.author?.firstOrNull()?.given ?: ""
        val author = if (given.isNotBlank()) "$family $given" else family
        val year = msg.published?.dateParts?.firstOrNull()?.firstOrNull() ?: 0
        val title = msg.title?.firstOrNull() ?: ""
        val journal = msg.containerTitle?.firstOrNull() ?: ""

        val completeRef = buildString {
            if (author.isNotBlank()) append("$author ($year). ")
            if (title.isNotBlank()) append("$title. ")
            if (journal.isNotBlank()) append(journal)
        }.trim().trimEnd('.')

        return DoiImportResult(firstAuthor = family, year = year, completeRef = completeRef)
    }
}
