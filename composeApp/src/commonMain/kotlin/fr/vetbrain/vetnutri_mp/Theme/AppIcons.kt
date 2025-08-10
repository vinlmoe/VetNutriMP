package fr.vetbrain.vetnutri_mp.Theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/** Objet contenant toutes les icônes utilisées dans l'application */
object AppIcons {
    // Navigation et actions générales
    val Add: ImageVector = Icons.Default.Add
    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowDropDown: ImageVector = Icons.Default.MoreVert
    val ArrowDropUp: ImageVector = Icons.Default.MoreVert
    val Check: ImageVector = Icons.Default.Check
    val Close: ImageVector = Icons.Default.Close
    val ContentCopy: ImageVector = Icons.Default.Add // Utiliser Add comme fallback temporaire
    val DateRange: ImageVector = Icons.Default.DateRange
    val Delete: ImageVector = Icons.Default.Delete
    val Edit: ImageVector = Icons.Default.Edit
    val Info: ImageVector = Icons.Default.Info
    val Menu: ImageVector = Icons.Default.Menu
    val MoreVert: ImageVector = Icons.Default.MoreVert
    val Search: ImageVector = Icons.Default.Search
    val Settings: ImageVector = Icons.Default.Settings
    val Share: ImageVector = Icons.Default.Share
    val ViewList: ImageVector = Icons.Default.List
    val Warning: ImageVector = Icons.Default.Warning
    val Analytics: ImageVector = Icons.Default.Analytics

    // Icônes spécifiques à l'application
    val Animal: ImageVector = Icons.Default.Star
    val Consultation: ImageVector = Icons.Default.DateRange
    val Ration: ImageVector = Icons.Default.List
    val Weight: ImageVector = Icons.Default.Star
    val Export: ImageVector = Icons.Default.Send
    val Import: ImageVector = Icons.Default.Send
    val Calculate: ImageVector = Icons.Default.Check
    val Save: ImageVector = Icons.Default.Done
    val Cancel: ImageVector = Icons.Default.Close
    val Library: ImageVector = Icons.Default.Info

    // Icônes pour les nutriments et analyses
    val Nutrient: ImageVector = Icons.Default.Star
    val Complete: ImageVector = Icons.Default.Check
    val Incomplete: ImageVector = Icons.Default.Warning
    val Analysis: ImageVector = Icons.Default.List
}
