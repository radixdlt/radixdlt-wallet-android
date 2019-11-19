package com.radixdlt.android.apps.wallet.ui.fragment.receive.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hadilq.liveevent.LiveEvent

class ReceivePaymentViewModel(radixAddress: String) : ViewModel() {

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _receivePaymentAction = LiveEvent<ReceivePaymentAction>()
    val receivePaymentAction: LiveEvent<ReceivePaymentAction> get() = _receivePaymentAction

    init {
        _address.value = radixAddress
    }

    fun copyAddressClickListener() {
        _receivePaymentAction.value = ReceivePaymentAction.COPY_ADDRESS
    }

    fun shareAddressClickListener() {
        _receivePaymentAction.value = ReceivePaymentAction.SHARE_ADDRESS
    }
}

class ReceivePaymentViewModelFactory(
    private val address: String
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ReceivePaymentViewModel(address) as T
    }
}
