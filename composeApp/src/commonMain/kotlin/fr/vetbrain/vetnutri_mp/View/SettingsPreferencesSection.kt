package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Composant pour la section des préférences */
@Composable
internal fun PreferencesSection(
        viewModel: SettingsViewModel,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        if (viewModel.preferencesAvailable) {
                // Utiliser le vrai système de persistance
                PreferencesContentWithPersistence(
                        settingsViewModel = viewModel,
                        modifier = modifier,
                        onSpeciesClick = onSpeciesClick
                )
        } else {
                // Fallback temporaire si PreferencesStorage n'est pas disponible
                PreferencesContentSimplified(modifier = modifier)
        }
}

/** Contenu des préférences avec persistance réelle */
@Composable
private fun PreferencesContentWithPersistence(
        settingsViewModel: SettingsViewModel,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // État pour les préférences chargées
        var preferencesLoaded by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var currentPreferences by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }
        var isLoading by remember { mutableStateOf(false) }

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                try {
                        isLoading = true
                        currentPreferences = settingsViewModel.getLoadedPreferences()
                        preferencesLoaded = true
                } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        // Utiliser des préférences par défaut
                        currentPreferences = fr.vetbrain.vetnutri_mp.Data.PreferencesApplication()
                        preferencesLoaded = true
                } finally {
                        isLoading = false
                }
        }

        if (isLoading) {
                // Indicateur de chargement
                Box(
                        modifier = modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(translate("analnut.msg.loading_preferences"))
                        }
                }
        } else if (preferencesLoaded && currentPreferences != null) {
                Column(
                        modifier = modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        var nomUtilisateur by remember {
                                mutableStateOf(currentPreferences!!.nomUtilisateur)
                        }
                        var numeroOrdre by remember {
                                mutableStateOf(currentPreferences!!.numeroOrdre)
                        }
                        var adressePostale by remember {
                                mutableStateOf(currentPreferences!!.adressePostale)
                        }
                        var codePostal by remember { mutableStateOf(currentPreferences!!.codePostal) }
                        var ville by remember { mutableStateOf(currentPreferences!!.ville) }
                        var telephone by remember { mutableStateOf(currentPreferences!!.telephone) }
                        var email by remember { mutableStateOf(currentPreferences!!.email) }
                        var isSavingUser by remember { mutableStateOf(false) }
                        Text(
                                text = translate("auto.view.settingsview.informations_utilisateur"),
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White,
                                elevation = 2.dp
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        OutlinedTextField(
                                                value = nomUtilisateur,
                                                onValueChange = { nomUtilisateur = it },
                                                label = { Text(translate("auto.view.settingsview.nom_de_l_utilisateur")) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = numeroOrdre,
                                                onValueChange = { numeroOrdre = it },
                                                label = { Text(translate("auto.view.settingsview.numero_d_ordre")) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = adressePostale,
                                                onValueChange = { adressePostale = it },
                                                label = { Text(translate("auto.view.settingsview.adresse_postale")) },
                                                singleLine = false,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                        value = codePostal,
                                                        onValueChange = { codePostal = it },
                                                        label = { Text(translate("auto.view.settingsview.code_postal")) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                        value = ville,
                                                        onValueChange = { ville = it },
                                                        label = { Text(translate("auto.view.settingsview.ville")) },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(2f)
                                                )
                                        }
                                        OutlinedTextField(
                                                value = telephone,
                                                onValueChange = { telephone = it },
                                                label = { Text(translate("auto.view.settingsview.telephone")) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = email,
                                                onValueChange = { email = it },
                                                label = { Text(translate("auto.view.settingsview.email")) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                                try {
                                                                                        isSavingUser =
                                                                                                true
                                                                                        val updated =
                                                                                                currentPreferences!!
                                                                                                        .copy(
                                                                                                                nomUtilisateur =
                                                                                                                        nomUtilisateur,
                                                                                                                numeroOrdre =
                                                                                                                        numeroOrdre,
                                                                                                                adressePostale =
                                                                                                                        adressePostale,
                                                                                                                codePostal =
                                                                                                                        codePostal,
                                                                                                                ville = ville,
                                                                                                                telephone =
                                                                                                                        telephone,
                                                                                                                email =
                                                                                                                        email
                                                                                                        )
                                                                                        settingsViewModel
                                                                                                .savePreferences(
                                                                                                        updated
                                                                                                )
                                                                                        currentPreferences =
                                                                                                updated
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) { e.rethrowIfCancellation(); AppLog.d("SettingsView", "Exception ignorée : ${e.message}") } finally {
                                                                                        isSavingUser =
                                                                                                false
                                                                                }
                                                                        }
                                                        },
                                                        enabled = !isSavingUser,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                ) { Text(translate("auto.view.settingsview.enregistrer"), color = Color.White) }
                                                if (isSavingUser) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(18.dp),
                                                                color = VetNutriColors.Primary,
                                                                strokeWidth = 2.dp
                                                        )
                                                }
                                        }
                                }
                        }
                        Text(
                                text = translate("auto.view.settingsview.expression_des_besoins_par_espece"),
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Text(
                                text =
                                        translate("auto.view.settingsview.definissez_pour_chaque_espece_comment_exprimer_l"),
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )

                        // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                        LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(
                                        fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                                fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                                        )
                                ) { espece ->
                                        SpeciesPreferenceCardWithPersistence(
                                                species = espece,
                                                settingsViewModel = settingsViewModel,
                                                currentPreferences = currentPreferences!!,
                                                onPreferencesChanged = { newPreferences ->
                                                        currentPreferences = newPreferences
                                                },
                                                onSpeciesClick = onSpeciesClick
                                        )
                                }
                        }

                        // Informations sur la persistance
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                elevation = 1.dp
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = translate("auto.view.settingsview.persistance_active"),
                                                        tint = VetNutriColors.Primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        text = translate("auto.view.settingsview.persistance_active"),
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        translate("auto.view.settingsview.vos_preferences_sont_automatiquement_sauvegardee"),
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}

/** Card de préférences avec persistance pour une espèce */
@Composable
private fun SpeciesPreferenceCardWithPersistence(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        settingsViewModel: SettingsViewModel,
        currentPreferences: fr.vetbrain.vetnutri_mp.Data.PreferencesApplication,
        onPreferencesChanged: (fr.vetbrain.vetnutri_mp.Data.PreferencesApplication) -> Unit,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        val speciesPreferences = currentPreferences.getPreferencesEspece(species)
        val currentExpressionType = speciesPreferences.getTypeExpressionBesoinEnum()

        Card(
                modifier = Modifier.fillMaxWidth().clickable { onSpeciesClick(species) },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                        text =
                                                                "Expression: ${currentExpressionType.displayName}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Primary
                                                )
                                                if (isSaving) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp),
                                                                color = VetNutriColors.Primary,
                                                                strokeWidth = 2.dp
                                                        )
                                                }
                                        }
                                }

                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expanded) "Réduire" else "Développer",
                                        tooltip = if (expanded) "Réduire" else "Développer"
                                )
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = translate("auto.view.settingsview.type_d_expression_des_besoins"),
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable(
                                                                                enabled = !isSaving
                                                                        ) {
                                                                                // Sauvegarder avec
                                                                                // persistance
                                                                                coroutineScope
                                                                                        .launch {
                                                                                                try {
                                                                                                        isSaving =
                                                                                                                true

                                                                                                        // Mettre à jour les préférences
                                                                                                        val updatedSpeciesPrefs =
                                                                                                                speciesPreferences
                                                                                                                        .copy(
                                                                                                                                typeExpressionBesoinId =
                                                                                                                                        type.id
                                                                                                                        )

                                                                                                        val updatedPrefs =
                                                                                                                currentPreferences
                                                                                                                        .updatePreferencesEspece(
                                                                                                                                updatedSpeciesPrefs
                                                                                                                        )

                                                                                                        // Sauvegarder dans PreferencesStorage
                                                                                                        settingsViewModel
                                                                                                                .savePreferences(
                                                                                                                        updatedPrefs
                                                                                                                )

                                                                                                        // Mettre à jour l'UI
                                                                                                        onPreferencesChanged(
                                                                                                                updatedPrefs
                                                                                                        )
                                                                                                } catch (
                                                                                                        e:
                                                                                                                Exception) { e.rethrowIfCancellation(); AppLog.d("SettingsView", "Exception ignorée : ${e.message}") } finally {
                                                                                                        isSaving =
                                                                                                                false
                                                                                                }
                                                                                        }
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = null, // Géré par le
                                                                // clickable du Row
                                                                enabled = !isSaving
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color =
                                                                                if (isSaving)
                                                                                        Color.Gray
                                                                                else Color.Black
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}

/** Contenu simplifié des préférences qui fonctionne sans PreferencesStorage */
@Composable
private fun PreferencesContentSimplified(modifier: Modifier = Modifier) {
        // État local pour simuler les préférences (sera remplacé par le vrai système)
        val speciesExpressionTypes = remember {
                mutableStateMapOf<
                        fr.vetbrain.vetnutri_mp.Enumer.Espece,
                        fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin>()
        }
        var nomUtilisateur by remember { mutableStateOf("") }
        var numeroOrdre by remember { mutableStateOf("") }
        var adressePostale by remember { mutableStateOf("") }
        var telephone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        Column(
                modifier = modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                        text = translate("auto.view.settingsview.informations_utilisateur"),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        elevation = 2.dp
                ) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                OutlinedTextField(
                                        value = nomUtilisateur,
                                        onValueChange = { nomUtilisateur = it },
                                        label = { Text(translate("auto.view.settingsview.nom_de_l_utilisateur")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = numeroOrdre,
                                        onValueChange = { numeroOrdre = it },
                                        label = { Text(translate("auto.view.settingsview.numero_d_ordre")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = adressePostale,
                                        onValueChange = { adressePostale = it },
                                        label = { Text(translate("auto.view.settingsview.adresse_postale")) },
                                        singleLine = false,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = telephone,
                                        onValueChange = { telephone = it },
                                        label = { Text(translate("auto.view.settingsview.telephone")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text(translate("auto.view.settingsview.email")) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                        text =
                                                translate("auto.view.settingsview.ces_informations_ne_sont_pas_encore_persistees_d"),
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                        }
                }
                Text(
                        text = translate("auto.view.settingsview.expression_des_besoins_par_espece"),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                Text(
                        text =
                                translate("auto.view.settingsview.definissez_pour_chaque_espece_comment_exprimer_l_3"),
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                )

                // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                        )
                        .forEach { espece ->
                                SpeciesPreferenceCardSimplified(
                                        species = espece,
                                        currentExpressionType = speciesExpressionTypes[espece]
                                                        ?: fr.vetbrain.vetnutri_mp.Enumer
                                                                .TypeExpressionBesoin.DEFAULT,
                                        onExpressionTypeChanged = { newType ->
                                                // Repli sans PreferencesStorage disponible : le choix
                                                // ne vaut que pour la session (la persistance est
                                                // assurée par PreferencesContentWithPersistence).
                                                speciesExpressionTypes[espece] = newType
                                        }
                                )
                        }

                // Note d'information
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 1.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = translate("settings.feedback.info"),
                                                tint = VetNutriColors.Primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = translate("auto.view.settingsview.persistance_des_donnees"),
                                                style = MaterialTheme.typography.subtitle2,
                                                fontWeight = FontWeight.Bold,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text =
                                                translate("auto.view.settingsview.les_preferences_sont_actuellement_stockees_tempo"),
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                        }
                }
        }
}

/** Card de préférences simplifiée pour une espèce */
@Composable
private fun SpeciesPreferenceCardSimplified(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        currentExpressionType: fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin,
        onExpressionTypeChanged: (fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }

        Card(
                modifier =
                        Modifier.fillMaxWidth().clickable {
                                onExpressionTypeChanged(currentExpressionType)
                        },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text =
                                                        "Expression: ${currentExpressionType.displayName}",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expanded) "Réduire" else "Développer",
                                        tooltip = if (expanded) "Réduire" else "Développer"
                                )
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = translate("auto.view.settingsview.type_d_expression_des_besoins"),
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onExpressionTypeChanged(
                                                                                        type
                                                                                )
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = {
                                                                        onExpressionTypeChanged(
                                                                                type
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}
