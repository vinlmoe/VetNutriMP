package fr.vetbrain.vetnutri_mp.Localization


fun translate(key: String, vararg args: String): String {
    val template = LocalizationManager.translate(key)
    if (args.isEmpty()) return template
    
    // Replace %1$s, %2$s (and %1$d, %2$d for counts), etc. with provided arguments
    var result = template
    args.forEachIndexed { index, arg ->
        result = result
            .replace("%${index + 1}\$s", arg)
            .replace("%${index + 1}\$d", arg)
    }
    return result
}
