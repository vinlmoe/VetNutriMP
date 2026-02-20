package fr.vetbrain.vetnutri_mp.Export

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.View.AnalNut.ReferenceBulletGraph

/**
 * Service pour générer des images de bullet graphs en utilisant directement les composables KoalaPlot
 */
object BulletGraphImageCapture {
    
    /**
     * Génère des images de bullet graphs pour une ration en utilisant la même logique que RationsView.kt
     */
    suspend fun generateRationBulletGraphImages(
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
        val valeursNutritionnelles =
            fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = reference
            )
        
        // Utiliser le type d'expression de besoin depuis les préférences
        val typeExpressionBesoin = preferences.getTypeExpressionBesoinEnum()
        
        // Parcourir tous les nutriments disponibles et ne générer des bullet graphs que pour ceux qui ont au moins une référence
        valeursNutritionnelles.forEach { (nomNutriment, valeurNutritionnelle) ->
            // Obtenir le nom traduit du nutriment
            val nomTraduit = obtenirNomTraduitNutriment(nomNutriment, valeurNutritionnelle.nutriment)
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
                    }
                }
                
                // Récupérer les unités de référence
                val minUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.MIN)
                val optiminUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.OPTIMIN)
                val optimaxUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.OPTIMAX)
                val maxUnit = reference.obtenirUniteNutriment(nutrient, Reflevel.MAX)
                
                // Debug des valeurs brutes
                
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
                
                // Générer l'image seulement si on a des valeurs valides
                val valeurs = listOfNotNull(valeurApport, minRefConverti, optiminRefConverti, optimaxRefConverti, maxRefConverti)
                if (valeurs.isNotEmpty()) {
                    // Utiliser la même logique que ReferenceBulletGraph pour générer l'image
                    val imageBytes = generateBulletGraphImageFromComposable(
                        nomNutriment,
                        nomTraduit,
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
                }
            } catch (e: Exception) {
                // En cas d'erreur, ignorer ce nutriment
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
        nomTraduit: String,
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
            nomTraduit,
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
        val width = 500f
        val height = 80f
        
        // Utiliser Compose Multiplatform pour toutes les plateformes
        val imageBitmap = ImageBitmap(width.toInt(), height.toInt())
        val canvas = Canvas(imageBitmap)
        
        drawBulletGraphOnCanvas(
            canvas = canvas,
            nomNutriment = nomNutriment,
            valeurApport = valeurApport,
            minRefConverti = minRefConverti,
            optiminRefConverti = optiminRefConverti,
            optimaxRefConverti = optimaxRefConverti,
            maxRefConverti = maxRefConverti,
            maxAxis = maxAxis,
            bornes = bornes,
            unite = unite,
            width = width,
            height = height
        )
        
        return imageBitmap.toByteArray()
    }
    
    
    /**
     * Dessine le bullet graph sur un Canvas Compose
     */
    private fun drawBulletGraphOnCanvas(
        canvas: Canvas,
        nomNutriment: String,
        valeurApport: Double,
        minRefConverti: Double?,
        optiminRefConverti: Double?,
        optimaxRefConverti: Double?,
        maxRefConverti: Double?,
        maxAxis: Double,
        bornes: List<Double>,
        unite: String,
        width: Float,
        height: Float
    ) {
        // Calculer les positions (même logique que KoalaPlot)
        val startX = 50f
        val endX = width - 50f
        val barY = height / 2 - 15f
        val barHeight = 30f
        val scale = (endX - startX) / maxAxis
        
        // Dessiner les intervalles colorés (MÊME LOGIQUE que ReferenceBulletGraph lignes 556-600)
        for (i in 0 until bornes.size - 1) {
            val start = bornes[i]
            val end = bornes[i + 1]
            if (end <= start) continue
            
            val x1 = startX + (start * scale).toFloat()
            val x2 = startX + (end * scale).toFloat()
            val intervalWidth = x2 - x1
            
            // MÊME LOGIQUE de couleurs que ReferenceBulletGraph lignes 563-607
            val color = when {
                // Rouge : 0 à MIN (si MIN existe)
                minRefConverti != null && start == 0.0 && end == minRefConverti -> Color(0xFFE53E3E) // VetNutriColors.Error
                // Rouge : MAX à maxAxis (si MAX existe)
                maxRefConverti != null && start == maxRefConverti && end == maxAxis -> Color(0xFFE53E3E) // VetNutriColors.Error
                // Bleu : MIN à OPTIMIN
                minRefConverti != null && optiminRefConverti != null && start == minRefConverti && end == optiminRefConverti -> Color(0xFF3182CE) // VetNutriColors.Primary
                // Bleu : OPTIMAX à MAX
                optimaxRefConverti != null && maxRefConverti != null && start == optimaxRefConverti && end == maxRefConverti -> Color(0xFF3182CE) // VetNutriColors.Primary
                // Bleu : OPTIMAX à maxAxis (si pas de MAX)
                optimaxRefConverti != null && maxRefConverti == null && start == optimaxRefConverti && end == maxAxis -> Color(0xFF3182CE) // VetNutriColors.Primary
                // Bleu : MIN à OPTIMIN (si pas de MIN)
                minRefConverti == null && optiminRefConverti != null && start == 0.0 && end == optiminRefConverti -> Color(0xFF3182CE) // VetNutriColors.Primary
                // Vert : tout le reste (par défaut)
                else -> Color(0xFF38A169) // VetNutriColors.Success
            }
            
            val paint = Paint().apply {
                this.color = color
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x1,
                top = barY,
                right = x1 + intervalWidth,
                bottom = barY + barHeight,
                paint = paint
            )
        }
        
        // Dessiner la barre principale (apport) - MÊME LOGIQUE que ReferenceBulletGraph ligne 536-540
        val apportX = startX + (valeurApport * scale).toFloat()
        val apportPaint = Paint().apply {
            color = Color(0xFF4A5568) // Couleur grise pour l'apport
            style = PaintingStyle.Fill
        }
        canvas.drawRect(
            left = startX,
            top = barY + 10f,
            right = apportX,
            bottom = barY + 10f + 10f,
            paint = apportPaint
        )
        
        // Dessiner les marqueurs de référence
        minRefConverti?.let { ref ->
            val x = startX + (ref * scale).toFloat()
            val redPaint = Paint().apply {
                color = Color(0xFFE53E3E) // Rouge
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY - 5f,
                right = x + 2f,
                bottom = barY - 5f + barHeight + 10f,
                paint = redPaint
            )
        }
        
        optiminRefConverti?.let { ref ->
            val x = startX + (ref * scale).toFloat()
            val bluePaint = Paint().apply {
                color = Color(0xFF3182CE) // Bleu
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY - 5f,
                right = x + 2f,
                bottom = barY - 5f + barHeight + 10f,
                paint = bluePaint
            )
        }
        
        optimaxRefConverti?.let { ref ->
            val x = startX + (ref * scale).toFloat()
            val bluePaint = Paint().apply {
                color = Color(0xFF3182CE) // Bleu
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY - 5f,
                right = x + 2f,
                bottom = barY - 5f + barHeight + 10f,
                paint = bluePaint
            )
        }
        
        maxRefConverti?.let { ref ->
            val x = startX + (ref * scale).toFloat()
            val redPaint = Paint().apply {
                color = Color(0xFFE53E3E) // Rouge
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY - 5f,
                right = x + 2f,
                bottom = barY - 5f + barHeight + 10f,
                paint = redPaint
            )
        }
        
        // Dessiner le titre du nutriment avec la quantité
        val titleText = "$nomNutriment (${(valeurApport * 10).toInt() / 10.0} $unite)"
        drawTextOnCanvas(canvas, titleText, 10f, 20f, 14f, Color.Black)
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
        val width = 400f
        val height = 80f
        
        // Créer une ImageBitmap avec Compose Multiplatform
        val imageBitmap = ImageBitmap(width.toInt(), height.toInt())
        val canvas = Canvas(imageBitmap)
        
        // Calculer les positions
        val maxValue = maxOf(valeurApport, maxRef, optimaxRef)
        val minValue = minOf(0.0, minRef, optiminRef)
        val range = maxValue - minValue
        val scale = (width - 100f) / range.toFloat() // 100px pour les labels
        
        val startX = 50f
        val barY = height / 2 - 10f
        val barHeight = 20f
        
        // Dessiner les barres de référence
        if (minRef > 0) {
            val x = startX + ((minRef - minValue) * scale).toFloat()
            val redPaint = Paint().apply {
                color = Color.Red
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY,
                right = x + 2f,
                bottom = barY + barHeight,
                paint = redPaint
            )
        }
        
        if (optiminRef > 0) {
            val x = startX + ((optiminRef - minValue) * scale).toFloat()
            val bluePaint = Paint().apply {
                color = Color.Blue
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY,
                right = x + 2f,
                bottom = barY + barHeight,
                paint = bluePaint
            )
        }
        
        if (optimaxRef > 0) {
            val x = startX + ((optimaxRef - minValue) * scale).toFloat()
            val bluePaint = Paint().apply {
                color = Color.Blue
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY,
                right = x + 2f,
                bottom = barY + barHeight,
                paint = bluePaint
            )
        }
        
        if (maxRef > 0) {
            val x = startX + ((maxRef - minValue) * scale).toFloat()
            val redPaint = Paint().apply {
                color = Color.Red
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY,
                right = x + 2f,
                bottom = barY + barHeight,
                paint = redPaint
            )
        }
        
        // Dessiner la barre principale (apport)
        val apportX = startX + ((valeurApport - minValue) * scale).toFloat()
        val grayPaint = Paint().apply {
            color = Color.Gray
            style = PaintingStyle.Fill
        }
        canvas.drawRect(
            left = startX,
            top = barY + 5f,
            right = apportX,
            bottom = barY + 5f + 10f,
            paint = grayPaint
        )
        
        // Dessiner les zones colorées
        if (minRef > 0) {
            val x = startX + ((minRef - minValue) * scale).toFloat()
            val lightRedPaint = Paint().apply {
                color = Color(0xFFFFC8C8) // Rouge clair
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = startX,
                top = barY,
                right = x,
                bottom = barY + barHeight,
                paint = lightRedPaint
            )
        }
        
        if (optiminRef > 0 && optimaxRef > 0) {
            val x1 = startX + ((optiminRef - minValue) * scale).toFloat()
            val x2 = startX + ((optimaxRef - minValue) * scale).toFloat()
            val lightGreenPaint = Paint().apply {
                color = Color(0xFFC8FFC8) // Vert clair
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x1,
                top = barY,
                right = x2,
                bottom = barY + barHeight,
                paint = lightGreenPaint
            )
        }
        
        if (maxRef > 0) {
            val x = startX + ((maxRef - minValue) * scale).toFloat()
            val lightRedPaint = Paint().apply {
                color = Color(0xFFFFC8C8) // Rouge clair
                style = PaintingStyle.Fill
            }
            canvas.drawRect(
                left = x,
                top = barY,
                right = width - 50f,
                bottom = barY + barHeight,
                paint = lightRedPaint
            )
        }
        
        // Dessiner les bordures
        val strokePaint = Paint().apply {
            color = Color.Black
            style = PaintingStyle.Stroke
            strokeWidth = 1f
        }
        canvas.drawRect(
            left = startX,
            top = barY,
            right = width - 50f,
            bottom = barY + barHeight,
            paint = strokePaint
        )
        
        // Dessiner des indicateurs visuels pour les valeurs
        drawValueIndicators(canvas, nomNutriment, valeurApport, unite, minRef, optiminRef, optimaxRef, maxRef, width.toInt(), height.toInt())
        
        // Convertir en ByteArray (PNG) - implémentation multiplatform
        return imageBitmap.toByteArray()
    }
    
    /**
     * Sauvegarde un ByteArray d'image dans un fichier temporaire
     */
    fun saveImageToTempFile(imageBytes: ByteArray, prefix: String): String {
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val randomId = (1000..9999).random() // ID aléatoire pour éviter les collisions
        val tempFileName = "${prefix}_bullet_graph_${timestamp}_${randomId}.png"
        return writeTempImageFile(tempFileName, imageBytes)
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
    
    /**
     * Dessine des indicateurs visuels pour les valeurs (nom, quantité, unité)
     */
    private fun drawValueIndicators(
        canvas: Canvas,
        nomNutriment: String,
        valeurApport: Double,
        unite: String,
        minRef: Double,
        optiminRef: Double,
        optimaxRef: Double,
        maxRef: Double,
        width: Int,
        height: Int
    ) {
        // Dessiner un rectangle de fond pour le titre
        val titleBackgroundPaint = Paint().apply {
            color = Color(0xFFF0F0F0)
            style = PaintingStyle.Fill
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 30f, titleBackgroundPaint)
        
        // Dessiner des barres colorées pour représenter la valeur
        val barWidth = (valeurApport * 2).toFloat().coerceAtMost(width.toFloat() - 20f)
        val valueBarPaint = Paint().apply {
            color = when {
                valeurApport > 0 -> Color(0xFF4CAF50) // Vert pour les valeurs positives
                else -> Color(0xFFF44336) // Rouge pour les valeurs nulles/négatives
            }
            style = PaintingStyle.Fill
        }
        canvas.drawRect(10f, 5f, 10f + barWidth, 25f, valueBarPaint)
        
        // Dessiner des marqueurs pour les références
        val maxValue = maxOf(minRef, optiminRef, optimaxRef, maxRef)
        if (maxValue > 0) {
            val referenceBarPaint = Paint().apply {
                color = Color(0xFF2196F3) // Bleu pour les références
                style = PaintingStyle.Fill
            }
            
            // Marqueur pour la valeur minimale
            if (minRef > 0) {
                val minX = 10f + (minRef * 2).toFloat().coerceAtMost(width.toFloat() - 20f)
                canvas.drawRect(minX, 5f, minX + 2f, 25f, referenceBarPaint)
            }
            
            // Marqueur pour la valeur optimale minimale
            if (optiminRef > 0) {
                val optiMinX = 10f + (optiminRef * 2).toFloat().coerceAtMost(width.toFloat() - 20f)
                canvas.drawRect(optiMinX, 5f, optiMinX + 2f, 25f, referenceBarPaint)
            }
        }
    }
}

/**
 * Convertit un ImageBitmap en ByteArray (PNG) - implémentation spécifique par plateforme
 */
expect fun ImageBitmap.toByteArray(): ByteArray

/**
 * Dessine du texte sur un Canvas - implémentation spécifique par plateforme
 */
expect fun drawTextOnCanvas(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    textSize: Float,
    color: Color
)

/**
 * Obtient le nom traduit d'un nutriment - copié depuis HtmlDocumentBuilder.kt
 */
private fun obtenirNomTraduitNutriment(nomNutriment: String, nutrient: Nutrient): String {
    return when (nutrient) {
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientMain -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE -> "Protéines"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE -> "Lipides"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.CELLULOSE -> "Cellulose"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE -> "Énergie"
                else -> nomNutriment
            }
        }
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL -> "Calcium"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS -> "Phosphore"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.MG -> "Magnésium"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.NA -> "Sodium"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.K -> "Potassium"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CHL -> "Chlore"
            }
        }
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientMin -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.FE -> "Fer"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.CU -> "Cuivre"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.ZN -> "Zinc"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.MN -> "Manganèse"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.I -> "Iode"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.SE -> "Sélénium"
            }
        }
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITA -> "Vitamine A"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITD -> "Vitamine D"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITE -> "Vitamine E"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITK -> "Vitamine K"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB1 -> "Vitamine B1"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB2 -> "Vitamine B2"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB3 -> "Vitamine B3"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB5 -> "Vitamine B5"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB6 -> "Vitamine B6"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB9 -> "Vitamine B9"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.VITB12 -> "Vitamine B12"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.CHOLINE -> "Choline"
                else -> nomNutriment
            }
        }
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.AG182 -> "Acide linoléique"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.AG183 -> "Acide α-linolénique"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.O3 -> "Oméga-3"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.O6 -> "Oméga-6"
                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.EPADHA -> "EPA/DHA"
                else -> nomNutriment
            }
        }
        is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis -> {
            when (nutrient) {
                fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.PCa -> "Rapport Ca/P"
                else -> nomNutriment
            }
        }
        else -> nomNutriment
    }
}
