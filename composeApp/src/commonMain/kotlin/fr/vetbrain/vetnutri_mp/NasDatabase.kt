package fr.vetbrain.vetnutri_mp

/** Ouvre un sélecteur de fichier pour choisir le chemin de la base NAS. Desktop uniquement ; retourne null sur mobile. */
expect fun browseNasDbPath(): String?
