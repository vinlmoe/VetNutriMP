package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.View.AnalNut.ReferenceBulletGraph
import java.awt.Color as AwtColor
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.runBlocking

/**
 * Service pour générer des images de bullet graphs en utilisant directement les composables KoalaPlot
 */
object BulletGraphImageCapture {
    
    /**
     * Génère des images de bullet graphs pour une ration en utilisant la même logique que RationsView.kt
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
        
        // Obtenir les valeurs nutritionnelles de la même façon que cardNutrient.kt
        val valeursNutritionnelles = kotlinx.coroutines.runBlocking {
            fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = reference
            )
        }
        
        // Utiliser le type d'expression de besoin depuis les préférences
        val typeExpressionBesoin = preferences.getTypeExpressionBesoinEnum()
        
        // Parcourir tous les nutriments disponibles et ne générer des bullet graphs que pour ceux qui ont au moins une référence
        valeursNutritionnelles.forEach { (nomNutriment, valeurNutritionnelle) ->
            try {
                val nutrient = valeurNutritionnelle.nutriment
                val valeurApportAbsolue = valeurNutritionnelle.valeur
                
                // Vérifier qu'il y a au moins une référence pour ce nutriment
                val minRef = reference.obtenirNutriment(nutrient, Reflevel.MIN)
                val optiminRef = reference.obtenirNutriment(nutrient, Reflevel.OPTIMIN)
                val optimaxRef = reference.obtenirNutriment(nutrient, Reflevel.OPTIMAX)
                val maxRef = reference.obtenirNutriment(nutrient, Reflevel.MAX)
                
                // Ne générer le bullet graph que si au moins une référence est définie (> 0) ET que la valeur d'apport n'est pas nulle
                if ((minRef <= 0.0 && optiminRef <= 0.0 && optimaxRef <= 0.0 && maxRef <= 0.0) || valeurApportAbsolue <= 0.0) {
                    return@forEach
                }
                
                // Convertir la valeur d'apport selon les préférences (MÊME LOGIQUE que cardNutrient.kt)
                val isRatio = nutrient is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis && nutrient.unite.isBlank()
                val valeurApport = if (isRatio) {
                    // Pour les ratios, utiliser la valeur brute sans conversion
                    valeurApportAbsolue
                } else {
                    // MÊME LOGIQUE que cardNutrient.kt lignes 350-375
                    when (typeExpressionBesoin) {
                        TypeExpressionBesoin.PAR_KG -> {
                            poidsAnimal?.let { poids ->
                                if (poids > 0) valeurApportAbsolue / poids else valeurApportAbsolue
                            } ?: valeurApportAbsolue
                        }
                        TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
                            poidsMetabolique?.let { poidsMetab ->
                                if (poidsMetab > 0) valeurApportAbsolue / poidsMetab else valeurApportAbsolue
                            } ?: valeurApportAbsolue
                        }
                        TypeExpressionBesoin.PAR_KCAL -> {
                            besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) (valeurApportAbsolue / bee) * 1000 else valeurApportAbsolue
                            } ?: valeurApportAbsolue
                        }
                        TypeExpressionBesoin.PAR_KJ -> {
                            besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                    val beeEnKj = bee * 4.184
                                    (valeurApportAbsolue / beeEnKj) * 1000
                                } else valeurApportAbsolue
                            } ?: valeurApportAbsolue
                        }
                        else -> valeurApportAbsolue
                    }
                }
                
                // Récupérer les unités de référence
                val minUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.MIN)
                val optiminUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.OPTIMIN)
                val optimaxUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.OPTIMAX)
                val maxUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.MAX)
                
                // Debug des valeurs brutes
                println("DEBUG: $nomNutriment - Valeurs brutes: Min=$minRef($minUnit), OptiMin=$optiminRef($optiminUnit), OptiMax=$optimaxRef($optimaxUnit), Max=$maxRef($maxUnit)")
                println("DEBUG: $nomNutriment - Type expression: $typeExpressionBesoin, isRatio: $isRatio, valeurApportAbsolue: $valeurApportAbsolue")
                
                // Conversion des valeurs de référence dans l'unité des préférences (MÊME LOGIQUE que ReferenceBulletGraph)
                val isAnalysisNoUnit = nutrient is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis && 
                    nutrient.unite.isBlank()
                
                val minRefConverti = if (minRef > 0.0) {
                    if (isAnalysisNoUnit) minRef
                    else convertirVersUnitePreferences(
                        minRef,
                        UnitReqEnum.getById(minUnit),
                        typeExpressionBesoin.unitReqEnum,
                        besoinEnergetiqueEntretien,
                        poidsAnimal,
                        poidsMetabolique
                    ) ?: minRef
                } else null

                val optiminRefConverti = if (optiminRef > 0.0) {
                    if (isAnalysisNoUnit) optiminRef
                    else convertirVersUnitePreferences(
                        optiminRef,
                        UnitReqEnum.getById(optiminUnit),
                        typeExpressionBesoin.unitReqEnum,
                        besoinEnergetiqueEntretien,
                        poidsAnimal,
                        poidsMetabolique
                    ) ?: optiminRef
                } else null

                val optimaxRefConverti = if (optimaxRef > 0.0) {
                    if (isAnalysisNoUnit) optimaxRef
                    else convertirVersUnitePreferences(
                        optimaxRef,
                        UnitReqEnum.getById(optimaxUnit),
                        typeExpressionBesoin.unitReqEnum,
                        besoinEnergetiqueEntretien,
                        poidsAnimal,
                        poidsMetabolique
                    ) ?: optimaxRef
                } else null

                val maxRefConverti = if (maxRef > 0.0) {
                    if (isAnalysisNoUnit) maxRef
                    else convertirVersUnitePreferences(
                        maxRef,
                        UnitReqEnum.getById(maxUnit),
                        typeExpressionBesoin.unitReqEnum,
                        besoinEnergetiqueEntretien,
                        poidsAnimal,
                        poidsMetabolique
                    ) ?: maxRef
                } else null
                
                // Obtenir l'unité d'affichage
                val unite = typeExpressionBesoin.unitReqEnum.label
                
                // Debug des valeurs converties
                println("DEBUG: $nomNutriment - Valeurs converties: Apport=$valeurApport, Min=${minRefConverti ?: 0.0}, OptiMin=${optiminRefConverti ?: 0.0}, OptiMax=${optimaxRefConverti ?: 0.0}, Max=${maxRefConverti ?: 0.0}, Unité=$unite")
                
                // Générer l'image seulement si on a des valeurs valides
                val valeurs = listOfNotNull(valeurApport, minRefConverti, optiminRefConverti, optimaxRefConverti, maxRefConverti)
                if (valeurs.isNotEmpty()) {
                    // Utiliser la même logique que ReferenceBulletGraph pour générer l'image
                    val imageBytes = generateBulletGraphImageFromComposable(
                        nomNutriment,
                        valeurApport,
                        minRefConverti,
                        optiminRefConverti,
                        optimaxRefConverti,
                        maxRefConverti,
                        unite,
                        nutrient,
                        typeExpressionBesoin,
                        poidsAnimal,
                        poidsMetabolique,
                        besoinEnergetiqueEntretien,
                        reference
                    )
                    images[nomNutriment] = imageBytes
                    println("DEBUG: Bullet graph $nomNutriment - Apport: $valeurApport, Min: ${minRefConverti ?: 0.0}, Max: ${maxRefConverti ?: 0.0}, OptiMin: ${optiminRefConverti ?: 0.0}, OptiMax: ${optimaxRefConverti ?: 0.0}")
                }
            } catch (e: Exception) {
                // En cas d'erreur, ignorer ce nutriment
                println("Erreur génération bullet graph pour $nomNutriment: ${e.message}")
                e.printStackTrace()
            }
        }
        
        return images
    }
    
    /**
     * Génère une image de bullet graph en utilisant EXACTEMENT la même logique que ReferenceBulletGraph
     */
    private fun generateBulletGraphImageFromComposable(
        nomNutriment: String,
        valeurApport: Double,
        minRefConverti: Double?,
        optiminRefConverti: Double?,
        optimaxRefConverti: Double?,
        maxRefConverti: Double?,
        unite: String,
        nutrient: Nutrient,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        reference: ReferenceEv
    ): ByteArray {
        // MÊME LOGIQUE que ReferenceBulletGraph lignes 473-483
        val valeurs = listOfNotNull(valeurApport, minRefConverti, optiminRefConverti, optimaxRefConverti, maxRefConverti)
        if (valeurs.isEmpty()) return ByteArray(0) // Rien à tracer
        
        val maxAxis = (valeurs.maxOrNull() ?: 0.0) * 1.1
        
        // MÊME LOGIQUE que ReferenceBulletGraph lignes 544-555 pour les bornes
        val bornes = buildList {
            add(0.0)
            minRefConverti?.let { add(it) }
            optiminRefConverti?.let { add(it) }
            optimaxRefConverti?.let { add(it) }
            maxRefConverti?.let { add(it) }
            add(maxAxis)
        }.distinct().sorted()
        
        // Générer l'image en utilisant la même logique de couleurs que ReferenceBulletGraph
        return generateBulletGraphImageWithReferenceLogic(
            nomNutriment,
            valeurApport,
            minRefConverti,
            optiminRefConverti,
            optimaxRefConverti,
            maxRefConverti,
            maxAxis,
            bornes,
            unite
        )
    }
    
    /**
     * Génère une image de bullet graph en utilisant EXACTEMENT la même logique de couleurs que ReferenceBulletGraph
     */
    private fun generateBulletGraphImageWithReferenceLogic(
        nomNutriment: String,
        valeurApport: Double,
        minRefConverti: Double?,
        optiminRefConverti: Double?,
        optimaxRefConverti: Double?,
        maxRefConverti: Double?,
        maxAxis: Double,
        bornes: List<Double>,
        unite: String
    ): ByteArray {
        val width = 500
        val height = 80
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // Configuration du rendu
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Fond blanc
        g2d.color = AwtColor.WHITE
        g2d.fillRect(0, 0, width, height)
        
        // Calculer les positions (même logique que KoalaPlot)
        val startX = 50
        val endX = width - 50
        val barY = height / 2 - 15
        val barHeight = 30
        val scale = (endX - startX) / maxAxis
        
        // Dessiner les intervalles colorés (MÊME LOGIQUE que ReferenceBulletGraph lignes 556-600)
        for (i in 0 until bornes.size - 1) {
            val start = bornes[i]
            val end = bornes[i + 1]
            if (end <= start) continue
            
            val x1 = startX + (start * scale).toInt()
            val x2 = startX + (end * scale).toInt()
            val intervalWidth = x2 - x1
            
            // MÊME LOGIQUE de couleurs que ReferenceBulletGraph lignes 563-607
            val color = when {
                // Rouge : 0 à MIN (si MIN existe)
                minRefConverti != null && start == 0.0 && end == minRefConverti -> AwtColor(0xE53E3E) // VetNutriColors.Error
                // Rouge : MAX à maxAxis (si MAX existe)
                maxRefConverti != null && start == maxRefConverti && end == maxAxis -> AwtColor(0xE53E3E) // VetNutriColors.Error
                // Bleu : MIN à OPTIMIN
                minRefConverti != null && optiminRefConverti != null && start == minRefConverti && end == optiminRefConverti -> AwtColor(0x3182CE) // VetNutriColors.Primary
                // Bleu : OPTIMAX à MAX
                optimaxRefConverti != null && maxRefConverti != null && start == optimaxRefConverti && end == maxRefConverti -> AwtColor(0x3182CE) // VetNutriColors.Primary
                // Bleu : OPTIMAX à maxAxis (si pas de MAX)
                optimaxRefConverti != null && maxRefConverti == null && start == optimaxRefConverti && end == maxAxis -> AwtColor(0x3182CE) // VetNutriColors.Primary
                // Bleu : MIN à OPTIMIN (si pas de MIN)
                minRefConverti == null && optiminRefConverti != null && start == 0.0 && end == optiminRefConverti -> AwtColor(0x3182CE) // VetNutriColors.Primary
                // Vert : tout le reste (par défaut)
                else -> AwtColor(0x38A169) // VetNutriColors.Success
            }
            
            g2d.color = color
            g2d.fillRect(x1, barY, intervalWidth, barHeight)
        }
        
        // Dessiner la barre principale (apport) - MÊME LOGIQUE que ReferenceBulletGraph ligne 536-540
        val apportX = startX + (valeurApport * scale).toInt()
        g2d.color = AwtColor(0x4A5568) // Couleur grise pour l'apport
        g2d.fillRect(startX, barY + 10, (apportX - startX).coerceAtLeast(1), 10)
        
        // Dessiner les marqueurs de référence
        minRefConverti?.let { ref ->
            val x = startX + (ref * scale).toInt()
            g2d.color = AwtColor(0xE53E3E) // Rouge
            g2d.fillRect(x, barY - 5, 2, barHeight + 10)
        }
        
        optiminRefConverti?.let { ref ->
            val x = startX + (ref * scale).toInt()
            g2d.color = AwtColor(0x3182CE) // Bleu
            g2d.fillRect(x, barY - 5, 2, barHeight + 10)
        }
        
        optimaxRefConverti?.let { ref ->
            val x = startX + (ref * scale).toInt()
            g2d.color = AwtColor(0x3182CE) // Bleu
            g2d.fillRect(x, barY - 5, 2, barHeight + 10)
        }
        
        maxRefConverti?.let { ref ->
            val x = startX + (ref * scale).toInt()
            g2d.color = AwtColor(0xE53E3E) // Rouge
            g2d.fillRect(x, barY - 5, 2, barHeight + 10)
        }
        
        // Ajouter le titre et l'unité
        g2d.color = AwtColor.BLACK
        g2d.font = Font("Arial", Font.BOLD, 12)
        g2d.drawString(nomNutriment, 10, 20)
        
        g2d.font = Font("Arial", Font.PLAIN, 10)
        g2d.drawString("$unite", 10, height - 10)
        
        // Ajouter la valeur d'apport
        g2d.font = Font("Arial", Font.BOLD, 10)
        g2d.drawString("${TextUtils.formatDecimal(valeurApport, 1)}", endX - 50, 20)
        
        g2d.dispose()
        
        // Convertir en ByteArray
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", baos)
        return baos.toByteArray()
    }
    
    /**
     * Génère une image de bullet graph simplifiée (méthode de fallback)
     */
    fun generateBulletGraphImage(
        nomNutriment: String,
        valeurApport: Double,
        minRef: Double,
        maxRef: Double,
        optiminRef: Double,
        optimaxRef: Double,
        unite: String
    ): ByteArray {
        val width = 400
        val height = 80
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // Configuration du rendu
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Fond blanc
        g2d.color = AwtColor.WHITE
        g2d.fillRect(0, 0, width, height)
        
        // Calculer les positions
        val maxValue = maxOf(valeurApport, maxRef, optimaxRef)
        val minValue = minOf(0.0, minRef, optiminRef)
        val range = maxValue - minValue
        val scale = (width - 100) / range // 100px pour les labels
        
        val startX = 50
        val barY = height / 2 - 10
        val barHeight = 20
        
        // Dessiner les barres de référence
        if (minRef > 0) {
            val x = startX + (minRef - minValue) * scale
            g2d.color = AwtColor.RED
            g2d.fillRect(x.toInt(), barY, 2, barHeight)
        }
        
        if (optiminRef > 0) {
            val x = startX + (optiminRef - minValue) * scale
            g2d.color = AwtColor.BLUE
            g2d.fillRect(x.toInt(), barY, 2, barHeight)
        }
        
        if (optimaxRef > 0) {
            val x = startX + (optimaxRef - minValue) * scale
            g2d.color = AwtColor.BLUE
            g2d.fillRect(x.toInt(), barY, 2, barHeight)
        }
        
        if (maxRef > 0) {
            val x = startX + (maxRef - minValue) * scale
            g2d.color = AwtColor.RED
            g2d.fillRect(x.toInt(), barY, 2, barHeight)
        }
        
        // Dessiner la barre principale (apport)
        val apportX = startX + (valeurApport - minValue) * scale
        g2d.color = AwtColor.GRAY
        g2d.fillRect(startX, barY + 5, (apportX - startX).toInt(), 10)
        
        // Dessiner les zones colorées
        if (minRef > 0) {
            val x = startX + (minRef - minValue) * scale
            g2d.color = AwtColor(255, 200, 200) // Rouge clair
            g2d.fillRect(startX, barY, (x - startX).toInt(), barHeight)
        }
        
        if (optiminRef > 0 && optimaxRef > 0) {
            val x1 = startX + (optiminRef - minValue) * scale
            val x2 = startX + (optimaxRef - minValue) * scale
            g2d.color = AwtColor(200, 255, 200) // Vert clair
            g2d.fillRect(x1.toInt(), barY, (x2 - x1).toInt(), barHeight)
        }
        
        if (maxRef > 0) {
            val x = startX + (maxRef - minValue) * scale
            g2d.color = AwtColor(255, 200, 200) // Rouge clair
            g2d.fillRect(x.toInt(), barY, (width - x.toInt() - 50), barHeight)
        }
        
        // Dessiner les bordures
        g2d.color = AwtColor.BLACK
        g2d.drawRect(startX, barY, width - 100, barHeight)
        
        // Ajouter le texte
        g2d.font = Font("Arial", Font.BOLD, 12)
        g2d.color = AwtColor.BLACK
        
        // Nom du nutriment
        g2d.drawString(nomNutriment, 10, 20)
        
        // Valeur d'apport
        g2d.drawString("${TextUtils.formatDecimal(valeurApport, 1)} $unite", 10, height - 10)
        
        // Valeurs de référence
        var yOffset = 35
        if (minRef > 0) {
            g2d.drawString("Min: ${TextUtils.formatDecimal(minRef, 1)}", 10, yOffset)
            yOffset += 15
        }
        if (optiminRef > 0) {
            g2d.drawString("OptiMin: ${TextUtils.formatDecimal(optiminRef, 1)}", 10, yOffset)
            yOffset += 15
        }
        if (optimaxRef > 0) {
            g2d.drawString("OptiMax: ${TextUtils.formatDecimal(optimaxRef, 1)}", 10, yOffset)
            yOffset += 15
        }
        if (maxRef > 0) {
            g2d.drawString("Max: ${TextUtils.formatDecimal(maxRef, 1)}", 10, yOffset)
        }
        
        g2d.dispose()
        
        // Convertir en ByteArray
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }
    
    /**
     * Sauvegarde un ByteArray d'image dans un fichier temporaire
     */
    fun saveImageToTempFile(imageBytes: ByteArray, prefix: String): File {
        val tempFile = File.createTempFile("${prefix}_bullet_graph", ".png")
        tempFile.writeBytes(imageBytes)
        return tempFile
    }
    
    
    /**
     * Convertit une valeur de référence vers l'unité des préférences (MÊME LOGIQUE que DetailNutrimentAnalysis.kt)
     */
    private fun convertirVersUnitePreferences(
        valeur: Double,
        uniteSource: UnitReqEnum,
        uniteCible: UnitReqEnum,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
    ): Double? {
        // Si les unités sont identiques, pas de conversion
        if (uniteSource == uniteCible) {
            return valeur
        }
        
        // MÊME LOGIQUE que DetailNutrimentAnalysis.kt lignes 760-803
        return when (uniteCible) {
            // Vers PERKG (par kg de poids vif)
            UnitReqEnum.PERKG -> {
                when (uniteSource) {
                    UnitReqEnum.PERKG -> valeur
                    UnitReqEnum.PERMS -> {
                        poidsMetabolique?.let { poidsMetab ->
                            if (poidsMetab > 0.0) (valeur * poidsMetab) / (poidsAnimal ?: 1.0) else null
                        }
                    }
                    UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) (valeur * bee) / (1000.0 * (poidsAnimal ?: 1.0)) else null
                        }
                    }
                    UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * beeEnKj) / (1000.0 * (poidsAnimal ?: 1.0))
                            } else null
                        }
                    }
                    else -> valeur
                }
            }
            // Vers PERMS (par kg de poids métabolique)
            UnitReqEnum.PERMS -> {
                when (uniteSource) {
                    UnitReqEnum.PERKG -> {
                        poidsMetabolique?.let { poidsMetab ->
                            if (poidsMetab > 0.0) (valeur * (poidsAnimal ?: 1.0)) / poidsMetab else null
                        }
                    }
                    UnitReqEnum.PERMS -> valeur
                    UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) (valeur * bee) / (1000.0 * (poidsMetabolique ?: 1.0)) else null
                        }
                    }
                    UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * beeEnKj) / (1000.0 * (poidsMetabolique ?: 1.0))
                            } else null
                        }
                    }
                    else -> valeur
                }
            }
            // Vers PERKCAL (par 1000 kcal)
            UnitReqEnum.PERKCAL -> {
                when (uniteSource) {
                    UnitReqEnum.PERKG -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) (valeur * (poidsAnimal ?: 1.0) * 1000.0) / bee else null
                        }
                    }
                    UnitReqEnum.PERMS -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) (valeur * (poidsMetabolique ?: 1.0) * 1000.0) / bee else null
                        }
                    }
                    UnitReqEnum.PERKCAL -> valeur
                    UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * beeEnKj) / 1000.0
                            } else null
                        }
                    }
                    else -> valeur
                }
            }
            // Vers PERKJ (par 1000 kJ)
            UnitReqEnum.PERKJ -> {
                when (uniteSource) {
                    UnitReqEnum.PERKG -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * (poidsAnimal ?: 1.0) * 1000.0) / beeEnKj
                            } else null
                        }
                    }
                    UnitReqEnum.PERMS -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * (poidsMetabolique ?: 1.0) * 1000.0) / beeEnKj
                            } else null
                        }
                    }
                    UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * beeEnKj) / 1000.0
                            } else null
                        }
                    }
                    UnitReqEnum.PERKJ -> valeur
                    else -> valeur
                }
            }
            // Vers ABSOLUTE (valeur absolue)
            UnitReqEnum.ABSOLUTE -> {
                when (uniteSource) {
                    UnitReqEnum.PERKG -> valeur * (poidsAnimal ?: 1.0)
                    UnitReqEnum.PERMS -> valeur * (poidsMetabolique ?: 1.0)
                    UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) (valeur * bee) / 1000.0 else null
                        }
                    }
                    UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                            if (bee > 0.0) {
                                val beeEnKj = bee * 4.184
                                (valeur * beeEnKj) / 1000.0
                            } else null
                        }
                    }
                    else -> valeur
                }
            }
            else -> valeur
        }
    }
}