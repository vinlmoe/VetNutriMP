package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.MultiNutrientAdjustmentView
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrientDetailDialog
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionAlimentsRation
import fr.vetbrain.vetnutri_mp.View.Components.*
import fr.vetbrain.vetnutri_mp.ViewModel.MultiRationViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Réutilise les composants de la vue ration, sans contexte animal ni consultation. */
@Composable
internal fun RationsExplorationView(
        viewModel: MultiRationViewModel,
        scenarios: List<ScenarioExploration>,
        modifier: Modifier = Modifier
) {
    var selection by remember { mutableStateOf(scenarios.first().id) }
    val scenario = scenarios.firstOrNull { it.id == selection } ?: scenarios.first()
    val ration = scenario.ration
    var ajout by remember(selection) { mutableStateOf(false) }
    var ajustement by remember(selection) { mutableStateOf(false) }
    var detail by remember(selection, ration) { mutableStateOf<Pair<String, ValeurNutritionnelle>?>(null) }
    var message by remember { mutableStateOf("") }
    var confirmerRetour by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val enregistrement by viewModel.enregistrementCorrections.collectAsState()
    val erreur by viewModel.erreur.collectAsState()
    val preferencesStorage = remember { createPreferencesStorage() }
    fun modifierAliments(aliments: List<AlimentRation>) {
        viewModel.modifierRationExploration(scenario.id, aliments)
    }
    Column(modifier.fillMaxSize()) {
        TopBarSimple(title = "Rations de l’exploration", onNavigateBack = { if (!enregistrement) confirmerRetour = true })
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Enregistrez les corrections pour actualiser les tableaux, la carte et les courbes de l’exploration.")
            Button(onClick = { viewModel.enregistrerCorrections() }, enabled = !enregistrement) {
                Text(if (enregistrement) "Enregistrement…" else "Enregistrer les corrections et mettre à jour les résultats")
            }
            erreur?.let { Text(it, color = MaterialTheme.colors.error) }
            DropdownField(label = "Ration", selectedValue = scenario.id, options = scenarios.map { it.id },
                    onValueChange = { selection = it }, valueToString = { id ->
                        val s = scenarios.first { it.id == id }
                        "$id — ${s.reference.nom} — ${formaterQuantite(s.poids)} kg — K ${formaterQuantite(s.k)}"
                    }, modifier = Modifier.fillMaxWidth())
            Text("Besoin standard : ${scenario.besoinStandard?.let { formaterQuantite(it) } ?: "—"} kcal ; besoin ajusté : ${scenario.besoinTotal?.let { formaterQuantite(it) } ?: "—"} kcal")
            if (message.isNotEmpty()) Text(message)
            key(selection) {
                SectionAlimentsRation(
                        selectedRation = ration, referenceUtilisee = scenario.reference,
                        besoinEnergetiqueTotal = scenario.besoinTotal,
                        besoinEnergetiqueStandard = scenario.besoinStandard,
                        equationRepository = viewModel.equationRepository,
                        onAddAliment = { ajout = true },
                        onMultiNutrientAdjustment = { ajustement = true },
                        onOpenRecipeDialog = {}, onSaveRecipe = {},
                        autoriserRecettes = false, showSnackbar = { message = it },
                        isCompact = true, isReadOnly = enregistrement,
                        onUpdateAliments = { _, aliments -> modifierAliments(aliments) },
                        onUpdateQuantite = { id, quantite ->
                            modifierAliments(ration.alimentMutableList.map {
                                if (it.uuid == id) it.copy(quantite = quantite) else it
                            })
                        },
                        onRemoveAliment = { id -> modifierAliments(ration.alimentMutableList.filterNot { it.uuid == id }) }
                )
            }
            Text("Analyse détaillée selon l’ensemble des seuils du référentiel. Les exclusions de l’exploration restent propres à la carte des résultats.")
            AnalyseNutritionnelleCard(
                    ration = ration, referenceUtilisee = scenario.reference,
                    poidsMetabolique = scenario.poidsMetabolique, poidsAnimal = scenario.poids,
                    besoinEnergetiqueEntretien = scenario.besoinStandard, besoinEnergetiqueCible = scenario.besoinTotal,
                    equationRepository = viewModel.equationRepository,
                    typeExpressionBesoin = TypeExpressionBesoin.DEFAULT,
                    onNutrimentClick = { nom, valeur -> detail = nom to valeur },
                    // La page défile déjà : la liste interne doit garder une hauteur finie.
                    // Le mode compact de la carte borne sa LazyColumn à 400 dp.
                    isLargeView = false, modifier = Modifier.fillMaxWidth()
            )
        }
    }
    if (confirmerRetour) AlertDialog(
            onDismissRequest = { confirmerRetour = false },
            title = { Text("Fermer les rations temporaires ?") },
            text = { Text("Les corrections non enregistrées seront perdues. Vous pouvez annuler et les enregistrer pour mettre à jour les résultats.") },
            confirmButton = { TextButton(onClick = { viewModel.fermerRations() }) { Text("Fermer") } },
            dismissButton = { TextButton(onClick = { confirmerRetour = false }) { Text("Continuer l’édition") } }
    )
    if (ajustement && scenario.besoinTotal != null && scenario.besoinStandard != null) {
        MultiNutrientAdjustmentView(
                ration = ration, referenceUtilisee = scenario.reference,
                besoinEnergetiqueTotal = scenario.besoinTotal, besoinEnergetiqueStandard = scenario.besoinStandard,
                poidsAnimal = scenario.poids, poidsMetabolique = scenario.poidsMetabolique,
                equationRepository = viewModel.equationRepository,
                onConfirm = { result ->
                    if (result.success) result.adjustedAliments?.let { modifierAliments(it) }
                    message = result.message
                    ajustement = false
                }, onDismiss = { ajustement = false }
        )
    }
    detail?.let { (nom, valeur) ->
        NutrientDetailDialog(
                nom = nom, valeurNutritionnelle = valeur, ration = ration,
                poidsMetabolique = scenario.poidsMetabolique, referenceUtilisee = scenario.reference,
                besoinEnergetiqueEntretien = scenario.besoinStandard, besoinEnergetiqueCible = scenario.besoinTotal,
                poidsAnimal = scenario.poids, espece = scenario.reference.espece,
                preferencesStorage = preferencesStorage, equationRepository = viewModel.equationRepository,
                onDismiss = { detail = null }
        )
    }
    if (ajout) {
        val aliments by viewModel.aliments.collectAsState(initial = emptyList())
        var filtres by remember { mutableStateOf(FoodSearchFilters(selectedEspece = scenario.reference.espece)) }
        var enCours by remember { mutableStateOf(false) }
        Dialog(onDismissRequest = { ajout = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxWidth(0.85f).fillMaxHeight(0.85f)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ajouter un aliment à la ration")
                    FoodSearchComponent(
                            foods = aliments, filters = filtres, onFiltersChange = { filtres = it },
                            config = FoodSearchConfig(layout = FoodSearchLayout.VERTICAL, isLoading = enCours,
                                    onFoodSelected = { aliment ->
                                        if (!enCours) {
                                            enCours = true
                                            scope.launch {
                                                try {
                                                    val complet = viewModel.alimentComplet(aliment.uuid)
                                                    if (complet == null) message = "Cet aliment n’est plus disponible."
                                                    else {
                                                        modifierAliments(ration.alimentMutableList + AlimentRation(
                                                                aliment = complet, quantite = 1.0,
                                                                refRation = ration.uuid, refAlimUnif = complet.uuid
                                                        ))
                                                        ajout = false
                                                    }
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    message = "Ajout impossible : ${e.message}"
                                                } finally {
                                                    enCours = false
                                                }
                                            }
                                        }
                                    }), modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    TextButton(onClick = { ajout = false }) { Text("Fermer") }
                }
            }
        }
    }
}
