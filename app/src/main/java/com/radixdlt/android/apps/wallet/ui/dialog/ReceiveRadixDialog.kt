package com.radixdlt.android.apps.wallet.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.bumptech.glide.Glide
import com.google.zxing.EncodeHintType
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.ui.activity.ReceiveRadixInvoiceActivity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.setAddressWithColors
import kotlinx.android.synthetic.main.dialog_receive_radix.view.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.toast

open class ReceiveRadixDialog : AppCompatDialogFragment() {

    private val size = RadixWalletApplication.densityPixel!!

    private lateinit var myAddress: String

    private lateinit var ctx: Context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = View.inflate(context, R.layout.dialog_receive_radix, null)
        ctx = v.context

        showAddress(v)

        val qrCode = QRCode.from(myAddress)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(this).load(qrCode).into(v.imageView)

        v.generateInvoiceButton.setOnClickListener {
            dismiss()
            ReceiveRadixInvoiceActivity.newIntent(activity!!)
        }

        return AlertDialog.Builder(activity!!)
            .setTitle(getString(R.string.receive_radix_dialog_title))
            .setView(v)
            .setPositiveButton(getString(R.string.receive_radix_dialog_copy)) { _, _ ->
                copyAddressToClipBoard(QueryPreferences.getPrefAddress(ctx))
            }
            .setNeutralButton(getString(R.string.receive_radix_dialog_share)) { _, _ ->
                BaseActivity.openedShareDialog = true

                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(Intent.EXTRA_TEXT, myAddress)
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

    private fun copyAddressToClipBoard(address: String) {
        copyToClipboard(ctx, address)
        activity?.toast(getString(R.string.common_toast_address_copied_clipboard))
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
