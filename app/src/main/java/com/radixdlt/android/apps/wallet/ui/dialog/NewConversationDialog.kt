package com.radixdlt.android.apps.wallet.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.BarcodeCaptureActivity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import kotlinx.android.synthetic.main.dialog_new_conversation.view.*
import org.jetbrains.anko.toast

open class NewConversationDialog : AppCompatDialogFragment() {

    private lateinit var v: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        v = View.inflate(activity, R.layout.dialog_new_conversation, null)

        return AlertDialog.Builder(activity!!)
                .setView(v)
                .setPositiveButton(getString(R.string.new_conversation_dialog_new)) { _, _ ->
                    if (v.inputAddressEditText.text.toString() == QueryPreferences.getPrefAddress(activity!!)) {
                        activity!!.toast(getString(R.string.toast_entered_own_address))
                        return@setPositiveButton
                    }
                    sendResult(Activity.RESULT_OK)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .setNeutralButton(getString(R.string.new_conversation_dialog_scan)) { _, _ ->
                    activity!!.startActivityForResult(Intent(activity!!,
                            BarcodeCaptureActivity::class.java),
                        RC_BARCODE_CAPTURE
                    )
                }
                .create()
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(EXTRA_ADDRESS, v.inputAddressEditText.text.toString())

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, intent)
    }

    companion object {
        private const val RC_BARCODE_CAPTURE = 9000
        const val EXTRA_ADDRESS = "com.radixdlt.android.ui.dialog.address"

        fun newInstance(): NewConversationDialog {
            return NewConversationDialog()
        }
    }
}
