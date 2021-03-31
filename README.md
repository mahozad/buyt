![Buyt logo](/app/src/main/base_hi_res_512.png?token=AHCNTO5UTDTPHEFMRS6MLU25FWK22)

a good android example: https://proandroiddev.com/android-architecture-starring-kotlin-coroutines-jetpack-mvvm-room-paging-retrofit-and-dagger-7749b2bae5f7

# Mention in the about screen that some icons are adapted and inspired by other icons found on web

Android has a settings called *Enable preview [something]* in *Settings* -> *Developer Settings*
that shows fake full battery and LTE and so on in the notification area which makes it
more appropriate for app screenshots.

To fix the paths so they can be morphed the following options are available:
  - A tool called [VectAlign](https://github.com/bonnyfone/vectalign)
    which is located in the */icons* directory
    (see this [SO post](https://stackoverflow.com/a/32386837)).
  - Good old [Shape Shifter](https://shapeshifter.design/)

TODO list:

- [x] if the bottomAppBar is hidden (by scrolling) and then you expand an Item, the fab jumps up
      The bug seems to have nothing to do with the expanding animation and persists even without that animation
- [x] Add a button (custom view) at the end of SelectionListAdapter to create a new Store
- [x] Add a separator (e.g. comma) in every 3 digits of price and other numeric fields
- [x] Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
- [x] Add feature to select a date to see its costs
- [ ] For application first run add some placeholder items and explain in the item title how to delete them and so on
- [ ] Show extended stats about items (most purchased items, most expensive items and so on) and stores
- [ ] Reimplement the item suggestion popup
- [ ] Add ability to backup all the data to user google drive account
- [ ] Add ability to export all the user data to xml, json, csv and so on
- [ ] Add ability to delete all application data (can just insert a shortcut to app settings -> clear data)
- [ ] Add automatic theme option which adapts to system theme (= day/night theme)
- [ ] This is an incomplete item
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
- [ ] Show a prompt (or an emoji or whatever) when there is no items in the home screen
- [ ] Use kotlin coroutines see[https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5]
- [ ] For testing app components see [https://developer.android.com/jetpack/docs/guide#test-components]
- [ ] Enable the user to disable location rationale dialog and always enter stores manually
- [ ] What is Spherical Law of Cosines? (for locations)
- [ ] Add the functionality to export and import all app data
- [ ] Try to first provide an MVP (minimally viable product) version of the app
- [ ] Make viewing stores on map a premium feature
- [ ] Add ability to remove all app data
- [ ] Add android.support.annotation to the app
- [ ] For every new version of the app display a what's new page on first app open
- [ ] Use list-item selection in recycler view;
      see [https://developer.android.com/guide/topics/ui/layout/recyclerview#select]
- [ ] For the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
- [ ] I can request the necessary permissions in the end of the app tutorial
- [ ] Redesign the logo in 24 by 24 grid in inkscape to make it crisp (like standard icons)
- [ ] Add widgets for the app see[https://developer.android.com/guide/topics/appwidgets/overview]
- [ ] Make icons animation durations consistent
- [ ] Convert the logo to path (with "path -> stroke to path" option) and then recreate the logo
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
