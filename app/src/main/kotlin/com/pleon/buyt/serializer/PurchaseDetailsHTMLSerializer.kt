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
import org.intellij.lang.annotations.Language
import org.koin.java.KoinJavaComponent.inject
import java.util.*

class PurchaseDetailsHTMLSerializer : Serializer<PurchaseDetail> {

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
              span {
                margin-inline-start: 16px;
              }
              h4 {
                margin-bottom: 0;
              }
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

    override fun setUpdateListener(listener: (Int, String) -> Unit) {
        updateListener = listener
    }

    override fun setFinishListener(listener: () -> Unit) {
        finishListener = listener
    }

    override fun serialize(entities: List<PurchaseDetail>) {
        updateListener(0, head)
        createBody(entities)
        updateListener(100, tail)
        Thread.sleep(1000)
        finishListener()
    }

    private fun createBody(entities: List<PurchaseDetail>) {
        if (entities.isEmpty()) {
            createEmptyHint()
        }
        val stringBuilder = StringBuilder()
        for ((i, purchaseDetail) in entities.withIndex()) {
            stringBuilder
                    .append(dateElement(purchaseDetail.purchase.date))
                    .append(storeElement(purchaseDetail.store))
                    .append(itemsElement(purchaseDetail.item))
                    .append(horizontalRuleElement())
            val progress = ((i + 1f) / entities.size * 100).toInt()
            updateListener(progress, stringBuilder.toString())
            stringBuilder.clear()
        }
    }

    private fun createEmptyHint() {
        """<div id="empty-hint">${getString(R.string.exported_html_empty_hint)}</div>"""
    }

    private fun dateElement(date: Date) = "<h3>${formatDate(date)}</h3>"

    private fun storeElement(store: Store?): String {
        if (store == null) return getString(R.string.no_value)
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

    private fun horizontalRuleElement() = "<hr />"

    private fun getString(@StringRes id: Int) = application.getString(id)
    private fun getString(@StringRes id: Int, vararg format: Any) = application.getString(id, *format)
    private fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any) = application.resources.getQuantityString(id, quantity, *formatArgs)
    private fun isRTL() = application.resources.getBoolean(R.bool.isRtl)
}
