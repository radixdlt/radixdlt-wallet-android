package com.radixdlt.android.apps.wallet.ui.dialog.pin.setup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.ui.activity.main.MainViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.FullScreenDialog
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.android.databinding.DialogSetupPinBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_setup_pin.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetupPinDialog : FullScreenDialog() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    private lateinit var ctx: Context

    private val viewModelSetup: SetupPinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding: DialogSetupPinBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_setup_pin, container, false)
        binding.viewmodel = viewModelSetup
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        toolbarDialog.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbarDialog.setNavigationContentDescription(R.string.setup_pin_dialog_content_description_back_button)
        toolbarDialog.setNavigationOnClickListener { dismiss() }
        initialiseViewModels()
    }

    private fun initialiseViewModels() {
        activity?.apply {
            mainViewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        }
        viewModelSetup.setupPinAction.observe(viewLifecycleOwner, Observer(::action))
    }

    private fun action(setupPinAction: SetupPinAction?) {
        when(setupPinAction) {
            SetupPinAction.NAVIGATE -> {
                savePrefWalletBackedUp()
                returnToStart()
            }
        }
    }

    private fun savePrefWalletBackedUp() {
        activity?.apply {
            defaultPrefs()[Pref.WALLET_BACKED_UP] = true
            defaultPrefs()[Pref.PIN_SET] = true
        }
    }

    private fun returnToStart() {
        lifecycleScope.launch {
            mainViewModel.showBackUpWalletNotification(false)
            findNavController().popBackStack(R.id.navigation_backup_wallet, true)
        }
    }
}
