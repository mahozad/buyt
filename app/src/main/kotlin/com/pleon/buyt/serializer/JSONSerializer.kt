package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.formatPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent

class JSONSerializer: Serializer<PurchaseDetail> {

    override val mimeType = "application/json"
    override val fileExtension = "json"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val application by KoinJavaComponent.inject(Application::class.java)
    private val head = "{ \"purchaseDetails\": [\n"
    private val tail = "]}"

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        val stringBuilder = StringBuilder()
        updateListener?.invoke(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            stringBuilder.append(purchaseElement(purchaseDetail))
            stringBuilder.append(if (i < entities.size - 1) ",\n" else "\n")
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
        stringBuilder.append("  { ").appendLine()
        stringBuilder.append("    \"date\":${purchaseDetail.purchase.date.time / 1000},").appendLine()
        stringBuilder.append("    \"store\": { ").appendLine()
        stringBuilder.append("      \"name\":\"${purchaseDetail.store?.name ?: noValue}\",").appendLine()
        stringBuilder.append("      \"category\":\"${storeCategory}\",").appendLine()
        stringBuilder.append("      \"location\": { ").appendLine()
        stringBuilder.append("        \"latitude\":${purchaseDetail.store?.location?.latitude ?: noValue},").appendLine()
        stringBuilder.append("        \"longitude\":${purchaseDetail.store?.location?.longitude ?: noValue}").appendLine()
        stringBuilder.append("      }").appendLine()
        stringBuilder.append("    },").appendLine()
        stringBuilder.append("    \"items\": [ ").appendLine()
        for ((i, item) in purchaseDetail.item.withIndex()) {
            stringBuilder.append("      { ").appendLine()
            stringBuilder.append("        \"name\":\"${item.name}\",").appendLine()
            stringBuilder.append("        \"quantity\":\"${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}\",").appendLine()
            stringBuilder.append("        \"description\":\"${item.description ?: noValue}\",").appendLine()
            stringBuilder.append("        \"totalCost\":\"${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}\",").appendLine()
            stringBuilder.append("        \"urgency\":\"${if (item.isUrgent) "!" else noValue}\"").appendLine()
            stringBuilder.append("      }${if (i < purchaseDetail.item.size - 1) "," else ""}").appendLine()
        }
        stringBuilder.append("    ]").appendLine()
        stringBuilder.append("  }")
        return stringBuilder.toString()
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
