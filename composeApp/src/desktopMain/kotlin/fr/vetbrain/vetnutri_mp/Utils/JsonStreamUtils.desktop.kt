package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import java.io.FileOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
actual fun Json.encodeEnvelopeToFile(envelope: ApiEnvelope, file: PlatformFile): Result<Unit> {
    return try {
        FileOutputStream(file.path).use { output ->
            this.encodeToStream(ApiEnvelope.serializer(), envelope, output)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
