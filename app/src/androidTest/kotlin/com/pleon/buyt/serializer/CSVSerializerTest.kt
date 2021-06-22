package com.pleon.buyt.serializer

import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.*
import com.pleon.buyt.util.formatDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class CSVSerializerTest {

    // FIXME: Inject the dispatcher to the serializer instead of using delay
    @Test fun shouldProduceCorrectResult() = runBlocking {
        val serializer = CSVSerializer()
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
        val expectedResult = """Date,Store,Items
"${formatDate(date)}","Store A:Grocery","Apple:2 kg:0 dollars⬛Ketchup:1 unit:0 dollars"
"${formatDate(date)}","Store B:Fruit","Apple:1500 g:0 dollars"
"""
        var result = ""
        serializer.updateListener = { _, fragment -> result += fragment }
        serializer.finishListener = {
            Assertions.assertThat(result).isEqualTo(expectedResult)
        }

        serializer.serialize(purchaseDetails)
        delay(1000)
    }
}
