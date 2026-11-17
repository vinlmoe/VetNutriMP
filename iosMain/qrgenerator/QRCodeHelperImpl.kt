package qrgenerator

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.QRCodeGenerator
import platform.CoreImage.createCGImage
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.setValue
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIWindow

private var uiImage: UIImage? = null

actual fun generateCode(text: String): ImageBitmap {
    uiImage = generateQRCode(text, QRCodeSize.WIDTH, QRCodeSize.HEIGHT)
    return uiImage?.toImageBitmap() ?: ImageBitmap(0, 0)
}

@OptIn(ExperimentalForeignApi::class)
fun generateQRCode(text: String, width: Int, height: Int): UIImage {
    val context = CIContext()
    val filter = CIFilter.QRCodeGenerator()

    val data = text.toByteArray().toNSData()
    filter.setValue(data, forKey = "inputMessage")

    val qrCodeImage =
        filter.outputImage ?: return UIImage.systemImageNamed("xmark") ?: UIImage()

    val qrWidth = memScoped {
        val ptr = qrCodeImage.extent().getPointer(this)
        ptr.pointed.size.width
    }
    val qrHeight = memScoped {
        val ptr = qrCodeImage.extent().getPointer(this)
        ptr.pointed.size.height
    }
    val scaleX = width.toFloat() / qrWidth.toFloat()
    val scaleY = height.toFloat() / qrHeight.toFloat()
    val transform = CGAffineTransformMakeScale(scaleX.toDouble(), scaleY.toDouble())
    val transformedImage = qrCodeImage.imageByApplyingTransform(transform)
    val cgImage = context.createCGImage(transformedImage, transformedImage.extent())
        ?: return UIImage.systemImageNamed("xmark") ?: UIImage()
    return UIImage.imageWithCGImage(cgImage)
}

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    memScoped {
        val data = this@toNSData
        val pointer = data.usePinned {
            it.addressOf(0)
        }
        return NSData.create(bytes = pointer, length = data.size.toULong())
    }
}

