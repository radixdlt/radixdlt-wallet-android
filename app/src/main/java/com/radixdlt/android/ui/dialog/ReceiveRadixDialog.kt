package com.radixdlt.android.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.Glide
import com.google.zxing.EncodeHintType
import com.radixdlt.android.R
import com.radixdlt.android.RadixWalletApplication
import com.radixdlt.android.ui.activity.BaseActivity
import com.radixdlt.android.ui.activity.ReceiveRadixInvoiceActivity
import com.radixdlt.android.util.QueryPreferences
import com.radixdlt.android.util.setAddressWithColors
import kotlinx.android.synthetic.main.dialog_receive_radix.view.*
import net.glxn.qrgen.android.QRCode

open class ReceiveRadixDialog : AppCompatDialogFragment() {

    private val size = RadixWalletApplication.densityPixel!!

    private lateinit var myAddress: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = View.inflate(context, R.layout.dialog_receive_radix, null)

        showAddress(v)

        val qrCode = QRCode.from(myAddress)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(activity!!).load(qrCode).into(v.imageView)

        v.generateInvoiceButton.setOnClickListener {
            dismiss()
            ReceiveRadixInvoiceActivity.newIntent(activity!!)
        }

        return AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.receive_radix_dialog_title))
            .setView(v)
            .setPositiveButton(getString(R.string.receive_radix_dialog_copy)) { _, _ ->
                sendResult(Activity.RESULT_OK)
            }
            .setNeutralButton(getString(R.string.receive_radix_dialog_share)) { _, _ ->
                BaseActivity.openedShareDialog = true

                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, myAddress)
                startActivity(
                    Intent.createChooser(
                        sharingIntent,
                        getString(R.string.receive_radix_dialog_intent_chooser_title)
                    )
                )
                dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, null)
    }

    companion object {
        fun newInstance(): ReceiveRadixDialog {
            return ReceiveRadixDialog()
        }
    }

    private fun showAddress(view: View) {
        myAddress = QueryPreferences.getPrefAddress(view.context)
        view.addressTextView.text = setAddressWithColors(view.context, myAddress)
    }
}
