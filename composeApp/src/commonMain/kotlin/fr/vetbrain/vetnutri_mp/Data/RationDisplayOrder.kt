package fr.vetbrain.vetnutri_mp.Data

/** Display order only: sums, current rations, then proposed rations, alphabetically per group. */
fun List<Ration>.sortedForDisplay(): List<Ration> = sortedWith(
    compareBy<Ration> {
        if (it.uuid == RationAggregator.UUID_GROUPE_ACTUELLES ||
            it.uuid == RationAggregator.UUID_GROUPE_PROPOSEES ||
            it.uuid.startsWith(RationAggregator.UUID_GROUPE_ACTUELLES + "-") ||
            it.uuid.startsWith(RationAggregator.UUID_GROUPE_PROPOSEES + "-")) 0 else 1
    }.thenBy { if (it.actual) 0 else 1 }
        .thenBy { rationNameSortKey(it.name) }
)

private fun rationNameSortKey(name: String): String = buildString {
    name.trim().lowercase().forEach { character ->
        append(when (character) {
            'à', 'á', 'â', 'ä', 'ã', 'å' -> "a"
            'ç' -> "c"
            'è', 'é', 'ê', 'ë' -> "e"
            'ì', 'í', 'î', 'ï' -> "i"
            'ñ' -> "n"
            'ò', 'ó', 'ô', 'ö', 'õ' -> "o"
            'ù', 'ú', 'û', 'ü' -> "u"
            'ý', 'ÿ' -> "y"
            'œ' -> "oe"
            'æ' -> "ae"
            else -> character.toString()
        })
    }
}
