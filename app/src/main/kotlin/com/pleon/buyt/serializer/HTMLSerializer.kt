package com.pleon.buyt.serializer

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getColor
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.formatPrice
import com.pleon.buyt.util.getCurrentLocale
import com.pleon.buyt.util.toHexColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.collections.Map.Entry

private const val DATE_AND_TABLE_HEADER_ROWS = 5 // Just a rough estimate
private const val PAGE_MAX_ROWS = 28 // Just a rough estimate

/**
 * Consider using [kotlinx.html](https://github.com/Kotlin/kotlinx.html) library for a more fluent HTML creation.
 *
 * For creating the tables see [this page](https://developer.mozilla.org/en-US/docs/Learn/HTML/Tables/Advanced).
 */
class HTMLSerializer(private val context: Context) : InteractiveSerializer<PurchaseDetail> {

    override val mimeType = "text/html"
    override val fileExtension = "html"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val stringBuilder = StringBuilder()
    private var pageCurrentRow = 0

    @Language("HTML")
    private val head = """
        <!DOCTYPE html>
        <html lang="${getCurrentLocale(context).language}">
          <head>
            <meta charset="UTF-8">
            <meta name="theme-color" content="${getColor(context, R.color.colorPrimary).toHexColor()}"/>
            <link rel="icon" sizes="any" type="image/svg+xml" href="data:image/svg+xml,%3Csvg%20version%3D%221.1%22%20viewBox%3D%220%200%208%2016%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20xmlns%3Axlink%3D%22http%3A%2F%2Fwww.w3.org%2F1999%2Fxlink%22%3E%0A%20%20%3Cdefs%3E%0A%20%20%20%20%3Cfilter%20id%3D%22filter%22%20x%3D%220%22%20y%3D%220%22%20color-interpolation-filters%3D%22sRGB%22%3E%0A%20%20%20%20%20%20%3CfeFlood%20flood-color%3D%22rgb%280%2C0%2C0%29%22%20flood-opacity%3D%22.33%22%20result%3D%22flood%22%2F%3E%0A%20%20%20%20%20%20%3CfeComposite%20in%3D%22flood%22%20in2%3D%22SourceGraphic%22%20operator%3D%22in%22%20result%3D%22composite1%22%2F%3E%0A%20%20%20%20%20%20%3CfeGaussianBlur%20in%3D%22composite1%22%20result%3D%22blur%22%20stdDeviation%3D%220.2%22%2F%3E%0A%20%20%20%20%20%20%3CfeOffset%20dx%3D%220%22%20dy%3D%220.3%22%20result%3D%22offset%22%2F%3E%0A%20%20%20%20%20%20%3CfeComposite%20in%3D%22SourceGraphic%22%20in2%3D%22offset%22%20result%3D%22composite2%22%2F%3E%0A%20%20%20%20%3C%2Ffilter%3E%0A%20%20%20%20%3CclipPath%20id%3D%22clip%22%3E%0A%20%20%20%20%20%20%3Cuse%20width%3D%22100%25%22%20height%3D%22100%25%22%20xlink%3Ahref%3D%22%23pin%22%20transform%3D%22translate%288%2C4%29%22%2F%3E%0A%20%20%20%20%3C%2FclipPath%3E%0A%20%20%3C%2Fdefs%3E%0A%20%20%3Cpath%20id%3D%22pin%22%20fill%3D%22%2356ab2f%22%20d%3D%22m3.9391%206.01a4%204%200%200%200-3.939%204%204%204%200%200%200%200%200.061v5.939l5.533-2.305a4%204%200%200%200%202.467-3.695%204%204%200%200%200-4-4%204%204%200%200%200-0.061%200zm0.061%202.5a1.5%201.5%200%200%201%201.5%201.5%201.5%201.5%200%200%201-1.5%201.5%201.5%201.5%200%200%201-1.5-1.5%201.5%201.5%200%200%201%201.5-1.5z%22%2F%3E%0A%20%20%3Cpath%20fill%3D%22%2356ab2f%22%20transform%3D%22translate%28-8%2C-4%29%22%20clip-path%3D%22url%28%23clip%29%22%20filter%3D%22url%28%23filter%29%22%20d%3D%22m11.94%209.99c-2.184%200-3.941%201.82-3.94%204.01l5.535-2.321c0.404-0.167%200.769-0.401%201.092-0.683-0.704-0.619-1.616-1.006-2.627-1.006z%22%2F%3E%0A%20%20%3Cpath%20fill%3D%22%2370ae28%22%20d%3D%22m3.9391%200c-2.184%200-3.94%201.82-3.939%204v6.01l5.535-2.32c1.448-0.6%202.465-2.02%202.465-3.69%200-2.21-1.791-4-4-4zm0.06%202.5a1.5%201.5%200%200%201%201.5%201.5%201.5%201.5%200%200%201-1.5%201.5%201.5%201.5%200%200%201-1.5-1.5%201.5%201.5%200%200%201%201.5-1.5z%22%2F%3E%0A%3C%2Fsvg%3E%0A">
            <!-- Provide a fallback favicon in case a browser does not support the SVG version -->
            <link rel="alternate icon" type="image/x-icon" href="data:image/x-icon;base64,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cP8ornD/KK5w/yiucOoornA/KK5wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wHCiucOMornD/KK5w/yiucP8ornD/KK5w6CiucHgorm8sKK5vLCiucHkornDoKK5w/yiucP8ornD/KK5w/yiucI4nrm0CKK5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACiucAAornAcKK5w4yiucP8ornD/KK5w/yiucPsornB3LKtyAimtcQAorm8AJ61vAiiucHgornD7KK5w/yiucP8ornD/KK5wwSevbxEornAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKK5wACiucBwornDjKK5w/yiucP8ornD/KK5w6CiucCsornAAAAAAAAAAAAAornAAKK5wLSiucOkornD/KK5w/yiucP8ornDbKK5wGSiucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wGiiucN4ornD/KK5w/yiucP8ornDoKK5wKiiucAAAAAAAAAAAACiucAAornAsKK5w6SiucP8ornD/KK5w/yiucN0ornAaKK5wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACiucAAnrnASKK5wxSiucP8ornD/KK5w/yiucPsornB1JrN0AievcQAqr3AAL7FwAiiucHcornD7KK5w/yiucP8ornD/KK5wxCitcBEorXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJq1xACSscwMornCRKK5w/yiucP8ornD/KK5w/yiucOcornB1KK5wKSiubyoornB2KK5w6CiucP8ornD/KK5w/yiucP8ornCQKqxwAimtcAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKK5wACiucEAornDqKK5w/yiucP8ornD/KK5w/yiucPkornDoKK5w6CiucPkornD/KK5w/yiucP8ornD/KK5w6iiub0AornAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAJ65wBiiucI8ornD9KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP0ornCOKq1wBimucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wGiiucK0ornD9KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP8ornD9KK5wqyitcBkorXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACKxcwAprW8AJ65wGSiucJMornDtKK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w6yiucI8ornAZKa9xACSlbAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAorXEAKK5wCyitcVAornCoKK5w4CiucPoornD5KK5w3CiucKIorXBKKa5wCSmucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/g////4D///+AH///gAf//4AB//+AAP//gAB//4AAP/+AAD//gAAf/4BgH/+A8B//gPAf/4BgH/+AAB//gAA//4AAP/+AAH//gAB//4AAP/+AAD//gAAf/4BgH/+A8B//gPAf/4BgH/+AAB//wAA//8AAP//gAH//8AD///gB/8=">
            <title>${getString(R.string.export_html_title)}</title>
            <style>
              * {
                direction: ${if (isRTL()) "RTL" else "LTR"};
              }
              #logo {
                 height: 256px;
              }
              #logo, #empty-hint, h1 {
                width: 100%;
                margin: 0 auto;
                text-align: center;
              }
              hr {
                margin: 36px 0 18px;
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
              @media screen and (prefers-color-scheme: dark) {
                * {
                  background: #242424;
                  color: #eaeaea; 
                }
                table, th, td {
                  border: 1px solid #ccc;
                }
                table caption div {
                  background: rgba(208, 219, 223, 0.2);
                }
                th {
                  background: rgba(208, 223, 219, 0.15);
                }
              }
              /* The following rules are used when printing a PDF */
              @page {
                size: A4;
                margin: 0;
              }
              @media print {
                body {
                  counter-reset: page_counter;
                }
                .page-counter:before {
                  position: absolute;
                  color: #121212;
                  font-size: 20px;
                  top: 282mm;
                  left: 50%;
                  transform: translate(-50%, 0);
                  counter-increment: page_counter;
                  content: counter(page_counter ${if (Locale.getDefault().language == "fa") ", persian" else ""});
                }
                .page {
                  position: relative;
                  width: 210mm;
                  min-height: 297mm;
                  padding: 1cm 5mm 5mm;
                  margin: 0;
                  width: initial;
                  min-height: initial;
                  page-break-after: always;
                }
                #empty-hint {
                  margin-top: 14cm;
                }
                #logo-and-title {
                  margin-top: 76mm
                }
                #logo {
                   height: 360px;
                }
                hr {
                  display: none;
                }
              }
            </style>
          </head>
          <body>
            <div class="page">
              <div id="logo-and-title">
                <svg id="logo" version="1.1" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
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
                <h1>${getString(R.string.export_html_title)}</h1>
              </div>
            </div>
            <hr />
    """

    @Language("HTML")
    private val tail = """
          </body>
        </html>
    """

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        resetState()
        updateListener?.invoke(0, head)
        createBody(entities)
        updateListener?.invoke(100, tail)
        finishListener?.invoke()
    }

    private fun resetState() {
        stringBuilder.clear()
        pageCurrentRow = 0
    }

    private suspend fun createBody(entities: List<PurchaseDetail>) = when {
        entities.isEmpty() -> updateListener?.invoke(100, createEmptyHint())
        else -> createContent(entities)
    }

    private suspend fun createContent(entities: List<PurchaseDetail>) {
        stringBuilder.append("""<div class="page"><div class="page-counter"></div>""")
        val purchaseDetailsByDate = entities.groupBy { formatDate(it.purchase.date) }
        purchaseDetailsByDate.onEachIndexed { i, entry ->
            createDatePurchaseDetails(entry, isFirstTable = i == 0)
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener?.invoke(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        updateListener?.invoke(95, "</div>") // Close the first page element
    }

    private fun createDatePurchaseDetails(entry: Entry<String, List<PurchaseDetail>>, isFirstTable: Boolean) {
        val (date, purchaseDetails) = entry
        val nextTableRows = DATE_AND_TABLE_HEADER_ROWS + purchaseDetails.sumOf { it.item.size }
        pageCurrentRow += nextTableRows
        if (pageCurrentRow > PAGE_MAX_ROWS && !isFirstTable) {
            endCurrentPageAndStartNextPage(nextTableRows)
        }
        addTable(date, purchaseDetails)
    }

    private fun endCurrentPageAndStartNextPage(nextTableRows: Int) {
        pageCurrentRow = nextTableRows
        stringBuilder.append("""</div><div class="page"><div class="page-counter"></div>""")
    }

    private fun addTable(date: String, purchaseDetails: List<PurchaseDetail>) {
        stringBuilder.append(tableStart(date))
        for (purchaseDetail in purchaseDetails) {
            addStoreAndFirstItem(purchaseDetail.store!!, purchaseDetail.item)
            addRemainingItems(purchaseDetail.item.drop(1))
        }
        stringBuilder.append("</table>")
    }

    private fun addRemainingItems(items: List<Item>) {
        for (item in items) stringBuilder.append(createItem(item))
    }

    @Language("HTML")
    private fun createItem(item: Item) = """
        <tr>
          <td>${item.name}</td>
          <td>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</td>
          <td>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</td>
        </tr>"""

    /**
     * Could also have incremented the `rowspan` of the first row by one
     * and moved the first item to a standalone `<tr>` element.
     */
    private fun addStoreAndFirstItem(store: Store, items: List<Item>) {
        val storeCategory = getString(store.category.nameRes)
        val totalCost = items.sumOf { it.totalPrice }
        val firstItem = items.first()
        stringBuilder.append("""
        <tr>
          <td rowspan="${items.size}">${store.name}</td>
          <td rowspan="${items.size}">${storeCategory}</td>
          <td rowspan="${items.size}">${getQuantityString(R.plurals.price_with_suffix, totalCost.toInt(), formatPrice(totalCost))}</td>
          <td>${firstItem.name}</td>
          <td>${getString(R.string.item_quantity, firstItem.quantity.value, getString(firstItem.quantity.unit.nameRes))}</td>
          <td>${getQuantityString(R.plurals.price_with_suffix, firstItem.totalPrice.toInt(), formatPrice(firstItem.totalPrice))}</td>
        </tr>
        """)
    }

    @Language("HTML")
    private fun tableStart(date: String): String = """
      <table>
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
          <th>${getString(R.string.export_purchase_detail_total_price)}</th>
        </tr> 
    """

    @Language("HTML")
    private fun createEmptyHint() = """<div id="empty-hint">${getString(R.string.export_html_empty_hint)}</div>"""

    private fun getString(@StringRes id: Int) = context.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = context.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) =
        context.resources.getQuantityString(id, quantity, *formatArgs)
    private fun isRTL() = context.resources.getBoolean(R.bool.isRtl)
}
