package fr.vetbrain.vetnutri_mp.Utils

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual object PlatformCrypto {
    private val secureRandom = SecureRandom()

    actual fun secureRandomBytes(size: Int): ByteArray =
        ByteArray(size).also { secureRandom.nextBytes(it) }

    actual fun aesCbcEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plain)
    }

    actual fun aesCbcDecrypt(cipher: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return decryptCipher.doFinal(cipher)
    }
}
