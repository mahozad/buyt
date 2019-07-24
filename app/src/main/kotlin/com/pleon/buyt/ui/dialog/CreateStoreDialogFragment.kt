package com.pleon.buyt.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.adapter.CatsSpinnerAdapter
import com.pleon.buyt.viewmodel.CreateStoreViewModel
import dagger.android.support.DaggerDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateStoreDialogFragment : DaggerDialogFragment() {

    private val viewModel by viewModel<CreateStoreViewModel>()
    private lateinit var name: EditText
    private lateinit var name_layout: TextInputLayout
    private lateinit var spinner: Spinner
    private lateinit var location: Location

    interface CreateStoreListener {
        fun onStoreCreated(store: Store)
    }

    private lateinit var dialog: AlertDialog
    private var createStoreListener: CreateStoreListener? = null

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
        val customView = activity!!.layoutInflater.inflate(R.layout.create_store_dialog, null)

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
                .setView(customView).setTitle(R.string.dialog_title_create_store)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()

        // To prevent the dialog to dismiss on button click, set the button listeners here
        dialog.setOnShowListener {
            dialog.getButton(BUTTON_POSITIVE).setOnClickListener { onDialogOk() }
        }

        return dialog
    }

    private fun onDialogOk() {
        if (validateFields()) {
            val name = name.text.toString()
            val store = Store(Coordinates(location), name, spinner.selectedItem as Category)
            // FIXME: "this" is used as the owner because "lifeCycleOwner" threw exception
            viewModel.addStore(store).observe(this, Observer {
                createStoreListener!!.onStoreCreated(store)
                dismiss()
            })
        }
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
        if (context is CreateStoreListener) createStoreListener = context as CreateStoreListener
        else throw  RuntimeException("$context  must implement CreateStoreListener")
    }

    override fun onDetach() {
        super.onDetach()
        createStoreListener = null
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
