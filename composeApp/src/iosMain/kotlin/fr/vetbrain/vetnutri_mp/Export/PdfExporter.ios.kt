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
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetPDFContextBounds
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.UIViewController

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
                val pdfBytes: NSData? = genererPdfDepuisHtml(html)
                if (pdfBytes == null) return false
                val nomFichier: String =
                        if (defaultFileName.isBlank()) "document.pdf" else defaultFileName
                val cheminTemp: String = NSTemporaryDirectory().plus(nomFichier)
                val ecrit: Boolean = (pdfBytes as NSMutableData).writeToFile(cheminTemp, true)
                if (!ecrit) return false
                val url: NSURL = NSURL.fileURLWithPath(path = cheminTemp)
                val controleur: UIViewController? = obtenirTopViewController()
                if (controleur == null) return true
                val activites: UIActivityViewController =
                        UIActivityViewController(
                                activityItems = listOf(url),
                                applicationActivities = null
                        )
                controleur.presentViewController(activites, animated = true, completion = null)
                return true
        }

        private fun genererPdfDepuisHtml(html: String): NSData? {
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

        private fun obtenirTopViewController(): UIViewController? {
                var controleur: UIViewController? =
                        UIApplication.sharedApplication.keyWindow?.rootViewController
                while (controleur?.presentedViewController != null) controleur =
                        controleur?.presentedViewController
                return controleur
        }
}
