package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.formatPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import org.koin.java.KoinJavaComponent.inject

/**
 * See https://developer.mozilla.org/en-US/docs/Learn/HTML/Tables/Advanced.
 * Could also have incremented the "rowspan" of the first row by one
 * and moved the first item to a standalone <tr> element.
 */
class PurchaseDetailsHTMLSerializer : Serializer<PurchaseDetail> {

    override val mimeType = "text/html"
    override val fileExtension = "html"

    private val application by inject(Application::class.java)
    override lateinit var updateListener: suspend (Int, String) -> Unit
    override lateinit var finishListener: suspend () -> Unit

    @Language("HTML")
    private val head = """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="UTF-8">
            <title>${getString(R.string.exported_html_title)}</title>
            <style>
              * {
                direction: ${if (isRTL()) "RTL" else "LTR"};
              }
              #logo, #empty-hint {
                width: 100%;
                margin: 0 auto;
              }
              #empty-hint {
                margin-top: 64px;
                font-size: 32px;
              }
              table caption div {
                display: inline-block;
                padding: 4px 16px;
                background: rgba(208, 219, 223, 0.6);
                border-radius: 100px;
                font-size: 18px;
                margin: 16px auto;
              }
              table {
                margin: auto;
                margin-bottom: 32px;
              }
              table, th, td {
                border: 1px solid black;
                border-collapse: collapse;
                text-align: center;
              }
              th, td {
                padding: 4px 8px;
              }
              th {
                background: rgba(208, 223, 219, 0.2);
              }
              .thick-border {}
            </style>
          </head>
          <body>
            <svg id="logo" version="1.1" width="256px" height="256px" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
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

    override suspend fun serialize(entities: List<PurchaseDetail>) = withContext(Dispatchers.Default){
        updateListener(0, head)
        createBody(entities)
        updateListener(100, tail)
        finishListener()
    }

    private suspend fun createBody(entities: List<PurchaseDetail>) {
        if (entities.isEmpty()) {
            updateListener(100, createEmptyHint())
        }
        val stringBuilder = StringBuilder()
        val purchaseDetailsByDate = entities.groupBy { formatDate(it.purchase.date) }
        purchaseDetailsByDate.onEachIndexed { i, entry ->
            val date = entry.key
            val datePurchaseDetails = entry.value
            stringBuilder.append(tableStart(date))
            for (purchaseDetail in datePurchaseDetails) {
                stringBuilder.append(storeAndFirstItem(purchaseDetail.store!!, purchaseDetail.item))
                stringBuilder.append(remainingItems(purchaseDetail.item.drop(1)))
            }
            stringBuilder.append("</table>")
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
    }

    private fun remainingItems(items: List<Item>): String {
        val stringBuilder = StringBuilder()
        for (item in items) {
            stringBuilder.append("""
                <tr>
                 <td>${item.name}</td>
                 <td>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</td>
                 <td>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</td>
               </tr>"""
            )
        }
        return stringBuilder.toString()
    }

    private fun storeAndFirstItem(store: Store, items: List<Item>): String {
        val storeCategory = getString(store.category.nameRes)
        val totalCost = items.sumOf { it.totalPrice }
        val firstItem = items.first()
        return """
        <tr class="thick-border">
          <td rowspan="${items.size}">${store.name}</td>
          <td rowspan="${items.size}">${storeCategory}</td>
          <td rowspan="${items.size}">${getQuantityString(R.plurals.price_with_suffix, totalCost.toInt(), formatPrice(totalCost))}</td>
          <td>${firstItem.name}</td>
          <td>${getString(R.string.item_quantity, firstItem.quantity.value, getString(firstItem.quantity.unit.nameRes))}</td>
          <td>${getQuantityString(R.plurals.price_with_suffix, firstItem.totalPrice.toInt(), formatPrice(firstItem.totalPrice))}</td>
        </tr>
        """
    }

    @Language("HTML")
    private fun tableStart(date: String): String = """
    <table class="outer-table">
      <caption><div>$date</div></caption>
      <tr>
        <th rowspan="2">${getString(R.string.export_purchase_detail_store_name)}</th>
        <th rowspan="2">${getString(R.string.export_purchase_detail_store_category)}</th>
        <th rowspan="2">${getString(R.string.export_purchase_detail_total_cost)}</th>
        <th colspan="3">${getString(R.string.export_purchase_detail_purchased_items)}</th>
      </tr>
      <tr>
        <th>${getString(R.string.export_purchase_detail_item_name)}</th>
        <th>${getString(R.string.export_purchase_detail_item_quantity)}</th>
        <th>${getString(R.string.export_purchase_detail_total_cost)}</th>
      </tr> 
    """

    private fun createEmptyHint() = """<div id="empty-hint">${getString(R.string.exported_html_empty_hint)}</div>"""

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
    private fun isRTL() = application.resources.getBoolean(R.bool.isRtl)
}
