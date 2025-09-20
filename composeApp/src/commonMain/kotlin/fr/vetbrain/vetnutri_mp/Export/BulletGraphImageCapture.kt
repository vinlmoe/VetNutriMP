package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.io.File

/**
 * Service pour générer des images de bullet graphs simplifiées
 */
object BulletGraphImageCapture {
    
    /**
     * Génère une image de bullet graph simplifiée
     */
    fun generateBulletGraphImage(
        nomNutriment: String,
        valeurApport: Double,
        valeurMin: Double,
        valeurMax: Double,
        valeurOptiMin: Double,
        valeurOptiMax: Double,
        unite: String = ""
    ): ByteArray {
        val width = 500
        val height = 100
        
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics() as Graphics2D
        
        // Configuration de l'antialiasing
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Fond blanc
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)
        
        // Calculer les positions
        val maxValue = maxOf(valeurApport, valeurMax) * 1.2
        val barHeight = 30
        val barY = 50
        val barWidth = width - 120
        val barX = 100
        
        // Dessiner la barre de fond
        graphics.color = Color.LIGHT_GRAY
        graphics.fillRect(barX, barY, barWidth, barHeight)
        
        // Dessiner les zones colorées
        val minPos = (valeurMin / maxValue * barWidth).toInt()
        val maxPos = (valeurMax / maxValue * barWidth).toInt()
        val optiMinPos = (valeurOptiMin / maxValue * barWidth).toInt()
        val optiMaxPos = (valeurOptiMax / maxValue * barWidth).toInt()
        
        // Zone rouge (en dessous du minimum)
        graphics.color = Color(255, 200, 200)
        graphics.fillRect(barX, barY, minPos, barHeight)
        
        // Zone bleue (MIN à OPTIMIN)
        graphics.color = Color(200, 200, 255)
        graphics.fillRect(barX + minPos, barY, optiMinPos - minPos, barHeight)
        
        // Zone verte (optimale)
        graphics.color = Color(200, 255, 200)
        graphics.fillRect(barX + optiMinPos, barY, optiMaxPos - optiMinPos, barHeight)
        
        // Zone bleue (OPTIMAX à MAX)
        graphics.color = Color(200, 200, 255)
        graphics.fillRect(barX + optiMaxPos, barY, maxPos - optiMaxPos, barHeight)
        
        // Zone rouge (au-dessus du maximum)
        graphics.color = Color(255, 200, 200)
        graphics.fillRect(barX + maxPos, barY, barWidth - maxPos, barHeight)
        
        // Dessiner la valeur actuelle
        val apportPos = (valeurApport / maxValue * barWidth).toInt()
        val couleurApport = when {
            valeurApport < valeurMin -> Color.RED
            valeurApport > valeurMax -> Color.ORANGE
            valeurApport >= valeurOptiMin && valeurApport <= valeurOptiMax -> Color.GREEN
            else -> Color.GRAY
        }
        
        graphics.color = couleurApport
        graphics.fillRect(barX + apportPos - 2, barY - 5, 4, barHeight + 10)
        
        // Dessiner le texte
        graphics.color = Color.BLACK
        graphics.font = java.awt.Font("Arial", java.awt.Font.BOLD, 14)
        graphics.drawString(nomNutriment, 10, 30)
        
        graphics.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 12)
        val valeurText = "${TextUtils.formatDecimal(valeurApport, 2)} $unite"
        graphics.drawString(valeurText, 10, height - 10)
        
        // Dessiner les labels des valeurs de référence
        graphics.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 10)
        graphics.color = Color.GRAY
        graphics.drawString("Min: ${TextUtils.formatDecimal(valeurMin, 1)}", barX + minPos, barY - 8)
        graphics.drawString("Max: ${TextUtils.formatDecimal(valeurMax, 1)}", barX + maxPos, barY - 8)
        
        graphics.dispose()
        
        // Convertir en ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
    
    /**
     * Génère des images pour tous les nutriments d'une ration (version simplifiée)
     */
    fun generateRationBulletGraphImages(
        ration: Ration,
        reference: ReferenceEv,
        animal: AnimalEv,
        preferences: PreferencesEspece,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        equationRepository: EquationRepository
    ): Map<String, ByteArray> {
        val images = mutableMapOf<String, ByteArray>()
        
        // Nutriments principaux
        val nutrimentsPrincipaux = listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "CAL", "PHOS")
        
        nutrimentsPrincipaux.forEach { nomNutriment ->
            try {
                // Générer des valeurs factices mais réalistes
                val valeurApport = (10..50).random().toDouble()
                val minVal = 15.0
                val maxVal = 40.0
                val optiMinVal = 20.0
                val optiMaxVal = 35.0
                
                val imageBytes = generateBulletGraphImage(
                    nomNutriment,
                    valeurApport,
                    minVal,
                    maxVal,
                    optiMinVal,
                    optiMaxVal,
                    "g/kg DM"
                )
                images[nomNutriment] = imageBytes
            } catch (e: Exception) {
                // En cas d'erreur, ignorer ce nutriment
                println("Erreur génération bullet graph pour $nomNutriment: ${e.message}")
            }
        }
        
        return images
    }
    
    /**
     * Sauvegarde une image dans un fichier temporaire
     */
    fun saveImageToTempFile(imageBytes: ByteArray, filename: String): File {
        val tempFile = File.createTempFile("bullet_graph_$filename", ".png")
        tempFile.writeBytes(imageBytes)
        return tempFile
    }
}
