package fr.vetbrain.vetnutri_mp.Export

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
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
            PdfRendererBuilder().withHtmlContent(html, null).toStream(baos).run()
            val bytes = baos.toByteArray()
            FileUtils.saveBinaryFileDialog(
                    bytes = bytes,
                    defaultFileName = defaultFileName.ifBlank { "document.pdf" }
            )
        } catch (t: Throwable) {
            println("PDF export failed: ${t.message}")
            false
        }
    }
}

