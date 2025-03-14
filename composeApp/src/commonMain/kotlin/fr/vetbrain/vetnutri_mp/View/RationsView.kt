package fr.vetbrain.vetnutri_mp.View

// Importation des composants nécessaires
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.LocalDate

/**
 * Vue pour afficher les rations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun RationsView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // État pour gérer l'édition active de la quantité d'un aliment
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        // État pour gérer l'affichage du dialogue d'édition de ration
        var showRationEditDialog by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }

        // État pour gérer le mode de division des valeurs nutritionnelles
        var divisionParPoidsAnimal by remember { mutableStateOf(true) }

        // État pour la gestion des onglets
        var ongletSelectionne by remember { mutableStateOf(0) }

        // Liste des nutriments principaux à afficher (tous les nutriments de type NutrientMain)
        val nutrimentsPrincipaux = remember<List<Nutrient>> { NutrientMain.entries.toList() }

        // Liste des minéraux à afficher (tous les nutriments de type NutrientMin)
        val mineraux = remember<List<Nutrient>> { NutrientMin.entries.toList() }

        // Liste des vitamines à afficher (tous les nutriments de type NutrientVitam)
        val vitamines = remember<List<Nutrient>> { NutrientVitam.entries.toList() }

        // Liste des lipides à afficher (tous les nutriments de type NutrientLipid)
        val lipides = remember<List<Nutrient>> { NutrientLipid.entries.toList() }

        // Liste des acides aminés à afficher (tous les nutriments de type AAEnum)
        val acidesAmines = remember<List<Nutrient>> { AAEnum.entries.toList() }

        // Liste des autres nutriments à afficher (tous les nutriments de type NutrientOther)
        val autresNutriments = remember<List<Nutrient>> { NutrientOther.entries.toList() }

        // Calculer le poids total de la ration
        val poidsRation =
                remember(selectedRation) {
                        selectedRation
                                ?.alimentMutableList
                                ?.sumOf { it.quantity.toDouble() }
                                ?.toFloat()
                                ?: 0f
                }

        // Calculer les valeurs nutritionnelles pour les différentes catégories
        val valeursNutritionnellesPrincipales =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, nutrimentsPrincipaux)
                        }
                                ?: emptyMap()
                }

        val valeursNutritionnellesMineraux =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, mineraux)
                        }
                                ?: emptyMap()
                }

        val valeursNutritionnellesVitamines =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, vitamines)
                        }
                                ?: emptyMap()
                }

        val valeursNutritionnellesLipides =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, lipides)
                        }
                                ?: emptyMap()
                }

        val valeursNutritionnellesAcidesAmines =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, acidesAmines)
                        }
                                ?: emptyMap()
                }

        val valeursNutritionnellesAutres =
                remember(selectedRation) {
                        selectedRation?.alimentMutableList?.let { aliments ->
                                calculerValeursNutritionnelles(aliments, autresNutriments)
                        }
                                ?: emptyMap()
                }

        // Diviseur en fonction du mode (poids animal ou poids ration)
        val diviseur =
                remember(divisionParPoidsAnimal, selectedConsultation, poidsRation) {
                        // Obtenir le poids de l'animal, ou utiliser une valeur par défaut
                        if (divisionParPoidsAnimal) {
                                // Utiliser le poids de la consultation actuelle
                                selectedConsultation?.weight ?: 0f
                        } else {
                                poidsRation / 1000f // Convertir en kg
                        }
                }

        // Texte décrivant le diviseur actuel
        val typeDiviseur =
                remember(divisionParPoidsAnimal) {
                        if (divisionParPoidsAnimal) "poids animal" else "poids ration"
                }

        // Déterminer quels nutriments et valeurs afficher en fonction de l'onglet sélectionné
        val (nutrimentsCourants, valeursCourantes) =
                when (ongletSelectionne) {
                        0 -> Pair(nutrimentsPrincipaux, valeursNutritionnellesPrincipales)
                        1 -> Pair(mineraux, valeursNutritionnellesMineraux)
                        2 -> Pair(vitamines, valeursNutritionnellesVitamines)
                        3 -> Pair(lipides, valeursNutritionnellesLipides)
                        4 -> Pair(acidesAmines, valeursNutritionnellesAcidesAmines)
                        5 -> Pair(autresNutriments, valeursNutritionnellesAutres)
                        else -> Pair(nutrimentsPrincipaux, valeursNutritionnellesPrincipales)
                }

        // DEBUG: Afficher des informations sur les données disponibles
        LaunchedEffect(Unit) {
                println(
                        "DEBUG Rations - Animal: ${animal?.nom}, Consultations: ${animal?.consultations?.size}"
                )
                println(
                        "DEBUG Rations - Consultation sélectionnée: ${selectedConsultation?.date}, Rations: ${selectedConsultation?.rations?.size}"
                )
                println(
                        "DEBUG Rations - Ration sélectionnée: ${selectedRation?.name}, Aliments: ${selectedRation?.alimentMutableList?.size}"
                )

                // Détails supplémentaires sur les aliments
                selectedRation?.alimentMutableList?.forEachIndexed { index, alimentRation ->
                        println(
                                "DEBUG Aliment[$index]: UUID=${alimentRation.uuid}, refAlimUnif=${alimentRation.refAlimUnif}, aliment=${alimentRation.aliment?.nom ?: "null"}"
                        )
                }
        }

        // Surveiller les changements de ration sélectionnée pour plus de détails
        LaunchedEffect(selectedRation) {
                println(
                        "DEBUG Ration changée: ${selectedRation?.name}, Aliments: ${selectedRation?.alimentMutableList?.size}"
                )
                selectedRation?.alimentMutableList?.forEachIndexed { index, alimentRation ->
                        println(
                                "DEBUG Aliment[$index]: UUID=${alimentRation.uuid}, refAlimUnif=${alimentRation.refAlimUnif}, aliment=${alimentRation.aliment?.nom ?: "null"}"
                        )
                }
        }

        // Sélectionner automatiquement la consultation la plus récente si aucune n'est sélectionnée
        LaunchedEffect(animal) {
                println("DEBUG Rations - Animal chargé: ${animal?.nom}")

                // Utiliser une variable locale pour éviter le problème de smart cast
                val animalLocal = animal
                val currentConsultation = selectedConsultation

                // Vérifier si une consultation valide est déjà sélectionnée pour cet animal
                val isValidConsultationSelected =
                        currentConsultation != null &&
                                animalLocal?.consultations?.any {
                                        it.uuid == currentConsultation.uuid
                                } == true

                // Ne sélectionner automatiquement que si aucune consultation valide n'est
                // sélectionnée
                if (!isValidConsultationSelected && animalLocal?.consultations?.isNotEmpty() == true
                ) {
                        // Sélectionner la consultation la plus récente uniquement à l'ouverture
                        // d'un animal ou si la sélection actuelle n'est pas valide
                        val defaultDate = LocalDate(2000, 1, 1)
                        val mostRecentConsultation =
                                animalLocal.consultations.maxByOrNull { it.date ?: defaultDate }
                        println(
                                "DEBUG Rations - Sélection auto consultation: ${mostRecentConsultation?.date}"
                        )
                        mostRecentConsultation?.let {
                                viewModel.selectConsultation(it)
                                println(
                                        "DEBUG Rations - Consultation auto-sélectionnée UUID: ${it.uuid}"
                                )
                        }
                } else {
                        println(
                                "DEBUG Rations - Conservation de la consultation sélectionnée: ${currentConsultation?.date}, UUID: ${currentConsultation?.uuid}"
                        )
                }
        }

        // Sélectionner automatiquement la première ration lorsqu'une consultation est sélectionnée
        LaunchedEffect(selectedConsultation) {
                println(
                        "DEBUG Rations - Consultation changée: ${selectedConsultation?.date}, UUID: ${selectedConsultation?.uuid}"
                )

                // Utiliser des variables locales pour éviter le problème de smart cast
                val consultationLocal = selectedConsultation
                val rationLocal = selectedRation

                if (consultationLocal?.rations?.isNotEmpty() == true) {
                        // Toujours sélectionner une ration valide pour la consultation active
                        val currentRationValid =
                                rationLocal != null &&
                                        consultationLocal.rations.any {
                                                it.uuid == rationLocal.uuid
                                        }

                        if (!currentRationValid) {
                                val firstRation = consultationLocal.rations.firstOrNull()
                                println(
                                        "DEBUG Rations - Sélection auto ration: ${firstRation?.name}, UUID: ${firstRation?.uuid}"
                                )
                                firstRation?.let {
                                        viewModel.selectRation(it)
                                        println(
                                                "DEBUG Rations - Ration auto-sélectionnée avec ${it.alimentMutableList.size} aliments"
                                        )
                                        // Afficher le détail des aliments pour le débogage
                                        it.alimentMutableList.forEachIndexed { index, aliment ->
                                                println(
                                                        "DEBUG Rations - Aliment[$index]: ${aliment.aliment?.nom ?: "Sans nom"}, Quantité: ${aliment.quantity}g"
                                                )
                                        }
                                }
                        }
                } else {
                        // Si la consultation n'a pas de rations, réinitialiser la ration
                        // sélectionnée
                        println(
                                "DEBUG Rations - La consultation n'a pas de rations, réinitialisation de la ration sélectionnée"
                        )
                        viewModel.resetSelectedRation()
                }
        }

        // Afficher le dialogue d'édition de ration si nécessaire
        if (showRationEditDialog) {
                RationEditDialog(
                        ration = rationToEdit,
                        onDismiss = { showRationEditDialog = false },
                        onSave = { ration ->
                                if (rationToEdit == null) {
                                        // Nouvelle ration - utiliser createDefaultRation plutôt que
                                        // d'accéder aux propriétés privées
                                        selectedConsultation?.let { consultation ->
                                                // Créer la ration par défaut via le viewModel
                                                viewModel.createDefaultRation(consultation)
                                        }
                                } else {
                                        // Mise à jour d'une ration existante via le viewModel
                                        viewModel.updateRationInConsultation(ration)
                                        println(
                                                "DEBUG: Ration mise à jour avec UUID=${ration.uuid}, nom=${ration.name}, coefficient=${ration.coef}"
                                        )
                                }
                                // Fermer le dialogue
                                showRationEditDialog = false
                        }
                )
        }

        Box(modifier = modifier.fillMaxSize()) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // En-tête compact avec les informations essentielles
                        Card(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(AppSizes.cardMinHeight.times(1.1f)),
                                elevation = AppSizes.elevationMedium,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(
                                                                horizontal = AppSizes.paddingMedium
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                                selectedConsultation?.date?.let { date ->
                                                        Text(
                                                                text = "Consultation du $date",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle1
                                                        )
                                                }
                                                selectedRation?.let { ration ->
                                                        Text(
                                                                text =
                                                                        "${ration.name} (${if (ration.actual) "Proposée" else "Actuelle"})",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }
                                }
                        }

                        // Contenu principal - grille 2x2 de cartes
                        Box(modifier = Modifier.weight(1f)) {
                                Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        // Colonne gauche
                                        Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // Segment 2: Liste des rations de la consultation
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1f).fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // En-tête avec titre et bouton
                                                                // d'ajout
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Rations de la consultation",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour ajouter une
                                                                        // nouvelle ration
                                                                        IconButton(
                                                                                onClick = {
                                                                                        rationToEdit =
                                                                                                null // Nouvelle ration
                                                                                        showRationEditDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                AppSizes.iconSizeMedium
                                                                                        )
                                                                        ) {
                                                                                Icon(
                                                                                        AppIcons.Add,
                                                                                        contentDescription =
                                                                                                "Ajouter une ration",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        }
                                                                }

                                                                Divider()

                                                                if (selectedConsultation?.rations
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucune ration disponible",
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                } else {
                                                                        LazyColumn(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        8.dp
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedConsultation
                                                                                                ?.rations
                                                                                                ?: emptyList()
                                                                                ) { ration ->
                                                                                        RationItem(
                                                                                                ration =
                                                                                                        ration,
                                                                                                isSelected =
                                                                                                        selectedRation
                                                                                                                ?.uuid ==
                                                                                                                ration.uuid,
                                                                                                onClick = {
                                                                                                        viewModel
                                                                                                                .selectRation(
                                                                                                                        ration
                                                                                                                )
                                                                                                },
                                                                                                onDelete = {
                                                                                                        // Confirmer la suppression avant de procéder
                                                                                                        selectedConsultation
                                                                                                                ?.let {
                                                                                                                        consultation
                                                                                                                        ->
                                                                                                                        viewModel
                                                                                                                                .removeRationFromConsultation(
                                                                                                                                        ration
                                                                                                                                )
                                                                                                                        println(
                                                                                                                                "DEBUG: Demande de suppression de la ration ${ration.uuid}"
                                                                                                                        )
                                                                                                                }
                                                                                                },
                                                                                                isDeleteEnabled =
                                                                                                        selectedConsultation
                                                                                                                ?.rations
                                                                                                                ?.size
                                                                                                                ?: 0 >
                                                                                                                1,
                                                                                                onEdit = {
                                                                                                        rationToEdit =
                                                                                                                ration
                                                                                                        showRationEditDialog =
                                                                                                                true
                                                                                                        println(
                                                                                                                "DEBUG: Demande d'édition de la ration ${ration.uuid}, coefficient=${ration.coef}"
                                                                                                        )
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Segment 3: Liste des aliments de la ration
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1f).fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Aliments de la ration",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                Divider()
                                                                if (selectedRation
                                                                                ?.alimentMutableList
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        // Message plus explicite et
                                                                        // vérification que la liste
                                                                        // est vide
                                                                        println(
                                                                                "DEBUG RationsView: La liste d'aliments est vide, affichage du message"
                                                                        )
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucun aliment dans cette ration",
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                } else {
                                                                        println(
                                                                                "DEBUG RationsView: Affichage de ${selectedRation?.alimentMutableList?.size ?: 0} aliments"
                                                                        )
                                                                        LazyColumn(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedRation
                                                                                                ?.alimentMutableList
                                                                                                ?: emptyList()
                                                                                ) { aliment ->
                                                                                        AlimentItem(
                                                                                                aliment =
                                                                                                        aliment,
                                                                                                isEditing =
                                                                                                        editingAlimentId ==
                                                                                                                aliment.uuid,
                                                                                                onStartEditing = {
                                                                                                        // Si une autre édition est en cours,
                                                                                                        // valider cette édition d'abord
                                                                                                        if (editingAlimentId !=
                                                                                                                        null &&
                                                                                                                        editingAlimentId !=
                                                                                                                                aliment.uuid
                                                                                                        ) {
                                                                                                                // La quantité précédente reste
                                                                                                                // inchangée car elle n'a pas été
                                                                                                                // modifiée
                                                                                                        }
                                                                                                        // Définir cet aliment comme celui en cours
                                                                                                        // d'édition
                                                                                                        editingAlimentId =
                                                                                                                aliment.uuid
                                                                                                },
                                                                                                onEndEditing = {
                                                                                                        // Effacer l'ID de l'aliment en cours
                                                                                                        // d'édition
                                                                                                        if (editingAlimentId ==
                                                                                                                        aliment.uuid
                                                                                                        ) {
                                                                                                                editingAlimentId =
                                                                                                                        null
                                                                                                        }
                                                                                                },
                                                                                                onQuantityChange = {
                                                                                                        newQuantity
                                                                                                        ->
                                                                                                        viewModel
                                                                                                                .updateAlimentQuantity(
                                                                                                                        aliment.uuid,
                                                                                                                        newQuantity
                                                                                                                )
                                                                                                        // Sortir du mode d'édition après validation
                                                                                                        editingAlimentId =
                                                                                                                null
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        // Colonne droite
                                        Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // Segment 4: Détails de la ration
                                                Card(
                                                        modifier =
                                                                Modifier.weight(0.4f)
                                                                        .fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // En-tête avec le titre et le
                                                                // bouton d'édition
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Détails de la ration",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        IconButton(
                                                                                onClick = {
                                                                                        // Définir
                                                                                        // la ration
                                                                                        // à éditer
                                                                                        // et
                                                                                        // afficher
                                                                                        // le
                                                                                        // dialogue
                                                                                        rationToEdit =
                                                                                                selectedRation
                                                                                        showRationEditDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                AppSizes.iconSizeMedium
                                                                                        )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Default
                                                                                                .Edit,
                                                                                        contentDescription =
                                                                                                "Modifier la ration"
                                                                                )
                                                                        }
                                                                }

                                                                Divider()

                                                                InfoRow(
                                                                        label = "Nom",
                                                                        value = selectedRation?.name
                                                                                        ?: ""
                                                                )
                                                                InfoRow(
                                                                        label = "Type",
                                                                        value =
                                                                                if (selectedRation
                                                                                                ?.actual ==
                                                                                                true
                                                                                )
                                                                                        "Actuelle"
                                                                                else "Proposée"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient",
                                                                        value =
                                                                                "${selectedRation?.coef ?: 1.0}"
                                                                )
                                                                InfoRow(
                                                                        label = "Nombre d'aliments",
                                                                        value =
                                                                                "${selectedRation?.alimentMutableList?.size ?: 0}"
                                                                )
                                                                InfoRow(
                                                                        label = "Poids total",
                                                                        value = "${poidsRation}g"
                                                                )
                                                        }
                                                }

                                                // Segment 5: Informations nutritionnelles avec
                                                // système d'onglets
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1.6f)
                                                                        .fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier = Modifier.fillMaxSize(),
                                                                verticalArrangement =
                                                                        Arrangement.Top
                                                        ) {
                                                                // Système d'onglets
                                                                TabRow(
                                                                        selectedTabIndex =
                                                                                ongletSelectionne,
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Surface,
                                                                        contentColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                ) {
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                0,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                0
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Macronutriments",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                1,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                1
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Minéraux",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                2,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                2
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Vitamines",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                3,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                3
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Lipides",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                4,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                4
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Acides aminés",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                        Tab(
                                                                                selected =
                                                                                        ongletSelectionne ==
                                                                                                5,
                                                                                onClick = {
                                                                                        ongletSelectionne =
                                                                                                5
                                                                                },
                                                                                text = {
                                                                                        Text(
                                                                                                "Autres",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2
                                                                                        )
                                                                                }
                                                                        )
                                                                }

                                                                // Affichage de l'analyse
                                                                // nutritionnelle selon l'onglet
                                                                // sélectionné
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                ) {
                                                                        AnalyseNutritionnelleCard(
                                                                                nutriments =
                                                                                        nutrimentsCourants,
                                                                                valeursTotales =
                                                                                        valeursCourantes,
                                                                                diviseur = diviseur,
                                                                                typeDiviseur =
                                                                                        typeDiviseur,
                                                                                couleurFond =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .surface,
                                                                                onModeDivisionChange = {
                                                                                        divisionParPoidsAnimal =
                                                                                                !divisionParPoidsAnimal
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
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

/**
 * Dialogue d'édition d'une ration
 *
 * @param ration La ration à éditer, ou null pour une nouvelle ration
 * @param onDismiss Action à exécuter lors de la fermeture du dialogue
 * @param onSave Action à exécuter lors de la sauvegarde de la ration
 */
@Composable
private fun RationEditDialog(ration: Ration?, onDismiss: () -> Unit, onSave: (Ration) -> Unit) {
        val isNewRation = ration == null
        val rationName = remember { mutableStateOf(ration?.name ?: "Nouvelle ration") }
        val isActual = remember { mutableStateOf(ration?.actual ?: false) }
        val coefficient = remember { mutableStateOf(ration?.coef?.toString() ?: "1.0") }
        val isError = remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                        elevation = AppSizes.elevationLarge,
                        backgroundColor = MaterialTheme.colors.surface
                ) {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Titre du dialogue
                                Text(
                                        text =
                                                if (isNewRation) "Ajouter une ration"
                                                else "Modifier la ration",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )

                                Divider()

                                // Champ pour le nom de la ration
                                OutlinedTextField(
                                        value = rationName.value,
                                        onValueChange = { rationName.value = it },
                                        label = { Text("Nom de la ration") },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Champ pour le coefficient
                                OutlinedTextField(
                                        value = coefficient.value,
                                        onValueChange = {
                                                coefficient.value = it
                                                isError.value = it.toFloatOrNull() == null
                                        },
                                        label = { Text("Coefficient") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = isError.value
                                )

                                if (isError.value) {
                                        Text(
                                                text =
                                                        "Veuillez entrer une valeur numérique valide",
                                                color = Color.Red,
                                                style = MaterialTheme.typography.caption
                                        )
                                }

                                // Switch pour le type de ration (actuelle/proposée)
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text =
                                                        "Type de ration : ${if (isActual.value) "Proposée" else "Actuelle"}",
                                                style = MaterialTheme.typography.body1
                                        )
                                        Switch(
                                                checked = isActual.value,
                                                onCheckedChange = { isActual.value = it },
                                                colors =
                                                        SwitchDefaults.colors(
                                                                checkedThumbColor =
                                                                        VetNutriColors.Primary,
                                                                checkedTrackColor =
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        )
                                }

                                Divider()

                                // Boutons d'action
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        TextButton(onClick = onDismiss) {
                                                Text("Annuler", color = Color.Gray)
                                        }
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Button(
                                                onClick = {
                                                        val coefValue =
                                                                coefficient.value.toFloatOrNull()
                                                        if (coefValue != null) {
                                                                val updatedRation =
                                                                        ration?.copy(
                                                                                name =
                                                                                        rationName
                                                                                                .value,
                                                                                actual =
                                                                                        isActual.value,
                                                                                coef = coefValue
                                                                        )
                                                                                ?: Ration(
                                                                                        name =
                                                                                                rationName
                                                                                                        .value,
                                                                                        actual =
                                                                                                isActual.value,
                                                                                        coef =
                                                                                                coefValue,
                                                                                        alimentMutableList =
                                                                                                mutableListOf()
                                                                                )
                                                                onSave(updatedRation)
                                                        }
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors.Primary
                                                        ),
                                                enabled = !isError.value
                                        ) { Text("Enregistrer", color = Color.White) }
                                }
                        }
                }
        }
}

/**
 * Composant d'affichage d'une ligne d'information avec libellé et valeur
 *
 * @param label Libellé de l'information
 * @param value Valeur à afficher
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
        Row(
                modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                        text = label,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                )

                Text(
                        text = value,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                )
        }
}

/**
 * Composant d'affichage d'un message centré dans un conteneur
 *
 * @param message Message à afficher
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
private fun CenteredMessage(message: String, modifier: Modifier = Modifier) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = message, style = MaterialTheme.typography.body2, color = Color.Gray)
        }
}

/**
 * Composant pour afficher un élément de ration dans une liste
 *
 * @param ration Ration à afficher
 * @param isSelected Indique si la ration est sélectionnée
 * @param onClick Action à exécuter lors du clic sur la ration
 * @param onDelete Action à exécuter pour supprimer la ration
 * @param isDeleteEnabled Indique si la suppression est autorisée
 * @param onEdit Action à exécuter pour éditer la ration
 */
@Composable
private fun RationItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        onDelete: () -> Unit,
        isDeleteEnabled: Boolean,
        onEdit: () -> Unit
) {
        val backgroundColor =
                if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f) else Color.Transparent

        Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                elevation = if (isSelected) 4.dp else 2.dp,
                backgroundColor = backgroundColor
        ) {
                Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                ) {
                        // Informations de la ration
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = ration.name,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        text = "Coefficient: ${ration.coef}",
                                        style = MaterialTheme.typography.caption
                                )
                                Text(
                                        text = if (ration.actual) "Actuelle" else "Proposée",
                                        style = MaterialTheme.typography.caption,
                                        color =
                                                if (ration.actual) Color.Green
                                                else VetNutriColors.Secondary
                                )
                        }

                        // Boutons d'action
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier",
                                                tint = VetNutriColors.Primary
                                        )
                                }

                                IconButton(
                                        onClick = onDelete,
                                        enabled = isDeleteEnabled,
                                        modifier = Modifier.size(32.dp)
                                ) {
                                        Icon(
                                                imageVector = AppIcons.Delete,
                                                contentDescription = "Supprimer",
                                                tint =
                                                        if (isDeleteEnabled)
                                                                Color.Red.copy(alpha = 0.8f)
                                                        else Color.Gray
                                        )
                                }
                        }
                }
        }
}

/**
 * Composant pour afficher un élément d'aliment dans une liste
 *
 * @param aliment Aliment à afficher
 * @param isEditing Indique si l'aliment est en cours d'édition
 * @param onStartEditing Action à exécuter pour commencer l'édition
 * @param onEndEditing Action à exécuter pour terminer l'édition
 * @param onQuantityChange Action à exécuter lors du changement de quantité
 */
@Composable
private fun AlimentItem(
        aliment: AlimentRation,
        isEditing: Boolean,
        onStartEditing: () -> Unit,
        onEndEditing: () -> Unit,
        onQuantityChange: (Float) -> Unit
) {
        var quantityText by
                remember(aliment.quantity) { mutableStateOf(aliment.quantity.toString()) }
        var hasError by remember { mutableStateOf(false) }

        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Nom de l'aliment
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = aliment.aliment?.nom ?: "Aliment inconnu",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium
                                )

                                // Gérer le cas où variete n'existerait pas ou serait null
                                // Nous utilisons de manière sécurisée une propriété qui devrait
                                // exister
                                val varieteTxt =
                                        try {
                                                // Essayer d'accéder à une propriété qui peut
                                                // exister
                                                val varieteField =
                                                        aliment.aliment?.javaClass
                                                                ?.getDeclaredField("variete")
                                                varieteField?.isAccessible = true
                                                varieteField?.get(aliment.aliment) as? String ?: ""
                                        } catch (e: Exception) {
                                                // En cas d'erreur, utiliser une chaîne vide
                                                ""
                                        }

                                if (varieteTxt.isNotBlank()) {
                                        Text(
                                                text = varieteTxt,
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                        )
                                }
                        }

                        // Champ de quantité
                        if (isEditing) {
                                OutlinedTextField(
                                        value = quantityText,
                                        onValueChange = { newValue ->
                                                quantityText = newValue
                                                hasError = newValue.toFloatOrNull() == null
                                        },
                                        label = { Text("Quantité (g)") },
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(120.dp),
                                        isError = hasError,
                                        singleLine = true,
                                        trailingIcon = {
                                                Row {
                                                        Text("g")

                                                        // Bouton OK pour valider
                                                        IconButton(
                                                                onClick = {
                                                                        quantityText.toFloatOrNull()
                                                                                ?.let { newQuantity
                                                                                        ->
                                                                                        onQuantityChange(
                                                                                                newQuantity
                                                                                        )
                                                                                }
                                                                },
                                                                enabled =
                                                                        !hasError &&
                                                                                quantityText
                                                                                        .toFloatOrNull() !=
                                                                                        null
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                AppIcons.Check,
                                                                        contentDescription =
                                                                                "Valider",
                                                                        tint =
                                                                                if (!hasError)
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                )
                        } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                                text = "${aliment.quantity} g",
                                                style = MaterialTheme.typography.body1
                                        )

                                        // Bouton d'édition
                                        IconButton(
                                                onClick = onStartEditing,
                                                modifier = Modifier.size(32.dp)
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Modifier la quantité",
                                                        tint = VetNutriColors.Primary
                                                )
                                        }
                                }
                        }
                }
        }
}
