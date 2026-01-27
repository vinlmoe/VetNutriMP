package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CommonCrypto.CCCryptorGCM
import platform.CommonCrypto.kCCAlgorithmAES
import platform.CommonCrypto.kCCDecrypt
import platform.CommonCrypto.kCCEncrypt
import platform.CommonCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

internal actual object PlatformCrypto {
    private const val TAG_SIZE_BYTES = 16

    actual fun secureRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        val status = bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.convert(), pinned.addressOf(0))
        }
        if (status != errSecSuccess) {
            throw IllegalStateException("Échec de génération aléatoire sécurisée: $status")
        }
        return bytes
    }

    actual fun aesGcmEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = ByteArray(plain.size)
        val tag = ByteArray(TAG_SIZE_BYTES)
        val status = key.usePinned { keyPinned ->
            iv.usePinned { ivPinned ->
                plain.usePinned { plainPinned ->
                    cipher.usePinned { cipherPinned ->
                        tag.usePinned { tagPinned ->
                            CCCryptorGCM(
                                kCCEncrypt,
                                kCCAlgorithmAES,
                                keyPinned.addressOf(0),
                                key.size.convert(),
                                ivPinned.addressOf(0),
                                iv.size.convert(),
                                null,
                                0.convert(),
                                plainPinned.addressOf(0),
                                plain.size.convert(),
                                cipherPinned.addressOf(0),
                                tagPinned.addressOf(0),
                                tag.size.convert()
                            )
                        }
                    }
                }
            }
        }
        if (status != kCCSuccess) {
            throw IllegalStateException("Échec du chiffrement AES-GCM: $status")
        }
        return cipher + tag
    }

    actual fun aesGcmDecrypt(cipher: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        if (cipher.size <= TAG_SIZE_BYTES) {
            throw IllegalArgumentException("Ciphertext trop court pour AES-GCM")
        }
        val dataLen = cipher.size - TAG_SIZE_BYTES
        val cipherText = cipher.copyOfRange(0, dataLen)
        val tag = cipher.copyOfRange(dataLen, cipher.size)
        val plain = ByteArray(cipherText.size)
        val status = key.usePinned { keyPinned ->
            iv.usePinned { ivPinned ->
                cipherText.usePinned { cipherPinned ->
                    plain.usePinned { plainPinned ->
                        tag.usePinned { tagPinned ->
                            CCCryptorGCM(
                                kCCDecrypt,
                                kCCAlgorithmAES,
                                keyPinned.addressOf(0),
                                key.size.convert(),
                                ivPinned.addressOf(0),
                                iv.size.convert(),
                                null,
                                0.convert(),
                                cipherPinned.addressOf(0),
                                cipherText.size.convert(),
                                plainPinned.addressOf(0),
                                tagPinned.addressOf(0),
                                tag.size.convert()
                            )
                        }
                    }
                }
            }
        }
        if (status != kCCSuccess) {
            throw IllegalStateException("Échec du déchiffrement AES-GCM: $status")
        }
        return plain
    }
}
