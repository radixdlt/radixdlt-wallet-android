package com.radixdlt.android.apps.wallet.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.radixdlt.android.R

open class WarningDialog : AppCompatDialogFragment() {

    private var nodeSelection = false
    private var randomSelection = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        nodeSelection = arguments!!.getBoolean(ARG_NODE_SELECTION_BOOLEAN, false)
        randomSelection = arguments!!.getBoolean(ARG_RANDOM_SELECTION_BOOLEAN, false)

        val title: String
        val message: String

        if (nodeSelection) {
            if (randomSelection) {
                title = getString(R.string.warning_dialog_random_selection_title)
                message = getString(R.string.warning_dialog_random_selection_message)
            } else {
                title = getString(R.string.warning_dialog_node_selection_title)
                message = getString(R.string.warning_dialog_node_selection_message)
            }
        } else {
            title = getString(R.string.warning_dialog_changing_universe_title)
            message = getString(R.string.warning_dialog_changing_universe_message)
        }

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.warning_dialog_change)) { _, _ ->
                sendResult(Activity.RESULT_OK)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.lightRed))
        }

        return alertDialog
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(EXTRA_NODE_SELECTION, nodeSelection)

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, intent)
    }

    companion object {
        const val EXTRA_NODE_SELECTION = "com.radixdlt.android.ui.dialog.node_selection"

        private const val ARG_NODE_SELECTION_BOOLEAN = "node_selection"
        private const val ARG_RANDOM_SELECTION_BOOLEAN = "random_selection"

        fun newInstance(nodeSelection: Boolean, randomSelection: Boolean?): WarningDialog {
            val args = Bundle()
            val warningDialog = WarningDialog()
            args.putBoolean(ARG_NODE_SELECTION_BOOLEAN, nodeSelection)
            randomSelection?.let {
                args.putBoolean(ARG_RANDOM_SELECTION_BOOLEAN, it)
            }
            warningDialog.arguments = args

            return warningDialog
        }
    }
}
