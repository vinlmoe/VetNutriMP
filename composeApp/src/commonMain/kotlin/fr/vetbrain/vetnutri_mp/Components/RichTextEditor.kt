package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Export.*

/** Éditeur de texte enrichi pour créer des sections HTML réutilisables */
@Composable
fun RichTextEditor(
        initialContent: RichTextContent = RichTextContent(),
        onContentChange: (RichTextContent) -> Unit,
        modifier: Modifier = Modifier
) {
    var content by remember { mutableStateOf(initialContent) }
    var selectedBlockIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier) {
        // Barre d'outils pour ajouter des blocs
        EditorToolbar(
                onAddParagraph = {
                    val newBlock =
                            TextBlock.Paragraph(
                                    id = generateBlockId(),
                                    text = "",
                                    formatting = TextFormatting()
                            )
                    content = content.copy(blocks = content.blocks + newBlock)
                    selectedBlockIndex = content.blocks.size
                },
                onAddHeading = { level ->
                    val newBlock =
                            TextBlock.Heading(id = generateBlockId(), level = level, text = "")
                    content = content.copy(blocks = content.blocks + newBlock)
                    selectedBlockIndex = content.blocks.size
                },
                onAddList = { isOrdered ->
                    val newBlock =
                            TextBlock.ListBlock(
                                    id = generateBlockId(),
                                    items = listOf(""),
                                    isOrdered = isOrdered
                            )
                    content = content.copy(blocks = content.blocks + newBlock)
                    selectedBlockIndex = content.blocks.size
                },
                onAddTable = {
                    val newBlock =
                            TextBlock.TableBlock(
                                    id = generateBlockId(),
                                    headers = listOf("Colonne 1", "Colonne 2"),
                                    rows = listOf(listOf("", ""))
                            )
                    content = content.copy(blocks = content.blocks + newBlock)
                    selectedBlockIndex = content.blocks.size
                }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Barre de formatage pour les paragraphes sélectionnés
        selectedBlockIndex?.let { index ->
            content.blocks.getOrNull(index)?.let { block ->
                if (block is TextBlock.Paragraph) {
                    FormattingToolbar(
                            formatting = block.formatting,
                            onFormattingChange = { newFormatting ->
                                val newBlock = block.copy(formatting = newFormatting)
                                val newBlocks = content.blocks.toMutableList()
                                newBlocks[index] = newBlock
                                content = content.copy(blocks = newBlocks)
                                onContentChange(content)
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Zone d'édition des blocs
        LazyColumn(
                modifier =
                        Modifier.fillMaxWidth()
                                .weight(1f)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(content.blocks) { index, block ->
                key(block.id) {
                    EditableBlock(
                            block = block,
                            isSelected = selectedBlockIndex == index,
                            onBlockChange = { newBlock ->
                                val newBlocks = content.blocks.toMutableList()
                                newBlocks[index] = newBlock
                                content = content.copy(blocks = newBlocks)
                                onContentChange(content)
                            },
                            onBlockSelect = { selectedBlockIndex = index },
                            onBlockDelete = {
                                val newBlocks = content.blocks.toMutableList()
                                newBlocks.removeAt(index)
                                content = content.copy(blocks = newBlocks)
                                onContentChange(content)
                                selectedBlockIndex = null
                            }
                    )
                }
            }

            // Bloc vide pour ajouter du contenu
            if (content.blocks.isEmpty()) {
                item {
                    Text(
                            "Cliquez sur les boutons ci-dessus pour ajouter du contenu",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                    )
                }
            }
        }
    }

    // Mettre à jour le contenu parent quand il change
    LaunchedEffect(content) { onContentChange(content) }
}

/** Barre d'outils de l'éditeur */
@Composable
private fun EditorToolbar(
        onAddParagraph: () -> Unit,
        onAddHeading: (Int) -> Unit,
        onAddList: (Boolean) -> Unit,
        onAddTable: () -> Unit
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.1f))
                            .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton paragraphe
        IconButton(onClick = onAddParagraph) { Icon(Icons.Default.TextFields, "Paragraphe") }

        // Boutons titres
        IconButton(onClick = { onAddHeading(1) }) {
            Text("H1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        IconButton(onClick = { onAddHeading(2) }) {
            Text("H2", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        IconButton(onClick = { onAddHeading(3) }) {
            Text("H3", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Boutons listes
        IconButton(onClick = { onAddList(false) }) { Icon(Icons.Default.List, "Liste") }
        IconButton(onClick = { onAddList(true) }) {
            Icon(Icons.Default.FormatListNumbered, "Liste numérotée")
        }

        // Bouton tableau
        IconButton(onClick = onAddTable) { Icon(Icons.Default.TableChart, "Tableau") }
    }
}

/** Barre d'outils de formatage pour les paragraphes */
@Composable
private fun FormattingToolbar(
        formatting: TextFormatting,
        onFormattingChange: (TextFormatting) -> Unit
) {
    Row(
            modifier =
                    Modifier.fillMaxWidth().background(Color.Blue.copy(alpha = 0.1f)).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton gras
        IconButton(onClick = { onFormattingChange(formatting.copy(isBold = !formatting.isBold)) }) {
            Icon(
                    Icons.Default.FormatBold,
                    "Gras",
                    tint = if (formatting.isBold) Color.Blue else Color.Gray
            )
        }

        // Bouton italique
        IconButton(
                onClick = { onFormattingChange(formatting.copy(isItalic = !formatting.isItalic)) }
        ) {
            Icon(
                    Icons.Default.FormatItalic,
                    "Italique",
                    tint = if (formatting.isItalic) Color.Blue else Color.Gray
            )
        }

        // Bouton souligné
        IconButton(
                onClick = {
                    onFormattingChange(formatting.copy(isUnderline = !formatting.isUnderline))
                }
        ) {
            Icon(
                    Icons.Default.FormatUnderlined,
                    "Souligné",
                    tint = if (formatting.isUnderline) Color.Blue else Color.Gray
            )
        }

        // Bouton barré
        IconButton(
                onClick = {
                    onFormattingChange(
                            formatting.copy(isStrikethrough = !formatting.isStrikethrough)
                    )
                }
        ) {
            Icon(
                    Icons.Default.FormatStrikethrough,
                    "Barré",
                    tint = if (formatting.isStrikethrough) Color.Blue else Color.Gray
            )
        }

        // Séparateur
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray))

        // Boutons d'alignement
        IconButton(
                onClick = { onFormattingChange(formatting.copy(alignment = TextAlignment.LEFT)) }
        ) {
            Icon(
                    Icons.Default.FormatAlignLeft,
                    "Aligner à gauche",
                    tint =
                            if (formatting.alignment == TextAlignment.LEFT) Color.Blue
                            else Color.Gray
            )
        }

        IconButton(
                onClick = { onFormattingChange(formatting.copy(alignment = TextAlignment.CENTER)) }
        ) {
            Icon(
                    Icons.Default.FormatAlignCenter,
                    "Centrer",
                    tint =
                            if (formatting.alignment == TextAlignment.CENTER) Color.Blue
                            else Color.Gray
            )
        }

        IconButton(
                onClick = { onFormattingChange(formatting.copy(alignment = TextAlignment.RIGHT)) }
        ) {
            Icon(
                    Icons.Default.FormatAlignRight,
                    "Aligner à droite",
                    tint =
                            if (formatting.alignment == TextAlignment.RIGHT) Color.Blue
                            else Color.Gray
            )
        }

        // Séparateur
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray))

        // Sélecteur de couleur
        ColorPickerButton(
                currentColor = formatting.color,
                onColorSelected = { color -> onFormattingChange(formatting.copy(color = color)) }
        )

        // Sélecteur de taille de police
        FontSizeSelector(
                currentSize = formatting.fontSize,
                onSizeSelected = { size -> onFormattingChange(formatting.copy(fontSize = size)) }
        )
    }
}

/** Bouton sélecteur de couleur */
@Composable
private fun ColorPickerButton(currentColor: String?, onColorSelected: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colors =
            listOf(
                    "#000000",
                    "#FF0000",
                    "#00FF00",
                    "#0000FF",
                    "#FFFF00",
                    "#FF00FF",
                    "#00FFFF",
                    "#FFA500",
                    "#800080",
                    "#008000",
                    "#000080",
                    "#808080"
            )

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                    Icons.Default.ColorLens,
                    "Couleur",
                    tint = if (currentColor != null) Color.Blue else Color.Gray
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Option pour aucune couleur
            DropdownMenuItem(
                    onClick = {
                        onColorSelected(null)
                        expanded = false
                    }
            ) { Text("Aucune couleur", color = Color.Gray) }

            // Palette de couleurs
            colors.forEach { colorHex ->
                DropdownMenuItem(
                        onClick = {
                            onColorSelected(colorHex)
                            expanded = false
                        }
                ) {
                    Box(
                            modifier =
                                    Modifier.size(20.dp)
                                            .background(
                                                    Color.Red
                                            ) // Couleur temporaire pour la démonstration
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(colorHex)
                }
            }
        }
    }
}

/** Sélecteur de taille de police */
@Composable
private fun FontSizeSelector(currentSize: Int?, onSizeSelected: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val fontSizes = listOf(8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 36)

    Box {
        IconButton(onClick = { expanded = true }) {
            Text(
                    text = currentSize?.toString() ?: "12",
                    fontSize = 14.sp,
                    color = if (currentSize != null) Color.Blue else Color.Gray
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Option taille par défaut
            DropdownMenuItem(
                    onClick = {
                        onSizeSelected(null)
                        expanded = false
                    }
            ) { Text("Taille par défaut", color = Color.Gray) }

            // Tailles disponibles
            fontSizes.forEach { size ->
                DropdownMenuItem(
                        onClick = {
                            onSizeSelected(size)
                            expanded = false
                        }
                ) { Text("${size}pt", fontSize = size.sp) }
            }
        }
    }
}

/** Bloc éditable générique */
@Composable
private fun EditableBlock(
        block: TextBlock,
        isSelected: Boolean,
        onBlockChange: (TextBlock) -> Unit,
        onBlockSelect: () -> Unit,
        onBlockDelete: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Blue.copy(alpha = 0.1f) else Color.Transparent

    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(backgroundColor, RoundedCornerShape(4.dp))
                            .border(
                                    1.dp,
                                    if (isSelected) Color.Blue else Color.LightGray,
                                    RoundedCornerShape(4.dp)
                            )
                            .clickable(onClick = onBlockSelect)
                            .padding(8.dp)
    ) {
        when (block) {
            is TextBlock.Paragraph -> ParagraphBlockEditor(block, onBlockChange)
            is TextBlock.Heading -> HeadingBlockEditor(block, onBlockChange)
            is TextBlock.ListBlock -> ListBlockEditor(block, onBlockChange)
            is TextBlock.TableBlock -> TableBlockEditor(block, onBlockChange)
        }

        // Boutons d'action quand sélectionné
        if (isSelected) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onBlockDelete) {
                    Icon(Icons.Default.Delete, "Supprimer", tint = Color.Red)
                }
            }
        }
    }
}

/** Éditeur de paragraphe */
@Composable
private fun ParagraphBlockEditor(block: TextBlock.Paragraph, onBlockChange: (TextBlock) -> Unit) {
    var text by remember(block.text) { mutableStateOf(block.text) }

    BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onBlockChange(block.copy(text = it))
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 14.sp),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text("Tapez votre paragraphe ici...", color = Color.Gray)
                }
                innerTextField()
            }
    )
}

/** Éditeur de titre */
@Composable
private fun HeadingBlockEditor(block: TextBlock.Heading, onBlockChange: (TextBlock) -> Unit) {
    var text by remember(block.text) { mutableStateOf(block.text) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("H${block.level}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    onBlockChange(block.copy(text = it))
                },
                modifier = Modifier.weight(1f),
                textStyle =
                        TextStyle(
                                fontSize =
                                        when (block.level) {
                                            1 -> 24.sp
                                            2 -> 20.sp
                                            3 -> 18.sp
                                            else -> 16.sp
                                        },
                                fontWeight = FontWeight.Bold
                        ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Titre H${block.level}", color = Color.Gray)
                    }
                    innerTextField()
                }
        )
    }
}

/** Éditeur de liste */
@Composable
private fun ListBlockEditor(block: TextBlock.ListBlock, onBlockChange: (TextBlock) -> Unit) {
    var items by remember(block.items) { mutableStateOf(block.items.toMutableList()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
                if (block.isOrdered) "Liste numérotée" else "Liste à puces",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                        if (block.isOrdered) "${index + 1}." else "•",
                        modifier = Modifier.padding(end = 8.dp)
                )
                BasicTextField(
                        value = item,
                        onValueChange = { newValue ->
                            items[index] = newValue
                            onBlockChange(block.copy(items = items))
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 14.sp),
                        decorationBox = { innerTextField ->
                            if (item.isEmpty()) {
                                Text("Élément de liste", color = Color.Gray)
                            }
                            innerTextField()
                        }
                )
                // Bouton pour supprimer l'élément
                if (items.size > 1) {
                    IconButton(
                            onClick = {
                                items.removeAt(index)
                                onBlockChange(block.copy(items = items))
                            },
                            modifier = Modifier.size(24.dp)
                    ) { Icon(Icons.Default.Close, "Supprimer", tint = Color.Red) }
                }
            }
        }

        // Bouton pour ajouter un élément
        TextButton(
                onClick = {
                    items.add("")
                    onBlockChange(block.copy(items = items))
                },
                modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, "Ajouter")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ajouter un élément")
        }
    }
}

/** Éditeur de tableau */
@Composable
private fun TableBlockEditor(block: TextBlock.TableBlock, onBlockChange: (TextBlock) -> Unit) {
    var headers by remember(block.headers) { mutableStateOf(block.headers.toMutableList()) }
    var rows by
            remember(block.rows) {
                mutableStateOf(block.rows.map { it.toMutableList() }.toMutableList())
            }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tableau", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        // Édition des en-têtes
        Row(modifier = Modifier.fillMaxWidth()) {
            headers.forEachIndexed { index, header ->
                BasicTextField(
                        value = header,
                        onValueChange = { newValue ->
                            headers[index] = newValue
                            onBlockChange(block.copy(headers = headers))
                        },
                        modifier = Modifier.weight(1f).border(1.dp, Color.LightGray).padding(4.dp),
                        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        decorationBox = { innerTextField ->
                            if (header.isEmpty()) {
                                Text("En-tête", color = Color.Gray, fontSize = 12.sp)
                            }
                            innerTextField()
                        }
                )
            }
        }

        // Édition des lignes
        rows.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIndex, cell ->
                    BasicTextField(
                            value = cell,
                            onValueChange = { newValue ->
                                rows[rowIndex][colIndex] = newValue
                                onBlockChange(block.copy(rows = rows))
                            },
                            modifier =
                                    Modifier.weight(1f).border(1.dp, Color.LightGray).padding(4.dp),
                            textStyle = TextStyle(fontSize = 12.sp),
                            decorationBox = { innerTextField ->
                                if (cell.isEmpty()) {
                                    Text("Cellule", color = Color.Gray, fontSize = 12.sp)
                                }
                                innerTextField()
                            }
                    )
                }
            }
        }

        // Boutons d'action
        Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                    onClick = {
                        headers.add("Colonne ${headers.size + 1}")
                        rows.forEach { it.add("") }
                        onBlockChange(block.copy(headers = headers, rows = rows))
                    }
            ) {
                Icon(Icons.Default.Add, "Ajouter colonne")
                Text("Colonne")
            }

            TextButton(
                    onClick = {
                        rows.add(MutableList(headers.size) { "" })
                        onBlockChange(block.copy(rows = rows))
                    }
            ) {
                Icon(Icons.Default.Add, "Ajouter ligne")
                Text("Ligne")
            }
        }
    }
}

/** Génère un ID unique pour un bloc */
private fun generateBlockId(): String =
        "block_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}"
