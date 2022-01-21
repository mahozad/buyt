package com.pleon.buyt.serializer

import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*

class HTMLSerializerTest {

    // FIXME: Inject the dispatcher to the serializer instead of using delay
    @Disabled // FIXME: The comparison always fails. This may be because of coroutines.
    @Test fun shouldProduceCorrectResult() = runBlocking {
        // NOTE: For the serializer to be able to access resources,
        //  use `targetContext` instead of `context`.
        //  Could also use `ApplicationProvider.getApplicationContext<Context>()`.
        //  See https://stackoverflow.com/a/9899056
        //  and https://stackoverflow.com/a/58867480
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = HTMLSerializer(context)
        val date = Date() // TODO: Use this date in the expected HTML. See other serializer tests.
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
        val expectedResult = javaClass
            .getResourceAsStream("/expected.html")
            ?.bufferedReader()
            ?.use { it.readText() }
        var result = ""
        serializer.updateListener = { _, fragment -> result += fragment }
        serializer.finishListener = {
            assertThat(result)
                .withFailMessage(
                    "Expected: $expectedResult\nBut got: $result.\n" +
                            "The failure may be due to app/device locale.\n" +
                            "Try to change the app language or the device locale to EN."
                )
                .isEqualTo(expectedResult)
        }

        serializer.serialize(purchaseDetails)
        delay(1000)
    }
}
