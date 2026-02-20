package fr.vetbrain.vetnutri_mp.Export

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext

actual object PdfExporter {
    actual suspend fun exportDocument(
            documentType: DocumentType,
            data: ExportData,
            defaultFileName: String
    ): Boolean {
                val activity: Activity? = AndroidContext.getCurrentActivityOrNull()
                if (activity == null) return false
                val html: String = HtmlDocumentBuilder.buildHtml(documentType, data)
                try {
                        val webView = WebView(activity)
                        webView.settings.javaScriptEnabled = false
                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                        webView.webViewClient =
                                object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                                val printManager =
                                                        activity.getSystemService(
                                                                Context.PRINT_SERVICE
                                                        ) as
                                                                PrintManager
                                                val jobName =
                                                        defaultFileName.ifBlank { "document.pdf" }
                                                val printAdapter: PrintDocumentAdapter =
                                                        view!!.createPrintDocumentAdapter(jobName)
                                                val attributes =
                                                        PrintAttributes.Builder()
                                                                .setMediaSize(
                                                                        PrintAttributes.MediaSize
                                                                                .ISO_A4
                                                                                .asLandscape()
                                                                )
                                                                .setResolution(
                                                                        PrintAttributes.Resolution(
                                                                                "pdf",
                                                                                "pdf",
                                                                                300,
                                                                                300
                                                                        )
                                                                )
                                                                .setMinMargins(
                                                                        PrintAttributes.Margins
                                                                                .NO_MARGINS
                                                                )
                                                                .build()
                                                printManager.print(
                                                        jobName,
                                                        printAdapter,
                                                        attributes
                                                )
                                        }
                                }
                        return true
                } catch (t: Throwable) {
                        return false
                }
        }
}
