package com.pleon.buyt.serializer

import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.*
import com.pleon.buyt.util.setLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class JSONSerializerTest {

    // FIXME: Inject the dispatcher to the serializer instead of using delay
    @Test fun shouldProduceCorrectResult() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().context
        setLocale(context, Locale.ENGLISH)
        val serializer = JSONSerializer()
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
        val expectedResult = """{ "purchaseDetails": [
  {
    "date":${date.time / 1000},
    "store": {
      "name":"Store A",
      "category":"Grocery",
      "location": {
        "latitude":2.3,
        "longitude":5.6
      }
    },
    "items": [
      {
        "name":"Apple",
        "quantity":"2 kg",
        "description":"-",
        "totalPrice":"0 dollars",
        "urgency":"-"
      },
      {
        "name":"Ketchup",
        "quantity":"1 unit",
        "description":"-",
        "totalPrice":"0 dollars",
        "urgency":"-"
      }
    ]
  },
  {
    "date":${date.time / 1000},
    "store": {
      "name":"Store B",
      "category":"Fruit",
      "location": {
        "latitude":90.0,
        "longitude":100.0
      }
    },
    "items": [
      {
        "name":"Apple",
        "quantity":"1500 g",
        "description":"-",
        "totalPrice":"0 dollars",
        "urgency":"!"
      }
    ]
  }
]}"""
        var result = ""
        serializer.updateListener = { _, fragment -> result += fragment }
        serializer.finishListener = {
            assertThat(result).isEqualTo(expectedResult)
        }

        serializer.serialize(purchaseDetails)
        delay(1000)
    }
}
