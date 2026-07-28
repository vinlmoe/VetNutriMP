package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import kotlinx.serialization.json.Json

expect fun Json.encodeEnvelopeToFile(envelope: ApiEnvelope, file: PlatformFile): Result<Unit>
