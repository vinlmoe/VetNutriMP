package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Service.FileService

actual fun createFileService(): FileService {
    return AndroidContext.appContext?.let { FileService(it) }
            ?: throw IllegalStateException("Android context not initialized")
}

actual fun exportPdfDocument(
    documentType: fr.vetbrain.vetnutri_mp.Export.DocumentType,
    data: fr.vetbrain.vetnutri_mp.Export.ExportData,
    defaultFileName: String
): Boolean {
    return fr.vetbrain.vetnutri_mp.Export.PdfExporter.exportDocument(
        documentType = documentType,
        data = data,
        defaultFileName = defaultFileName
    )
}
