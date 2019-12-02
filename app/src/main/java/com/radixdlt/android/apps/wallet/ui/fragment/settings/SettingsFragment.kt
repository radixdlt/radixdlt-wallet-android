package com.radixdlt.android.apps.wallet.ui.fragment.settings

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsChecker
import com.radixdlt.android.apps.wallet.data.model.AssetDao
import com.radixdlt.android.apps.wallet.data.model.TransactionsDaoOM
import com.radixdlt.android.apps.wallet.data.model.message.MessagesDao
import com.radixdlt.android.apps.wallet.data.model.newtransaction.TransactionsDao2
import com.radixdlt.android.apps.wallet.data.model.transaction.TransactionsDao
import com.radixdlt.android.apps.wallet.databinding.FragmentSettingsBinding
import com.radixdlt.android.apps.wallet.helper.CustomTabsHelper.openCustomTab
import com.radixdlt.android.apps.wallet.helper.WebviewFallback
import com.radixdlt.android.apps.wallet.ui.activity.BaseActivity
import com.radixdlt.android.apps.wallet.ui.activity.main.MainActivity
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.get
import com.radixdlt.android.apps.wallet.util.Pref.set
import com.radixdlt.android.apps.wallet.util.URL_REPORT_ISSUE
import com.radixdlt.android.apps.wallet.util.initialiseToolbar
import com.radixdlt.android.apps.wallet.util.showSuccessSnackbarAboveNavigationView
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var transactionsDao: TransactionsDao

    @Inject
    lateinit var transactionsDao2: TransactionsDao2

    @Inject
    lateinit var messagesDao: MessagesDao

    @Inject
    lateinit var assetDao: AssetDao

    @Inject
    lateinit var transactionsDaoOM: TransactionsDaoOM

    lateinit var ctx: Context
    private lateinit var customTabsIntent: CustomTabsIntent

    private lateinit var binding: FragmentSettingsBinding

    private val isUsingBiometrics by lazy {
        activity?.run {
            BiometricsChecker.getInstance(this).isUsingBiometrics
        } ?: throw NullPointerException()
    }

    private val settingsViewModel: SettingsViewModel by viewModels()

    private val settingsSharedViewModel by activityViewModels<SettingsSharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = initialiseDataBinding(inflater, container)

    private fun initialiseDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.viewmodel = settingsViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx = view.context
        (activity as MainActivity).setNavAndBottomNavigationVisible()

        createCustomTabsBuilder()
        initialiseViewModel()
    }

    private fun initialiseViewModel() {
        settingsViewModel.showSecurityOptions(
            isUsingBiometrics,
            defaultPrefs()[Pref.USE_BIOMETRICS, false],
            defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH, false],
            defaultPrefs()[Pref.PIN_SET, false]
        )
        settingsViewModel.settingsAction.observe(viewLifecycleOwner, Observer(::settingsAction))
        settingsViewModel.useBiometrics.observe(viewLifecycleOwner, Observer(::useBiometrics))
        settingsViewModel.authenticateOnLaunch.observe(
            viewLifecycleOwner,
            Observer(::authenticateOnLaunch)
        )
        settingsSharedViewModel.showChangedPinSnackbar.observe(viewLifecycleOwner, Observer {
            showSuccessSnackbarAboveNavigationView(
                R.string.settings_fragment_change_pin_success_snackbar
            )
        })
        settingsSharedViewModel.authenticateAction.observe(
            viewLifecycleOwner,
            Observer(::authenticationAction)
        )
    }

    private fun settingsAction(settingsAction: SettingsAction) {
        when (settingsAction) {
            SettingsAction.BACKUP_WALLET -> checkWalletBackedUp()
            SettingsAction.CHANGE_PIN -> changePin()
            SettingsAction.USE_BIOMETRICS -> authenticate(AuthenticateFunctionality.BIOMETRICS)
            SettingsAction.DELETE_WALLET -> showDeleteWalletDialog()
            SettingsAction.REPORT_ISSUE -> reportIssue()
        }
    }

    private fun authenticationAction(action: AuthenticateAction) {
        when (action) {
            AuthenticateAction.Cancel -> checkBiometricsToggle()
            AuthenticateAction.Backup -> backupWallet()
            AuthenticateAction.UseBiometrics -> toggleBiometrics()
            is AuthenticateAction.UsePin -> navigateToPinAuthentication(action.functionality)
        }
    }

    private fun checkBiometricsToggle() {
        lifecycleScope.launchWhenResumed {
            delay(300)
            val usingBiometrics = defaultPrefs()[Pref.USE_BIOMETRICS, false]
            if (binding.settingsUseBiometricsSwitch.isChecked != usingBiometrics) {
                binding.settingsUseBiometricsSwitch.toggle()
            }
        }
    }

    private fun authenticate(functionality: AuthenticateFunctionality) {
        if (defaultPrefs()[Pref.USE_BIOMETRICS, false]) {
            navigateToBiometricsAuthentication(functionality)
        } else {
            navigateToPinAuthentication(functionality)
        }
    }

    private fun navigateToBiometricsAuthentication(functionality: AuthenticateFunctionality) {
        val action = SettingsFragmentDirections
            .actionNavigationSettingsToNavigationAuthenticateBiometricsDialog(functionality)
        findNavController().navigate(action)
    }

    private fun changePin() {
        val action = SettingsFragmentDirections
            .navigationSettingsToNavigationChangePin()
        findNavController().navigate(action)
    }

    private fun checkWalletBackedUp() {
        if (defaultPrefs()[Pref.WALLET_BACKED_UP, false]) {
            authenticate(AuthenticateFunctionality.BACKUP)
        } else {
            backupWallet()
        }
    }

    private fun backupWallet() {
        val action = SettingsFragmentDirections
            .navigationSettingsToNavigationBackupWallet()
        findNavController().navigate(action)
    }

    private fun showDeleteWalletDialog() {
        val action = SettingsFragmentDirections
            .navigationSettingsToNavigationSettingsDeleteWallet()
        findNavController().navigate(action)
    }

    private fun reportIssue() {
        activity?.apply {
            BaseActivity.openedCustomTabs = true
            openCustomTab(this, customTabsIntent, Uri.parse(URL_REPORT_ISSUE), WebviewFallback())
        }
    }

    private fun useBiometrics(use: Boolean) {
        defaultPrefs()[Pref.USE_BIOMETRICS] = use
    }

    private fun toggleBiometrics() {
        lifecycleScope.launchWhenResumed {
            settingsViewModel.toggleBiometrics()
        }
    }

    private fun authenticateOnLaunch(use: Boolean) {
        defaultPrefs()[Pref.AUTHENTICATE_ON_LAUNCH] = use
    }

    private fun createCustomTabsBuilder() {
        customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
            .setShowTitle(true)
            .enableUrlBarHiding()
            .setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back))
            .build()
    }

    private fun navigateToPinAuthentication(functionality: AuthenticateFunctionality) {
        val action = SettingsFragmentDirections
            .actionNavigationSettingsToNavigationAuthenticatePinDialog(functionality)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        initialiseToolbar(R.string.app_name, false)
        checkBiometricsToggle()
        BaseActivity.openedShareDialog = false
        BaseActivity.openedCustomTabs = false
    }
}
