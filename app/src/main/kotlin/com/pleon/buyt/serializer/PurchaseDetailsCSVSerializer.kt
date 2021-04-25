package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.formatPrice
import org.koin.java.KoinJavaComponent.inject

class PurchaseDetailsCSVSerializer : Serializer<PurchaseDetail> {

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

    override fun serialize(entities: List<PurchaseDetail>) {
        val stringBuilder = StringBuilder()
        updateListener(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            stringBuilder.append(makeRecord(purchaseDetail))
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
        Thread.sleep(1000)
        finishListener()
    }

    private fun makeRecord(purchaseDetail: PurchaseDetail): String {
        val noValue = getString(R.string.no_value)
        val valueSeparator = ":"
        val itemSeparator = "⬛"
        val stringBuilder = StringBuilder()
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: noValue
        stringBuilder.append("\"${formatDate(purchaseDetail.purchase.date)}\"")
        stringBuilder.append(",")
        stringBuilder.append("\"")
        stringBuilder.append(purchaseDetail.store?.name ?: noValue)
        stringBuilder.append(valueSeparator)
        stringBuilder.append(storeCategory)
        stringBuilder.append("\"")
        stringBuilder.append(",")
        stringBuilder.append("\"")
        for ((i, item) in purchaseDetail.item.withIndex()) {
            stringBuilder.append(item.name)
            stringBuilder.append(valueSeparator)
            stringBuilder.append(getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes)))
            stringBuilder.append(valueSeparator)
            stringBuilder.append(getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice)))
            if (i < purchaseDetail.item.size - 1) stringBuilder.append(itemSeparator)
        }
        stringBuilder.append("\"")
        stringBuilder.appendLine()
        return stringBuilder.toString()
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
