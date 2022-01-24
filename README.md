[![Crowdin](https://badges.crowdin.net/buyt/localized.svg)](https://crowdin.com/project/buyt)
![Buyt logo](/style-guide/new-logo-2-optimized.svg)

## The name

See https://en.wikipedia.org/wiki/Portmanteau

## The logo
source: new-logo-hidden-parts-removed.svg
foreground
	trim: no
	resize: 70%

a good android example: https://proandroiddev.com/android-architecture-starring-kotlin-coroutines-jetpack-mvvm-room-paging-retrofit-and-dagger-7749b2bae5f7

FIXME: Sometimes app crashes. (For me once when I tapped on a store in stores screen, the app jumped to main screen).
The logcat in my phone showed an error *process died fore TOP*
see [this post](https://stackoverflow.com/q/16052097) and its links.

## Signing the APK
The signing information is stored in the file *local.properties* which is not added to VCS.
The signing info is also available in the *Secrets* section of the GitHub repository.
Also, the signing info and its key file are available in one my private repositories.

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

## Convert single live event to channel
https://cesarmorigaki.medium.com/replace-singleliveevent-with-kotlin-channel-flow-b983f095a47a

## Provided scopes
Coroutines created with scopes `lifecycleScope` and `viewModelScope` do not need to
be canceled in activity or viewModel onDestroy method. 

## A good article about test types in Android
https://proandroiddev.com/an-effective-testing-strategy-for-android-i-4a269d134acf

## `runBlockingTest` vs `runBlocking` in unit tests 
Executes a testBody inside an immediate execution dispatcher.
This is similar to `runBlocking` but it will immediately progress past delays and
into launch and async blocks. You can use this to write tests that execute in the
presence of calls to delay without causing your test to take extra time.

## Terms of Service example
This application is provided by Pleon at no cost, as an Ad-supported app and is intended for use as is.
        Which1 neither collect nor share your information with anyone; however, the app does use third-party services that may collect your information.
        The free version of Which1 may contain Ads that link to external entities. By clicking on Ads you will be directed to a website that is not operated by us so
        we have no control over and assume no responsibility for the content, privacy policies, or practices of any of these sites or services.

        \n\nAnd as Google states in its terms of service: \"Don’t misuse our Services. For example, don’t try to access our Services using a method other than the interface and the instructions that we provide.
        We may suspend or stop providing our Services to you if you do not comply with our terms or policies or if we are investigating suspected misconduct.\"
        the use of this application may be illegal in your country.

# Mention in the about screen that some icons are adapted and inspired by other icons found on web

Android has a settings called *Enable preview [something]* in *Settings* -> *Developer Settings*
that shows fake full battery and LTE and so on in the notification area which makes it
more appropriate for app screenshots.

To fix the paths so they can be morphed the following options are available:
  - A tool called [VectAlign](https://github.com/bonnyfone/vectalign)
    which is located in the */icons* directory
    (see this [SO post](https://stackoverflow.com/a/32386837)).
  - Good old [Shape Shifter](https://shapeshifter.design/)

To fix the bug when sort or filter buttons are clicked very fast successively, see [this](https://medium.com/androiddevelopers/coroutines-on-android-part-iii-real-work-2ba8a2ec2f45)

To scale a font down/up do [this](https://www.fonttutorials.com/how-to-scale-glyphs/):
  1. Download [FontForge](https://github.com/fontforge/fontforge)
  2. Open the font in FontForge
  3. Select all the glyphs by pressing <kbd>CTRL</kbd> + <kbd>A</kbd>
  4. Select *Elements* -> *Transformations* -> *Transform*
  5. Select *Scale Uniformly...* from the drop down
  6. After scaling, generate the font in desired format

TODO list:

- [ ] Migrate from SharedPreferences to [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
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
- [ ] Rename the package name to ir.mahozad (?)
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
