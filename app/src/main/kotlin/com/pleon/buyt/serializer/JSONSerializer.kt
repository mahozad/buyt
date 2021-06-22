package com.pleon.buyt.serializer

import android.app.Application
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.util.formatPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class JSONSerializer: InteractiveSerializer<PurchaseDetail> {

    override val mimeType = "application/json"
    override val fileExtension = "json"
    override var updateListener: (suspend (Int, String) -> Unit)? = null
    override var finishListener: (suspend () -> Unit)? = null
    private val application by inject(Application::class.java)
    private val head = "{ \"purchaseDetails\": [\n"
    private val tail = "]}"

    override suspend fun serialize(entities: List<PurchaseDetail>): Unit = withContext(Dispatchers.Default) {
        updateListener?.invoke(0, head)
        for ((i, purchaseDetail) in entities.withIndex()) {
            val isLast = (i == entities.size - 1)
            val fragment = purchaseElement(purchaseDetail, isLast)
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener?.invoke(progress, fragment)
        }
        updateListener?.invoke(100, tail)
        finishListener?.invoke()
    }

    private fun purchaseElement(purchaseDetail: PurchaseDetail, isLast: Boolean): String {
        val noValue = getString(R.string.no_value)
        val storeCategory = purchaseDetail.store?.category?.nameRes?.let { getString(it) } ?: noValue
        return buildString {
            appendLine("  {")
            appendLine("""    "date":${purchaseDetail.purchase.date.time / 1000},""")
            appendLine("""    "store": {""")
            appendLine("""      "name":"${purchaseDetail.store?.name ?: noValue}",""")
            appendLine("""      "category":"$storeCategory",""")
            appendLine("""      "location": {""")
            appendLine("""        "latitude":${purchaseDetail.store?.location?.latitude ?: noValue},""")
            appendLine("""        "longitude":${purchaseDetail.store?.location?.longitude ?: noValue}""")
            appendLine("      }")
            appendLine("    },")
            appendLine("""    "items": [""")
            for ((i, item) in purchaseDetail.item.withIndex()) {
                appendLine("      {")
                appendLine("""        "name":"${item.name}",""")
                appendLine("""        "quantity":"${getString(R.string.item_quantity, item.quantity.value, getString(item.quantity.unit.nameRes))}",""")
                appendLine("""        "description":"${item.description ?: noValue}",""")
                appendLine("""        "totalCost":"${getQuantityString(R.plurals.price_with_suffix, item.totalPrice.toInt(), formatPrice(item.totalPrice))}",""")
                appendLine("""        "urgency":"${if (item.isUrgent) "!" else noValue}"""")
                appendLine("      }${if (i < purchaseDetail.item.size - 1) "," else ""}")
            }
            appendLine("    ]")
            appendLine("  }${if (isLast) "" else ","}")
        }
    }

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
}
