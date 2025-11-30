package fr.vetbrain.vetnutri_mp.Utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.Foundation.NSURL

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
            controller.popoverPresentationController?.sourceView = window
            controller.popoverPresentationController?.sourceRect = window?.bounds ?: platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)

            rootViewController?.presentViewController(controller, true, null)
        }
    }
}





