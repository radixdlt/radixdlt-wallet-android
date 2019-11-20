package com.radixdlt.android.apps.wallet.ui.fragment.receive.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.EncodeHintType
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.RadixWalletApplication
import com.radixdlt.android.apps.wallet.databinding.FragmentReceivePaymentBinding
import com.radixdlt.android.apps.wallet.identity.Identity
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.util.copyToClipboard
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import net.glxn.qrgen.android.QRCode

class ReceivePaymentFragment : Fragment() {

    private val size by lazy { (activity?.application as RadixWalletApplication).densityPixel }

    private lateinit var ctx: Context
    private lateinit var binding: FragmentReceivePaymentBinding

    private val receivePaymentViewModel by viewModels<ReceivePaymentViewModel>(
        factoryProducer = { ReceivePaymentViewModelFactory(Identity.api?.address.toString()) }
    )
    private val address = Identity.api?.address.toString()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_receive_payment, container, false)
        binding.viewmodel = receivePaymentViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        setHasOptionsMenu(true)
        initialiseToolbar(R.string.receive_fragment_title)
        observeReceivePaymentViewModel()
        showQrCode()
    }

    private fun observeReceivePaymentViewModel() {
        receivePaymentViewModel.receivePaymentAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(receivePaymentAction: ReceivePaymentAction) {
        when (receivePaymentAction) {
            ReceivePaymentAction.COPY_ADDRESS -> copyAddress()
            ReceivePaymentAction.SHARE_ADDRESS -> shareAddress()
        }
    }

    private fun copyAddress() {
        view?.let {
            copyToClipboard(it.context, address)
            val message = R.string.receive_fragment_address_copied_snackbar
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun shareAddress() {
        BaseActivity.openedShareDialog = true

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, address)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                getString(R.string.receive_fragment__intent_chooser_title)
            )
        )
    }

    private fun showQrCode() {
        val qrCode = QRCode.from(address)
            .withSize(size, size)
            .withHint(EncodeHintType.MARGIN, 1)
            .bitmap()

        Glide.with(this).load(qrCode).into(binding.imageView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
