package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.SettingsSection

/**
 * Menu latéral pour la navigation dans les paramètres
 * @param currentSection Section actuellement sélectionnée
 * @param onSectionSelected Callback appelé lors de la sélection d'une section
 * @param onClose Callback appelé pour fermer le drawer
 */
@Composable
fun SettingsDrawer(
        currentSection: SettingsSection,
        onSectionSelected: (SettingsSection) -> Unit,
        onClose: () -> Unit
) {
    Column(
            modifier =
                    Modifier.fillMaxHeight()
                            .width(300.dp)
                            .background(MaterialTheme.colors.surface)
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // En-tête du drawer
        Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paramètres", style = MaterialTheme.typography.h6, color = VetNutriColors.Primary)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.Gray)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Sections de paramètres
        SettingsSectionItem(
                section = SettingsSection.INTERFACE,
                isSelected = currentSection == SettingsSection.INTERFACE,
                onSelected = onSectionSelected,
                icon = Icons.Default.Settings
        )

        SettingsSectionItem(
                section = SettingsSection.PREFERENCES,
                isSelected = currentSection == SettingsSection.PREFERENCES,
                onSelected = onSectionSelected,
                icon = Icons.Default.Settings
        )

        SettingsSectionItem(
                section = SettingsSection.IMPORTATION,
                isSelected = currentSection == SettingsSection.IMPORTATION,
                onSelected = onSectionSelected,
                icon = Icons.Default.Build
        )

        SettingsSectionItem(
                section = SettingsSection.ADMINISTRATION,
                isSelected = currentSection == SettingsSection.ADMINISTRATION,
                onSelected = onSectionSelected,
                icon = Icons.Default.Settings
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider(color = Color.LightGray, thickness = 1.dp)

        // Informations de version
        Text(
                "VetNutri MP",
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text("Version 1.0", style = MaterialTheme.typography.caption, color = Color.Gray)
    }
}

/**
 * Élément d'une section dans le menu latéral
 * @param section Section à afficher
 * @param isSelected Indique si la section est sélectionnée
 * @param onSelected Callback appelé lors de la sélection
 * @param icon Icône à afficher pour la section
 */
@Composable
private fun SettingsSectionItem(
        section: SettingsSection,
        isSelected: Boolean,
        onSelected: (SettingsSection) -> Unit,
        icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val backgroundColor =
            if (isSelected) {
                VetNutriColors.Primary.copy(alpha = 0.1f)
            } else {
                Color.Transparent
            }

    val textColor =
            if (isSelected) {
                VetNutriColors.Primary
            } else {
                Color.DarkGray
            }

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(backgroundColor, RoundedCornerShape(4.dp))
                            .clickable { onSelected(section) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = section.title, tint = textColor)

        Text(
                section.title,
                style = MaterialTheme.typography.body1,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
