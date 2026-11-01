package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.ui.graphics.ImageBitmap

/** Encode une image en JPEG, sur fond blanc (le JPEG n'a pas de transparence). */
expect fun encoderJpeg(image: ImageBitmap, qualite: Int = 90): ByteArray

/**
 * Enregistre une archive ZIP choisie par l'utilisateur (dialogue d'enregistrement sur ordinateur
 * et Android, feuille de partage sur iOS). Retourne false si l'utilisateur annule.
 */
expect suspend fun exporterZip(contenu: ByteArray, nomFichier: String): Boolean
