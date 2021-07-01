package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.formatPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class XMLSerializer : InteractiveSerializer<PurchaseDetail> {

    override val mimeType = "text/xml"
    override val fileExtension = "xml"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val application by inject(Application::class.java)
    private val head = "<purchase-details>\n"
    private val tail = "</purchase-details>"

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        updateListener?.invoke(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            val fragment = purchaseElement(purchaseDetail)
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener?.invoke(progress, fragment)
        }
        updateListener?.invoke(100, tail)
        finishListener?.invoke()
    }

    private fun purchaseElement(purchaseDetail: PurchaseDetail): String {
        val noValue = getString(R.string.no_value)
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: noValue
        return buildString {
            appendLine("  <purchase>")
            appendLine("    <date>${formatDate(purchaseDetail.purchase.date)}</date>")
            appendLine("    <store>")
            appendLine("      <store-name>${purchaseDetail.store?.name ?: noValue}</store-name>")
            appendLine("      <store-category>${storeCategory}</store-category>")
            appendLine("      <store-location>")
            appendLine("        <store-location-latitude>${purchaseDetail.store?.location?.latitude ?: noValue}</store-location-latitude>")
            appendLine("        <store-location-longitude>${purchaseDetail.store?.location?.longitude ?: noValue}</store-location-longitude>")
            appendLine("      </store-location>")
            appendLine("    </store>")
            appendLine("    <items>")
            for (item in purchaseDetail.item) {
                appendLine("      <item>")
                appendLine("        <item-name>${item.name}</item-name>")
                appendLine("        <item-quantity>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</item-quantity>")
                appendLine("        <item-description>${item.description ?: noValue}</item-description>")
                appendLine("        <item-total-price>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</item-total-price>")
                appendLine("        <item-urgency>${if (item.isUrgent) "!" else noValue}</item-urgency>")
                appendLine("      </item>")
            }
            appendLine("    </items>")
            appendLine("  </purchase>")
        }
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
