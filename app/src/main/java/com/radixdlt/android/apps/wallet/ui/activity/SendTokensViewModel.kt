package com.radixdlt.android.apps.wallet.ui.activity

import androidx.lifecycle.ViewModel
import com.radixdlt.android.apps.wallet.data.model.transaction.SendTokensLiveData
import com.radixdlt.android.apps.wallet.data.model.transaction.TokenTypesLiveData
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Named

class SendTokensViewModel @Inject constructor(
    @Named("tokenTypes") val tokenTypesLiveData: TokenTypesLiveData,
    @Named("sendToken") val sendTokensLiveData: SendTokensLiveData
) : ViewModel() {

    fun sendToken(to: String, amount: BigDecimal, token: String, payLoad: String?) {
        sendTokensLiveData.sendToken(to, amount, token, payLoad)
    }
}
