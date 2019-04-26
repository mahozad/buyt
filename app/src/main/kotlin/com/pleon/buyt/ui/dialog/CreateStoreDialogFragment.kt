package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.adapter.CatsSpinnerAdapter
import com.pleon.buyt.viewmodel.CreateStoreViewModel

class CreateStoreDialogFragment : AppCompatDialogFragment() {

    private lateinit var viewModel: CreateStoreViewModel
    private lateinit var name: EditText
    private lateinit var name_layout: TextInputLayout
    private lateinit var spinner: Spinner
    private lateinit var location: Location

    interface Callback {
        fun onStoreCreated(store: Store)
    }

    private lateinit var dialog: AlertDialog
    private var callback: Callback? = null

    /**
     * When you override `onCreateDialog`, Android COMPLETELY IGNORES several
     * attributes of the root node of the .xml Layout you inflate. This includes,
     * but probably isn't limited to:
     *  * background_color
     *  * layout_gravity
     *  * layout_width
     *  * layout_height
     *
     * See [this very good explanation](https://stackoverflow.com/a/41495370/8583692)
     *
     * @param savedState
     * @return
     */
    override fun onCreateDialog(savedState: Bundle?): Dialog {
//        Mapbox.getInstance(context!!, getString(R.string.mapbox_access_token)) // should be called before inflation
        val customView = activity!!.layoutInflater.inflate(R.layout.create_store_dialog, null)
//        val mapView: MapView = view.findViewById(R.id.mapView)
//        mapView.onCreate(savedState)
//        mapView.getMapAsync {
//            val latLng = LatLng(location)
//            val position: CameraPosition = CameraPosition.Builder()
//                    .target(latLng).zoom(14.0).tilt(20.0).build()
//            it.animateCamera(CameraUpdateFactory.newCameraPosition(position))
//            it.addMarker(MarkerOptions().position(latLng))
//            it.setStyle(Style.Builder().fromUrl("mapbox://styles/crygas/cjthow4p00b831fs6w5n9hhrt"))
//        }

        viewModel = ViewModelProviders.of(this).get(CreateStoreViewModel::class.java)
        location = arguments!!.getParcelable("LOCATION")!!

        spinner = customView.findViewById(R.id.catSpinner)
        name = customView.findViewById(R.id.name)
        name_layout = customView.findViewById(R.id.name_layout)
        val spinnerContainer = customView.findViewById<TextInputLayout>(R.id.spinnerContainer)

        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = onNameChanged()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Request textInputLayout focus on spinner click
        spinner.setOnTouchListener { _, _ ->
            spinnerContainer.requestFocus()
            return@setOnTouchListener false
        }

        val adapter = CatsSpinnerAdapter(context!!)
        spinner.adapter = adapter

        dialog = AlertDialog.Builder(activity!!)
                .setView(customView).setTitle(getString(R.string.dialog_title_create_store))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                // .setNeutralButton(R.string.button_text_view_on_map, null)
                .create()

        // To prevent the dialog to dismiss on button click, set the button listeners here
        dialog.setOnShowListener {
            dialog.getButton(BUTTON_POSITIVE).setOnClickListener { onDialogOk() }
            // dialog.getButton(BUTTON_NEUTRAL).setOnClickListener { onShowMap() }
        }

        return dialog
    }

    private fun onDialogOk() {
        if (validateFields()) {
            val name = name.text.toString()
            val store = Store(Coordinates(location), name, spinner.selectedItem as Category)
            // FIXME: "this" is used as the owner because "lifeCycleOwner" threw exception
            viewModel.addStore(store).observe(this, Observer {
                callback!!.onStoreCreated(store)
                dismiss()
            })
        }
    }

    private fun onShowMap() {
        // or use Uri.parse("geo:${location.latitude},${location.longitude}?z=15")
        val uri: Uri = Uri.parse("http://maps.google.com/maps?q=loc:" +
                "${location.latitude}," +
                "${location.longitude} " +
                "(${getString(R.string.map_location_label)})")
        val intent = Intent(Intent.ACTION_VIEW).apply { data = uri }
        // intent.setPackage("com.google.android.apps.maps") // If desired, Make the Intent explicit
        if (intent.resolveActivity(activity!!.packageManager) != null) startActivity(intent)
    }

    private fun onNameChanged() {
        if (name.text.isNotEmpty()) name_layout.error = null // clear error if exists
    }

    private fun validateFields(): Boolean {
        if (isEmpty(name)) {
            name_layout.error = getString(R.string.input_error_store_name)
            return false
        }
        return true
    }

    private fun isEmpty(editText: EditText) = editText.text.toString().trim { it <= ' ' }.isEmpty()

    override fun onAttach(cxt: Context) {
        super.onAttach(cxt)
        if (context is Callback) callback = context as Callback
        else throw  RuntimeException("$context  must implement Callback")
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    companion object {
        fun newInstance(location: Location?): CreateStoreDialogFragment {

            val fragment = CreateStoreDialogFragment()
            val args = Bundle()
            fragment.arguments = args
            args.putParcelable("LOCATION", location)

            return fragment
        }
    }
}
