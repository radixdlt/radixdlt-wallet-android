package com.radixdlt.android.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.radixdlt.android.R

open class DeleteWalletDialog : AppCompatDialogFragment() {

    private var listener: DeleteWalletDialogListener? = null

    interface DeleteWalletDialogListener {
        fun onDialogPositiveClick(dialog: AppCompatDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.delete_wallet_dialog_title))
            .setMessage(getString(R.string.delete_wallet_dialog_message))
            .setPositiveButton(getString(R.string.delete_wallet_dialog_delete)) { _, _ ->
                listener?.let {
                    it.onDialogPositiveClick(this@DeleteWalletDialog)
                    return@setPositiveButton
                }

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

    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        // Needed only if dialog is shown from an Activity
        if (activity is DeleteWalletDialogListener) {
            listener = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        fun newInstance(): DeleteWalletDialog {
            return DeleteWalletDialog()
        }
    }
}
