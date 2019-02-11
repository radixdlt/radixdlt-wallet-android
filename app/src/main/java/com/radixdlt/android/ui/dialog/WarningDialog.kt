package com.radixdlt.android.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.radixdlt.android.R

open class WarningDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.warning_dialog_title))
            .setMessage(getString(R.string.warning_dialog_message))
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

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, null)
    }

    companion object {
        fun newInstance(): WarningDialog {
            return WarningDialog()
        }
    }
}
