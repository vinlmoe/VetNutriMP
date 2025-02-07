package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.serialization.Serializable

@Serializable data class LocalizedStrings(val translations: Map<String, String> = mapOf())
