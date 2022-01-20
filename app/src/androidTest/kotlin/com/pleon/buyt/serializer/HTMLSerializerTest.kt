package com.pleon.buyt.serializer

import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.util.*

class HTMLSerializerTest {

    // FIXME: Inject the dispatcher to the serializer instead of using delay
    @Test fun shouldProduceCorrectResult() = runBlocking {
        // NOTE: For the serializer to be able to access resources,
        //  use `targetContext` instead of `context`.
        //  Could also use `ApplicationProvider.getApplicationContext<Context>()`.
        //  See https://stackoverflow.com/a/9899056
        //  and https://stackoverflow.com/a/58867480
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = HTMLSerializer(context)
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
        @Language("HTML")
        val expectedResult = """<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="theme-color" content="#70ae28"/>
    <link rel="icon" sizes="any" type="image/svg+xml" href="data:image/svg+xml,%3Csvg%20viewBox%3D%220%200%208%2016%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%20%20%3Cdefs%3E%0A%20%20%20%20%3Cfilter%20id%3D%22filter%22%20x%3D%220%22%20y%3D%220%22%20color-interpolation-filters%3D%22sRGB%22%3E%0A%20%20%20%20%20%20%3CfeFlood%20flood-color%3D%22rgb%280%2C0%2C0%29%22%20flood-opacity%3D%22.33%22%20result%3D%22flood%22%2F%3E%0A%20%20%20%20%20%20%3CfeComposite%20in%3D%22flood%22%20in2%3D%22SourceGraphic%22%20operator%3D%22in%22%20result%3D%22composite1%22%2F%3E%0A%20%20%20%20%20%20%3CfeGaussianBlur%20in%3D%22composite1%22%20result%3D%22blur%22%20stdDeviation%3D%220.2%22%2F%3E%0A%20%20%20%20%20%20%3CfeOffset%20dx%3D%220%22%20dy%3D%220.3%22%20result%3D%22offset%22%2F%3E%0A%20%20%20%20%20%20%3CfeComposite%20in%3D%22SourceGraphic%22%20in2%3D%22offset%22%20result%3D%22composite2%22%2F%3E%0A%20%20%20%20%3C%2Ffilter%3E%0A%20%20%20%20%3CclipPath%20id%3D%22clip%22%3E%0A%20%20%20%20%20%20%3Cuse%20width%3D%22100%25%22%20height%3D%22100%25%22%20href%3D%22%23pin%22%20transform%3D%22translate%288%2C4%29%22%2F%3E%0A%20%20%20%20%3C%2FclipPath%3E%0A%20%20%3C%2Fdefs%3E%0A%20%20%3Cpath%20id%3D%22pin%22%20fill%3D%22%2356ab2f%22%20d%3D%22m3.9391%206.01a4%204%200%200%200-3.939%204%204%204%200%200%200%200%200.061v5.939l5.533-2.305a4%204%200%200%200%202.467-3.695%204%204%200%200%200-4-4%204%204%200%200%200-0.061%200zm0.061%202.5a1.5%201.5%200%200%201%201.5%201.5%201.5%201.5%200%200%201-1.5%201.5%201.5%201.5%200%200%201-1.5-1.5%201.5%201.5%200%200%201%201.5-1.5z%22%2F%3E%0A%20%20%3Cpath%20fill%3D%22%2356ab2f%22%20transform%3D%22translate%28-8%2C-4%29%22%20clip-path%3D%22url%28%23clip%29%22%20filter%3D%22url%28%23filter%29%22%20d%3D%22m11.94%209.99c-2.184%200-3.941%201.82-3.94%204.01l5.535-2.321c0.404-0.167%200.769-0.401%201.092-0.683-0.704-0.619-1.616-1.006-2.627-1.006z%22%2F%3E%0A%20%20%3Cpath%20fill%3D%22%2370ae28%22%20d%3D%22m3.9391%200c-2.184%200-3.94%201.82-3.939%204v6.01l5.535-2.32c1.448-0.6%202.465-2.02%202.465-3.69%200-2.21-1.791-4-4-4zm0.06%202.5a1.5%201.5%200%200%201%201.5%201.5%201.5%201.5%200%200%201-1.5%201.5%201.5%201.5%200%200%201-1.5-1.5%201.5%201.5%200%200%201%201.5-1.5z%22%2F%3E%0A%3C%2Fsvg%3E%0A">
    <!-- Provide a fallback favicon in case a browser does not support the SVG version -->
    <link rel="alternate icon" type="image/x-icon" href="data:image/x-icon;base64,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cP8ornD/KK5w/yiucOoornA/KK5wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wHCiucOMornD/KK5w/yiucP8ornD/KK5w6CiucHgorm8sKK5vLCiucHkornDoKK5w/yiucP8ornD/KK5w/yiucI4nrm0CKK5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACiucAAornAcKK5w4yiucP8ornD/KK5w/yiucPsornB3LKtyAimtcQAorm8AJ61vAiiucHgornD7KK5w/yiucP8ornD/KK5wwSevbxEornAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKK5wACiucBwornDjKK5w/yiucP8ornD/KK5w6CiucCsornAAAAAAAAAAAAAornAAKK5wLSiucOkornD/KK5w/yiucP8ornDbKK5wGSiucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wGiiucN4ornD/KK5w/yiucP8ornDoKK5wKiiucAAAAAAAAAAAACiucAAornAsKK5w6SiucP8ornD/KK5w/yiucN0ornAaKK5wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACiucAAnrnASKK5wxSiucP8ornD/KK5w/yiucPsornB1JrN0AievcQAqr3AAL7FwAiiucHcornD7KK5w/yiucP8ornD/KK5wxCitcBEorXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJq1xACSscwMornCRKK5w/yiucP8ornD/KK5w/yiucOcornB1KK5wKSiubyoornB2KK5w6CiucP8ornD/KK5w/yiucP8ornCQKqxwAimtcAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKK5wACiucEAornDqKK5w/yiucP8ornD/KK5w/yiucPkornDoKK5w6CiucPkornD/KK5w/yiucP8ornD/KK5w6iiub0AornAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAJ65wBiiucI8ornD9KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP0ornCOKq1wBimucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAornAAKK5wGiiucK0ornD9KK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w/yiucP8ornD9KK5wqyitcBkorXAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACKxcwAprW8AJ65wGSiucJMornDtKK5w/yiucP8ornD/KK5w/yiucP8ornD/KK5w6yiucI8ornAZKa9xACSlbAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAorXEAKK5wCyitcVAornCoKK5w4CiucPoornD5KK5w3CiucKIorXBKKa5wCSmucAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/g////4D///+AH///gAf//4AB//+AAP//gAB//4AAP/+AAD//gAAf/4BgH/+A8B//gPAf/4BgH/+AAB//gAA//4AAP/+AAH//gAB//4AAP/+AAD//gAAf/4BgH/+A8B//gPAf/4BgH/+AAB//wAA//8AAP//gAH//8AD///gB/8=">
    <title>Buyt purchase details</title>
    <style>
      * {
        direction: LTR;
      }
      #logo {
         height: 256px;
      }
      #logo, #empty-hint, h1 {
        width: 100%;
        margin: 0 auto;
        text-align: center;
      }
      hr {
        margin: 36px 0 18px;
      }
      #empty-hint {
        margin-top: 64px;
        font-size: 32px;
      }
      table caption div {
        display: inline-block;
        padding: 4px 16px;
        background: rgba(208, 219, 223, 0.6);
        border-radius: 100px;
        font-size: 18px;
        margin: 16px auto;
      }
      table {
        margin: auto;
        margin-bottom: 32px;
      }
      table, th, td {
        border: 1px solid black;
        border-collapse: collapse;
        text-align: center;
      }
      th, td {
        padding: 4px 8px;
      }
      th {
        background: rgba(208, 223, 219, 0.2);
      }
      @media screen and (prefers-color-scheme: dark) {
        * {
          background: #242424;
          color: #eaeaea;
        }
        table, th, td {
          border: 1px solid #ccc;
        }
        table caption div {
          background: rgba(208, 219, 223, 0.2);
        }
        th {
          background: rgba(208, 223, 219, 0.15);
        }
      }
      /* The following rules are used when printing a PDF */
      @page {
        size: A4;
        margin: 0;
      }
      @media print {
        body {
          counter-reset: page_counter;
        }
        .page-counter:before {
          position: absolute;
          color: #121212;
          font-size: 20px;
          top: 282mm;
          left: 50%;
          transform: translate(-50%, 0);
          counter-increment: page_counter;
          content: counter(page_counter );
        }
        .page {
          position: relative;
          width: 210mm;
          min-height: 297mm;
          padding: 1cm 5mm 5mm;
          margin: 0;
          width: initial;
          min-height: initial;
          page-break-after: always;
        }
        #empty-hint {
          margin-top: 14cm;
        }
        #logo-and-title {
          margin-top: 76mm
        }
        #logo {
           height: 360px;
        }
        hr {
          display: none;
        }
      }
    </style>
  </head>
  <body>
    <div class="page">
      <div id="logo-and-title">
        <svg id="logo" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <filter id="filter" x="0" y="0" color-interpolation-filters="sRGB">
              <feFlood flood-color="rgb(0,0,0)" flood-opacity=".33" result="flood"></feFlood>
              <feComposite in="flood" in2="SourceGraphic" operator="in" result="composite1"></feComposite>
              <feGaussianBlur in="composite1" result="blur" stdDeviation="0.2"></feGaussianBlur>
              <feOffset dx="0" dy="0.3" result="offset"></feOffset>
              <feComposite in="SourceGraphic" in2="offset" result="composite2"></feComposite>
            </filter>
            <clipPath id="clip"><use href="#pin"></use></clipPath>
          </defs>
          <path id="pin" fill="#56ab2f" d="m11.939 10a4 4 0 0 0-3.939 4 4 4 0 0 0 0 0.061v5.939l5.533-2.305a4 4 0 0 0 2.467-3.695 4 4 0 0 0-4-4 4 4 0 0 0-0.061 0zm0.061 2.5a1.5 1.5 0 0 1 1.5 1.5 1.5 1.5 0 0 1-1.5 1.5 1.5 1.5 0 0 1-1.5-1.5 1.5 1.5 0 0 1 1.5-1.5z"></path>
          <path clip-path="url(#clip)" fill="#56ab2f" filter="url(#filter)" d="m11.94 9.99c-2.184 0-3.941 1.82-3.94 4.01l5.535-2.321c0.404-0.167 0.769-0.401 1.092-0.683-0.704-0.619-1.616-1.006-2.627-1.006z"></path>
          <path fill="#70ae28" d="m11.939 3.99c-2.184 0-3.94 1.82-3.939 4v6.01l5.535-2.32c1.448-0.6 2.465-2.02 2.465-3.69 0-2.21-1.791-4-4-4zm0.06 2.5a1.5 1.5 0 0 1 1.5 1.5 1.5 1.5 0 0 1-1.5 1.5 1.5 1.5 0 0 1-1.5-1.5 1.5 1.5 0 0 1 1.5-1.5z"></path>
        </svg>
        <h1>Buyt purchase details</h1>
      </div>
    </div>
    <hr /><div class="page"><div class="page-counter"></div>
      <table>
        <caption><div>01/20/2022</div></caption>
        <tr>
          <th rowspan="2">Store name</th>
          <th rowspan="2">Store category</th>
          <th rowspan="2">Total cost</th>
          <th colspan="3">Purchased items</th>
        </tr>
        <tr>
          <th>Item name</th>
          <th>Quantity</th>
          <th>Total price</th>
        </tr> 
    
        <tr>
          <td rowspan="2">Store A</td>
          <td rowspan="2">Grocery</td>
          <td rowspan="2">0 dollars</td>
          <td>Apple</td>
          <td>2 kg</td>
          <td>0 dollars</td>
        </tr>
        
        <tr>
          <td>Ketchup</td>
          <td>1 unit</td>
          <td>0 dollars</td>
        </tr>
        <tr>
          <td rowspan="1">Store B</td>
          <td rowspan="1">Fruit</td>
          <td rowspan="1">0 dollars</td>
          <td>Apple</td>
          <td>1500 g</td>
          <td>0 dollars</td>
        </tr>
        </table></div>
          </body>
        </html>
    """
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
