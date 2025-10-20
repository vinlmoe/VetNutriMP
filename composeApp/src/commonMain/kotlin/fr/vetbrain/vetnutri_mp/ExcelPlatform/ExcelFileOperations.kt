package fr.vetbrain.vetnutri_mp.ExcelPlatform

/**
 * Opérations de fichiers Excel/CSV multiplateformes
 * Interface expect/actual pour la gestion des fichiers CSV
 *
 * ATTENTION: Ce package ne doit pas entrer en conflit avec d'autres classes Platform
 */

/**
 * Ouvre un dialogue de sélection de fichier CSV pour l'import
 * @return Le contenu du fichier CSV sélectionné ou null si aucun fichier n'est sélectionné
 */
expect fun openCsvFileForImport(): String?

/**
 * Ouvre un dialogue de sauvegarde de fichier CSV pour l'export
 * @param csvContent Le contenu CSV à sauvegarder
 * @param defaultFileName Nom de fichier par défaut
 * @return true si la sauvegarde a réussi, false sinon
 */
expect fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean

/**
 * Ouvre un dialogue de sélection de fichier CSV avec prévisualisation
 * @return Le contenu du fichier CSV sélectionné ou null si aucun fichier n'est sélectionné
 */
expect fun openCsvFileWithPreview(): String?

/**
 * Vérifie si les opérations de fichiers CSV sont disponibles sur cette plateforme
 * @return true si les opérations CSV sont supportées, false sinon
 */
expect fun isCsvFileOperationsSupported(): Boolean
