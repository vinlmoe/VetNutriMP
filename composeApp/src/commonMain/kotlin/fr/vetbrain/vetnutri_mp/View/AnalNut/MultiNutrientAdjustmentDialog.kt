package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlinx.coroutines.launch

/** Données d'ajustement pour un aliment spécifique */
data class AlimentAdjustmentData(
        val alimentRation: AlimentRation,
        var selectedNutrient: String? = null,
        var isLocked: Boolean = false,
        var mode: ConstraintMode = ConstraintMode.ADJUSTABLE,
        var weight: Double = 1.0,
        var minQuantity: Double = 0.0,
        var maxQuantity: Double = Double.MAX_VALUE,
        var isEnergyAdjustable: Boolean = true
)

/** Modes de contrainte pour un aliment */
enum class ConstraintMode {
        ADJUSTABLE,
        FIXED,
        EXCLUDED
}

/** Contrainte appliquée lors du calcul */
data class AlimentConstraint(
        val alimentUuid: String,
        val mode: ConstraintMode,
        val weight: Double,
        val minQuantity: Double,
        val maxQuantity: Double
)

/** Paramètres de l'algorithme d'ajustement */
data class AdjustmentParams(
        val toleranceGrams: Double = 0.1,
        val roundTo: Double = 0.1,
        val energyLastRebalance: Boolean = true
)

/** Résultat d'un ajustement de ration */
data class RationAdjustmentResult(
        val success: Boolean,
        val message: String,
        val adjustedAliments: List<AlimentRation>? = null
)

/**
 * Récupère la valeur CAP minimale depuis la référence nutritionnelle
 * @param referenceUtilisee La référence nutritionnelle
 * @return La valeur CAP OPTIMIN si disponible, sinon CAP MIN, sinon 1.0 par défaut
 */
private fun obtenirCapMinDepuisReference(referenceUtilisee: ReferenceEv): Double {
        // Essayer d'abord CAP OPTIMIN (priorité maximale)
        val capOptiminRef = referenceUtilisee.obtenirNutrimentRef(NutrientAnalysis.PCa, Reflevel.OPTIMIN)
        if (capOptiminRef != null && capOptiminRef.quantite > 0) {
                println("DEBUG: CAP OPTIMIN trouvé: ${capOptiminRef.quantite}")
                return capOptiminRef.quantite.toDouble()
        }
        
        // Si OPTIMIN n'existe pas, essayer CAP MIN
        val capMinRef = referenceUtilisee.obtenirNutrimentRef(NutrientAnalysis.PCa, Reflevel.MIN)
        if (capMinRef != null && capMinRef.quantite > 0) {
                println("DEBUG: CAP MIN trouvé: ${capMinRef.quantite}")
                return capMinRef.quantite.toDouble()
        }
        
        // Valeur par défaut si aucune référence n'est trouvée
        println("DEBUG: Aucune référence CAP trouvée, utilisation de 1.0 par défaut")
        return 1.0
}

/** Vue complète pour l'ajustement multi-nutriments de la ration */
@Composable
fun MultiNutrientAdjustmentView(
        ration: Ration,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        onConfirm: (RationAdjustmentResult) -> Unit,
        onDismiss: () -> Unit
) {
        var adjustmentData by remember {
                mutableStateOf(
                        ration.alimentMutableList.map { alimentRation ->
                                val suggestion =
                                        suggestDefaultTargetNutrient(
                                                alimentRation,
                                                referenceUtilisee
                                        )
                                AlimentAdjustmentData(
                                        alimentRation = alimentRation,
                                        selectedNutrient = suggestion,
                                        isLocked = false,
                                        isEnergyAdjustable = true
                                )
                        }
                )
        }

        var refLevelByNutrient by remember { mutableStateOf<Map<String, Reflevel>>(emptyMap()) }
        var preview by remember { mutableStateOf<RationAdjustmentResult?>(null) }
        var isProcessing by remember { mutableStateOf(false) }
        var processingMessage by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()

        // Fond opaque pour éviter la transparence
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.surface)) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(AppSizes.paddingMedium)
                                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // En-tête avec titre principal et bouton retour
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Ajustement Multi-Nutriments",
                                        style = MaterialTheme.typography.h5,
                                        fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Fermer",
                                                tint = MaterialTheme.colors.onSurface
                                        )
                                }
                        }

                        // Actions rapides avec icônes dans une Card
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colors.surface,
                                elevation = 4.dp
                        ) {
                                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                                        Text(
                                                text = "Actions rapides",
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                modifier =
                                                        Modifier.padding(
                                                                bottom = AppSizes.paddingSmall
                                                        )
                                        )

                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                // Bouton "Tout verrouiller"
                                                IconButton(
                                                        onClick = {
                                                                adjustmentData =
                                                                        adjustmentData.map { a ->
                                                                                a.copy(
                                                                                        isLocked =
                                                                                                true
                                                                                )
                                                                        }
                                                        },
                                                        modifier = Modifier.size(48.dp)
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.LockPerson,
                                                                contentDescription =
                                                                        "Tout verrouiller",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                }

                                                // Bouton "Tout déverrouiller"
                                                IconButton(
                                                        onClick = {
                                                                adjustmentData =
                                                                        adjustmentData.map { a ->
                                                                                a.copy(
                                                                                        isLocked =
                                                                                                false
                                                                                )
                                                                        }
                                                        },
                                                        modifier = Modifier.size(48.dp)
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.LockOpen,
                                                                contentDescription =
                                                                        "Tout déverrouiller",
                                                                tint = VetNutriColors.Secondary
                                                        )
                                                }

                                                // Bouton "Autoselect"
                                                IconButton(
                                                        onClick = {
                                                                // Auto-sélection pour ceux sans
                                                                // cible
                                                                adjustmentData =
                                                                        adjustmentData.map { a ->
                                                                                if (a.selectedNutrient ==
                                                                                                null
                                                                                ) {
                                                                                        val suggestion =
                                                                                                suggestDefaultTargetNutrient(
                                                                                                        a.alimentRation,
                                                                                                        referenceUtilisee
                                                                                                )
                                                                                        a.copy(
                                                                                                selectedNutrient =
                                                                                                        suggestion
                                                                                        )
                                                                                } else a
                                                                        }
                                                        },
                                                        modifier = Modifier.size(48.dp)
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.AutoAwesome,
                                                                contentDescription =
                                                                        "Sélection automatique",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                }

                                                // Bouton "Réinitialiser"
                                                IconButton(
                                                        onClick = {
                                                                // Réinitialiser les sélections
                                                                adjustmentData =
                                                                        ration.alimentMutableList
                                                                                .map { alimentRation
                                                                                        ->
                                                                                        val suggestion =
                                                                                                suggestDefaultTargetNutrient(
                                                                                                        alimentRation,
                                                                                                        referenceUtilisee
                                                                                                )
                                                                                        AlimentAdjustmentData(
                                                                                                alimentRation =
                                                                                                        alimentRation,
                                                                                                selectedNutrient =
                                                                                                        suggestion,
                                                                                                isLocked =
                                                                                                        false,
                                                                                                isEnergyAdjustable =
                                                                                                        true
                                                                                        )
                                                                                }
                                                                refLevelByNutrient = emptyMap()
                                                                preview = null
                                                        },
                                                        modifier = Modifier.size(48.dp)
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Refresh,
                                                                contentDescription =
                                                                        "Réinitialiser",
                                                                tint = VetNutriColors.Error
                                                        )
                                                }
                                        }
                                }
                        }

                        // Configuration des aliments dans une Card
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colors.surface,
                                elevation = 4.dp
                        ) {
                                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                                        Text(
                                                text = "Configuration des aliments",
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                modifier =
                                                        Modifier.padding(
                                                                bottom = AppSizes.paddingSmall
                                                        )
                                        )

                                        Column(
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                adjustmentData.forEach { alimentData ->
                                                        AlimentAdjustmentItem(
                                                                alimentData = alimentData,
                                                                referenceUtilisee =
                                                                        referenceUtilisee,
                                                                onNutrientChange = { newNutrient ->
                                                                        adjustmentData =
                                                                                adjustmentData
                                                                                        .map { a ->
                                                                                                if (a.alimentRation
                                                                                                                .uuid ==
                                                                                                                alimentData
                                                                                                                        .alimentRation
                                                                                                                        .uuid
                                                                                                ) {
                                                                                                        a.copy(
                                                                                                                selectedNutrient =
                                                                                                                        newNutrient
                                                                                                        )
                                                                                                } else
                                                                                                        a
                                                                                        }
                                                                },
                                                                onLockToggle = {
                                                                        adjustmentData =
                                                                                adjustmentData
                                                                                        .map { a ->
                                                                                                if (a.alimentRation
                                                                                                                .uuid ==
                                                                                                                alimentData
                                                                                                                        .alimentRation
                                                                                                                        .uuid
                                                                                                ) {
                                                                                                        a.copy(
                                                                                                                isLocked =
                                                                                                                        !a.isLocked
                                                                                                        )
                                                                                                } else
                                                                                                        a
                                                                                        }
                                                                },
                                                                onEnergyAdjustableToggle = {
                                                                        isAdjustable ->
                                                                        adjustmentData =
                                                                                adjustmentData
                                                                                        .map { a ->
                                                                                                if (a.alimentRation
                                                                                                                .uuid ==
                                                                                                                alimentData
                                                                                                                        .alimentRation
                                                                                                                        .uuid
                                                                                                ) {
                                                                                                        a.copy(
                                                                                                                isEnergyAdjustable =
                                                                                                                        isAdjustable
                                                                                                        )
                                                                                                } else
                                                                                                        a
                                                                                        }
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }

                        // Sélection des niveaux de référence par nutriment
                        val selectedLabels =
                                remember(adjustmentData) {
                                        adjustmentData.mapNotNull { it.selectedNutrient }.distinct()
                                }
                        if (selectedLabels.isNotEmpty()) {
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = MaterialTheme.colors.surface,
                                        elevation = 4.dp
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium)
                                        ) {
                                                Text(
                                                        text = "Niveaux de référence par nutriment",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier =
                                                                Modifier.padding(
                                                                        bottom =
                                                                                AppSizes.paddingSmall
                                                                )
                                                )

                                                selectedLabels.forEach { label ->
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text = label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                )

                                                                var expanded by remember {
                                                                        mutableStateOf(false)
                                                                }
                                                                val current =
                                                                        refLevelByNutrient[label]
                                                                                ?: Reflevel.OPTIMIN

                                                                Box {
                                                                        OutlinedButton(
                                                                                onClick = {
                                                                                        expanded =
                                                                                                true
                                                                                }
                                                                        ) { Text(current.name) }

                                                                        DropdownMenu(
                                                                                expanded = expanded,
                                                                                onDismissRequest = {
                                                                                        expanded =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Reflevel.entries
                                                                                        .forEach {
                                                                                                refLevel
                                                                                                ->
                                                                                                DropdownMenuItem(
                                                                                                        onClick = {
                                                                                                                refLevelByNutrient =
                                                                                                                        refLevelByNutrient
                                                                                                                                .toMutableMap()
                                                                                                                                .apply {
                                                                                                                                        put(
                                                                                                                                                label,
                                                                                                                                                refLevel
                                                                                                                                        )
                                                                                                                                }
                                                                                                                expanded =
                                                                                                                        false
                                                                                                        }
                                                                                                ) {
                                                                                                        Text(
                                                                                                                refLevel.name
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Actions dans une Card
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colors.surface,
                                elevation = 4.dp
                        ) {
                                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                                        Text(
                                                text = "Actions",
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                modifier =
                                                        Modifier.padding(
                                                                bottom = AppSizes.paddingSmall
                                                        )
                                        )

                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                                OutlinedButton(
                                                        onClick = {
                                                                scope.launch {
                                                                        isProcessing = true
                                                                        processingMessage =
                                                                                "Calcul en cours..."
                                                                        preview =
                                                                                calculerAjustement(
                                                                                        ration =
                                                                                                ration,
                                                                                        adjustmentData =
                                                                                                adjustmentData,
                                                                                        referenceUtilisee =
                                                                                                referenceUtilisee,
                                                                                        besoinEnergetiqueTotal =
                                                                                                besoinEnergetiqueTotal,
                                                                                        besoinEnergetiqueStandard =
                                                                                                besoinEnergetiqueStandard,
                                                                                        poidsAnimal =
                                                                                                poidsAnimal,
                                                                                        poidsMetabolique =
                                                                                                poidsMetabolique
                                                                                )
                                                                        isProcessing = false
                                                                        processingMessage = ""
                                                                }
                                                        },
                                                        enabled =
                                                                !isProcessing &&
                                                                        adjustmentData.any {
                                                                                it.selectedNutrient !=
                                                                                        null
                                                                        },
                                                        modifier = Modifier.weight(1f)
                                                ) { Text("Prévisualiser") }

                                                Button(
                                                        onClick = {
                                                                scope.launch {
                                                                        isProcessing = true
                                                                        processingMessage =
                                                                                "Ajustement en cours..."
                                                                        val result =
                                                                                calculerAjustement(
                                                                                        ration =
                                                                                                ration,
                                                                                        adjustmentData =
                                                                                                adjustmentData,
                                                                                        referenceUtilisee =
                                                                                                referenceUtilisee,
                                                                                        besoinEnergetiqueTotal =
                                                                                                besoinEnergetiqueTotal,
                                                                                        besoinEnergetiqueStandard =
                                                                                                besoinEnergetiqueStandard,
                                                                                        poidsAnimal =
                                                                                                poidsAnimal,
                                                                                        poidsMetabolique =
                                                                                                poidsMetabolique
                                                                                )
                                                                        isProcessing = false
                                                                        processingMessage = ""
                                                                        onConfirm(result)
                                                                }
                                                        },
                                                        enabled =
                                                                !isProcessing &&
                                                                        adjustmentData.any {
                                                                                it.selectedNutrient !=
                                                                                        null
                                                                        },
                                                        modifier = Modifier.weight(1f)
                                                ) { Text("Ajuster") }
                                        }

                                        // Affichage du message de traitement
                                        if (isProcessing) {
                                                Column(
                                                        modifier =
                                                                Modifier.padding(
                                                                        top = AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        LinearProgressIndicator(
                                                                modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Text(
                                                                text = processingMessage,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        ),
                                                                modifier =
                                                                        Modifier.padding(
                                                                                top =
                                                                                        AppSizes.paddingSmall
                                                                        )
                                                        )
                                                }
                                        }
                                }
                        }

                        // Affichage de la prévisualisation
                        preview?.let { result ->
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor =
                                                if (result.success) Color(0xFFE8F5E8)
                                                else Color(0xFFFFEBEE),
                                        elevation = 4.dp
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium)
                                        ) {
                                                Text(
                                                        text =
                                                                if (result.success)
                                                                        "✅ Ajustement réussi"
                                                                else "❌ Échec de l'ajustement",
                                                        fontWeight = FontWeight.Bold,
                                                        color =
                                                                if (result.success)
                                                                        Color(0xFF2E7D32)
                                                                else Color(0xFFC62828)
                                                )
                                                Text(
                                                        text = result.message,
                                                        modifier =
                                                                Modifier.padding(
                                                                        top = AppSizes.paddingSmall
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

/**
 * Suggère automatiquement le nutriment cible à ajuster pour un aliment, en adaptant la logique
 * fournie. Les comparaisons se font sur matière sèche (MS): 100 * valeur / (100 - HUMIDITE).
 *
 * @param alimentRation L'aliment ration pour lequel suggérer un nutriment cible
 * @param referenceUtilisee La référence utilisée pour vérifier la disponibilité des nutriments
 * @return Le label du nutriment suggéré ou null si aucun n'est disponible
 */
fun suggestDefaultTargetNutrient(
        alimentRation: AlimentRation,
        referenceUtilisee: ReferenceEv
): String? {
        val aliment = alimentRation.aliment ?: return null
        val kind: FoodKind? = aliment.typeAliment

        // Si aliment complet → par défaut ÉNERGIE (ajustement énergétique par défaut)
        if (kind == FoodKind.COMPLET) return NutrientMain.ENERGIE.label

        fun n(nutrient: Nutrient): Double {
                return aliment.valMap[nutrient]?.value ?: 0.0
        }

        val humidite: Double = n(NutrientMain.HUMIDITE).coerceIn(0.0, 99.9)
        val msDenom: Double = (100.0 - humidite).takeIf { it > 0.001 } ?: 100.0
        fun toMsPercent(value: Double): Double = (100.0 * value) / msDenom

        val cendreMs: Double = toMsPercent(n(NutrientMain.CENDRE))
        val proteineMs: Double = toMsPercent(n(NutrientMain.PROTEINE))
        val lipideMs: Double = toMsPercent(n(NutrientMain.LIPIDE))
        val enaMs: Double = toMsPercent(n(NutrientMain.ENA))
        val celluloseMs: Double = toMsPercent(n(NutrientMain.CELLULOSE))
        val o6Ms: Double = toMsPercent(n(NutrientLipid.O6))
        val epaDha: Double = n(NutrientLipid.EPADHA)

        // Adaptation des cibles: chaînes compatibles avec nos labels de nutriments pour
        // pré-sélection
        // targetAdjust.CALCIUMPHOS → favoriser le Calcium (CAL) en pratique
        // targetAdjust.PROT → PROTEINE
        // targetAdjust.O6 → O6
        // targetAdjust.EPA → EPADHA
        // targetAdjust.LIP → LIPIDE
        // targetAdjust.FIBER → CELLULOSE
        // L'énergie est maintenant proposée mais traitée en dernier dans l'ordre de traitement

        // Fonction helper pour vérifier si un nutriment a des références disponibles
        fun hasReferenceForNutrient(nutrient: Nutrient): Boolean {
                return Reflevel.entries.any { refLevel ->
                        referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
                }
        }

        return when {
                // (Cendres/MS > 10) et présence de Calcium
                (cendreMs > 10.0 &&
                        aliment.valMap.containsKey(NutrientMacro.CAL) &&
                        hasReferenceForNutrient(NutrientMacro.CAL)) -> NutrientMacro.CAL.label
                // Protéines/MS > 30
                (proteineMs > 30.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.PROTEINE.label
                // Lipides/MS > 30 et O6/MS > 15
                (lipideMs > 30.0 && o6Ms > 15.0 && hasReferenceForNutrient(NutrientLipid.O6)) ->
                        NutrientLipid.O6.label
                // Lipides/MS > 30 et EPADHA > 0
                (lipideMs > 30.0 &&
                        epaDha > 0.0 &&
                        hasReferenceForNutrient(NutrientLipid.EPADHA)) -> NutrientLipid.EPADHA.label
                // Lipides/MS > 40
                (lipideMs > 40.0 && hasReferenceForNutrient(NutrientMain.LIPIDE)) ->
                        NutrientMain.LIPIDE.label
                // Cellulose/MS > 20
                (celluloseMs > 20.0 && hasReferenceForNutrient(NutrientMain.CELLULOSE)) ->
                        NutrientMain.CELLULOSE.label
                // ENA/MS > 50 → énergie/carbs
                (enaMs > 50.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.ENERGIE.label
                // Pour tous les autres cas (aliments inclassables) → ÉNERGIE par défaut
                // L'énergie est toujours disponible, pas besoin de vérifier les références
                else -> NutrientMain.ENERGIE.label
        }
}

/** Composant pour un aliment dans le dialog d'ajustement */
@Composable
private fun AlimentAdjustmentItem(
        alimentData: AlimentAdjustmentData,
        referenceUtilisee: ReferenceEv,
        onNutrientChange: (String?) -> Unit,
        onLockToggle: () -> Unit,
        onEnergyAdjustableToggle: (Boolean) -> Unit
) {
        val aliment = alimentData.alimentRation.aliment
        if (aliment == null) return

        // Filtrer les nutriments disponibles pour cet aliment spécifique
        val availableNutrients =
                remember(aliment, referenceUtilisee) {
                        val labels = mutableSetOf<String>()
                        val valMap = aliment.valMap
                        if (valMap != null) {
                                for ((nutr, qty) in valMap.entries) {
                                        if (qty.value > 0) {
                                                // Vérifier que le nutriment a au moins une
                                                // référence disponible
                                                val hasReference =
                                                        Reflevel.entries.any { refLevel ->
                                                                referenceUtilisee
                                                                        .obtenirNutrimentRef(
                                                                                nutr,
                                                                                refLevel
                                                                        ) != null
                                                        }
                                                if (hasReference) {
                                                        labels.add(nutr.label)
                                                }
                                        }
                                }
                        }
                        // Ajouter ÉNERGIE car elle peut maintenant être sélectionnée comme
                        // nutriment cible
                        // (l'énergie est toujours disponible, pas besoin de vérifier les
                        // références)
                        // L'énergie doit toujours être disponible pour tous les aliments
                        labels.add(NutrientMain.ENERGIE.label)

                        // Appliquer le même ordre de priorité que dans le calcul
                        val orderedLabels = mutableListOf<String>()

                        // 1er : NutrientMain (sans l'énergie)
                        val mainNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMain.entries.any {
                                                        it.label == label &&
                                                                it != NutrientMain.ENERGIE
                                                }
                                        }
                                        .sorted()
                        orderedLabels.addAll(mainNutrients)

                        // 2ème : NutrientMacro
                        val macroNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMacro.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(macroNutrients)

                        // 3ème : NutrientMin
                        val minNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMin.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(minNutrients)

                        // 4ème : NutrientVitam
                        val vitamNutrients =
                                labels
                                        .filter { label ->
                                                NutrientVitam.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(vitamNutrients)

                        // 5ème : NutrientLipid
                        val lipidNutrients =
                                labels
                                        .filter { label ->
                                                NutrientLipid.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(lipidNutrients)

                        // 6ème : AAEnum
                        val aaNutrients =
                                labels
                                        .filter { label ->
                                                AAEnum.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(aaNutrients)

                        // 7ème : NutrientOther
                        val otherNutrients =
                                labels
                                        .filter { label ->
                                                NutrientOther.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(otherNutrients)

                        // Dernier : ENERGIE (toujours en dernier)
                        if (labels.contains(NutrientMain.ENERGIE.label)) {
                                orderedLabels.add(NutrientMain.ENERGIE.label)
                        }

                        orderedLabels
                }

        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor =
                        if (alimentData.isLocked) MaterialTheme.colors.surface.copy(alpha = 0.5f)
                        else MaterialTheme.colors.surface
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = aliment.nom ?: "Aliment inconnu",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                                if (alimentData.isLocked)
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.5f
                                                        )
                                                else MaterialTheme.colors.onSurface
                                )

                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        // Case pour l'ajustement énergétique
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Text(
                                                        text = "Énergie",
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Checkbox(
                                                        checked = alimentData.isEnergyAdjustable,
                                                        onCheckedChange = { isChecked ->
                                                                onEnergyAdjustableToggle(isChecked)
                                                        },
                                                        enabled = !alimentData.isLocked,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }

                                        // Bouton de verrouillage
                                        IconButton(
                                                onClick = onLockToggle,
                                                modifier = Modifier.size(24.dp)
                                        ) {
                                                Icon(
                                                        imageVector =
                                                                if (alimentData.isLocked)
                                                                        Icons.Filled.Lock
                                                                else Icons.Filled.LockOpen,
                                                        contentDescription =
                                                                if (alimentData.isLocked)
                                                                        "Déverrouiller"
                                                                else "Verrouiller",
                                                        tint =
                                                                if (alimentData.isLocked) Color.Gray
                                                                else VetNutriColors.Primary
                                                )
                                        }
                                }
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text =
                                                "Quantité actuelle: ${alimentData.alimentRation.quantite}g",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        if (!alimentData.isLocked) {
                                DropdownField(
                                        label = "Nutriment cible",
                                        selectedValue = alimentData.selectedNutrient,
                                        onValueChange = { value -> onNutrientChange(value) },
                                        options = availableNutrients,
                                        valueToString = { it },
                                        enabled = !alimentData.isLocked,
                                        modifier = Modifier.fillMaxWidth()
                                )
                        } else {
                                Text(
                                        text = "Verrouillé - Aucun ajustement",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                )
                        }
                }
        }
}

/** Calcule l'ajustement multi-nutriments de la ration */
suspend fun calculerAjustement(
        ration: Ration,
        adjustmentData: List<AlimentAdjustmentData>,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): RationAdjustmentResult {
        try {
                // Créer une copie des aliments pour les ajustements
                val adjustedAliments = ration.alimentMutableList.map { it.copy() }.toMutableList()

                // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
                val alimentsVerrouilles = mutableSetOf<String>()

                for (i in adjustedAliments.indices) {
                        val alimentUuid = adjustedAliments[i].uuid
                        val estVerrouille =
                                adjustmentData.any {
                                        it.alimentRation.uuid == alimentUuid && it.isLocked
                                }

                        if (estVerrouille) {
                                alimentsVerrouilles.add(alimentUuid)
                        } else {
                                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
                        }
                }

                // Étape 2: Traiter les nutriments sélectionnés par l'utilisateur, avec ordre
                // dynamique
                val nutrimentsTraites = mutableSetOf<String>()
                val processingOrder = buildProcessingOrderFromSelections(adjustmentData)

                adjustmentData.forEach { data -> }

                // PREMIÈRE ÉTAPE : Ajuster tous les nutriments sauf l'énergie
                val nutrimentsNonEnergetiques = processingOrder.filter { it != "ENERGIE" }
                for (nutrientLabel in nutrimentsNonEnergetiques) {
                        val nutrient = findNutrientByLabel(nutrientLabel)
                        if (nutrient == null) {

                                continue
                        }

                        // Calculer le besoin absolu en fonction du nutriment
                        val rl = Reflevel.OPTIMIN
                        val nutrimentRef = referenceUtilisee.obtenirNutrimentRef(nutrient, rl)
                        if (nutrimentRef == null || nutrimentRef.quantite <= 0) {

                                continue
                        }

                        val besoinAbsoluGrammes =
                                calculerBesoinAbsoluGrammes(
                                        nutrimentRef = nutrimentRef,
                                        poidsAnimal = poidsAnimal,
                                        poidsMetabolique = poidsMetabolique,
                                        besoinEnergetiqueReference = besoinEnergetiqueStandard
                                )

                        if (besoinAbsoluGrammes <= 0) {

                                continue
                        }

                        // Trouver les aliments ajustables pour ce nutriment
                        val constraintByUuid = emptyMap<String, AlimentConstraint>()
                        val alimentsAjustables =
                                adjustmentData.filter {
                                        it.selectedNutrient == nutrientLabel && !it.isLocked
                                }

                        if (alimentsAjustables.isEmpty()) {

                                continue
                        }

                        // Calculer l'apport actuel pour ce nutriment
                        var apportActuel = 0.0
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val aliment = alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                                apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                        }

                        val manque = besoinAbsoluGrammes - apportActuel

                        if (manque > 0.01) {

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = nutrient,
                                                manque = manque,
                                                alimentsAjustables = alimentsAjustables,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                preferences = null,
                                                equationRepository = null
                                        )

                                if (!result.success) {

                                        return result
                                } else {}
                        } else {}

                        nutrimentsTraites.add(nutrientLabel)
                }

                // Vérification et ajustement CAP après tous les nutriments
                val calciumSelectionne: Boolean = adjustmentData.any { it.selectedNutrient == NutrientMacro.CAL.label && !it.isLocked }
                println("DEBUG: Calcium sélectionné: $calciumSelectionne")
                
                if (calciumSelectionne) {
                        val capMinRequis: Double = obtenirCapMinDepuisReference(referenceUtilisee)
                        val alimentsCalcium = adjustmentData.filter { 
                                it.selectedNutrient == NutrientMacro.CAL.label && !it.isLocked 
                        }
                        
                        if (alimentsCalcium.isNotEmpty()) {
                                // Ajustement itératif pour respecter le ratio CAP
                                var iterations = 0
                                val maxIterations = 10
                                
                        while (iterations < maxIterations) {
                                // Utiliser la même logique que le système pour calculer les totaux
                                var totalCalcium: Double = 0.0
                                var totalPhosphore: Double = 0.0
                                
                                // Recalculer les totaux avec les quantités actuelles (même logique que calculerQuantiteTotaleNutriment)
                                for (i in adjustedAliments.indices) {
                                        val aliment = adjustedAliments[i].aliment ?: continue
                                        val quantite = adjustedAliments[i].quantite.toDouble()
                                        
                                        // Calcium
                                        val calPar100g = aliment.getNutrient(NutrientMacro.CAL)
                                        if (calPar100g != null) {
                                                totalCalcium += (calPar100g * quantite) / 100.0
                                        }
                                        
                                        // Phosphore
                                        val pPar100g = aliment.getNutrient(NutrientMacro.PHOS)
                                        if (pPar100g != null) {
                                                totalPhosphore += (pPar100g * quantite) / 100.0
                                        }
                                }
                                
                                println("DEBUG: Itération $iterations - Total Calcium: ${String.format("%.4f", totalCalcium)}, Total Phosphore: ${String.format("%.4f", totalPhosphore)}")
                                        
                                        if (totalPhosphore > 0.0) {
                                                val ratioActuel: Double = totalCalcium / totalPhosphore
                                                println("DEBUG: Ratio Ca/P actuel: ${String.format("%.4f", ratioActuel)}, CAP requis: ${String.format("%.4f", capMinRequis)}")
                                                
                                                // Vérifier si le ratio est suffisant (avec une petite tolérance)
                                                if (ratioActuel >= capMinRequis - 0.001) {
                                                        println("DEBUG: Ratio CAP atteint après $iterations itérations (${String.format("%.4f", ratioActuel)} >= ${String.format("%.4f", capMinRequis)})")
                                                        break
                                                }
                                                
                                                // Calculer le facteur d'ajustement nécessaire
                                                val facteurAjustement: Double = capMinRequis / ratioActuel
                                                println("DEBUG: Facteur d'ajustement nécessaire: ${String.format("%.4f", facteurAjustement)}")
                                                
                                                // Ajuster les aliments calcium proportionnellement
                                                var ajustementEffectue = false
                                                for (alimentData in alimentsCalcium) {
                                                        val index = adjustedAliments.indexOfFirst { 
                                                                it.uuid == alimentData.alimentRation.uuid 
                                                        }
                                                        if (index >= 0) {
                                                                val quantiteActuelle: Double = adjustedAliments[index].quantite.toDouble()
                                                                val nouvelleQuantite: Double = quantiteActuelle * facteurAjustement
                                                                // Arrondir au centième de gramme pour éviter les erreurs de précision
                                                                val quantiteArrondie: Double = kotlin.math.round(nouvelleQuantite * 100.0) / 100.0
                                                                adjustedAliments[index] = adjustedAliments[index].copy(
                                                                        quantite = quantiteArrondie
                                                                )
                                                                ajustementEffectue = true
                                                                println("DEBUG: Aliment ${alimentData.alimentRation.aliment?.nom}: ${String.format("%.2f", quantiteActuelle)}g → ${String.format("%.2f", quantiteArrondie)}g")
                                                        }
                                                }
                                                
                                                if (!ajustementEffectue) {
                                                        println("DEBUG: Aucun ajustement possible")
                                                        break
                                                }
                                        } else {
                                                println("DEBUG: Aucun phosphore détecté")
                                                break
                                        }
                                        
                                        iterations++
                                }
                                
                                if (iterations >= maxIterations) {
                                        println("DEBUG: Maximum d'itérations atteint pour l'ajustement CAP")
                                }
                        }
                }

                // DEUXIÈME ÉTAPE : Ajuster l'énergie en recalculant l'apport total de la ration
                // finale
                if (processingOrder.contains("ENERGIE")) {

                        // Créer une ration temporaire avec les ajustements effectués
                        val rationTemp = ration.copy()
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val alimentRationTemp =
                                        rationTemp.alimentMutableList.find {
                                                it.uuid == alimentRation.uuid
                                        }
                                if (alimentRationTemp != null) {
                                        // Créer une nouvelle instance avec la quantité modifiée
                                        val index =
                                                rationTemp.alimentMutableList.indexOf(
                                                        alimentRationTemp
                                                )
                                        if (index >= 0) {
                                                rationTemp.alimentMutableList[index] =
                                                        alimentRationTemp.copy(
                                                                quantite = alimentRation.quantite
                                                        )
                                        }
                                }
                        }

                        // Calculer l'apport énergétique total de la ration finale
                        var apportEnergetiqueTotal = 0.0
                        for (alimentRation in rationTemp.alimentMutableList) {
                                if (alimentRation.quantite > 0.01) {
                                        // Utiliser la même logique que le système principal pour
                                        // assurer la cohérence
                                        val energieAliment =
                                                alimentRation.getEnergie(
                                                        referenceUtilisee,
                                                        null,
                                                        null
                                                )
                                        apportEnergetiqueTotal += energieAliment
                                }
                        }

                        val manqueEnergie = besoinEnergetiqueTotal - apportEnergetiqueTotal

                        if (manqueEnergie > 0.01) {
                                // PRIORITÉ : Utiliser d'abord les aliments qui ont l'énergie comme
                                // nutriment
                                // principal
                                val constraintByUuid = emptyMap<String, AlimentConstraint>()

                                // 1. Aliments avec l'énergie comme nutriment principal (priorité
                                // maximale)
                                val alimentsEnergiePrincipale =
                                        adjustmentData.filter {
                                                it.selectedNutrient == "ENERGIE" && !it.isLocked
                                        }

                                // 2. Aliments avec ajustement énergétique secondaire (priorité
                                // secondaire)
                                // IMPORTANT : On n'ajuste les autres aliments pour l'énergie QUE si
                                // aucun aliment n'a l'énergie comme nutriment principal
                                val alimentsEnergieSecondaire =
                                        if (alimentsEnergiePrincipale.isNotEmpty()) {
                                                // Si on a des aliments avec l'énergie comme
                                                // nutriment principal,
                                                // on n'ajuste PAS les autres aliments pour
                                                // l'énergie
                                                emptyList()
                                        } else {
                                                // Seulement si aucun aliment n'a l'énergie comme
                                                // nutriment principal
                                                adjustmentData.filter {
                                                        it.selectedNutrient !=
                                                                "ENERGIE" && // Pas déjà traité
                                                                // comme nutriment
                                                                // principal
                                                                it.isEnergyAdjustable &&
                                                                !it.isLocked
                                                }
                                        }

                                // Combiner les deux listes avec priorité aux aliments énergie
                                // principale
                                val alimentsAjustablesEnergie =
                                        alimentsEnergiePrincipale + alimentsEnergieSecondaire

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = NutrientMain.ENERGIE,
                                                manque = manqueEnergie,
                                                alimentsAjustables = alimentsAjustablesEnergie,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                preferences = null,
                                                equationRepository = null
                                        )

                                if (result.success) {

                                        nutrimentsTraites.add("ENERGIE")
                                } else {

                                        return RationAdjustmentResult(
                                                success = false,
                                                message =
                                                        "Échec de l'ajustement énergétique: ${result.message}",
                                                adjustedAliments = null
                                        )
                                }
                        } else {

                                nutrimentsTraites.add("ENERGIE")
                        }
                }

                // Rééquilibrage énergie final désactivé car l'énergie est maintenant traitée comme
                // les
                // autres nutriments
                // L'énergie est ajustée dans la boucle principale des nutriments
                /*
                /*
                // Code commenté pour éviter les erreurs de compilation
                // TODO: Implémenter la logique d'ajustement énergétique final
                if (params.energyLastRebalance) {
                    // Logique d'ajustement énergétique final à implémenter
                }
                */
                */

                // Arrondi final pour stabiliser l'UI - arrondi au gramme
                for (i in adjustedAliments.indices) {
                        val q: Double = adjustedAliments[i].quantite.toDouble()
                        val rounded: Double = kotlin.math.round(q)
                        adjustedAliments[i] = adjustedAliments[i].copy(quantite = rounded)
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments",
                        adjustedAliments = adjustedAliments
                )
        } catch (e: Exception) {
                return RationAdjustmentResult(
                        success = false,
                        message = "Erreur lors de l'ajustement séquentiel: ${e.message}"
                )
        }
}

/** Calcule le besoin absolu en grammes pour un nutriment donné */
private fun calculerBesoinAbsoluGrammes(
        nutrimentRef: ReferenceEv.Nut4Ref,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueReference: Double
): Double {
        val quantite = nutrimentRef.quantite.toDouble()
        val uniteBase = nutrimentRef.unite
        val uniteRequis = nutrimentRef.uniteReq

        // Conversion initiale en grammes
        val quantiteEnGrammes: Double = quantite * uniteBase.conv.toDouble()

        // Calcul du besoin absolu en fonction de l'unité requise
        return when (uniteRequis) {
                UnitReqEnum.PERKG -> quantiteEnGrammes * (poidsAnimal ?: 0.0)
                UnitReqEnum.PERMS -> quantiteEnGrammes * (poidsMetabolique ?: 0.0)
                UnitReqEnum.PERKCAL -> (quantiteEnGrammes / 1000.0) * besoinEnergetiqueReference
                else -> quantiteEnGrammes // Si c'est déjà en besoin journalier
        }
}

/** Obtient l'ordre de traitement des nutriments */
private fun buildProcessingOrderFromSelections(
        adjustmentData: List<AlimentAdjustmentData>
): List<String> {
        val selected = adjustmentData.mapNotNull { it.selectedNutrient }.toSet()
        if (selected.isEmpty()) return emptyList()

        val energyLabel = NutrientMain.ENERGIE.label
        // L'énergie doit être traitée si elle est sélectionnée OU si au moins un aliment a
        // l'énergie
        // cochée
        val hasEnergy =
                selected.contains(energyLabel) || adjustmentData.any { it.isEnergyAdjustable }

        fun isOf(label: String, group: Array<out Nutrient>): Boolean {
                return group.any { it.label == label }
        }

        val nonEnergy = selected.filter { it != energyLabel }

        val order = mutableListOf<String>()

        // Priorité: Main (sans l'énergie) > Macro > Minéraux > Vitamines > Lipides > AA > Autres,
        // trié
        // par label
        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

        if (hasEnergy) order += energyLabel // Toujours en dernier

        // Dédupliquer en conservant l'ordre
        return order.distinct()
}

private fun buildProcessingOrderFromSelectionLabels(labels: List<String>): List<String> {
        val selected = labels.toSet()
        if (selected.isEmpty()) return emptyList()

        val energyLabel = NutrientMain.ENERGIE.label
        val hasEnergy = selected.contains(energyLabel)

        fun isOf(label: String, group: Array<out Nutrient>): Boolean {
                return group.any { it.label == label }
        }

        val nonEnergy = selected.filter { it != energyLabel }

        val order = mutableListOf<String>()

        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

        if (hasEnergy) order += energyLabel // Toujours en dernier
        return order.distinct()
}

/** Trouve un nutriment par son label */
private fun findNutrientByLabel(label: String): Nutrient? {
        return when {
                NutrientMain.entries.any { it.label == label } ->
                        NutrientMain.entries.find { it.label == label }
                NutrientLipid.entries.any { it.label == label } ->
                        NutrientLipid.entries.find { it.label == label }
                NutrientVitam.entries.any { it.label == label } ->
                        NutrientVitam.entries.find { it.label == label }
                NutrientMacro.entries.any { it.label == label } ->
                        NutrientMacro.entries.find { it.label == label }
                NutrientMin.entries.any { it.label == label } ->
                        NutrientMin.entries.find { it.label == label }
                NutrientOther.entries.any { it.label == label } ->
                        NutrientOther.entries.find { it.label == label }
                AAEnum.entries.any { it.label == label } ->
                        AAEnum.entries.find { it.label == label }
                else -> null
        }
}

/** Vérifie quels niveaux de référence sont disponibles pour un nutriment donné */
private fun getAvailableRefLevelsForNutrient(
        nutrient: Nutrient,
        referenceUtilisee: ReferenceEv
): List<Reflevel> {
        return Reflevel.entries.filter { refLevel ->
                referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
        }
}

/** Ajuste la ration pour un nutriment spécifique */
private fun adjustRationForNutrient(
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        nutrient: Nutrient,
        besoinAbsoluGrammes: Double,
        referenceUtilisee: ReferenceEv
): RationAdjustmentResult {
        try {
                // Calculer l'apport actuel du nutriment
                var apportActuel = 0.0
                for (alimentData in alimentsAjustables) {
                        val aliment = alimentData.alimentRation.aliment ?: continue
                        val quantiteNutriment: Double =
                                (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                        val quantiteAliment: Double = alimentData.alimentRation.quantite.toDouble()
                        apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                }

                if (apportActuel <= 0) {
                        return RationAdjustmentResult(
                                success = false,
                                message = "Aucun apport en ${nutrient.label} détecté"
                        )
                }

                // Calculer le ratio d'ajustement
                val ratio = besoinAbsoluGrammes / apportActuel

                // Appliquer l'ajustement aux aliments concernés
                for (alimentData in alimentsAjustables) {
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index >= 0) {
                                val quantiteActuelle: Double =
                                        alimentData.alimentRation.quantite.toDouble()
                                val nouvelleQuantite: Double = quantiteActuelle * ratio
                                // Arrondir au gramme
                                val nouvelleQuantiteArrondie: Double =
                                        kotlin.math.round(nouvelleQuantite)
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )
                        }
                }

                return RationAdjustmentResult(
                        success = true,
                        message = "Ajustement pour ${nutrient.label} réussi"
                )
        } catch (e: Exception) {
                return RationAdjustmentResult(
                        success = false,
                        message = "Erreur lors de l'ajustement: ${e.message}"
                )
        }
}

/** Ajuste les aliments pour couvrir le manque d'un nutriment spécifique */
private fun ajusterAlimentsPourNutriment(
        nutriment: Nutrient,
        manque: Double,
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        alimentsVerrouilles: Set<String>,
        constraints: Map<String, AlimentConstraint> = emptyMap(),
        referenceUtilisee: ReferenceEv? = null,
        preferences: PreferencesEspece? = null,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
): RationAdjustmentResult {
        try {
                // Filtrer les aliments disponibles pour l'ajustement
                val alimentsDisponibles =
                        alimentsAjustables.filter { data ->
                                if (alimentsVerrouilles.contains(data.alimentRation.uuid))
                                        return@filter false
                                val aliment = data.alimentRation.aliment ?: return@filter false
                                if (nutriment == NutrientMain.ENERGIE) {
                                        // Utiliser la référence pour déterminer la disponibilité
                                        // énergétique
                                        val energiePar100gRef: Double =
                                                aliment.getNutrient(
                                                        NutrientMain.ENERGIE,
                                                        referenceUtilisee
                                                )
                                                        ?: 0.0
                                        val densite: Double =
                                                if (energiePar100gRef > 0.0)
                                                        energiePar100gRef / 100.0
                                                else data.alimentRation.densiteEnergetique
                                        densite > 0.0
                                } else {
                                        (aliment.valMap[nutriment]?.value ?: 0.0) > 0.0
                                }
                        }

                if (alimentsDisponibles.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        "Aucun aliment disponible pour le nutriment ${nutriment.label}"
                        )
                }

                // Calculer la contribution potentielle de chaque aliment
                val contributions = mutableListOf<Triple<AlimentAdjustmentData, Double, Int>>()

                for (alimentData in alimentsDisponibles) {
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index < 0) continue
                        if (nutriment == NutrientMain.ENERGIE) {
                                val energiePar100gRef: Double =
                                        alimentData.alimentRation.aliment?.getNutrient(
                                                NutrientMain.ENERGIE,
                                                referenceUtilisee
                                        )
                                                ?: 0.0
                                val densite: Double =
                                        if (energiePar100gRef > 0.0) energiePar100gRef / 100.0
                                        else adjustedAliments[index].densiteEnergetique
                                if (densite > 0.0) {
                                        val quantiteNecessaire = manque / densite
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        } else {
                                val aliment = alimentData.alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap[nutriment]?.value ?: 0.0).toDouble()
                                if (quantiteNutriment > 0.0) {
                                        // Quantité (g) nécessaire pour couvrir le manque (g) à
                                        // partir de valeur/100g
                                        val quantiteNecessaire =
                                                (manque * 100.0) / quantiteNutriment
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        }
                }

                if (contributions.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        "Aucune contribution possible pour le nutriment ${nutriment.label}"
                        )
                }

                // Appliquer une pondération par poids (préférence d'usage) avant tri
                for (i in contributions.indices) {
                        val (data, need, idx) = contributions[i]
                        val w =
                                (constraints[data.alimentRation.uuid]?.weight ?: data.weight)
                                        .coerceIn(0.0, 1.0)
                        val adjNeed = if (w > 0.0) need / w else Double.POSITIVE_INFINITY
                        contributions[i] = Triple(data, adjNeed, idx)
                }
                // Trier par efficacité pondérée
                contributions.sortBy { it.second }

                // Répartir le manque entre les aliments les plus efficaces
                var manqueRestant = manque
                var indexContribution = 0
                var totalAjoute = 0.0

                while (manqueRestant > 0.01 &&
                        indexContribution < contributions.size) { // Tolérance
                        val (alimentData, quantiteNecessaire, index) =
                                contributions[indexContribution]
                        val quantiteAAjouter =
                                if (nutriment == NutrientMain.ENERGIE) {
                                        val energiePar100gRef: Double =
                                                adjustedAliments[index].aliment?.getNutrient(
                                                        NutrientMain.ENERGIE,
                                                        referenceUtilisee
                                                )
                                                        ?: 0.0
                                        val densite: Double =
                                                if (energiePar100gRef > 0.0)
                                                        energiePar100gRef / 100.0
                                                else adjustedAliments[index].densiteEnergetique
                                        if (densite <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                manqueRestant / densite
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant / densite) / alimentsRestants
                                        }
                                } else {
                                        val aliment = alimentData.alimentRation.aliment
                                        val quantiteNutriment: Double =
                                                (aliment?.valMap?.get(nutriment)?.value ?: 0.0)
                                                        .toDouble()
                                        if (quantiteNutriment <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                (manqueRestant * 100.0) / quantiteNutriment
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant * 100.0) /
                                                        (quantiteNutriment * alimentsRestants)
                                        }
                                }

                        if (quantiteAAjouter > 0) {
                                // Ajouter cette quantité à l'aliment en respectant min/max
                                val quantiteActuelle: Double = adjustedAliments[index].quantite
                                val minQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.minQuantity
                                                ?: 0.0
                                val maxQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.maxQuantity
                                                ?: Double.MAX_VALUE
                                val nouvelleQuantite: Double =
                                        (quantiteActuelle + quantiteAAjouter).coerceIn(minQ, maxQ)
                                // Arrondir au gramme
                                val nouvelleQuantiteArrondie: Double =
                                        kotlin.math.round(nouvelleQuantite)
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )

                                // Mettre à jour le manque restant
                                val apportAjoute: Double =
                                        if (nutriment == NutrientMain.ENERGIE) {
                                                val energiePar100gRef: Double =
                                                        adjustedAliments[index].aliment
                                                                ?.getNutrient(
                                                                        NutrientMain.ENERGIE,
                                                                        referenceUtilisee
                                                                )
                                                                ?: 0.0
                                                val densite: Double =
                                                        if (energiePar100gRef > 0.0)
                                                                energiePar100gRef / 100.0
                                                        else
                                                                adjustedAliments[index]
                                                                        .densiteEnergetique
                                                densite * quantiteAAjouter
                                        } else {
                                                val aliment = alimentData.alimentRation.aliment
                                                val quantiteNutriment: Double =
                                                        (aliment?.valMap?.get(nutriment)?.value
                                                                        ?: 0.0).toDouble()
                                                (quantiteNutriment * quantiteAAjouter) / 100.0
                                        }
                                manqueRestant -= apportAjoute
                                totalAjoute += apportAjoute
                        }

                        indexContribution++
                }

                if (manqueRestant > 0.01) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        "Impossible de couvrir complètement le besoin en ${nutriment.label}. Manque: ${TextUtils.formatDecimal(manqueRestant, 2)}g, Ajouté: ${TextUtils.formatDecimal(totalAjoute, 2)}g"
                        )
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                "Ajustement réussi pour ${nutriment.label}: ajouté ${TextUtils.formatDecimal(totalAjoute, 2)}g"
                )
        } catch (e: Exception) {
                return RationAdjustmentResult(
                        success = false,
                        message =
                                "Erreur lors de l'ajustement pour ${nutriment.label}: ${e.message}"
                )
        }
}

/** Ajuste la ration pour plusieurs nutriments de manière séquentielle */
private fun adjustRationForMultipleNutrients(
        besoinsNutriments: Map<String, Double>,
        alimentsParNutriment: Map<String, List<AlimentAdjustmentData>>,
        adjustedAliments: MutableList<AlimentRation>,
        referenceUtilisee: ReferenceEv
): RationAdjustmentResult {
        try {
                // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
                val alimentsVerrouilles = mutableSetOf<String>()

                for (i in adjustedAliments.indices) {
                        val alimentUuid = adjustedAliments[i].uuid
                        val estVerrouille =
                                alimentsParNutriment.values.flatten().any {
                                        it.alimentRation.uuid == alimentUuid && it.isLocked
                                }

                        if (estVerrouille) {
                                alimentsVerrouilles.add(alimentUuid)
                        } else {
                                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
                        }
                }

                // Étape 2: Traiter les nutriments selon les sélections utilisateur
                val nutrimentsTraites = mutableSetOf<String>()
                val processingOrder =
                        buildProcessingOrderFromSelectionLabels(alimentsParNutriment.keys.toList())

                for (nutrientLabel in processingOrder) {
                        val besoinAbsolu = besoinsNutriments[nutrientLabel] ?: continue
                        val alimentsAjustables = alimentsParNutriment[nutrientLabel] ?: continue
                        val nutrient = findNutrientByLabel(nutrientLabel) ?: continue

                        // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments
                        // déjà ajustés)
                        var apportActuel = 0.0
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val aliment = alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                                apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                        }

                        // Étape 4: Calculer ce qui manque
                        val manque = besoinAbsolu - apportActuel

                        val unite = if (nutrientLabel == NutrientMain.ENERGIE.label) "kcal" else "g"

                        if (manque > 0.01) { // Tolérance de 0.01g
                                // Étape 5: Ajuster les aliments pour couvrir le manque

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = nutrient,
                                                manque = manque,
                                                alimentsAjustables = alimentsAjustables,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles
                                        )

                                if (!result.success) {

                                        return result
                                } else {}
                        } else {}

                        nutrimentsTraites.add(nutrientLabel)
                }

                // Vérification et ajustement CAP après tous les nutriments
                val calciumSelectionne: Boolean = alimentsParNutriment.keys.any { it == NutrientMacro.CAL.label }
                if (calciumSelectionne) {
                        val capMinRequis: Double = obtenirCapMinDepuisReference(referenceUtilisee)
                        val alimentsCalcium = alimentsParNutriment[NutrientMacro.CAL.label] ?: emptyList()
                        
                        if (alimentsCalcium.isNotEmpty()) {
                                // Ajustement itératif pour respecter le ratio CAP
                                var iterations = 0
                                val maxIterations = 10
                                
                        while (iterations < maxIterations) {
                                // Utiliser la même logique que le système pour calculer les totaux
                                var totalCalcium: Double = 0.0
                                var totalPhosphore: Double = 0.0
                                
                                // Recalculer les totaux avec les quantités actuelles (même logique que calculerQuantiteTotaleNutriment)
                                for (i in adjustedAliments.indices) {
                                        val aliment = adjustedAliments[i].aliment ?: continue
                                        val quantite = adjustedAliments[i].quantite.toDouble()
                                        
                                        // Calcium
                                        val calPar100g = aliment.getNutrient(NutrientMacro.CAL)
                                        if (calPar100g != null) {
                                                totalCalcium += (calPar100g * quantite) / 100.0
                                        }
                                        
                                        // Phosphore
                                        val pPar100g = aliment.getNutrient(NutrientMacro.PHOS)
                                        if (pPar100g != null) {
                                                totalPhosphore += (pPar100g * quantite) / 100.0
                                        }
                                }
                                        
                                        if (totalPhosphore > 0.0) {
                                                val ratioActuel: Double = totalCalcium / totalPhosphore
                                                
                                                // Vérifier si le ratio est suffisant
                                                if (ratioActuel >= capMinRequis) {
                                                        break
                                                }
                                                
                                                // Calculer le facteur d'ajustement nécessaire
                                                val facteurAjustement: Double = capMinRequis / ratioActuel
                                                
                                                // Ajuster les aliments calcium proportionnellement
                                                var ajustementEffectue = false
                                                for (alimentData in alimentsCalcium) {
                                                        val index = adjustedAliments.indexOfFirst { 
                                                                it.uuid == alimentData.alimentRation.uuid 
                                                        }
                                                        if (index >= 0) {
                                                                val quantiteActuelle: Double = adjustedAliments[index].quantite.toDouble()
                                                                val nouvelleQuantite: Double = quantiteActuelle * facteurAjustement
                                                                adjustedAliments[index] = adjustedAliments[index].copy(
                                                                        quantite = kotlin.math.round(nouvelleQuantite)
                                                                )
                                                                ajustementEffectue = true
                                                        }
                                                }
                                                
                                                if (!ajustementEffectue) {
                                                        break
                                                }
                                        } else {
                                                break
                                        }
                                        
                                        iterations++
                                }
                        }
                }

                return RationAdjustmentResult(
                        success = true,
                        message =
                                "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments"
                )
        } catch (e: Exception) {
                return RationAdjustmentResult(
                        success = false,
                        message = "Erreur lors de l'ajustement séquentiel: ${e.message}"
                )
        }
}
