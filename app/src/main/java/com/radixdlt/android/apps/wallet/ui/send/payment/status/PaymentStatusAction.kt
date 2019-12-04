package com.radixdlt.android.apps.wallet.ui.send.payment.status

sealed class PaymentStatusAction {
    object ClosePaymentAction : PaymentStatusAction()
    object TryPaymentAgainAction : PaymentStatusAction()
    object OpenExplorerAction : PaymentStatusAction()
}
