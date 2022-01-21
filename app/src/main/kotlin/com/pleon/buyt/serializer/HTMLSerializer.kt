package com.pleon.buyt.serializer

import android.content.Context
import android.util.Base64
import android.util.Base64.NO_WRAP
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
    private val tail = """
          </body>
        </html>
    """

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        resetState()
        val head = generateHead()
        updateListener?.invoke(0, head)
        createBody(entities)
        updateListener?.invoke(100, tail)
        finishListener?.invoke()
    }

    private suspend fun generateHead() = withContext(Dispatchers.IO) {
        // The variable variant of Vazir v30.1.0
        val fontPersian = R.raw.vazir_variable_v_30_1_0.readAsBase64()
        // The unofficial woff2 format of Roboto Slab
        val fontLatin = R.raw.roboto_slab_regular.readAsBase64()
        val fontLatinBold = R.raw.roboto_slab_bold.readAsBase64()
        context
            .resources
            .openRawResource(R.raw.template)
            .bufferedReader()
            .lineSequence()
            .replace("{{ font-persian }}", fontPersian)
            .replace("{{ font-latin }}", fontLatin)
            .replace("{{ font-latin-bold }}", fontLatinBold)
            .replace("{{ lang }}", getCurrentLocale(context).language)
            .replace("{{ title }}", getString(R.string.export_html_title))
            .replace("{{ direction }}", if (isRTL()) "RTL" else "LTR")
            .replace("{{ theme-color }}", getColor(context, R.color.colorPrimary).toHexColor())
            .replace("{{ counter-language }}", if (Locale.getDefault().language == "fa") ", persian" else "")
            .joinToString("\n")
    }

    private fun Int.readAsBase64() = context.resources.openRawResource(this).use {
        Base64.encodeToString(it.readBytes(), NO_WRAP)
    }

    private fun Sequence<String>.replace(old: String, new: String) = map { it.replace(old, new) }

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
