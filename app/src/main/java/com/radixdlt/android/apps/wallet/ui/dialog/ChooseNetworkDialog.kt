package com.radixdlt.android.apps.wallet.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.util.ALPHANET
import com.radixdlt.android.apps.wallet.util.ALPHANET2
import com.radixdlt.android.apps.wallet.util.QueryPreferences

open class ChooseNetworkDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        universe = if (QueryPreferences.getPrefNetwork(activity!!) == ALPHANET) 0 else 1

        val preSelectedUniverse = universe

        val alertDialog = AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.choose_network_dialog_title))
            .setSingleChoiceItems(arrayOf(
                ALPHANET,
                ALPHANET2
            ), universe
            ) { _, which ->
                universe = which
            }
            .setPositiveButton(getString(R.string.choose_network_select)) { _, _ ->
                if (preSelectedUniverse == universe) return@setPositiveButton
                sendResult(Activity.RESULT_OK)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.lightRed))
        }

        return alertDialog
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        val i = Intent()
        i.putExtra(
            EXTRA_UNIVERSE,
            universe
        )

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, i)
    }

    companion object {
        const val EXTRA_UNIVERSE = "com.radixdlt.android.ui.dialogs.universe"
        var universe: Int = 0

        fun newInstance(): ChooseNetworkDialog {
            return ChooseNetworkDialog()
        }
    }
}
