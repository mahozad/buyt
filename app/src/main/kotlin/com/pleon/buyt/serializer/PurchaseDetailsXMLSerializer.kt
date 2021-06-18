package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.formatPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class PurchaseDetailsXMLSerializer : Serializer<PurchaseDetail> {

    override val mimeType = "text/xml"
    override val fileExtension = "xml"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val application by inject(Application::class.java)
    private val head = "<purchase-details>\n"
    private val tail = "</purchase-details>"

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        updateListener?.invoke(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            stringBuilder.append(purchaseElement(purchaseDetail))
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener?.invoke(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        updateListener?.invoke(100, tail)
        finishListener?.invoke()
    }

    private fun purchaseElement(purchaseDetail: PurchaseDetail): String {
        val noValue = getString(R.string.no_value)
        val stringBuilder = StringBuilder()
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: noValue
        stringBuilder.append("  <purchase>").appendLine()
        stringBuilder.append("    <date>${formatDate(purchaseDetail.purchase.date)}</date>").appendLine()
        stringBuilder.append("    <store>").appendLine()
        stringBuilder.append("      <store-name>${purchaseDetail.store?.name ?: noValue}</store-name>").appendLine()
        stringBuilder.append("      <store-category>${storeCategory}</store-category>").appendLine()
        stringBuilder.append("      <store-location>").appendLine()
        stringBuilder.append("        <store-location-latitude>${purchaseDetail.store?.location?.latitude ?: noValue}</store-location-latitude>").appendLine()
        stringBuilder.append("        <store-location-longitude>${purchaseDetail.store?.location?.longitude ?: noValue}</store-location-longitude>").appendLine()
        stringBuilder.append("      </store-location>").appendLine()
        stringBuilder.append("    </store>").appendLine()
        stringBuilder.append("    <items>").appendLine()
        for (item in purchaseDetail.item) {
            stringBuilder.append("      <item>").appendLine()
            stringBuilder.append("        <item-name>${item.name}</item-name>").appendLine()
            stringBuilder.append("        <item-quantity>${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}</item-quantity>").appendLine()
            stringBuilder.append("        <item-description>${item.description ?: noValue}</item-description>").appendLine()
            stringBuilder.append("        <item-total-cost>${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}</item-total-cost>").appendLine()
            stringBuilder.append("        <item-urgency>${if (item.isUrgent) "!" else noValue}</item-urgency>").appendLine()
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
