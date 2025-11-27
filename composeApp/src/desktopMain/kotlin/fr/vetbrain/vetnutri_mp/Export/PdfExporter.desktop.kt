package fr.vetbrain.vetnutri_mp.Export

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import fr.vetbrain.vetnutri_mp.Utils.FileUtils
import java.io.ByteArrayOutputStream

actual object PdfExporter {
    actual fun exportDocument(
            documentType: DocumentType,
            data: ExportData,
            defaultFileName: String
    ): Boolean {
        val html: String = HtmlDocumentBuilder.buildHtml(documentType, data)
        return try {
            val baos = ByteArrayOutputStream()
            val builder = PdfRendererBuilder()
            builder.useSVGDrawer(BatikSVGDrawer())
            builder.withHtmlContent(html, null)
            builder.toStream(baos)
            builder.run()
            
            val bytes = baos.toByteArray()
            
            // Utiliser SwingUtilities.invokeAndWait comme dans exportJsonToFile
            var result = false
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                result = FileUtils.saveBinaryFileDialog(
                        bytes = bytes,
                        defaultFileName = defaultFileName.ifBlank { "document.pdf" }
                )
            } else {
                javax.swing.SwingUtilities.invokeAndWait {
                    result = FileUtils.saveBinaryFileDialog(
                            bytes = bytes,
                            defaultFileName = defaultFileName.ifBlank { "document.pdf" }
                    )
                }
            }
            result
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }
}

