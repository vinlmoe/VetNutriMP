package fr.vetbrain.vetnutri_mp.Export

/**
 * Archive ZIP minimale, sans compression (méthode « stored ») : un document et ses images en un
 * seul fichier à enregistrer ou partager, sur toutes les plateformes. Les JPEG sont déjà
 * compressés ; le texte est petit.
 */
object ArchiveZip {

    /** Construit l'archive ; les chemins utilisent « / » (ex. « images/energie.jpg »). */
    fun creer(fichiers: List<Pair<String, ByteArray>>): ByteArray {
        val sortie = Tampon()
        val central = Tampon()
        fichiers.forEach { (chemin, contenu) ->
            val nom = chemin.encodeToByteArray()
            val crc = crc32(contenu)
            val decalage = sortie.taille

            // En-tête local
            sortie.entier32(0x04034b50)
            sortie.entier16(20) // version nécessaire
            sortie.entier16(0x0800) // noms en UTF-8
            sortie.entier16(0) // stocké
            sortie.entier16(0)
            sortie.entier16(0x21) // date DOS : 1980-01-01
            sortie.entier32(crc)
            sortie.entier32(contenu.size)
            sortie.entier32(contenu.size)
            sortie.entier16(nom.size)
            sortie.entier16(0)
            sortie.octets(nom)
            sortie.octets(contenu)

            // Entrée du répertoire central
            central.entier32(0x02014b50)
            central.entier16(20)
            central.entier16(20)
            central.entier16(0x0800)
            central.entier16(0)
            central.entier16(0)
            central.entier16(0x21)
            central.entier32(crc)
            central.entier32(contenu.size)
            central.entier32(contenu.size)
            central.entier16(nom.size)
            central.entier16(0)
            central.entier16(0)
            central.entier16(0)
            central.entier16(0)
            central.entier32(0)
            central.entier32(decalage)
            central.octets(nom)
        }
        val debutCentral = sortie.taille
        val tailleCentral = central.taille
        sortie.octets(central.versOctets())

        // Fin du répertoire central
        sortie.entier32(0x06054b50)
        sortie.entier16(0)
        sortie.entier16(0)
        sortie.entier16(fichiers.size)
        sortie.entier16(fichiers.size)
        sortie.entier32(tailleCentral)
        sortie.entier32(debutCentral)
        sortie.entier16(0)
        return sortie.versOctets()
    }

    private val tableCrc = IntArray(256) { n ->
        var c = n
        repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
        c
    }

    internal fun crc32(octets: ByteArray): Int {
        var crc = -1
        for (b in octets) crc = tableCrc[(crc xor b.toInt()) and 0xFF] xor (crc ushr 8)
        return crc.inv()
    }

    /** Tampon d'octets en petit-boutiste. */
    private class Tampon {
        private var donnees = ByteArray(1024)
        var taille = 0
            private set

        private fun reserver(n: Int) {
            if (taille + n > donnees.size) donnees = donnees.copyOf(maxOf(donnees.size * 2, taille + n))
        }

        fun entier16(v: Int) {
            reserver(2)
            donnees[taille++] = v.toByte()
            donnees[taille++] = (v ushr 8).toByte()
        }

        fun entier32(v: Int) {
            entier16(v and 0xFFFF)
            entier16(v ushr 16)
        }

        fun octets(b: ByteArray) {
            reserver(b.size)
            b.copyInto(donnees, taille)
            taille += b.size
        }

        fun versOctets(): ByteArray = donnees.copyOf(taille)
    }
}
