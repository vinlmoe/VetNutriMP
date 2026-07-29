package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Dp
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import org.jetbrains.skia.Image as SkiaImage
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AppLogo(
    modifier: Modifier,
    size: Dp,
    tint: Color,
    contentDescription: String?
) {
    val painter = remember { loadAppIconPainter() }

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(color = tint, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                style = androidx.compose.material.MaterialTheme.typography.h6
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun loadAppIconPainter(): BitmapPainter? {
    return try {
        val uiImage = UIImage.imageNamed("AppIcon") ?: return null
        val pngData = UIImagePNGRepresentation(uiImage) ?: return null
        val length = pngData.length.toInt()
        if (length == 0) return null
        val bytes = pngData.bytes?.reinterpret<ByteVar>()?.readBytes(length) ?: return null
        val skiaImage = SkiaImage.makeFromEncoded(bytes)
        BitmapPainter(skiaImage.toComposeImageBitmap())
    } catch (_: Throwable) {
        null
    }
}
