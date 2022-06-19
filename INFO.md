# The repository is hosted on both [GitHub](https://github.com/mahozad/buyt) and [GitLab](https://gitlab.com/mahozad/buyt). Push the commits to both of them.

# The name
- buyt - بایت (از ترکیب دو کلمه‌ی buy it درست شده است)
- See https://en.wikipedia.org/wiki/Portmanteau
- Boxor
- autoshop
- چکه
در اصل از کلمه‌ی تیک انگلیسی (check mark) گرفته شده است.
هر خریدی که انجام می‌دهید را با زدن تیک (چک مارک) از لیست سفارش به لیست خرید اضافه کنید.
میزان خریدها و مخارج خود را چک کنید.
- شاپ‌تیک (shoptick)
- ماله (از کلمه‌ی mall گرفته شده است. به کلمه‌ی مال (ثروت) فارسی هم شباهت دارد)
- مال در مال (مال در mall)
- صندوق

# The logo
  - source: new-logo-hidden-parts-removed.svg
  - foreground
  - trim: no
  - resize: 70%

# [![Crowdin](https://badges.crowdin.net/buyt/localized.svg)](https://crowdin.com/project/buyt) Crowdin translation project

# Signing the APK
The signing information is stored in the file *local.properties* which is not added to VCS.
The signing info is also available in the *Secrets* section of the GitHub repository.
Also, the signing info and its key file are available in one my private repositories.

# A good Android example
https://proandroiddev.com/android-architecture-starring-kotlin-coroutines-jetpack-mvvm-room-paging-retrofit-and-dagger-7749b2bae5f7

# Convert single live event to channel
https://cesarmorigaki.medium.com/replace-singleliveevent-with-kotlin-channel-flow-b983f095a47a

# A good article about test types in Android
https://proandroiddev.com/an-effective-testing-strategy-for-android-i-4a269d134acf

# Morphing paths
To fix the paths, so they can be morphed, the following options are available:
- A tool called [VectAlign](https://github.com/bonnyfone/vectalign)
  which is located in the */icons* directory
  (see this [SO post](https://stackoverflow.com/a/32386837)).
- Good old [Shape Shifter](https://shapeshifter.design/)

# Android preview for creating screenshots
Android has a settings called *Enable preview [something]* in *Settings* -> *Developer Settings*
that shows fake full battery and LTE and so on in the notification area which makes it
more appropriate for app screenshots.

# Scale a font
To scale a font down/up do [this](https://www.fonttutorials.com/how-to-scale-glyphs/):
1. Download [FontForge](https://github.com/fontforge/fontforge)
2. Open the font in FontForge
3. Select all the glyphs by pressing <kbd>CTRL</kbd> + <kbd>A</kbd>
4. Select *Elements* -> *Transformations* -> *Transform*
5. Select *Scale Uniformly...* from the drop down
6. After scaling, generate the font in desired format

# Bugs
- [ ] Sometimes app crashes. (For me once when I tapped on a store in stores screen, the app jumped to main screen).
  The logcat in my phone showed an error *process died fore TOP*
  see [this post](https://stackoverflow.com/q/16052097) and its links.
- [ ] This error occured once when I had searched for the store (not skipping), after the
  store was found, entering price for items and then after hitting done button the app exited:  
  (got the log in Windows with this command: `adb logcat -t "02-25 10:10:10.000"` which gots all the logs after February 25th 10:10:10)
- [ ] To fix the bug when sort or filter buttons are clicked very fast successively, see [this](https://medium.com/androiddevelopers/coroutines-on-android-part-iii-real-work-2ba8a2ec2f45)
  ```log
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: FATAL EXCEPTION: main
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: Process: com.pleon.buyt, PID: 13524
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: java.lang.NullPointerException
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at q3.e.d(:13)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at n3.h.onClick(:3)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.view.View.performClick(View.java:7259)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.view.View.performClickInternal(View.java:7236)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.view.View.access$3600(View.java:801)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.view.View$PerformClick.run(View.java:27892)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.os.Handler.handleCallback(Handler.java:883)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.os.Handler.dispatchMessage(Handler.java:100)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.os.Looper.loop(Looper.java:214)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at android.app.ActivityThread.main(ActivityThread.java:7356)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Native Method)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:491)
  02-27 11:54:22.443 13524 13524 E AndroidRuntime: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:940)
  ```

# TODOs
  - [ ] Make an FSM (finite state machine) diagram of Buyt states and add it to repository.
  - [ ] Use [segmented buttons](https://m3.material.io/components/segmented-buttons/overview) instead of a custom toggle button for item unit
  - [ ] Mention in the about screen that some icons are adapted and inspired by other icons found on web
  - [ ] Migrate the splash screen to [Android 12 Splash screens](https://developer.android.com/guide/topics/ui/splash-screen)
  - [ ] Provide animated images in notification on Android 12 (https://developer.android.com/about/versions/12/features#enriched_image_support_for_notifications)
  - [ ] Migrate from SharedPreferences to [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
  - [ ] Use the new [LocationRequest.Builder](https://developer.android.com/reference/kotlin/android/location/LocationRequest.Builder) introduced in Android 12
  - [ ] Make tables in HTML export responsive. See [this codepen](https://codepen.io/AllThingsSmitty/pen/MyqmdM)
  - [ ] Improve the dependencies block in the build script. See [this article](https://proandroiddev.com/avoid-repetitive-dependency-declarations-with-gradle-kotlin-dsl-97c904704727)
  - [ ] Migrate the purchasing code to [Poolaki](https://developers.cafebazaar.ir/fa/guidelines/in-app-billing/implementation/kotlin#)
  - [ ] Add a changelog page (or button) so the user can see the full history and changelog of all versions of the app
  - [ ] Add a button to store item for editing store name
  - [ ] Add a button to store item for viewing the store on maps (using intents to open on external map apps)
  - [ ] Add a plus botton to stores bottom app bar to open an external map for getting lat and long and then showing create store dialog
  - [ ] Convert the quantity to an editable text fiend and enable the edition when transitioning to buy state.
  - [ ] Add export to PDF to export options
  - [ ] Add backup and restore option to backup all app data to Google Drive (using Google Drive API)
  - [ ] Add option to change the empty hint animated icon of the main screen
  - [ ] Show line chart y-axis labels when using logarithmic scale
  - [ ] Reimplement the pie chart
      - [ ] Make lines start at the center of the slice (instead of starting at the edge of the slice)
      - [ ] Make the width of the pie chart consistent across different DPIs
  
  - [x] Add an option to settings to toggle the line chart y axis between regular and logarithmic values
  - [x] if the bottomAppBar is hidden (by scrolling) and then you expand an Item, the fab jumps up
    The bug seems to have nothing to do with the expanding animation and persists even without that animation
  - [x] Add a button (custom view) at the end of SelectionListAdapter to create a new Store
  - [x] Add a separator (e.g. comma) in every 3 digits of price and other numeric fields
  - [x] Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
  - [x] Add feature to select a date to see its costs
  - [ ] Add new categories (icons are already available in */icons/material-icons/*)
  - [x] Use vector illustrations and animated vectors instead of tutorial screenshots and empty hints
  - [x] Write tests for the application
  - [ ] Rename the package name to *ir.mahozad* (?)
  - [x] Wrap the text *Already bought* in a button (or a rectangle around it) to indicate it's clickable
  - [ ] When user adds a new item, somehow indicate that the item was added
  - [x] Update the string "کالای ... حذف شد" to "... حذف شد"
  - [x] In already bought section show a prompt in the store dialog when there is no store
  - [ ] For application first run add some placeholder items and explain in the item title how to delete them and so on
  - [x] Show extended stats about items (most purchased items, most expensive items and so on) and stores
  - [x] Reimplement the item suggestion popup
  - [ ] Add ability to backup all the data to user google drive account
  - [x] Add ability to export all the user data to xml, json, csv and so on
  - [ ] Add ability to delete all application data (can just insert a shortcut to app settings -> clear data)(https://stackoverflow.com/q/6134103)
  - [x] Add automatic theme option which adapts to system theme (= day/night theme)
  - [ ] Remove all nullable values and null checks
  - [ ] Localize the app: [poeditor.com]
  - [ ] Add an option so when a new item is added the fragment pop up be dismissed immediately
  - [ ] Make separate free and paid version flavors for the app
  - [ ] Upgrade to paid option vs two separate free and paid flavors
  - [ ] Limit the max buys in a day in free version to 3
  - [ ] Suggestion: instead of embedding map in the application, the app can use an implicit
    intent to show map provided by other apps (e.g. google map)
    see [https://developer.android.com/training/basics/intents/sending]
  - [ ] In onDestroy(), onPause() and... do the reverse things you did in onCreate(), onResume() and...
  - [ ] Convert all ...left and ...right attributes to ...start and ...end
  - [ ] Show a small progress bar of how much has been spent if user has set a limit on spends
  - [ ] Use downloadable fonts instead of integrating the font in the app to reduce the app size
  - [ ] New version of MaterialCardView will include a setCheckedIcon. check it out
  - [x] Show a prompt (or an emoji or whatever) when there is no items in the home screen
  - [ ] Use kotlin coroutines see[https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5]
  - [ ] For testing app components see [https://developer.android.com/jetpack/docs/guide#test-components]
  - [ ] Enable the user to disable location rationale dialog and always enter stores manually
  - [ ] What is Spherical Law of Cosines? (for locations)
  - [ ] Try to first provide an MVP (minimally viable product) version of the app
  - [ ] Add a button to store cards which opens the store location in a map application
  - [ ] Add android.support.annotation to the app
  - [ ] For every new version of the app display a what's new page on first app open
  - [ ] Use list-item selection in recycler view;
    see [https://developer.android.com/guide/topics/ui/layout/recyclerview#select]
  - [ ] For the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
  - [ ] I can request the necessary permissions in the end of the app tutorial
  - [x] Redesign the logo in 24 by 24 grid in inkscape to make it crisp (like standard icons)
  - [ ] Add widgets for the app see[https://developer.android.com/guide/topics/appwidgets/overview]
  - [ ] Make icons animation durations consistent
  - [ ] Add option in settings to set the default item quantity in add new item activity (1 seems good)
  - [ ] Reimplement item unit switch button with this approach: https://stackoverflow.com/a/48640424/8583692
  - [ ] Add a functionality to merge another device data to a device (e.g. can merge all family spending data to father's phone)
  - [ ] Add an action in bottomAppBar in Add Item activity to select a date for showing the item in home page
  - [ ] Use DiffUtil class (google it!) instead of calling notifyDataSetChanged() method of adapter
  - [ ] For correct margins of cards, Texts, ... see the page of that component in design section of material.io
  - [ ] disable the reorder items icon in bottomAppBar when number of items is less than 2 (by 'enabled' property of the menu item)
  - [ ] Embed ads in between of regular items
  - [ ] Collapse the chart when scrolling down (with coordinatorLayout)
  - [ ] extract margins and dimensions into xml files
  - [ ] Add snap to center for recyclerView items
  - [ ] Add option in settings to enable/disable showing urgent items at top of the list
  - [ ] Use a ViewStub in AddItemFragment layout for the part that is not shown until bought is checked
    see [https://developer.android.com/training/improving-layouts/loading-ondemand]
  - [ ] Add an option in settings for the user to be able to add a pinned shortcut to e.g. add item screen
    see [https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts]
  - [ ] Do you have multiple tables in your database and find yourself copying the same Insert,
    Update and Delete methods? DAOs support inheritance, so create a BaseDao<T> class, and define
    your generic @Insert,... there. Have each DAO extend the BaseDao and add methods specific to each of them.

# More Queries for the database
1. Most purchased item of each store (this query is incomplete)
```sql
SELECT Store.name AS 'Store name', Item.name AS 'Item name', Count(*) AS 'Purchase count'
FROM Item JOIN Purchase ON Item.purchaseId = Purchase.purchaseId
          JOIN Store ON store.storeId = Purchase.storeId
GROUP BY Store.storeId, Item.name
ORDER BY Store.storeId, "Purchase count" DESC;
-- LIMIT 1; -- This shows the first group not the first item of each group
```

2. Most constly purchase
```sql
SELECT 
      Purchase.purchaseId AS "Purchase id",
      strftime('%Y-%m-%d %H:%M:%S', Purchase.date, 'unixepoch', 'localtime') AS "Purchase date",
      Store.name AS "Store name",
      Item.name AS "Item name",
      Item.totalPrice AS "Item price"
      -- Sum(Item.totalPrice) AS "Total cost"
FROM Item JOIN Purchase ON Item.purchaseId = Purchase.purchaseId
        JOIN Store ON Store.storeId = Purchase.storeId
WHERE Purchase.purchaseId = (
  SELECT Purchase.purchaseId
    FROM Item JOIN Purchase ON Item.purchaseId = Purchase.purchaseId
    GROUP BY Purchase.purchaseId
    ORDER BY Sum(Item.totalPrice) DESC
     LIMIT 1
)
ORDER BY Item.totalPrice DESC;

-- OR another partial solution

-- SELECT 
-- 		Purchase.purchaseId,
--         Purchase.date,
--         Item.name AS "Item name",
--         Item.totalPrice AS "Item price",
--         Sum(Item.totalPrice) AS "Total cost"
-- FROM Item JOIN Purchase ON Item.purchaseId = Purchase.purchaseId
-- GROUP BY Purchase.purchaseId
-- ORDER BY Sum(totalPrice) DESC
-- LIMIT 1;
```

3. Purchase with most item variety
```sql
SELECT 
		Purchase.purchaseId,
        strftime('%Y-%m-%d %H:%M:%S', Purchase.date, 'unixepoch', 'localtime') AS "Purchase date",
        Item.name AS "Item name"
FROM Purchase JOIN Item ON Purchase.purchaseId = Item.purchaseId
WHERE Purchase.purchaseId = (
	SELECT 
			Purchase.purchaseId
	FROM Purchase JOIN Item ON Purchase.purchaseId = Item.purchaseId
	GROUP BY Purchase.purchaseId
	ORDER BY Count(DISTINCT Item.name) DESC
);

-- OR to get the count column

SELECT 
		Purchase.purchaseId,
        strftime('%Y-%m-%d %H:%M:%S', Purchase.date, 'unixepoch', 'localtime') AS "Purchase date",
        ItemCount AS "Item types",
        Item.name AS "Item name"
FROM Purchase JOIN Item ON Purchase.purchaseId = Item.purchaseId
			  JOIN (
					SELECT Purchase.purchaseId AS PMaxId, Count(DISTINCT Item.name) AS ItemCount
					FROM Purchase JOIN Item ON Purchase.purchaseId = Item.purchaseId
					GROUP BY Purchase.purchaseId
					ORDER BY ItemCount DESC
                	LIMIT 1
              ) ON Purchase.purchaseId = PMaxId;
```

4. Date with most purchase count
```sql
SELECT 
	strftime('%Y-%m-%d', Purchase.date, 'unixepoch', 'localtime') AS "Date",
    Count(*) AS "Purchase count"
--     ,Sum(Item.totalPrice) AS "Total cost"
FROM Purchase 
-- JOIN Item ON Purchase.purchaseId = Item.purchaseId
GROUP BY strftime('%Y-%m-%d', Purchase.date, 'unixepoch', 'localtime')
ORDER BY Count(*) DESC
LIMIT 1;

-- Also showing the total cost of all the items in all the purchases of that day

SELECT theDate AS "Date", purchaseCount AS "Purchase count", Sum(Item.totalPrice) AS "Total cost"
FROM Item JOIN Purchase ON Item.purchaseId = Purchase.purchaseId 
	      JOIN (
				SELECT 
					strftime('%Y-%m-%d', Purchase.date, 'unixepoch', 'localtime') AS theDate,
   					Count(*) AS purchaseCount
				FROM Purchase
				GROUP BY strftime('%Y-%m-%d', Purchase.date, 'unixepoch', 'localtime')
				ORDER BY Count(*) DESC
				LIMIT 1
          ) ON strftime('%Y-%m-%d', Purchase.date, 'unixepoch', 'localtime') = theDate
;
```

5. Most purchased items (in regard to occuring in different purchases not item count in each purchase)
```sql
SELECT Item.name AS "Item name", Count(*) AS "Item count"
FROM Item
GROUP BY Item.name
ORDER BY Count(*) DESC
LIMIT 10;
```

# Terms of Service example
This application is provided by Pleon at no cost, as an Ad-supported app and is intended for use as is.
Which1 neither collect nor share your information with anyone; however, the app does use third-party services that may collect your information.
The free version of Which1 may contain Ads that link to external entities. By clicking on Ads you will be directed to a website that is not operated by us so
we have no control over and assume no responsibility for the content, privacy policies, or practices of any of these sites or services.

        And as Google states in its terms of service: \"Don’t misuse our Services. For example, don’t try to access our Services using a method other than the interface and the instructions that we provide.
        We may suspend or stop providing our Services to you if you do not comply with our terms or policies or if we are investigating suspected misconduct.\"
        the use of this application may be illegal in your country.

# Kotlin Coroutines and Flow
https://developer.android.com/topic/libraries/architecture/coroutines
https://developer.android.com/kotlin/coroutines/coroutines-best-practices
https://developer.android.com/training/data-storage/room/async-queries
https://developer.android.com/codelabs/advanced-kotlin-coroutines#9
https://developer.android.com/kotlin/flow/test
https://elizarov.medium.com/shared-flows-broadcast-channels-899b675e805c
https://medium.com/androiddevelopers/migrating-from-livedata-to-kotlins-flow-379292f419fb
https://medium.com/androiddevelopers/room-flow-273acffe5b57
https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
https://proandroiddev.com/should-we-choose-kotlins-stateflow-or-sharedflow-to-substitute-for-android-s-livedata-2d69f2bd6fa5
https://proandroiddev.com/from-rxjava-2-to-kotlin-flow-threading-8618867e1955
https://proandroiddev.com/kotlin-coroutines-patterns-anti-patterns-f9d12984c68e
https://proandroiddev.com/kotlin-coroutines-channels-csp-android-db441400965f

## Provided scopes
Coroutines created with scopes `lifecycleScope` and `viewModelScope` do not need to
be canceled in activity or viewModel onDestroy method.

## `runBlockingTest` vs `runBlocking` in unit tests
Executes a testBody inside an immediate execution dispatcher.
This is similar to `runBlocking` but it will immediately progress past delays and
into launch and async blocks. You can use this to write tests that execute in the
presence of calls to delay without causing your test to take extra time.
