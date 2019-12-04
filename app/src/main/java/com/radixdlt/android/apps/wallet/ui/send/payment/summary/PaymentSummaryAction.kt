package com.radixdlt.android.apps.wallet.ui.send.payment.summary

sealed class PaymentSummaryAction {
    class CopyToClipboard(val message: String) : PaymentSummaryAction()
    object Authenticate : PaymentSummaryAction()
}
