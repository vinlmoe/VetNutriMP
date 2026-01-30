package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

actual fun Json.encodeEnvelopeToFile(envelope: ApiEnvelope, file: PlatformFile): Result<Unit> {
    return try {
        file.writeText(this.encodeToString(ApiEnvelope.serializer(), envelope))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
