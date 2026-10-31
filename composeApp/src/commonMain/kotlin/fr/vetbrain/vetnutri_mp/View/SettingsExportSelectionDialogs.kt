package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.Recette
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Export.SectionCategory
import fr.vetbrain.vetnutri_mp.Localization.translate

/** Choix des animaux à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionAnimauxExport(
        availableAnimals: List<AnimalEv>,
        isLoadingAnimals: Boolean,
        selection: MutableState<Set<String>>,
        filtreEspece: MutableState<FilterOption<Espece>>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedAnimalIds by selection
        var animalFilterEspece by filtreEspece
        var selectionLoadError by erreur
        var showAnimalSelectionDialog by visible

        if (showAnimalSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showAnimalSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingAnimals -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("auto.view.settingsview.chargement_des_animaux")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showAnimalSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableAnimals.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.aucun_animal_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showAnimalSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                val animalById =
                                                        remember(
                                                                availableAnimals
                                                        ) {
                                                                availableAnimals
                                                                        .associateBy {
                                                                                it.uuid
                                                                        }
                                                        }
                                                val animalFilterOptions =
                                                        remember {
                                                                listOf<FilterOption<Espece>>(
                                                                        FilterOption<Espece>(
                                                                                label =
                                                                                        translate("crossConsultation.speciesAll"),
                                                                                value = null
                                                                        )
                                                                ) +
                                                                        Espece
                                                                                .values()
                                                                                .map {
                                                                                        FilterOption(
                                                                                                label =
                                                                                                        it.label,
                                                                                                value =
                                                                                                        it
                                                                                        )
                                                                                }
                                                        }
                                                SelectionDialog(
                                                        title =
                                                                translate("auto.view.settingsview.selection_des_animaux"),
                                                        items =
                                                                availableAnimals.map {
                                                                        SelectionItem(
                                                                                id =
                                                                                        it.uuid,
                                                                                title =
                                                                                        it.nom
                                                                                                .ifBlank {
                                                                                                        "Sans nom"
                                                                                                },
                                                                                subtitle =
                                                                                        it.ownerName
                                                                                                .ifBlank {
                                                                                                        ""
                                                                                                }
                                                                        )
                                                                },
                                                        initialSelectedIds =
                                                                selectedAnimalIds,
                                                        onConfirm = {
                                                                ids ->
                                                                selectedAnimalIds =
                                                                        ids
                                                                showAnimalSelectionDialog =
                                                                        false
                                                        },
                                                        onDismiss = {
                                                                showAnimalSelectionDialog =
                                                                        false
                                                        },
                                                        confirmLabel =
                                                                "Valider la sélection",
                                                        emptyLabel =
                                                                "Aucun animal disponible.",
                                                        filtersContent = {
                                                                DropdownField(
                                                                        label =
                                                                                translate("auto.view.settingsview.espece"),
                                                                        selectedValue =
                                                                                animalFilterEspece,
                                                                        options =
                                                                                animalFilterOptions,
                                                                        onValueChange = {
                                                                                animalFilterEspece =
                                                                                        it
                                                                        },
                                                                        valueToString = {
                                                                                it.label
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                )
                                                        },
                                                        filterPredicate = { item ->
                                                                val animal =
                                                                        animalById[
                                                                                item.id
                                                                        ]
                                                                val selected =
                                                                        animalFilterEspece.value
                                                                selected == null ||
                                                                        animal?.getEspece() ==
                                                                                selected
                                                        }
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showAnimalSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}

/** Choix des aliments à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionAlimentsExport(
        availableFoods: List<AlimentEv>,
        isLoadingFoods: Boolean,
        selection: MutableState<Set<String>>,
        filtres: MutableState<FoodSearchFilters>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedFoodIds by selection
        var foodSelectionFilters by filtres
        var selectionLoadError by erreur
        var showFoodSelectionDialog by visible

        if (showFoodSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showFoodSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingFoods -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("animalDetail.loadingFoods")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showFoodSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableFoods.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.excelimportexportcomponents.aucun_aliment_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showFoodSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                AnalyseSelectionAlimentsView(
                                                        aliments =
                                                                availableFoods,
                                                        onClose = {
                                                                showFoodSelectionDialog =
                                                                        false
                                                        },
                                                        onPrimaryAction = {
                                                                aliments ->
                                                                selectedFoodIds =
                                                                        aliments.map {
                                                                                it.uuid
                                                                        }
                                                                                .toSet()
                                                                showFoodSelectionDialog =
                                                                        false
                                                        },
                                                        primaryActionLabel =
                                                                "Valider la sélection",
                                                        alimentsInitialementSelectionnes =
                                                                availableFoods.filter {
                                                                        selectedFoodIds.contains(
                                                                                it.uuid
                                                                        )
                                                                },
                                                        onSelectionChanged = {
                                                                selectedFoodIds =
                                                                        it.map { al ->
                                                                                al.uuid
                                                                        }
                                                                                .toSet()
                                                        },
                                                        filtersInitial =
                                                                foodSelectionFilters,
                                                        onFiltersChange = {
                                                                foodSelectionFilters =
                                                                        it
                                                        },
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                16.dp
                                                                        )
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showFoodSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}

/** Choix des équations à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionEquationsExport(
        availableEquations: List<Equation>,
        isLoadingEquations: Boolean,
        selection: MutableState<Set<String>>,
        filtreEspece: MutableState<FilterOption<Espece>>,
        filtreType: MutableState<FilterOption<EquationKind>>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedEquationIds by selection
        var equationFilterEspece by filtreEspece
        var equationFilterKind by filtreType
        var selectionLoadError by erreur
        var showEquationSelectionDialog by visible

        if (showEquationSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showEquationSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingEquations -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("auto.view.settingsview.chargement_des_equations")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showEquationSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableEquations.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.aucune_equation_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showEquationSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                val equationById =
                                                        remember(
                                                                availableEquations
                                                        ) {
                                                                availableEquations
                                                                        .associateBy {
                                                                                it.uuid
                                                                        }
                                                        }
                                                val equationSpecieOptions =
                                                        remember {
                                                                listOf<FilterOption<Espece>>(
                                                                        FilterOption<Espece>(
                                                                                label =
                                                                                        translate("crossConsultation.speciesAll"),
                                                                                value = null
                                                                        )
                                                                ) +
                                                                        Espece
                                                                                .values()
                                                                                .map {
                                                                                        FilterOption(
                                                                                                label =
                                                                                                        it.label,
                                                                                                value =
                                                                                                        it
                                                                                        )
                                                                                }
                                                        }
                                                val equationKindOptions =
                                                        remember {
                                                                listOf<FilterOption<EquationKind>>(
                                                                        FilterOption<EquationKind>(
                                                                                label =
                                                                                        translate("crossConsultation.keywordsAll"),
                                                                                value = null
                                                                        )
                                                                ) +
                                                                        EquationKind
                                                                                .values()
                                                                                .map {
                                                                                        FilterOption(
                                                                                                label =
                                                                                                        it.name,
                                                                                                value =
                                                                                                        it
                                                                                        )
                                                                                }
                                                        }
                                                SelectionDialog(
                                                        title =
                                                                translate("auto.view.settingsview.selection_des_equations"),
                                                        items =
                                                                availableEquations.map {
                                                                        val specie =
                                                                                it.specie?.name
                                                                                        ?: "ALL"
                                                                        val kind =
                                                                                it.kind.name
                                                                        SelectionItem(
                                                                                id =
                                                                                        it.uuid,
                                                                                title =
                                                                                        it.name.ifBlank {
                                                                                                it.description.ifBlank {
                                                                                                        "Equation"
                                                                                                }
                                                                                        },
                                                                                subtitle =
                                                                                        "$kind - $specie"
                                                                        )
                                                                },
                                                        initialSelectedIds =
                                                                selectedEquationIds,
                                                        onConfirm = {
                                                                ids ->
                                                                selectedEquationIds =
                                                                        ids
                                                                showEquationSelectionDialog =
                                                                        false
                                                        },
                                                        onDismiss = {
                                                                showEquationSelectionDialog =
                                                                        false
                                                        },
                                                        confirmLabel =
                                                                "Valider la sélection",
                                                        emptyLabel =
                                                                "Aucune équation disponible.",
                                                        filtersContent = {
                                                                Column(
                                                                        verticalArrangement =
                                                                                Arrangement.spacedBy(
                                                                                        8.dp
                                                                                )
                                                                ) {
                                                                        DropdownField(
                                                                                label =
                                                                                        translate("auto.view.settingsview.espece"),
                                                                                selectedValue =
                                                                                        equationFilterEspece,
                                                                                options =
                                                                                        equationSpecieOptions,
                                                                                onValueChange = {
                                                                                        equationFilterEspece =
                                                                                                it
                                                                                },
                                                                                valueToString = {
                                                                                        it.label
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                        )
                                                                        DropdownField(
                                                                                label =
                                                                                        translate("auto.view.settingsview.type_d_equation"),
                                                                                selectedValue =
                                                                                        equationFilterKind,
                                                                                options =
                                                                                        equationKindOptions,
                                                                                onValueChange = {
                                                                                        equationFilterKind =
                                                                                                it
                                                                                },
                                                                                valueToString = {
                                                                                        it.label
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                        )
                                                                }
                                                        },
                                                        filterPredicate = { item ->
                                                                val equation =
                                                                        equationById[
                                                                                item.id
                                                                        ]
                                                                val selectedSpecie =
                                                                        equationFilterEspece.value
                                                                val selectedKind =
                                                                        equationFilterKind.value
                                                                val matchesSpecie =
                                                                        selectedSpecie ==
                                                                                null ||
                                                                                equation?.specie ==
                                                                                selectedSpecie
                                                                val matchesKind =
                                                                        selectedKind ==
                                                                                null ||
                                                                                equation?.kind ==
                                                                                selectedKind
                                                                matchesSpecie &&
                                                                        matchesKind
                                                        }
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showEquationSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}

/** Choix des référentiels à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionReferencesExport(
        availableReferences: List<ReferenceEv>,
        isLoadingReferences: Boolean,
        selection: MutableState<Set<String>>,
        filtreEspece: MutableState<FilterOption<Espece>>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedReferenceIds by selection
        var referenceFilterEspece by filtreEspece
        var selectionLoadError by erreur
        var showReferenceSelectionDialog by visible

        if (showReferenceSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showReferenceSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingReferences -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("auto.view.settingsview.chargement_des_references")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showReferenceSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableReferences.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.aucune_reference_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showReferenceSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                val referenceById =
                                                        remember(
                                                                availableReferences
                                                        ) {
                                                                availableReferences
                                                                        .associateBy {
                                                                                it.uuid
                                                                        }
                                                        }
                                                val referenceSpecieOptions =
                                                        remember {
                                                                listOf<FilterOption<Espece>>(
                                                                        FilterOption<Espece>(
                                                                                label =
                                                                                        translate("crossConsultation.speciesAll"),
                                                                                value = null
                                                                        )
                                                                ) +
                                                                        Espece
                                                                                .values()
                                                                                .map {
                                                                                        FilterOption(
                                                                                                label =
                                                                                                        it.label,
                                                                                                value =
                                                                                                        it
                                                                                        )
                                                                                }
                                                        }
                                                SelectionDialog(
                                                        title =
                                                                translate("auto.view.settingsview.selection_des_references"),
                                                        items =
                                                                availableReferences.map {
                                                                        SelectionItem(
                                                                                id =
                                                                                        it.uuid,
                                                                                title =
                                                                                        it.nom.ifBlank {
                                                                                                "Reference"
                                                                                        },
                                                                                subtitle =
                                                                                        it.espece.label
                                                                        )
                                                                },
                                                        initialSelectedIds =
                                                                selectedReferenceIds,
                                                        onConfirm = {
                                                                ids ->
                                                                selectedReferenceIds =
                                                                        ids
                                                                showReferenceSelectionDialog =
                                                                        false
                                                        },
                                                        onDismiss = {
                                                                showReferenceSelectionDialog =
                                                                        false
                                                        },
                                                        confirmLabel =
                                                                "Valider la sélection",
                                                        emptyLabel =
                                                                "Aucune référence disponible.",
                                                        filtersContent = {
                                                                DropdownField(
                                                                        label =
                                                                                translate("auto.view.settingsview.espece"),
                                                                        selectedValue =
                                                                                referenceFilterEspece,
                                                                        options =
                                                                                referenceSpecieOptions,
                                                                        onValueChange = {
                                                                                referenceFilterEspece =
                                                                                        it
                                                                        },
                                                                        valueToString = {
                                                                                it.label
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                )
                                                        },
                                                        filterPredicate = { item ->
                                                                val reference =
                                                                        referenceById[
                                                                                item.id
                                                                        ]
                                                                val selected =
                                                                        referenceFilterEspece.value
                                                                selected == null ||
                                                                        reference?.espece ==
                                                                                selected
                                                        }
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showReferenceSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}

/** Choix des conseils à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionConseilsExport(
        availableConseils: List<HtmlSection>,
        isLoadingConseils: Boolean,
        selection: MutableState<Set<String>>,
        filtreCategorie: MutableState<FilterOption<SectionCategory>>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedConseilIds by selection
        var conseilFilterCategory by filtreCategorie
        var selectionLoadError by erreur
        var showConseilSelectionDialog by visible

        if (showConseilSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showConseilSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingConseils -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("auto.view.settingsview.chargement_des_conseils")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showConseilSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableConseils.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.aucun_conseil_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showConseilSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                val conseilById =
                                                        remember(
                                                                availableConseils
                                                        ) {
                                                                availableConseils
                                                                        .associateBy {
                                                                                it.id
                                                                        }
                                                        }
                                                val conseilCategoryOptions =
                                                        remember {
                                                                listOf(
                                                                        FilterOption<SectionCategory>(
                                                                                label =
                                                                                        translate("crossConsultation.speciesAll"),
                                                                                value = null
                                                                        )
                                                                ) +
                                                                        SectionCategory
                                                                                .entries
                                                                                .filter {
                                                                                        it.name.startsWith(
                                                                                                "CONSEIL"
                                                                                        )
                                                                                }
                                                                                .map {
                                                                                        FilterOption(
                                                                                                label =
                                                                                                        it.name,
                                                                                                value =
                                                                                                        it
                                                                                        )
                                                                                }
                                                        }
                                                SelectionDialog(
                                                        title =
                                                                translate("auto.view.settingsview.selection_des_conseils"),
                                                        items =
                                                                availableConseils.map {
                                                                        val tags =
                                                                                it.tags
                                                                                        .takeIf {
                                                                                                tags ->
                                                                                                tags.isNotEmpty()
                                                                                        }
                                                                                        ?.joinToString(
                                                                                                ", "
                                                                                        )
                                                                                        ?: it.category.name
                                                                        SelectionItem(
                                                                                id =
                                                                                        it.id,
                                                                                title =
                                                                                        it.title.ifBlank {
                                                                                                "Conseil"
                                                                                        },
                                                                                subtitle =
                                                                                        tags
                                                                        )
                                                                },
                                                        initialSelectedIds =
                                                                selectedConseilIds,
                                                        onConfirm = {
                                                                ids ->
                                                                selectedConseilIds =
                                                                        ids
                                                                showConseilSelectionDialog =
                                                                        false
                                                        },
                                                        onDismiss = {
                                                                showConseilSelectionDialog =
                                                                        false
                                                        },
                                                        confirmLabel =
                                                                "Valider la sélection",
                                                        emptyLabel =
                                                                "Aucun conseil disponible.",
                                                        filtersContent = {
                                                                DropdownField(
                                                                        label =
                                                                                translate("auto.view.settingsview.categorie"),
                                                                        selectedValue =
                                                                                conseilFilterCategory,
                                                                        options =
                                                                                conseilCategoryOptions,
                                                                        onValueChange = {
                                                                                conseilFilterCategory =
                                                                                        it
                                                                        },
                                                                        valueToString = {
                                                                                it.label
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                )
                                                        },
                                                        filterPredicate = { item ->
                                                                val conseil =
                                                                        conseilById[
                                                                                item.id
                                                                        ]
                                                                val selected =
                                                                        conseilFilterCategory
                                                                                .value
                                                                selected == null ||
                                                                        conseil?.category ==
                                                                                selected
                                                        }
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showConseilSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}

/** Choix des recettes à exporter (Paramètres > Import/Export, sélection avancée). */
@Composable
internal fun SelectionRecettesExport(
        availableRecipes: List<Recette>,
        isLoadingRecipes: Boolean,
        selection: MutableState<Set<String>>,
        erreur: MutableState<String?>,
        visible: MutableState<Boolean>
) {
        var selectedRecipeIds by selection
        var selectionLoadError by erreur
        var showRecipeSelectionDialog by visible

        if (showRecipeSelectionDialog) {
                Dialog(
                        onDismissRequest = {
                                showRecipeSelectionDialog = false
                        },
                        properties =
                                DialogProperties(
                                        usePlatformDefaultWidth =
                                                false
                                )
                ) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(
                                                        MaterialTheme
                                                                .colors
                                                                .background
                                                )
                        ) {
                                when {
                                        isLoadingRecipes -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator()
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                12.dp
                                                                        )
                                                        )
                                                        Text(
                                                                translate("auto.view.settingsview.chargement_des_recettes")
                                                        )
                                                }
                                        }
                                        selectionLoadError != null -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                selectionLoadError
                                                                        ?: "Erreur inconnue",
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .error,
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body1
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showRecipeSelectionDialog =
                                                                                false
                                                                        selectionLoadError =
                                                                                null
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        availableRecipes.isEmpty() -> {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                24.dp
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.aucune_recette_disponible")
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Button(
                                                                onClick = {
                                                                        showRecipeSelectionDialog =
                                                                                false
                                                                }
                                                        ) { Text(translate("settings.close")) }
                                                }
                                        }
                                        else -> {
                                                SelectionDialog(
                                                        title =
                                                                translate("auto.view.settingsview.selection_des_recettes"),
                                                        items =
                                                                availableRecipes.map {
                                                                        SelectionItem(
                                                                                id =
                                                                                        it.uuid,
                                                                                title =
                                                                                        it.name
                                                                                                ?.ifBlank {
                                                                                                        "Recette"
                                                                                                }
                                                                                                ?: "Recette",
                                                                                subtitle =
                                                                                        it.description
                                                                                                ?: ""
                                                                        )
                                                                },
                                                        initialSelectedIds =
                                                                selectedRecipeIds,
                                                        onConfirm = {
                                                                ids ->
                                                                selectedRecipeIds =
                                                                        ids
                                                                showRecipeSelectionDialog =
                                                                        false
                                                        },
                                                        onDismiss = {
                                                                showRecipeSelectionDialog =
                                                                        false
                                                        },
                                                        confirmLabel =
                                                                "Valider la sélection",
                                                        emptyLabel =
                                                                "Aucune recette disponible."
                                                )
                                        }
                                }

                                IconButton(
                                        onClick = {
                                                showRecipeSelectionDialog =
                                                        false
                                        },
                                        modifier =
                                                Modifier.align(
                                                        Alignment.TopEnd
                                                )
                                                        .padding(
                                                                16.dp
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default.Close,
                                                contentDescription =
                                                        translate("settings.close")
                                        )
                                }
                        }
                }
        }
}
