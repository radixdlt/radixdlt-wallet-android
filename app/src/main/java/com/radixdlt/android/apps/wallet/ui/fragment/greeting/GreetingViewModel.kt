package com.radixdlt.android.apps.wallet.ui.fragment.greeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GreetingViewModel : ViewModel() {

    private var termsAndConditionsAccepted = false
    private var privacyPolicyAccepted = false

    private val _enableGetStartedButton = MutableLiveData<Boolean>()
    val enableGetStartedButton: LiveData<Boolean> get() = _enableGetStartedButton

    init {
        _enableGetStartedButton.value = false
    }

    fun onTermsAndConditionsCheckedChange(isChecked: Boolean) {
        termsAndConditionsAccepted = isChecked
        _enableGetStartedButton.value = termsAndConditionsAccepted && privacyPolicyAccepted
    }

    fun onPrivacyPolicyCheckedChange(isChecked: Boolean) {
        privacyPolicyAccepted = isChecked
        _enableGetStartedButton.value = privacyPolicyAccepted && termsAndConditionsAccepted
    }
}
