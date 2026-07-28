package fr.vetbrain.vetnutri_mp.Localization

fun String.translate(): String = LocalizationManager.translate(this)

fun String.translate(vararg args: String): String = fr.vetbrain.vetnutri_mp.Localization.translate(this, *args)
