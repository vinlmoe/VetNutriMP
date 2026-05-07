package fr.vetbrain.vetnutri_mp.Export

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import fr.vetbrain.vetnutri_mp.Utils.FileUtils
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object PdfExporter {
    actual suspend fun exportDocument(
            documentType: DocumentType,
            data: ExportData,
            defaultFileName: String
    ): Boolean {
        val html: String = HtmlDocumentBuilder.buildHtml(documentType, data)
        return exportHtmlDocument(html, defaultFileName)
    }

    actual suspend fun exportHtmlDocument(
            html: String,
            defaultFileName: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val baos = ByteArrayOutputStream()
                val builder = PdfRendererBuilder()
                builder.useSVGDrawer(BatikSVGDrawer())
                builder.withHtmlContent(html, null)
                builder.toStream(baos)
                builder.run()

                val bytes = baos.toByteArray()

                var result = false
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    result =
                            FileUtils.saveBinaryFileDialog(
                                    bytes = bytes,
                                    defaultFileName = defaultFileName.ifBlank { "document.pdf" }
                            )
                } else {
                    javax.swing.SwingUtilities.invokeAndWait {
                        result =
                                FileUtils.saveBinaryFileDialog(
                                        bytes = bytes,
                                        defaultFileName =
                                                defaultFileName.ifBlank { "document.pdf" }
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
}
