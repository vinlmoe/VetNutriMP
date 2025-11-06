package fr.vetbrain.vetnutri_mp.Export

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPopoverPresentationController
import platform.UIKit.UIModalPresentationPopover
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetPDFContextBounds
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
actual object PdfExporter {
        private const val a4Width: Double = 595.0
        private const val a4Height: Double = 842.0
        private const val margin: Double = 20.0

        actual fun exportDocument(
                documentType: DocumentType,
                data: ExportData,
                defaultFileName: String
        ): Boolean {
                val html: String = HtmlDocumentBuilder.buildHtml(documentType, data)
                return imprimerDocument(html)
        }
        
        private fun imprimerDocument(html: String): Boolean {
                return try {
                        // Nettoyer le HTML
                        val cleanHtml = nettoyerHtml(html)
                        if (cleanHtml.isBlank()) {
                                return false
                        }
                        
                        
                        // Exécuter toute la logique d'impression sur le thread principal
                        dispatch_async(dispatch_get_main_queue()) {
                                try {
                val controleur: UIViewController? = obtenirTopViewController()
                                        if (controleur == null) {
                                                return@dispatch_async
                                        }
                                        
                                        // Essayer d'abord avec UIMarkupTextPrintFormatter
                                        val success = imprimerAvecMarkupFormatter(cleanHtml, controleur)
                                        if (success) {
                                                return@dispatch_async
                                        }
                                        
                                        // Si échec, essayer avec UISimpleTextPrintFormatter
                                        imprimerAvecSimpleTextFormatter(cleanHtml, controleur)
                                        
                                } catch (t: Throwable) {
                                        t.printStackTrace()
                                }
                        }
                        
                        true
                } catch (t: Throwable) {
                        t.printStackTrace()
                        false
                }
        }
        
        private fun imprimerAvecMarkupFormatter(html: String, controleur: UIViewController): Boolean {
                return try {
                        
                        // Créer le formatteur d'impression
                        val printFormatter = UIMarkupTextPrintFormatter(markupText = html)
                        
                        // Configurer les marges
                        printFormatter.perPageContentInsets = platform.UIKit.UIEdgeInsetsMake(
                                margin, margin, margin, margin
                        )
                        
                        // Créer le contrôleur d'impression
                        val printController = platform.UIKit.UIPrintInteractionController.sharedPrintController()
                        printController.printFormatter = printFormatter
                        
                        // Configurer les options d'impression
                        val printInfo = platform.UIKit.UIPrintInfo.printInfo()
                        printInfo.outputType = platform.UIKit.UIPrintInfoOutputType.UIPrintInfoOutputGeneral
                        printInfo.jobName = "Document VetNutri"
                        printController.printInfo = printInfo
                        
                        // Présenter le dialogue d'impression
                        printController.presentAnimated(
                                animated = true,
                                completionHandler = { controller, completed, error ->
                                        if (completed) {
                                        } else if (error != null) {
                                        } else {
                                        }
                                }
                        )
                        
                        true
                } catch (t: Throwable) {
                        false
                }
        }
        
        private fun imprimerAvecSimpleTextFormatter(html: String, controleur: UIViewController): Boolean {
                return try {
                        
                        // Extraire le texte du HTML (supprimer les balises)
                        val textContent = extraireTexteDuHtml(html)
                        
                        // Créer le formatteur d'impression simple
                        val printFormatter = platform.UIKit.UISimpleTextPrintFormatter(textContent)
                        
                        // Configurer les marges
                        printFormatter.perPageContentInsets = platform.UIKit.UIEdgeInsetsMake(
                                margin, margin, margin, margin
                        )
                        
                        // Créer le contrôleur d'impression
                        val printController = platform.UIKit.UIPrintInteractionController.sharedPrintController()
                        printController.printFormatter = printFormatter
                        
                        // Configurer les options d'impression
                        val printInfo = platform.UIKit.UIPrintInfo.printInfo()
                        printInfo.outputType = platform.UIKit.UIPrintInfoOutputType.UIPrintInfoOutputGeneral
                        printInfo.jobName = "Document VetNutri"
                        printController.printInfo = printInfo
                        
                        // Présenter le dialogue d'impression
                        printController.presentAnimated(
                                animated = true,
                                completionHandler = { controller, completed, error ->
                                        if (completed) {
                                        } else if (error != null) {
                                        } else {
                                        }
                                }
                        )
                        
                        true
                } catch (t: Throwable) {
                        false
                }
        }
        
        private fun extraireTexteDuHtml(html: String): String {
                return try {
                        // Supprimer les balises HTML et extraire le texte
                        var text = html
                                .replace(Regex("<[^>]*>"), " ") // Supprimer les balises HTML
                                .replace(Regex("\\s+"), " ") // Remplacer les espaces multiples par un seul
                                .trim()
                        
                        // Si le texte est trop court, utiliser le HTML original
                        if (text.length < 50) {
                                text = html
                        }
                        
                        text
                } catch (e: Exception) {
                        html
                }
        }

        private fun genererPdfDepuisHtml(html: String): NSData? {
                return try {
                        // Validation et nettoyage du HTML
                        val cleanHtml = nettoyerHtml(html)
                        if (cleanHtml.isBlank()) {
                                return null
                        }
                        
                        // Vérification de la taille du HTML (limite de sécurité)
                        if (cleanHtml.length > 10_000_000) { // 10MB
                                return null
                        }
                        
                        
                        // Essayer d'abord avec UIMarkupTextPrintFormatter
                        val result = genererPdfAvecMarkupFormatter(cleanHtml)
                        if (result != null) {
                                return result
                        }
                        
                        // Si échec, essayer une méthode alternative
                        return genererPdfAlternative(cleanHtml)
                        
                } catch (t: Throwable) {
                        t.printStackTrace()
                        null
                }
        }
        
        private fun genererPdfAvecMarkupFormatter(html: String): NSData? {
                return try {
                        val formatteur: UIMarkupTextPrintFormatter =
                                UIMarkupTextPrintFormatter(markupText = html)
                        
                        val pageRect = CGRectMake(0.0, 0.0, a4Width, a4Height)
                        val printableRect =
                                CGRectMake(
                                        margin,
                                        margin,
                                        a4Width - 2.toDouble() * margin,
                                        a4Height - 2.toDouble() * margin
                                )
                        
                        val renderer: UIPrintPageRenderer =
                                object : UIPrintPageRenderer() {
                                        override fun paperRect() = pageRect
                                        override fun printableRect() = printableRect
                                }
                        
                        renderer.addPrintFormatter(formatteur, startingAtPageAtIndex = 0)
                        
                        val data: NSMutableData = NSMutableData()
                        UIGraphicsBeginPDFContextToData(data, pageRect, null)
                        
                        val pages: Int = renderer.numberOfPages.toInt()
                        
                        var i: Int = 0
                        while (i < pages) {
                                UIGraphicsBeginPDFPage()
                                renderer.drawPageAtIndex(
                                        pageIndex = i.toLong(),
                                        inRect = UIGraphicsGetPDFContextBounds()
                                )
                                i += 1
                        }
                        UIGraphicsEndPDFContext()
                        
                        data
                } catch (t: Throwable) {
                        null
                }
        }
        
        private fun genererPdfAlternative(html: String): NSData? {
                return try {
                        // Méthode alternative : créer un PDF simple vide
                        val pageRect = CGRectMake(0.0, 0.0, a4Width, a4Height)
                        val data: NSMutableData = NSMutableData()
                        UIGraphicsBeginPDFContextToData(data, pageRect, null)
                        UIGraphicsBeginPDFPage()
                        UIGraphicsEndPDFContext()
                        
                        data
                } catch (t: Throwable) {
                        null
                }
        }
        
        private fun nettoyerHtml(html: String): String {
                return try {
                        // Supprimer les caractères de contrôle problématiques
                        var cleanHtml = html
                                .replace("\u0000", "") // Null bytes
                                .replace("\u0001", "") // SOH
                                .replace("\u0002", "") // STX
                                .replace("\u0003", "") // ETX
                                .replace("\u0004", "") // EOT
                                .replace("\u0005", "") // ENQ
                                .replace("\u0006", "") // ACK
                                .replace("\u0007", "") // BEL
                                .replace("\u0008", "") // BS
                                .replace("\u000B", "") // VT
                                .replace("\u000C", "") // FF
                                .replace("\u000E", "") // SO
                                .replace("\u000F", "") // SI
                                .replace("\u0010", "") // DLE
                                .replace("\u0011", "") // DC1
                                .replace("\u0012", "") // DC2
                                .replace("\u0013", "") // DC3
                                .replace("\u0014", "") // DC4
                                .replace("\u0015", "") // NAK
                                .replace("\u0016", "") // SYN
                                .replace("\u0017", "") // ETB
                                .replace("\u0018", "") // CAN
                                .replace("\u0019", "") // EM
                                .replace("\u001A", "") // SUB
                                .replace("\u001B", "") // ESC
                                .replace("\u001C", "") // FS
                                .replace("\u001D", "") // GS
                                .replace("\u001E", "") // RS
                                .replace("\u001F", "") // US
                        
                        // S'assurer que le HTML est bien formé
                        if (!cleanHtml.trimStart().startsWith("<!DOCTYPE html>") && 
                            !cleanHtml.trimStart().startsWith("<html")) {
                                cleanHtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>$cleanHtml</body></html>"
                        }
                        
                        cleanHtml
                } catch (e: Exception) {
                        html // Retourner l'original en cas d'erreur
                }
        }

        private fun obtenirTopViewController(): UIViewController? {
                var controleur: UIViewController? =
                        UIApplication.sharedApplication.keyWindow?.rootViewController
                while (controleur?.presentedViewController != null) controleur =
                        controleur?.presentedViewController
                return controleur
        }
}
