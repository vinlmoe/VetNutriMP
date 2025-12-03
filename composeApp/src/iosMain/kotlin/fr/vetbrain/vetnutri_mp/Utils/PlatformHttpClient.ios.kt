package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest

/**
 * Implémentation iOS pour PlatformHttpClient
 * Utilise NSURLSession pour récupérer le XML de mise à jour.
 */
actual object PlatformHttpClient {
    actual suspend fun fetchXml(url: String): String {
        return suspendCancellableCoroutine { continuation ->
            val nsUrl: NSURL? = NSURL(string = url)
            if (nsUrl == null) {
                continuation.resumeWithException(
                        UpdateException("URL de mise à jour invalide: $url")
                )
                return@suspendCancellableCoroutine
            }
            val request: NSURLRequest = NSURLRequest.requestWithURL(nsUrl)
            val session = platform.Foundation.NSURLSession.sharedSession()
            val task =
                    session.dataTaskWithRequest(
                            request
                    ) { data: NSData?, response, error: NSError? ->
                        when {
                            error != null -> {
                                val message: String =
                                        error.localizedDescription ?: "Erreur réseau inconnue"
                                continuation.resumeWithException(
                                        UpdateException("Erreur réseau iOS: $message")
                                )
                            }
                            data != null -> {
                                val nsString: NSString? =
                                        NSString.create(data = data, encoding = NSUTF8StringEncoding)
                                val text: String? = nsString as String?
                                if (text != null) {
                                    continuation.resume(text)
                                } else {
                                    continuation.resumeWithException(
                                            UpdateException(
                                                    "Impossible de décoder la réponse de mise à jour"
                                            )
                                    )
                                }
                            }
                            else -> {
                                continuation.resumeWithException(
                                        UpdateException("Réponse vide du serveur de mise à jour")
                                )
                            }
                        }
                    }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }
    }
}
