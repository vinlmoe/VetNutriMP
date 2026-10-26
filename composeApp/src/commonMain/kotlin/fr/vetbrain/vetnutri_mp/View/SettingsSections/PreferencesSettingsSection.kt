package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.ViewModel.PreferencesViewModel
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Composant pour la section des préférences */
@Composable
internal fun PreferencesSection(
        preferencesViewModel: PreferencesViewModel,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        PreferencesContentWithPersistence(
                preferencesViewModel = preferencesViewModel,
                modifier = modifier,
                onSpeciesClick = onSpeciesClick
        )
}

/** Contenu des préférences avec persistance réelle */
@Composable
private fun PreferencesContentWithPersistence(
        preferencesViewModel: PreferencesViewModel,
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
                        currentPreferences = preferencesViewModel.loadPreferences()
                        preferencesLoaded = true
                } catch (e: Exception) {
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
                                                                                        preferencesViewModel.savePreferences(
                                                                                                        updated
                                                                                                )
                                                                                        currentPreferences =
                                                                                                updated
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) {} finally {
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
                                                preferencesViewModel = preferencesViewModel,
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
        preferencesViewModel: PreferencesViewModel,
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
                                                                                                        preferencesViewModel.savePreferences(
                                                                                                                        updatedPrefs
                                                                                                                )

                                                                                                        // Mettre à jour l'UI
                                                                                                        onPreferencesChanged(
                                                                                                                updatedPrefs
                                                                                                        )
                                                                                                } catch (
                                                                                                        e:
                                                                                                                Exception) {} finally {
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
