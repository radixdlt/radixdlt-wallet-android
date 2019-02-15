package com.radixdlt.android.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.radixdlt.android.R
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.validateIPAddress
import kotlinx.android.synthetic.main.dialog_ip_address.view.*
import org.jetbrains.anko.toast

open class InputNodeIPAddressDialog : AppCompatDialogFragment() {

    private lateinit var v: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        v = View.inflate(activity!!, R.layout.dialog_ip_address, null)

        if (QueryPreferences.getPrefIsRandomNodeSelection(v.context)) {
            v.randomRadioButton.isChecked = true
            v.inputIPAddressEditText.isEnabled = false
        } else {
            v.customRadioButton.isChecked = true
            v.inputIPAddressEditText.isEnabled = true
            v.inputIPAddressEditText.setSelection(v.inputIPAddressEditText.text.length)
        }

        if (QueryPreferences.getPrefNodeIP(v.context).isNotEmpty()) {
            v.inputIPAddressEditText.setText(QueryPreferences.getPrefNodeIP(v.context))
            v.inputIPAddressEditText.setSelection(v.inputIPAddressEditText.text.length)
        }

        v.ipAddressRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.randomRadioButton -> {
                    v.inputIPAddressEditText.isEnabled = false
                }
                R.id.customRadioButton -> {
                    v.inputIPAddressEditText.isEnabled = true
                    v.inputIPAddressEditText.setSelection(v.inputIPAddressEditText.text.length)
                }
            }
        }

        val alertDialog = AlertDialog.Builder(activity!!)
            .setView(v)
            .setPositiveButton(getString(R.string.node_ip_address_dialog_set), null)
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()

        alertDialog.setOnShowListener {
            val button = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            button.setOnClickListener {
                val ipAddress = v.inputIPAddressEditText.text.toString()
                val valid = validateIPAddress(ipAddress)
                if (v.customRadioButton.isChecked && !valid) {
                    activity!!.toast("IP address is NOT valid")
                    return@setOnClickListener
                }

//                QueryPreferences.setPrefRandomNodeSelection(
//                    v.context,
//                    v.randomRadioButton.isChecked
//                )
//                QueryPreferences.setPrefNodeIP(v.context, ipAddress)
                alertDialog.dismiss()
                sendResult(Activity.RESULT_OK)
            }
        }

        return alertDialog
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(EXTRA_ADDRESS, v.inputIPAddressEditText.text.toString())
        intent.putExtra(EXTRA_RANDOM_SELECTION, v.randomRadioButton.isChecked)

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, intent)
    }

    companion object {
        const val EXTRA_ADDRESS = "com.radixdlt.android.ui.dialog.ip_address"
        const val EXTRA_RANDOM_SELECTION = "com.radixdlt.android.ui.dialog.random_selection"

        fun newInstance(): InputNodeIPAddressDialog {
            return InputNodeIPAddressDialog()
        }
    }
}
