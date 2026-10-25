package fr.vetbrain.vetnutri_mp.Utils

import okio.ByteString
import okio.ByteString.Companion.decodeBase64

/**
 * Résultat d'un chiffrement de partage.
 *
 * - [keyBase64] : clé maîtresse aléatoire (32 octets) transmise hors bande (QR code).
 * - [ivBase64]  : IV AES-CBC (16 octets).
 * - [tagBase64] : étiquette HMAC-SHA256 sur `iv || cipherText` (format v2). `null` uniquement
 *   pour les charges héritées (format v1, sans authentification) — un chiffrement neuf
 *   produit toujours une étiquette.
 */
internal data class EncryptionResult(
    val cipherTextBase64: String,
    val keyBase64: String,
    val ivBase64: String,
    val tagBase64: String? = null
)

/**
 * Chiffrement des partages JSON : AES-CBC + HMAC-SHA256 (encrypt-then-MAC).
 *
 * Format v2 (courant) : la clé maîtresse de 32 octets partagée via QR code sert à dériver deux
 * sous-clés indépendantes, `encKey = HMAC(key, "enc")` pour AES et `macKey = HMAC(key, "mac")`
 * pour l'authentification. L'étiquette couvre l'IV et le texte chiffré, ce qui ferme la porte
 * aux oracles de padding et aux altérations silencieuses d'un bin public.
 *
 * Format v1 (hérité) : AES-CBC directement avec la clé maîtresse, sans étiquette. Il reste
 * accepté en déchiffrement pour les bins déjà partagés ; il n'est plus jamais produit.
 */
internal object CryptoUtils {
    private const val KEY_SIZE_BYTES = 32
    private const val IV_SIZE_BYTES = 16
    private const val TAG_SIZE_BYTES = 32

    /** Version du format produit par [encryptJson]. 1 = AES-CBC seul (hérité), 2 = AES-CBC + HMAC. */
    const val FORMAT_VERSION_LEGACY = 1
    const val FORMAT_VERSION_AUTHENTICATED = 2

    private val ENC_LABEL = "enc".encodeToByteArray()
    private val MAC_LABEL = "mac".encodeToByteArray()

    fun encryptJson(plainText: String): EncryptionResult {
        val key = PlatformCrypto.secureRandomBytes(KEY_SIZE_BYTES)
        val iv = PlatformCrypto.secureRandomBytes(IV_SIZE_BYTES)
        val encKey = deriveSubKey(key, ENC_LABEL)
        val macKey = deriveSubKey(key, MAC_LABEL)
        val cipherText = PlatformCrypto.aesCbcEncrypt(plainText.encodeToByteArray(), encKey, iv)
        val tag = PlatformCrypto.hmacSha256(macKey, iv + cipherText)
        return EncryptionResult(
            cipherTextBase64 = cipherText.toBase64(),
            keyBase64 = key.toBase64(),
            ivBase64 = iv.toBase64(),
            tagBase64 = tag.toBase64()
        )
    }

    /**
     * Déchiffre une charge partagée.
     *
     * @param tagBase64 étiquette HMAC (format v2). `null` = charge héritée v1, acceptée sans
     *   authentification. Une étiquette présente mais invalide lève [IllegalArgumentException]
     *   avant toute tentative de déchiffrement.
     */
    fun decryptJson(
        cipherTextBase64: String,
        keyBase64: String,
        ivBase64: String,
        tagBase64: String? = null
    ): String {
        val cipherText = cipherTextBase64.fromBase64()
        val key = keyBase64.fromBase64()
        val iv = ivBase64.fromBase64()
        val plainBytes = if (tagBase64 == null) {
            PlatformCrypto.aesCbcDecrypt(cipherText, key, iv)
        } else {
            val tag = tagBase64.fromBase64()
            val encKey = deriveSubKey(key, ENC_LABEL)
            val macKey = deriveSubKey(key, MAC_LABEL)
            val expected = PlatformCrypto.hmacSha256(macKey, iv + cipherText)
            if (tag.size != TAG_SIZE_BYTES || !constantTimeEquals(expected, tag)) {
                throw IllegalArgumentException("Étiquette d'authentification invalide : contenu altéré ou clé incorrecte")
            }
            PlatformCrypto.aesCbcDecrypt(cipherText, encKey, iv)
        }
        return plainBytes.decodeToString()
    }

    private fun deriveSubKey(masterKey: ByteArray, label: ByteArray): ByteArray =
        PlatformCrypto.hmacSha256(masterKey, label)

    /** Comparaison en temps constant : le temps ne dépend pas de la position du premier octet différent. */
    internal fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    private fun ByteArray.toBase64(): String = ByteString.of(*this).base64()

    private fun String.fromBase64(): ByteArray =
        this.decodeBase64()?.toByteArray()
            ?: throw IllegalArgumentException("Base64 invalide")
}

internal expect object PlatformCrypto {
    fun secureRandomBytes(size: Int): ByteArray
    fun aesCbcEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    fun aesCbcDecrypt(cipher: ByteArray, key: ByteArray, iv: ByteArray): ByteArray
    /** HMAC-SHA256 (32 octets) de [data] sous [key]. */
    fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray
}
