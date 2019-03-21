package com.pleon.buyt.ui.fragment

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogRow
import kotlinx.android.synthetic.main.fragment_create_store.*
import java.util.*

const val ARG_LOCATION = "com.pleon.buyt.extra.LOCATION"

/**
 * This fragment requires a Toolbar as it needs to inflate and use a menu item for selection of
 * store category. So the activities using this fragment must have a Toolbar set.
 */
class CreateStoreFragment : Fragment(), SelectDialogFragment.Callback {

    interface Callback {
        fun onSubmit(store: Store)
    }

    private lateinit var location: Location
    private var callback: Callback? = null
    private var storeCategory = GROCERY
    private var selectCategoryTxvi: TextView? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        location = activity!!.intent.getParcelableExtra(ARG_LOCATION)
    }

    /**
     * Your fragments can contribute menu items to the activity's Options Menu (and, consequently,
     * the app bar) by implementing onCreateOptionsMenu(). In order for this method to receive calls,
     * however, setHasOptionsMenu() must be called during onCreate(), to indicate that the fragment
     * would like to add items to the Options Menu. Otherwise, the fragment doesn't receive a call
     * to onCreateOptionsMenu(). Any items that you then add to the Options Menu from the fragment
     * are appended to the existing menu items.
     *
     * Note: Although your fragment receives an on-item-selected callback for each menu item it adds,
     * the activity is first to receive the respective callback when the user selects a menu item.
     * If the activity's implementation of the on-item-selected callback does not handle the
     * selected item, then the event is passed to the fragment's callback.
     *
     * @param inflater
     * @param container
     * @param savedState
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_create_store, container, false)
        // Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token)); // should be called before inflation
        // MapView mapView = view.findViewById(R.id.mapView);
        // mapView.onCreate(savedState);
        // mapView.getMapAsync(mapboxMap -> {
        //         LatLng latLng = new LatLng(location);
        //         CameraPosition position = new CameraPosition.Builder()
        //                 .target(latLng).zoom(14).tilt(20).build();
        //         mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        //         mapboxMap.addMarker(new MarkerOptions().position(latLng));
        //         mapboxMap.setStyle(Style.DARK, style -> {
        //             // Map is set up and the style has loaded. Now you can add data or make other map adjustments
        //         });
        //     }
        // );

        setHasOptionsMenu(true) // for the onCreateOptionsMenu() method to be called
        return view
    }

    /**
     * For this method to be called, it is required that setHasOptionsMenu() has been set.
     *
     * Note that the containing activity must have a Toolbar set so this fragment can inflate and
     * use its own menu item.
     *
     * @param menu
     * @param inflater
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_store, menu)

        val menuItem = menu.findItem(R.id.action_store_category)
        selectCategoryTxvi = menuItem.actionView.findViewById(R.id.select_category)
        // Setting up "Choose category" action because it has custom layout
        menuItem.actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_store_category) {
            // FIXME: initialize this only once
            val selectionList = ArrayList<SelectDialogRow>() // dialog requires ArrayList
            for (category in Category.values()) {
                val selection = SelectDialogRow(getString(category.storeNameRes), category.storeImageRes)
                selectionList.add(selection)
            }
            val selectStoreDialog = SelectDialogFragment
                    .newInstance(this, R.string.dialog_title_select_cat, selectionList)
            selectStoreDialog.show(activity!!.supportFragmentManager, "SELECT_STORE_DIALOG")
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Callback) callback = context
        else throw RuntimeException("$context must implement Callback")
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onSelected(index: Int) {
        selectCategoryTxvi!!.setCompoundDrawablesRelativeWithIntrinsicBounds(Category.values()[index].storeImageRes, 0, 0, 0)
        selectCategoryTxvi!!.setText(Category.values()[index].storeNameRes)
        storeCategory = Category.values()[index]
    }

    // FIXME: These methods are duplicate (from AddItemFragment). Refactor them

    fun onDonePressed() {
        if (validateFields()) {
            val coordinates = Coordinates(location)

            val name = name.text.toString()
            val store = Store(coordinates, name, storeCategory)

            callback!!.onSubmit(store)
        }
    }

    private fun validateFields(): Boolean {
        if (isEmpty(name)) {
            name_layout.error = "Name cannot be empty"
            return false
        }
        return true
    }

    private fun isEmpty(editText: EditText) = editText.text.toString().trim { it <= ' ' }.isEmpty()
}
