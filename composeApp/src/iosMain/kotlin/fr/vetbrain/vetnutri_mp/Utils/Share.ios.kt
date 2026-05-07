package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController
import platform.Foundation.NSURL
import kotlinx.cinterop.ExperimentalForeignApi

@Composable
actual fun rememberShareLauncher(): (String) -> Unit {
    return remember {
        { text ->
            // On peut partager le texte directement, ou une URL si c'en est une
            val item = if (text.startsWith("http")) NSURL.URLWithString(text) ?: text else text
            
            val controller = UIActivityViewController(listOf(item), null)
            val window = UIApplication.sharedApplication.keyWindow 
                ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
            
            val rootViewController = window?.rootViewController
            
            // Configuration pour iPad
            @OptIn(ExperimentalForeignApi::class)
            val popover = controller.popoverPresentationController
            if (popover != null) {
                popover.sourceView = window
                @OptIn(ExperimentalForeignApi::class)
                popover.sourceRect = window?.bounds ?: platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)
            }

            rootViewController?.presentViewController(controller, true, null)
        }
    }
}







