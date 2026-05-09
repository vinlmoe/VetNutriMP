package qrgenerator

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale

expect fun generateCode(text: String): ImageBitmap

fun generateQrCode(
    url: String,
    onSuccess: (String, ImageBitmap) -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        val imageBitmap = generateCode(url)
        onSuccess(url, imageBitmap)
    } catch (e: Exception) {
        onFailure("${e.message}")
    }
}

@Composable
fun QRCodeImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    onSuccess: (ImageBitmap) -> Unit = { qrImage -> },
    onFailure: (String) -> Unit = { message -> }
) {
    val qrCode = remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        generateQrCode(url, onSuccess = { url, qrImage ->
            qrCode.value = qrImage
            onSuccess(qrImage)
        }, onFailure = {
            onFailure(it)
        })
    }

    qrCode.value?.let {
        Image(
            bitmap = it,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}