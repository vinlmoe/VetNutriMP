package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCBlockSizeAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import kotlinx.cinterop.ULongVar

@OptIn(ExperimentalForeignApi::class)
internal actual object PlatformCrypto {
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

    actual fun aesCbcEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val output = ByteArray(plain.size + kCCBlockSizeAES128.toInt())
        return memScoped {
            val outMoved = allocArray<ULongVar>(1)
            val status = key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    plain.usePinned { plainPinned ->
                        output.usePinned { outPinned ->
                            CCCrypt(
                                kCCEncrypt.convert(),
                                kCCAlgorithmAES.convert(),
                                kCCOptionPKCS7Padding.convert(),
                                keyPinned.addressOf(0),
                                key.size.convert(),
                                ivPinned.addressOf(0),
                                plainPinned.addressOf(0),
                                plain.size.convert(),
                                outPinned.addressOf(0),
                                output.size.convert(),
                                outMoved
                            )
                        }
                    }
                }
            }
            if (status != kCCSuccess) {
                throw IllegalStateException("Échec du chiffrement AES-CBC: $status")
            }
            output.copyOf(outMoved[0].toInt())
        }
    }

    actual fun aesCbcDecrypt(cipher: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val output = ByteArray(cipher.size)
        return memScoped {
            val outMoved = allocArray<ULongVar>(1)
            val status = key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    cipher.usePinned { cipherPinned ->
                        output.usePinned { outPinned ->
                            CCCrypt(
                                kCCDecrypt.convert(),
                                kCCAlgorithmAES.convert(),
                                kCCOptionPKCS7Padding.convert(),
                                keyPinned.addressOf(0),
                                key.size.convert(),
                                ivPinned.addressOf(0),
                                cipherPinned.addressOf(0),
                                cipher.size.convert(),
                                outPinned.addressOf(0),
                                output.size.convert(),
                                outMoved
                            )
                        }
                    }
                }
            }
            if (status != kCCSuccess) {
                throw IllegalStateException("Échec du déchiffrement AES-CBC: $status")
            }
            output.copyOf(outMoved[0].toInt())
        }
    }
}
