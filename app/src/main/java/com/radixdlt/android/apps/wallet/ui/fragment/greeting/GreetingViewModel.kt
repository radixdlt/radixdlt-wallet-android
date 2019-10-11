package com.radixdlt.android.apps.wallet.ui.fragment.greeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GreetingViewModel : ViewModel() {

    private var termsAndConditionsAccepted = false
    private var privacyPolicyAccepted = false

    private val isTermsAndConditionsAndPrivacyPolicyAccepted: Boolean
        get() = termsAndConditionsAccepted && privacyPolicyAccepted

    private val _enableGetStartedButton = MutableLiveData<Boolean>()
    val enableGetStartedButton: LiveData<Boolean>
        get() = _enableGetStartedButton

    private val _greetingWalletAction = MutableLiveData<GreetingAction?>()
    val greetingWalletAction: LiveData<GreetingAction?>
        get() = _greetingWalletAction

    init {
        _enableGetStartedButton.value = false
    }

    fun onTermsAndConditionsCheckedChange(isChecked: Boolean) {
        termsAndConditionsAccepted = isChecked
        _enableGetStartedButton.value = isTermsAndConditionsAndPrivacyPolicyAccepted
    }

    fun onPrivacyPolicyCheckedChange(isChecked: Boolean) {
        privacyPolicyAccepted = isChecked
        _enableGetStartedButton.value = isTermsAndConditionsAndPrivacyPolicyAccepted
    }

    fun getStartedButtonClick() {
        _greetingWalletAction.value = GreetingAction.GetStarted
        _greetingWalletAction.value = null
    }
}
