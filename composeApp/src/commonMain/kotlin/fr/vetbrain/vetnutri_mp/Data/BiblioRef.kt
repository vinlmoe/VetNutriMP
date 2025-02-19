import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class BiblioRef(
        val uuid: String = Uuid.random().toString(),
        var firstAuthor: String?,
        var year: String?,
        var completeRef: String?,
        var comments: String?,
        var consistent: Int?
)
