package com.pleon.buyt.util

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import org.intellij.lang.annotations.Language
import org.koin.java.KoinJavaComponent.inject
import java.util.*

interface PurchaseDetailsSerializer {
    val mimeType: String
    val fileExtension: String
    fun serialize(purchaseDetails: List<PurchaseDetail>)
    fun setUpdateListener(listener: (progress: Int, fragment: String) -> Unit)
    fun setFinishListener(listener: () -> Unit)
}

class PurchaseDetailsHTMLSerializer : PurchaseDetailsSerializer {

    override val mimeType = "text/html"
    override val fileExtension = "html"

    private val application by inject(Application::class.java)
    private lateinit var updateListener: (Int, String) -> Unit
    private lateinit var finishListener: () -> Unit

    @Language("HTML")
    private val head = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="UTF-8">
            <title>Buyt purchase details</title>
            <style>
              * {
                direction: ${if (isRTL()) "RTL" else "LTR"};
              }
              svg {
                width: 100%;
                margin: 0 auto;
              }
              span {
                margin-inline-start: 16px;
              }
              h4 {
                margin-bottom: 0;
              }
            </style>
          </head>
          <body>
            <svg version="1.1" width="256px" height="256px" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="gradient" x1="11.403" x2="11.642" y1="12.574" y2="13.145" gradientUnits="userSpaceOnUse">
                  <stop stop-opacity=".3" offset="0" />
                  <stop stop-opacity="0" offset="1" />
                </linearGradient>
              </defs>
              <path fill="#56ab2f" d="m14.633 10.99c-0.325 0.285-0.692 0.522-1.098 0.69l-5.535 2.32c-2e-4 0.02 0 6 0 6l5.533-2.305c1.494-0.62 2.467-2.078 2.467-3.695 0-1.154-0.499-2.251-1.367-3.01zm-2.633 1.51c0.828 0 1.5 0.672 1.5 1.5s-0.672 1.5-1.5 1.5-1.5-0.672-1.5-1.5 0.672-1.5 1.5-1.5z" />
              <path fill="url(#gradient)" d="m14.633 10.988c-0.325 0.285-0.692 0.523-1.098 0.692l-5.535 2.32a4 4 0 0 0 0 0.061v0.609l2.555-1.07a1.5 1.5 0 0 1 1.445-1.1 1.5 1.5 0 0 1 0.727 0.189l0.808-0.339c0.556-0.231 1.046-0.583 1.442-1.022a4 4 0 0 0-0.344-0.34z" />
              <path fill="#70ae28" d="m11.939 3.99c-2.184 0-3.94 1.83-3.939 4.01v6l5.535-2.32c1.448-0.6 2.465-2.02 2.465-3.69 0-2.21-1.791-4-4-4zm0.06 2.5c0.828 0 1.5 0.6716 1.5 1.5s-0.672 1.5-1.5 1.5-1.5-0.6716-1.5-1.5 0.672-1.5 1.5-1.5z" />
            </svg>
            <hr />
    """

    @Language("HTML")
    private val tail = """
          </body>
        </html>
    """

    override fun setUpdateListener(listener: (Int, String) -> Unit) {
        updateListener = listener
    }

    override fun setFinishListener(listener: () -> Unit) {
        finishListener = listener
    }

    override fun serialize(purchaseDetails: List<PurchaseDetail>) {
        val stringBuilder = StringBuilder()
        updateListener(0, head)
        for ((i, purchaseDetail) in purchaseDetails.withIndex()) {
            stringBuilder
                    .append(dateElement(purchaseDetail.purchase.date))
                    .append(storeElement(purchaseDetail.store))
                    .append(itemsElement(purchaseDetail.item))
                    .append(horizontalRuleElement())
            val progress = ((i + 1f) / purchaseDetails.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        updateListener(100, tail)
        Thread.sleep(1000)
        finishListener()
    }

    private fun dateElement(date: Date): String {
        val formattedDate = formatDate(date)
        return "<h3>$formattedDate</h3>"
    }

    private fun storeElement(store: Store?): String {
        if (store == null) return "-"
        val stringBuilder = StringBuilder()
        val storeCategory = getString(store.category.nameRes)
        stringBuilder.append("<h4>${getString(R.string.purchase_detail_store_name)} ${store.name}</h4>")
        stringBuilder.append("<sub>$storeCategory</sub>")
        return stringBuilder.toString()
    }

    private fun itemsElement(items: List<Item>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("<ul>")
        for (item in items) {
            stringBuilder.append("<li>")
            stringBuilder.append("<span>${item.name}</span>")
            stringBuilder.append("<span>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</span>")
            stringBuilder.append("<span>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</span>")
            stringBuilder.append("</li>")
        }
        stringBuilder.append("</ul>")
        return stringBuilder.toString()
    }

    private fun horizontalRuleElement() = "  <hr />\n"

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
    private fun isRTL() = application.resources.getBoolean(R.bool.isRtl)
}

class PurchaseDetailsXMLSerializer : PurchaseDetailsSerializer {

    override val mimeType = "text/xml"
    override val fileExtension = "xml"

    private val application by inject(Application::class.java)
    private lateinit var updateListener: (Int, String) -> Unit
    private lateinit var finishListener: () -> Unit
    private val head = "<purchase-details>\n"
    private val tail = "</purchase-details>"

    override fun setUpdateListener(listener: (Int, String) -> Unit) {
        updateListener = listener
    }

    override fun setFinishListener(listener: () -> Unit) {
        finishListener = listener
    }

    override fun serialize(purchaseDetails: List<PurchaseDetail>) {
        val stringBuilder = StringBuilder()
        updateListener(0, head)
        for ((i, purchaseDetail) in purchaseDetails.withIndex()) {
            stringBuilder.append(purchaseElement(purchaseDetail))
            val progress = ((i + 1f) / purchaseDetails.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        updateListener(100, tail)
        Thread.sleep(1000)
        finishListener()
    }

    private fun purchaseElement(purchaseDetail: PurchaseDetail): String {
        val stringBuilder = StringBuilder()
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: "-"
        stringBuilder.append("  <purchase>").appendLine()
        stringBuilder.append("    <date>${formatDate(purchaseDetail.purchase.date)}</date>").appendLine()
        stringBuilder.append("    <store>").appendLine()
        stringBuilder.append("      <store-name>${purchaseDetail.store?.name ?: "-"}</store-name>").appendLine()
        stringBuilder.append("      <store-category>${storeCategory}</store-category>").appendLine()
        stringBuilder.append("      <store-location>").appendLine()
        stringBuilder.append("        <store-location-latitude>${purchaseDetail.store?.location?.latitude ?: "-"}</store-location-latitude>").appendLine()
        stringBuilder.append("        <store-location-longitude>${purchaseDetail.store?.location?.longitude ?: "-"}</store-location-longitude>").appendLine()
        stringBuilder.append("      </store-location>").appendLine()
        stringBuilder.append("    </store>").appendLine()
        stringBuilder.append("    <items>").appendLine()
        for (item in purchaseDetail.item) {
            stringBuilder.append("      <item>").appendLine()
            stringBuilder.append("        <item-name>${item.name}</item-name>").appendLine()
            stringBuilder.append("        <item-quantity>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</item-quantity>").appendLine()
            stringBuilder.append("        <item-description>${item.description ?: "-"}</item-description>").appendLine()
            stringBuilder.append("        <item-total-cost>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</item-total-cost>").appendLine()
            stringBuilder.append("        <item-urgency>${if (item.isUrgent) "!" else "-"}</item-urgency>").appendLine()
            stringBuilder.append("      </item>").appendLine()
        }
        stringBuilder.append("    </items>").appendLine()
        stringBuilder.append("  </purchase>").appendLine()
        return stringBuilder.toString()
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}

class PurchaseDetailsCSVSerializer : PurchaseDetailsSerializer {

    // CSV format sample:
    /*
      Year,Make,Model
      1997,Ford,E350
      2000,Mercury,Cougar
     */

    override val mimeType = "text/csv"
    override val fileExtension = "csv"

    private val application by inject(Application::class.java)
    private lateinit var updateListener: (Int, String) -> Unit
    private lateinit var finishListener: () -> Unit
    private val head = "Date,Store,Items\n"

    override fun setUpdateListener(listener: (Int, String) -> Unit) {
        updateListener = listener
    }

    override fun setFinishListener(listener: () -> Unit) {
        finishListener = listener
    }

    override fun serialize(purchaseDetails: List<PurchaseDetail>) {
        val stringBuilder = StringBuilder()
        updateListener(0, head)
        for ((i, purchaseDetail) in purchaseDetails.withIndex()) {
            stringBuilder.append(makeRecord(purchaseDetail))
            val progress = ((i + 1f) / purchaseDetails.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        Thread.sleep(1000)
        finishListener()
    }

    private fun makeRecord(purchaseDetail: PurchaseDetail): String {
        val stringBuilder = StringBuilder()
        val formattedDate = formatDate(purchaseDetail.purchase.date)
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: "-"
        stringBuilder.append("\"${formattedDate}\"")
        stringBuilder.append(",")
        stringBuilder.append("\"")
        stringBuilder.append(purchaseDetail.store?.name ?: "-")
        stringBuilder.append("*")
        stringBuilder.append(storeCategory)
        stringBuilder.append("\"")
        stringBuilder.append(",")
        stringBuilder.append("\"")
        for ((i, item) in purchaseDetail.item.withIndex()) {
            stringBuilder.append(item.name)
            stringBuilder.append("*")
            stringBuilder.append(getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes)))
            stringBuilder.append("*")
            stringBuilder.append(getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice)))
            if (i < purchaseDetail.item.size - 1) stringBuilder.append("\t")
        }
        stringBuilder.append("\"")
        stringBuilder.appendLine()
        return stringBuilder.toString()
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
