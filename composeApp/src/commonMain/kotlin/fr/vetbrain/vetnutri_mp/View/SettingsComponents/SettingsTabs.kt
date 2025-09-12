package fr.vetbrain.vetnutri_mp.View.SettingsComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.SettingsSection

/**
 * Navigation par onglets pour les paramètres avec animations
 * @param selectedTab Index de l'onglet sélectionné
 * @param onTabSelected Callback appelé lors de la sélection d'un onglet
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun SettingsTabs(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tabs =
            listOf(
                    TabInfo(
                            title = "Interface",
                            icon = Icons.Default.Settings,
                            section = SettingsSection.INTERFACE
                    ),
                    TabInfo(
                            title = "Préférences",
                            icon = Icons.Default.Person,
                            section = SettingsSection.PREFERENCES
                    ),
                    TabInfo(
                            title = "Import/Export",
                            icon = Icons.Default.Build,
                            section = SettingsSection.IMPORTATION
                    ),
                    TabInfo(
                            title = "Excel",
                            icon = Icons.Default.TableChart,
                            section = SettingsSection.EXCEL
                    ),
                    TabInfo(
                            title = "Recettes",
                            icon = Icons.Default.Restaurant,
                            section = SettingsSection.RECIPES
                    ),
                    TabInfo(
                            title = "Administration",
                            icon = Icons.Default.AdminPanelSettings,
                            section = SettingsSection.ADMINISTRATION
                    )
            )

    Column(modifier = modifier) {
        // Navigation par onglets avec animations
        TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = VetNutriColors.Primary
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                val scale by
                        animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1.0f,
                                animationSpec = tween(durationMillis = 200),
                                label = "tab_scale"
                        )

                Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.padding(vertical = 8.dp).scale(scale)
                ) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                        )
                        Text(
                                text = tab.title,
                                style = MaterialTheme.typography.body2,
                                fontWeight =
                                        if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Ligne de séparation avec animation
        Divider(color = VetNutriColors.Primary.copy(alpha = 0.2f), thickness = 1.dp)
    }
}

/**
 * Informations sur un onglet
 * @param title Titre de l'onglet
 * @param icon Icône de l'onglet
 * @param section Section correspondante
 */
private data class TabInfo(
        val title: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val section: SettingsSection
)
