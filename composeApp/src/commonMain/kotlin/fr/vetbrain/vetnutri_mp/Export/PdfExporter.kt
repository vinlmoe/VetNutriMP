package fr.vetbrain.vetnutri_mp.Export

expect object PdfExporter {
    /** Exporte un document en PDF. Retourne true si la sauvegarde a réussi. */
    suspend fun exportDocument(
            documentType: DocumentType,
            data: ExportData,
            defaultFileName: String = "document.pdf"
    ): Boolean

    /** Exporte un HTML déjà construit en PDF. */
    suspend fun exportHtmlDocument(
            html: String,
            defaultFileName: String = "document.pdf"
    ): Boolean
}
