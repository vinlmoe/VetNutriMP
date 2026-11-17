package fr.vetbrain.vetnutri_mp.Utils

/**
 * Recherche textuelle commune à toutes les listes : insensible à la casse, aux accents et aux
 * ligatures. « oeuf » trouve « Œuf », « creme » trouve « Crème », « l'agneau » trouve « l’agneau ».
 *
 * La requête est découpée en mots (espaces, tirets, apostrophes) : chaque mot doit figurer dans au
 * moins un des champs, dans n'importe quel ordre.
 */
object Recherche {

    /** Sépare les champs d'un texte indexé : un mot de la requête ne peut pas chevaucher deux champs. */
    private const val SEPARATEUR_CHAMPS = '\u0001'

    /** Minuscules, sans accent, ligatures développées ; espaces, tirets et apostrophes réduits à un espace. */
    fun normaliser(texte: String): String {
        val resultat = StringBuilder(texte.length)
        var separateurEnAttente = false
        for (c in texte) {
            if (estSeparateur(c)) {
                separateurEnAttente = resultat.isNotEmpty()
                continue
            }
            // Diacritiques combinants (texte décomposé, ex. noms de fichiers macOS) : ignorés
            if (c in '̀'..'ͯ') continue
            if (separateurEnAttente) {
                resultat.append(' ')
                separateurEnAttente = false
            }
            val minuscule = c.lowercaseChar()
            val remplacement = sansAccent(minuscule)
            if (remplacement != null) resultat.append(remplacement) else resultat.append(minuscule)
        }
        return resultat.toString()
    }

    /** Mots normalisés de la requête ; vide si la requête est vide. */
    fun mots(requete: String): List<String> =
            normaliser(requete).split(' ').filter { it.isNotEmpty() }

    /** Texte normalisé de plusieurs champs, à calculer une fois quand la liste est longue. */
    fun indexer(vararg champs: String?): String =
            champs.filterNotNull().joinToString(SEPARATEUR_CHAMPS.toString()) { normaliser(it) }

    /** Vrai si chaque mot (déjà normalisé, voir [mots]) figure dans le texte indexé. */
    fun correspond(mots: List<String>, texteIndexe: String): Boolean =
            mots.all { texteIndexe.contains(it) }

    /** Vrai si chaque mot de la requête figure dans au moins un des champs ; vrai si la requête est vide. */
    fun correspond(requete: String, vararg champs: String?): Boolean {
        val mots = mots(requete)
        return mots.isEmpty() || correspond(mots, indexer(*champs))
    }

    private fun estSeparateur(c: Char): Boolean =
            c.isWhitespace() ||
                    c == ' ' || c == ' ' || // espaces insécables
                    c == '-' || c == '‐' || c == '‑' || c == '–' || c == '—' ||
                    c == '_' ||
                    c == '\'' || c == '’' || c == '‘' || c == 'ʼ' || c == '`' || c == '´'

    private fun sansAccent(c: Char): String? =
            when (c) {
                'à', 'á', 'â', 'ã', 'ä', 'å', 'ā', 'ă', 'ą' -> "a"
                'ç', 'ć', 'č' -> "c"
                'ď', 'đ' -> "d"
                'è', 'é', 'ê', 'ë', 'ē', 'ė', 'ę', 'ě' -> "e"
                'ì', 'í', 'î', 'ï', 'ī', 'į', 'ı' -> "i"
                'ł', 'ľ' -> "l"
                'ñ', 'ń', 'ň' -> "n"
                'ò', 'ó', 'ô', 'õ', 'ö', 'ø', 'ō', 'ő' -> "o"
                'ŕ', 'ř' -> "r"
                'ś', 'š', 'ş' -> "s"
                'ť', 'ţ' -> "t"
                'ù', 'ú', 'û', 'ü', 'ū', 'ů', 'ű', 'ų' -> "u"
                'ý', 'ÿ' -> "y"
                'ź', 'ż', 'ž' -> "z"
                'œ' -> "oe"
                'æ' -> "ae"
                'ß' -> "ss"
                'μ' -> "µ" // mu grec et signe micro : même lettre pour « µg »
                else -> null
            }
}
