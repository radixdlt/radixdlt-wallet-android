package com.radixdlt.android.apps.wallet.ui.fragment.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.zxing.EncodeHintType
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.setAddressWithColors
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.toast

class AccountFragment : Fragment() {

    private val size = RadixWalletApplication.densityPixel!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myAddress = QueryPreferences.getPrefAddress(view.context)
        view.addressTextView.text = setAddressWithColors(view.context, myAddress)

        setQrCode(myAddress)
        setClickListeners(myAddress)
    }

    private fun setClickListeners(myAddress: String) {
        setQrCodeImageClickListener(myAddress)
        setQrCodeImageLongClickListener(myAddress)
    }

    private fun setQrCodeImageLongClickListener(myAddress: String) {
        imageViewFrameLayout.setOnLongClickListener {
            BaseActivity.openedShareDialog = true

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, myAddress)
            startActivity(Intent.createChooser(sharingIntent, activity!!.getString(R.string.account_fragment_sharing_intent_chooser_title)))

            return@setOnLongClickListener true
        }
    }

    private fun setQrCodeImageClickListener(myAddress: String) {
        imageViewFrameLayout.setOnClickListener {
            copyToClipboard(activity!!, myAddress)

            activity?.toast(activity!!.getString(R.string.toast_address_copied_clipboard))
        }
    }

    private fun setQrCode(myAddress: String) {
        val qrCode = QRCode.from(myAddress)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(activity!!).load(qrCode).into(imageView)
    }

    override fun onResume() {
        super.onResume()
        BaseActivity.openedShareDialog = false
    }
}
