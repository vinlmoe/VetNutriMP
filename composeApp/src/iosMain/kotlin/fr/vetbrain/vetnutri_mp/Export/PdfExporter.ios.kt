@file:OptIn(ExperimentalForeignApi::class)

package fr.vetbrain.vetnutri_mp.Export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetPDFContextBounds
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.UIKit.valueWithCGRect
import platform.UIKit.viewPrintFormatter
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Export PDF iOS.
 *
 * Le HTML est rendu par un `WKWebView` (seul moteur iOS qui dessine les SVG des courbes de poids),
 * puis paginé en A4 via `viewPrintFormatter()` + `UIPrintPageRenderer`. Le PDF obtenu est proposé
 * dans la feuille de partage iOS (Fichiers, Mail, AirDrop, impression…).
 */
actual object PdfExporter {
        // A4 en points
        private const val a4Largeur: Double = 595.0
        private const val a4Hauteur: Double = 842.0
        private const val marge: Double = 28.0

        /** Délai max d'attente du chargement HTML dans le WKWebView. */
        private const val delaiChargementMs: Long = 20_000

        // navigationDelegate est weak : on garde les délégués vivants pendant le chargement.
        private val deleguesActifs = mutableSetOf<DelegueChargement>()

        actual suspend fun exportDocument(
                documentType: DocumentType,
                data: ExportData,
                defaultFileName: String
        ): Boolean {
                val html: String = HtmlDocumentBuilder.buildHtml(documentType, data)
                return exporterHtml(html, defaultFileName, data.isLandscape)
        }

        actual suspend fun exportHtmlDocument(
                html: String,
                defaultFileName: String
        ): Boolean {
                val paysage = html.contains("A4 landscape", ignoreCase = true)
                return exporterHtml(html, defaultFileName, paysage)
        }

        private suspend fun exporterHtml(
                html: String,
                nomFichier: String,
                paysage: Boolean
        ): Boolean {
                val cleanHtml = nettoyerHtml(html)
                if (cleanHtml.isBlank()) return false

                return withContext(Dispatchers.Main) {
                        val controleur = obtenirTopViewController() ?: return@withContext false
                        val largeur = if (paysage) a4Hauteur else a4Largeur
                        val hauteur = if (paysage) a4Largeur else a4Hauteur

                        // Hors écran mais dans la hiérarchie de vues : certaines versions d'iOS
                        // ne dessinent pas un WKWebView détaché.
                        val webView = WKWebView(
                                frame = CGRectMake(-largeur * 2, 0.0, largeur, hauteur),
                                configuration = WKWebViewConfiguration()
                        )
                        controleur.view.addSubview(webView)

                        try {
                                val charge = withTimeoutOrNull(delaiChargementMs) {
                                        chargerHtml(webView, cleanHtml)
                                } ?: false
                                if (!charge) return@withContext false

                                val pdfData = genererPdf(webView, largeur, hauteur)
                                        ?: return@withContext false
                                partagerPdf(pdfData, nomFichier, controleur)
                        } catch (t: Throwable) {
                                t.printStackTrace()
                                false
                        } finally {
                                webView.navigationDelegate = null
                                webView.removeFromSuperview()
                        }
                }
        }

        /** Charge le HTML et suspend jusqu'à la fin du rendu (didFinishNavigation). */
        private suspend fun chargerHtml(webView: WKWebView, html: String): Boolean =
                suspendCancellableCoroutine { continuation ->
                        lateinit var delegue: DelegueChargement
                        delegue = DelegueChargement { succes ->
                                deleguesActifs.remove(delegue)
                                if (continuation.isActive) continuation.resume(succes)
                        }
                        deleguesActifs.add(delegue)
                        webView.navigationDelegate = delegue
                        continuation.invokeOnCancellation {
                                // Le WKWebView est nettoyé dans le finally de exporterHtml.
                                deleguesActifs.remove(delegue)
                        }
                        webView.loadHTMLString(html, baseURL = null)
                }

        private fun genererPdf(webView: WKWebView, largeur: Double, hauteur: Double): NSData? {
                val pageRect = CGRectMake(0.0, 0.0, largeur, hauteur)
                val zoneImprimable = CGRectMake(marge, marge, largeur - 2 * marge, hauteur - 2 * marge)

                val renderer = UIPrintPageRenderer()
                // paperRect / printableRect sont en lecture seule : on passe par KVC.
                renderer.setValue(NSValue.valueWithCGRect(pageRect), forKey = "paperRect")
                renderer.setValue(NSValue.valueWithCGRect(zoneImprimable), forKey = "printableRect")
                renderer.addPrintFormatter(webView.viewPrintFormatter(), startingAtPageAtIndex = 0)

                val data = NSMutableData()
                UIGraphicsBeginPDFContextToData(data, pageRect, null)
                // Le nombre de pages n'est fiable qu'une fois le contexte PDF ouvert.
                val nbPages = renderer.numberOfPages
                val limites = UIGraphicsGetPDFContextBounds()
                var i = 0L
                while (i < nbPages) {
                        UIGraphicsBeginPDFPage()
                        renderer.drawPageAtIndex(pageIndex = i, inRect = limites)
                        i++
                }
                UIGraphicsEndPDFContext()

                return if (nbPages > 0L && data.length > 0UL) data else null
        }

        private fun partagerPdf(
                pdfData: NSData,
                nomFichier: String,
                controleur: UIViewController
        ): Boolean {
                val cheminFichier = "${NSTemporaryDirectory()}${nomFichierSur(nomFichier)}"
                if (!pdfData.writeToFile(cheminFichier, atomically = true)) return false

                val activityController = UIActivityViewController(
                        activityItems = listOf(NSURL.fileURLWithPath(cheminFichier)),
                        applicationActivities = null
                )
                // iPad : la feuille de partage s'affiche en popover
                activityController.popoverPresentationController?.let { popover ->
                        popover.sourceView = controleur.view
                        popover.sourceRect = controleur.view.bounds
                }
                controleur.presentViewController(
                        viewControllerToPresent = activityController,
                        animated = true,
                        completion = null
                )
                return true
        }

        private fun nomFichierSur(nomFichier: String): String {
                val base = nomFichier
                        .replace(Regex("[/\\\\:*?\"<>|]"), "_")
                        .trim()
                        .ifBlank { "document_vetnutri.pdf" }
                return if (base.endsWith(".pdf", ignoreCase = true)) base else "$base.pdf"
        }

        private fun nettoyerHtml(html: String): String {
                // Supprimer les caractères de contrôle (hors \t, \n, \r)
                var cleanHtml = html.replace(Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]"), "")
                if (!cleanHtml.trimStart().startsWith("<!DOCTYPE html>", ignoreCase = true) &&
                        !cleanHtml.trimStart().startsWith("<html", ignoreCase = true)
                ) {
                        cleanHtml =
                                "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>$cleanHtml</body></html>"
                }
                return cleanHtml
        }

        private fun obtenirTopViewController(): UIViewController? {
                val window = UIApplication.sharedApplication.keyWindow
                        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
                var controleur: UIViewController = window?.rootViewController ?: return null
                while (controleur.presentedViewController != null) {
                        controleur = controleur.presentedViewController!!
                }
                return controleur
        }
}

private class DelegueChargement(
        private val onTermine: (Boolean) -> Unit
) : NSObject(), WKNavigationDelegateProtocol {
        @ObjCSignatureOverride
        override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
                onTermine(true)
        }

        @ObjCSignatureOverride
        override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError
        ) {
                onTermine(false)
        }

        @ObjCSignatureOverride
        override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError
        ) {
                onTermine(false)
        }
}
