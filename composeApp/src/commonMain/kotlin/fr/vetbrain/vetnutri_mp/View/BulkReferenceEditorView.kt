package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BulkReferenceEditorViewModel

private val NUTRIENT_COL_WIDTH = 170.dp
private val REF_COL_WIDTH = 130.dp

private val EDITABLE_CATEGORIES =
        listOf(
                MainNutrientEnum.MACRO,
                MainNutrientEnum.MIN,
                MainNutrientEnum.VITAM,
                MainNutrientEnum.LIPID,
                MainNutrientEnum.AMA,
                MainNutrientEnum.OTHER,
                MainNutrientEnum.ANA,
                MainNutrientEnum.BASE
        )

private val LEVELS = Reflevel.values().toList()

@Composable
fun BulkReferenceEditorView(
        viewModel: BulkReferenceEditorViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val references by viewModel.references.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val nutrients = remember(selectedCategory) { viewModel.getNutrientsForCategory(selectedCategory) }
    val hScroll = rememberScrollState()

    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Édition groupée (${references.size} référence${if (references.size > 1) "s" else ""})",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { paddingValues ->
        if (loading) {
            Box(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
            }
            return@Scaffold
        }

        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (error.isNotBlank()) {
                Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.caption
                )
            }

            // --- Onglets catégories ---
            val categoryIndex = EDITABLE_CATEGORIES.indexOf(selectedCategory).coerceAtLeast(0)
            ScrollableTabRow(
                    selectedTabIndex = categoryIndex,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = VetNutriColors.Primary,
                    edgePadding = 0.dp
            ) {
                EDITABLE_CATEGORIES.forEachIndexed { idx, cat ->
                    Tab(
                            selected = idx == categoryIndex,
                            onClick = { viewModel.selectCategory(cat) },
                            text = { Text(cat.label, maxLines = 1) }
                    )
                }
            }

            // --- Onglets niveaux ---
            val levelIndex = LEVELS.indexOf(selectedLevel).coerceAtLeast(0)
            TabRow(
                    selectedTabIndex = levelIndex,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = VetNutriColors.Primary
            ) {
                LEVELS.forEachIndexed { idx, level ->
                    Tab(
                            selected = idx == levelIndex,
                            onClick = { viewModel.selectLevel(level) },
                            text = { Text(level.name) }
                    )
                }
            }

            // --- En-tête du tableau ---
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colors.surface)
                                    .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Nutriment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.width(NUTRIENT_COL_WIDTH).padding(start = 8.dp)
                )
                Row(modifier = Modifier.horizontalScroll(hScroll)) {
                    references.forEach { ref ->
                        Text(
                                text = ref.nom,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier =
                                        Modifier.width(REF_COL_WIDTH)
                                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Divider()

            // --- Lignes de données ---
            if (nutrients.isEmpty()) {
                Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            "Aucun nutriment pour cette catégorie",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(nutrients, key = { it.label }) { nutrient ->
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colonne fixe — nom du nutriment
                            Text(
                                    text = nutrient.label,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                            Modifier.width(NUTRIENT_COL_WIDTH).padding(start = 8.dp)
                            )

                            // Colonnes défilantes — une cellule par référence
                            Row(modifier = Modifier.horizontalScroll(hScroll)) {
                                references.forEach { ref ->
                                    // État local pour ne pas perdre le focus à chaque frappe.
                                    // Se réinitialise quand le niveau ou la catégorie change.
                                    var cellValue by remember(
                                            ref.uuid,
                                            nutrient.label,
                                            selectedLevel,
                                            selectedCategory
                                    ) {
                                        mutableStateOf(
                                                viewModel.getCellValue(ref.uuid, nutrient.label)
                                        )
                                    }
                                    OutlinedTextField(
                                            value = cellValue,
                                            onValueChange = { newVal ->
                                                cellValue = newVal
                                                viewModel.updateCell(
                                                        ref.uuid,
                                                        nutrient.label,
                                                        newVal
                                                )
                                            },
                                            modifier =
                                                    Modifier.width(REF_COL_WIDTH)
                                                            .padding(
                                                                    horizontal = 4.dp,
                                                                    vertical = 2.dp
                                                            ),
                                            singleLine = true,
                                            textStyle =
                                                    LocalTextStyle.current.copy(fontSize = 12.sp),
                                            colors =
                                                    TextFieldDefaults.outlinedTextFieldColors(
                                                            focusedBorderColor = VetNutriColors.Primary,
                                                            unfocusedBorderColor = Color.Gray
                                                    )
                                    )
                                }
                            }
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }

            Divider()

            // --- Boutons d'action ---
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Annuler")
                }

                Button(
                        onClick = {
                            viewModel.saveAll()
                            onNavigateBack()
                        },
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )
                ) {
                    Text("Enregistrer tout")
                }
            }
        }
    }
}
