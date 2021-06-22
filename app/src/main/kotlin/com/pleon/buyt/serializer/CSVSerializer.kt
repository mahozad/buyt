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

class CSVSerializer : InteractiveSerializer<PurchaseDetail> {

    override val mimeType = "text/csv"
    override val fileExtension = "csv"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val application by inject(Application::class.java)
    private val head = "Date,Store,Items\n"

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        updateListener?.invoke(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            val fragment = makeRecord(purchaseDetail)
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener?.invoke(progress, fragment)
        }
        finishListener?.invoke()
    }

    private fun makeRecord(purchaseDetail: PurchaseDetail): String {
        val noValue = getString(R.string.no_value)
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: noValue
        val valueSeparator = ":"
        val itemSeparator = "⬛"
        return buildString {
            append("\"${formatDate(purchaseDetail.purchase.date)}\"")
            append(",")
            append("\"")
            append(purchaseDetail.store?.name ?: noValue)
            append(valueSeparator)
            append(storeCategory)
            append("\"")
            append(",")
            append("\"")
            for ((i, item) in purchaseDetail.item.withIndex()) {
                append(item.name)
                append(valueSeparator)
                append(getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes)))
                append(valueSeparator)
                append(getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice)))
                if (i < purchaseDetail.item.size - 1) append(itemSeparator)
            }
            appendLine("\"")
        }
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
