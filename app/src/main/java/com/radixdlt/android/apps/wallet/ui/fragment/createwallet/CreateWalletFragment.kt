package com.radixdlt.android.apps.wallet.ui.fragment.createwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.NewWalletActivity
import com.radixdlt.android.apps.wallet.util.toast
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.jetbrains.anko.startActivity

class CreateWalletFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_wallet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createWalletCreateNewWalletButton.setOnClickListener {
            toast("Creates a wallet")
        }

        createWalletImportWalletButton.setOnClickListener {
            val action = CreateWalletFragmentDirections
                .actionNavigationCreateWalletToNavigationImportWallet()
            findNavController().navigate(action)
        }

        createWalletDisclaimerTextView.setOnClickListener {
            activity?.apply {
                startActivity<NewWalletActivity>()
                finish()
            }
        }
    }
}
