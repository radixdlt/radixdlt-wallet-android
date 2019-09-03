package com.radixdlt.android.apps.wallet.ui.fragment.payment.summary

sealed class PaymentSummaryAction {
    class CopyToClipboard(val message: String) : PaymentSummaryAction()
    object ShowLoadingDialog : PaymentSummaryAction()
}
