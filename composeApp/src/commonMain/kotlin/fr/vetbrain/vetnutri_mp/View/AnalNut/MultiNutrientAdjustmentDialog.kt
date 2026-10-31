package fr.vetbrain.vetnutri_mp.View.AnalNut

import fr.vetbrain.vetnutri_mp.Data.calculerAjustement
import fr.vetbrain.vetnutri_mp.Data.suggestDefaultTargetNutrient
import fr.vetbrain.vetnutri_mp.Data.toRationAdjustmentResult
import fr.vetbrain.vetnutri_mp.Data.RationAdjustmentResult
import fr.vetbrain.vetnutri_mp.Data.AlimentAdjustmentData
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
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
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.Data.adjustRationByConstraints

/** Vue complète pour l'ajustement multi-nutriments de la ration */
@Composable
fun MultiNutrientAdjustmentView(
        ration: Ration,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
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
        var pourcentageEnergieCompletsText by remember { mutableStateOf("") }
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
                                        text = translate(LocalizationKeys.AnalNut.ADJUSTMENT_TITLE),
                                        style = MaterialTheme.typography.h5,
                                        fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = translate(LocalizationKeys.AnalNut.CLOSE),
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
                                                text = translate(LocalizationKeys.AnalNut.QUICK_ACTIONS),
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
                                                                        translate(LocalizationKeys.AnalNut.LOCK_ALL),
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
                                                                        translate(LocalizationKeys.AnalNut.UNLOCK_ALL),
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
                                                                        translate(LocalizationKeys.AnalNut.AUTO_SELECT),
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
                                                                        translate(LocalizationKeys.AnalNut.RESET),
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
                                                text = translate(LocalizationKeys.AnalNut.FOOD_CONFIG),
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
                                                        text = translate(LocalizationKeys.AnalNut.REF_LEVELS_BY_NUTRIENT),
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
                                                                                    if (!isIosPlatform) {

                                                                                        expanded =
                                                                                                false

                                                                                    }
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

                        // Part d'énergie fixe à couvrir par les aliments complets
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colors.surface,
                                elevation = 4.dp
                        ) {
                                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                                        OutlinedTextField(
                                                value = pourcentageEnergieCompletsText,
                                                onValueChange = { pourcentageEnergieCompletsText = it },
                                                label = { Text(translate(LocalizationKeys.AnalNut.ENERGY_SHARE_COMPLETE_FOOD_LABEL)) },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                                text = translate(LocalizationKeys.AnalNut.ENERGY_SHARE_COMPLETE_FOOD_HINT),
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(top = AppSizes.paddingSmall)
                                        )
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
                                                text = translate(LocalizationKeys.AnalNut.ACTIONS),
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
                                                                                translate(LocalizationKeys.AnalNut.CALCULATING)
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
                                                                                                poidsMetabolique,
                                                                                        equationRepository =
                                                                                                equationRepository,
                                                                                        pourcentageEnergieAlimentsComplets =
                                                                                                pourcentageEnergieCompletsText.takeIf { it.isNotBlank() }?.let { it.replace(',', '.').toDoubleOrNull() ?: Double.NaN }
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
                                                ) { Text(translate(LocalizationKeys.AnalNut.PREVIEW)) }

                                                Button(
                                                        onClick = {
                                                                scope.launch {
                                                                        isProcessing = true
                                                                        processingMessage =
                                                                                translate(LocalizationKeys.AnalNut.ADJUSTING_MSG)
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
                                                                                                poidsMetabolique,
                                                                                        equationRepository =
                                                                                                equationRepository,
                                                                                        pourcentageEnergieAlimentsComplets =
                                                                                                pourcentageEnergieCompletsText.takeIf { it.isNotBlank() }?.let { it.replace(',', '.').toDoubleOrNull() ?: Double.NaN }
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
                                                ) { Text(translate(LocalizationKeys.AnalNut.ADJUST)) }

                                                // Nouveau bouton: Ajuster l'énergie en conservant les proportions (quantités égales)
                                                OutlinedButton(
                                                        onClick = {
                                                                scope.launch {
                                                                        isProcessing = true
                                                                        processingMessage = translate(LocalizationKeys.AnalNut.ADJUSTING_ENERGY_MSG)

                                                                        try {
                                                                                // Calculer l'énergie actuelle de la ration
                                                                                var energieActuelle = 0.0
                                                                                for (alimentRation in ration.alimentMutableList) {
                                                                                        if (alimentRation.quantite > 0.0) {
                                                                                                val e = alimentRation.getEnergie(
                                                                                                        referenceUtilisee,
                                                                                                        equationRepository
                                                                                                )
                                                                                                energieActuelle += e
                                                                                        }
                                                                                }

                                                                                if (energieActuelle <= 0.0) {
                                                                                        val result = RationAdjustmentResult(
                                                                                                success = false,
                                                                                                message = translate(LocalizationKeys.AnalNut.ENERGY_NULL_ERROR),
                                                                                                adjustedAliments = null
                                                                                        )
                                                                                        preview = result
                                                                                } else {
                                                                                        val facteur = besoinEnergetiqueTotal / energieActuelle
                                                                                        // Appliquer le même facteur à toutes les quantités
                                                                                        val adjustedAliments = ration.alimentMutableList.map { ar ->
                                                                                                val nouvelleQuantite = kotlin.math.round(ar.quantite * facteur)
                                                                                                ar.copy(quantite = nouvelleQuantite.coerceAtLeast(0.0))
                                                                                        }
                                                                                        val result = RationAdjustmentResult(
                                                                                                success = true,
                                                                                                message = translate(LocalizationKeys.AnalNut.ADJUST_ENERGY_SUCCESS, TextUtils.formatDecimal(facteur, 3)),
                                                                                                adjustedAliments = adjustedAliments
                                                                                        )
                                                                                        preview = result
                                                                                        // Appliquer immédiatement comme l'action "Ajuster"
                                                                                        onConfirm(result)
                                                                                }
                                                                        } catch (e: Exception) {
                                                                                e.rethrowIfCancellation()
                                                                                val result = RationAdjustmentResult(
                                                                                        success = false,
                                                                                        message = translate(LocalizationKeys.AnalNut.ADJUST_FAIL) + ": ${e.message}"
                                                                                )
                                                                                preview = result
                                                                        } finally {
                                                                                isProcessing = false
                                                                                processingMessage = ""
                                                                        }
                                                                }
                                                        },
                                                        enabled = !isProcessing && pourcentageEnergieCompletsText.isBlank(),
                                                        modifier = Modifier.weight(1f)
                                                ) { Text(translate(LocalizationKeys.AnalNut.ADJUST_ENERGY_EQUAL)) }

                                                // Ajustement par programmation sous contrainte (LP) : cherche une
                                                // solution qui satisfait simultanément toutes les bornes MIN/OPTIMIN
                                                // et MAX/OPTIMAX de la référence, contrairement à "Ajuster" qui ne
                                                // traite qu'un nutriment sélectionné à la fois.
                                                Button(
                                                        onClick = {
                                                                scope.launch {
                                                                        isProcessing = true
                                                                        processingMessage =
                                                                                translate(LocalizationKeys.AnalNut.CALCULATING_CONSTRAINTS)
                                                                        val constraintResult =
                                                                                adjustRationByConstraints(
                                                                                        ration = ration,
                                                                                        adjustmentData = adjustmentData,
                                                                                        referenceUtilisee = referenceUtilisee,
                                                                                        besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                                                                        besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                                                                        poidsAnimal = poidsAnimal,
                                                                                        poidsMetabolique = poidsMetabolique,
                                                                                        equationRepository = equationRepository,
                                                                                        pourcentageEnergieAlimentsComplets =
                                                                                                pourcentageEnergieCompletsText.takeIf { it.isNotBlank() }?.let { it.replace(',', '.').toDoubleOrNull() ?: Double.NaN }
                                                                                )
                                                                        val result = constraintResult.toRationAdjustmentResult()
                                                                        isProcessing = false
                                                                        processingMessage = ""
                                                                        preview = result
                                                                        if (result.success) {
                                                                                onConfirm(result)
                                                                        }
                                                                }
                                                        },
                                                        enabled = !isProcessing,
                                                        modifier = Modifier.weight(1f)
                                                ) { Text(translate(LocalizationKeys.AnalNut.ADJUST_CONSTRAINTS)) }
                                        }

                                        Text(
                                                text = translate(LocalizationKeys.AnalNut.ADJUST_CONSTRAINTS_HINT),
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(top = AppSizes.paddingSmall)
                                        )

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
                                                                        translate(LocalizationKeys.AnalNut.ADJUST_SUCCESS)
                                                                else translate(LocalizationKeys.AnalNut.ADJUST_FAIL),
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
                                        text = aliment.nom ?: translate("analnut.contribution.unknown_food"),
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
                                                        text = NutrientMain.ENERGIE.translateEnum(),
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
                                                                        translate(LocalizationKeys.AnalNut.UNLOCK)
                                                                else translate(LocalizationKeys.AnalNut.LOCK),
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
                                                translate(LocalizationKeys.AnalNut.CURRENT_QUANTITY, alimentData.alimentRation.quantite.toString()),
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        if (!alimentData.isLocked) {
                                DropdownField(
                                        label = translate(LocalizationKeys.AnalNut.TARGET_NUTRIENT),
                                        selectedValue = alimentData.selectedNutrient,
                                        onValueChange = { value -> onNutrientChange(value) },
                                        options = availableNutrients,
                                        valueToString = { it },
                                        enabled = !alimentData.isLocked,
                                        modifier = Modifier.fillMaxWidth()
                                )
                        } else {
                                Text(
                                        text = translate(LocalizationKeys.AnalNut.LOCKED_NO_ADJUSTMENT),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                )
                        }
                }
        }
}
