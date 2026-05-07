package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

data class SelectionItem(
        val id: String,
        val title: String,
        val subtitle: String? = null
)

@Composable
fun SelectionDialog(
        title: String,
        items: List<SelectionItem>,
        initialSelectedIds: Set<String>,
        onConfirm: (Set<String>) -> Unit,
        onDismiss: () -> Unit,
        confirmLabel: String = "Valider",
        emptyLabel: String = "Aucun element disponible.",
        filtersContent: (@Composable () -> Unit)? = null,
        filterPredicate: (SelectionItem) -> Boolean = { true }
) {
        var selectedIds by remember { mutableStateOf(initialSelectedIds) }
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(initialSelectedIds) {
                selectedIds = initialSelectedIds
        }

        val filteredItems =
                remember(items, searchQuery, filterPredicate) {
                        val baseItems = items.filter(filterPredicate)
                        if (searchQuery.isBlank()) baseItems
                        else {
                                val words =
                                        searchQuery.trim().split("\\s+".toRegex()).map {
                                                it.lowercase()
                                        }
                                baseItems.filter { item ->
                                        val haystack =
                                                buildString {
                                                        append(item.title.lowercase())
                                                        item.subtitle?.let {
                                                                append(" ")
                                                                append(it.lowercase())
                                                        }
                                                }
                                        words.all { word -> haystack.contains(word) }
                                }
                        }
                }

        val availableItems = filteredItems.filterNot { it.id in selectedIds }
        val selectedItems = items.filter { it.id in selectedIds }

        Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
                Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = title,
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Fermer"
                                                )
                                        }
                                }

                                OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        label = { Text("Rechercher") },
                                        leadingIcon = {
                                                Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = null
                                                )
                                        },
                                        trailingIcon = {
                                                if (searchQuery.isNotBlank()) {
                                                        IconButton(
                                                                onClick = {
                                                                        searchQuery = ""
                                                                }
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default.Clear,
                                                                        contentDescription = "Effacer"
                                                                )
                                                        }
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )
                                filtersContent?.invoke()

                                if (items.isEmpty()) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(emptyLabel)
                                        }
                                } else {
                                        BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                                val isCompact = maxWidth < 700.dp

                                                if (isCompact) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize(),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                12.dp
                                                                        )
                                                        ) {
                                                                SelectionListCard(
                                                                        title =
                                                                                "Disponibles (${availableItems.size})",
                                                                        items = availableItems,
                                                                        showAdd = true,
                                                                        onAdd = { item ->
                                                                                selectedIds =
                                                                                        selectedIds +
                                                                                                item.id
                                                                        }
                                                                )

                                                                SelectionActionButtons(
                                                                        onAddAll = {
                                                                                selectedIds =
                                                                                        selectedIds +
                                                                                                availableItems.map {
                                                                                                        it.id
                                                                                                }
                                                                                        .toSet()
                                                                        },
                                                                        onRemoveAll = {
                                                                                selectedIds =
                                                                                        emptySet()
                                                                        },
                                                                        enableAddAll =
                                                                                availableItems
                                                                                        .isNotEmpty(),
                                                                        enableRemoveAll =
                                                                                selectedIds
                                                                                        .isNotEmpty()
                                                                )

                                                                SelectionListCard(
                                                                        title =
                                                                                "Selectionnes (${selectedItems.size})",
                                                                        items = selectedItems,
                                                                        showRemove = true,
                                                                        onRemove = { item ->
                                                                                selectedIds =
                                                                                        selectedIds -
                                                                                                item.id
                                                                        }
                                                                )
                                                        }
                                                } else {
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxSize(),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                12.dp
                                                                        )
                                                        ) {
                                                                SelectionListCard(
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        1f
                                                                                ),
                                                                        title =
                                                                                "Disponibles (${availableItems.size})",
                                                                        items = availableItems,
                                                                        showAdd = true,
                                                                        onAdd = { item ->
                                                                                selectedIds =
                                                                                        selectedIds +
                                                                                                item.id
                                                                        }
                                                                )

                                                                SelectionActionButtons(
                                                                        onAddAll = {
                                                                                selectedIds =
                                                                                        selectedIds +
                                                                                                availableItems.map {
                                                                                                        it.id
                                                                                                }
                                                                                        .toSet()
                                                                        },
                                                                        onRemoveAll = {
                                                                                selectedIds =
                                                                                        emptySet()
                                                                        },
                                                                        enableAddAll =
                                                                                availableItems
                                                                                        .isNotEmpty(),
                                                                        enableRemoveAll =
                                                                                selectedIds
                                                                                        .isNotEmpty()
                                                                )

                                                                SelectionListCard(
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        1f
                                                                                ),
                                                                        title =
                                                                                "Selectionnes (${selectedItems.size})",
                                                                        items = selectedItems,
                                                                        showRemove = true,
                                                                        onRemove = { item ->
                                                                                selectedIds =
                                                                                        selectedIds -
                                                                                                item.id
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) {
                                                Text("Annuler")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { onConfirm(selectedIds) }) {
                                                Text(confirmLabel)
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun SelectionListCard(
        title: String,
        items: List<SelectionItem>,
        modifier: Modifier = Modifier,
        showAdd: Boolean = false,
        showRemove: Boolean = false,
        onAdd: ((SelectionItem) -> Unit)? = null,
        onRemove: ((SelectionItem) -> Unit)? = null
) {
        Card(modifier = modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSizes.paddingSmall)) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.subtitle2,
                                fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        LazyColumn(
                                modifier = Modifier.heightIn(min = 180.dp, max = 360.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                                items(items) { item ->
                                        SelectionItemRow(
                                                item = item,
                                                showAdd = showAdd,
                                                showRemove = showRemove,
                                                onAdd = onAdd,
                                                onRemove = onRemove
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun SelectionItemRow(
        item: SelectionItem,
        showAdd: Boolean,
        showRemove: Boolean,
        onAdd: ((SelectionItem) -> Unit)?,
        onRemove: ((SelectionItem) -> Unit)?
) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
                Row(
                        modifier = Modifier.padding(AppSizes.paddingSmall / 2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                                if (!item.subtitle.isNullOrBlank()) {
                                        Text(
                                                text = item.subtitle ?: "",
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }
                        }

                        if (showAdd && onAdd != null) {
                                IconButtonWithTooltip(
                                        onClick = { onAdd(item) },
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ajouter",
                                        tooltip = "Ajouter"
                                )
                        }
                        if (showRemove && onRemove != null) {
                                IconButtonWithTooltip(
                                        onClick = { onRemove(item) },
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Retirer",
                                        tooltip = "Retirer"
                                )
                        }
                }
        }
}

@Composable
private fun SelectionActionButtons(
        onAddAll: () -> Unit,
        onRemoveAll: () -> Unit,
        enableAddAll: Boolean,
        enableRemoveAll: Boolean
) {
        Column(
                modifier = Modifier.padding(vertical = AppSizes.paddingMedium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Button(onClick = onAddAll, enabled = enableAddAll) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tout ajouter", style = MaterialTheme.typography.caption)
                }
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Button(onClick = onRemoveAll, enabled = enableRemoveAll) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tout retirer", style = MaterialTheme.typography.caption)
                }
        }
}
