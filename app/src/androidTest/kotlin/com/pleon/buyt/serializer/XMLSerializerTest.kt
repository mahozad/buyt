package com.pleon.buyt.serializer

import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.*
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.util.setLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class XMLSerializerTest {

    // FIXME: Inject the dispatcher to the serializer instead of using delay
    @Test fun shouldProduceCorrectResult() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        setLocale(context, Locale.ENGLISH)
        val serializer = XMLSerializer()
        val date = Date()
        val purchaseDetails = listOf(
            PurchaseDetail().apply {
                purchase = Purchase(date)
                item = listOf(
                    Item("Apple", Item.Quantity(2, Item.Quantity.Unit.KILOGRAM), Category.FRUIT, isUrgent = false, isBought = true),
                    Item("Ketchup", Item.Quantity(1, Item.Quantity.Unit.UNIT), Category.GROCERY, isUrgent = false, isBought = true)
                )
                store = Store(Coordinates(2.3, 5.6), "Store A", Category.GROCERY)
            },
            PurchaseDetail().apply {
                purchase = Purchase(date)
                item = listOf(
                    Item("Apple", Item.Quantity(1500, Item.Quantity.Unit.GRAM), Category.FRUIT, isUrgent = true, isBought = true),
                )
                store = Store(Coordinates(90.0, 100.0), "Store B", Category.FRUIT)
            }
        )
        val expectedResult = """<purchase-details>
  <purchase>
    <date>${formatDate(date)}</date>
    <store>
      <store-name>Store A</store-name>
      <store-category>Grocery</store-category>
      <store-location>
        <store-location-latitude>2.3</store-location-latitude>
        <store-location-longitude>5.6</store-location-longitude>
      </store-location>
    </store>
    <items>
      <item>
        <item-name>Apple</item-name>
        <item-quantity>2 kg</item-quantity>
        <item-description>-</item-description>
        <item-total-price>0 dollars</item-total-price>
        <item-urgency>-</item-urgency>
      </item>
      <item>
        <item-name>Ketchup</item-name>
        <item-quantity>1 unit</item-quantity>
        <item-description>-</item-description>
        <item-total-price>0 dollars</item-total-price>
        <item-urgency>-</item-urgency>
      </item>
    </items>
  </purchase>
  <purchase>
    <date>${formatDate(date)}</date>
    <store>
      <store-name>Store B</store-name>
      <store-category>Fruit</store-category>
      <store-location>
        <store-location-latitude>90.0</store-location-latitude>
        <store-location-longitude>100.0</store-location-longitude>
      </store-location>
    </store>
    <items>
      <item>
        <item-name>Apple</item-name>
        <item-quantity>1500 g</item-quantity>
        <item-description>-</item-description>
        <item-total-price>0 dollars</item-total-price>
        <item-urgency>!</item-urgency>
      </item>
    </items>
  </purchase>
</purchase-details>"""
        var result = ""
        serializer.updateListener = { _, fragment -> result += fragment }
        serializer.finishListener = {
            Assertions.assertThat(result).isEqualTo(expectedResult)
        }

        serializer.serialize(purchaseDetails)
        delay(1000)
    }
}
