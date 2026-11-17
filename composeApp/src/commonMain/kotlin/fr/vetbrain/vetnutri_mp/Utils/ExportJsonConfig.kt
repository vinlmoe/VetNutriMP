package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.serialization.json.Json

fun createExportJson(): Json {
    return Json {
        prettyPrint = isDebugBuild()
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
