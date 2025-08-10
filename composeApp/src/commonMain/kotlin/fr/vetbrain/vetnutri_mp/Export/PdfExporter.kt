package fr.vetbrain.vetnutri_mp.Export

expect object PdfExporter {
    /** Exporte un document en PDF. Retourne true si la sauvegarde a réussi. */
    fun exportDocument(
            documentType: DocumentType,
            data: ExportData,
            defaultFileName: String = "document.pdf"
    ): Boolean
}

