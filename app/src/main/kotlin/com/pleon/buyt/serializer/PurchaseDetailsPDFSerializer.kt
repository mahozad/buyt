package com.pleon.buyt.serializer

import android.app.Activity
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintJob
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.getCurrentLocale
import com.pleon.buyt.util.setLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.util.*

class PurchaseDetailsPDFSerializer(
    private val activity: Activity,
    private val defaultExportFileName: String,
    private val htmlSerializer: PurchaseDetailsHTMLSerializer
) : Serializer<PurchaseDetail> {

    override val mimeType = "application/pdf"
    override val fileExtension = "pdf"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private lateinit var printJob: PrintJob
    private var webViewRef: WebView? = null

    /**
     * WebView should be created in the main thread, thus the *Dispatchers.Main*.
     *
     * NOTE: Creating a *WebView* instance changes the locale of the app to the
     *  default locale of the system. To see this, with system (phone) language
     *  set to *English* and app language set to *Farsi*, select *PDF* from
     *  export dialog and see the tables headings being in *English* instead of in *Farsi*.
     *  For more information see [this post](https://stackoverflow.com/q/40398528).
     */
    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Main) {
        val appCurrentLocale = getCurrentLocale(activity)
        // Create a WebView object specifically for printing
        val webView = WebView(activity)
        fixBugWithWebViewLocale(appCurrentLocale)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, req: WebResourceRequest) = false
            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view, activity)
                webViewRef = null
            }
        }
        // Generate an HTML document on the fly:
        val htmlBuilder = StringBuilder()
        htmlSerializer.updateListener = { _, fragment ->
            htmlBuilder.append(fragment)
            updateListener?.invoke(80, "")
        }
        htmlSerializer.finishListener = {
            val html = htmlBuilder.toString()
            withContext(Dispatchers.Main) {
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
            finishListener?.invoke()
        }
        // Keep a reference to WebView object until you pass the PrintDocumentAdapter to the PrintManager
        webViewRef = webView
        htmlSerializer.serialize(entities)
    }

    /**
     * FIXME: This still does not fully fix the problem with locale.
     *  For an example, see units and price tags in purchase details screen.
     *  For more information see the comments of the [serialize] function.
     */
    private fun fixBugWithWebViewLocale(locale: Locale) {
        setLocale(activity, locale)
    }

    private fun createWebPrintJob(webView: WebView, activity: Activity) {
        (activity.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->
            val jobName = "Buyt Print Document"
            val defaultDocumentName = "$defaultExportFileName.$fileExtension"
            val printAdapter = webView.createPrintDocumentAdapter(defaultDocumentName)
            // Save the job object for later status checking
            printJob = printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    }
}
