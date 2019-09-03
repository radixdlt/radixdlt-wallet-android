package com.radixdlt.android.apps.wallet.util

import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.helper.TextFormatHelper
import com.radixdlt.android.apps.wallet.ui.fragment.payment.status.PaymentStatusState

@BindingAdapter("visibleGone")
fun View.bindVisible(visible: Boolean?) {
    visibility = if (visible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleInvisible")
fun View.bindInvisible(visible: Boolean?) {
    visibility = if (visible == true) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("animation")
fun LottieAnimationView.bindAnimation(state: PaymentStatusState?) {
    when (state) {
        PaymentStatusState.LOADING -> {
            setAnimation(R.raw.loading)
            repeatCount = ValueAnimator.INFINITE
        }
        PaymentStatusState.SUCCESS -> {
            repeatCount = 0
            setAnimation(R.raw.success)
            playAnimation()
        }
        PaymentStatusState.FAILED -> setImageResource(R.drawable.failed)
    }
}

@BindingAdapter("statusText")
fun TextView.bindStatusText(state: PaymentStatusState?) {
    text = when (state) {
        PaymentStatusState.SUCCESS -> context.resources.getString(R.string.payment_status_dialog_transaction_completed)
        PaymentStatusState.FAILED -> context.resources.getString(R.string.payment_status_dialog_transaction_failure)
        else -> context.resources.getString(R.string.payment_status_dialog_transaction_sending)
    }
}

@BindingAdapter("colorLastChars")
fun TextView.bindColorLastChars(address: String) {
    val firstPart = address.substring(0, address.length - 7)
    val lastSeven = address.substring(address.length - 7, address.length)

    text =  TextFormatHelper.normal(
        TextFormatHelper.color(ContextCompat.getColor(context, R.color.radixBlueGrey2), firstPart),
        TextFormatHelper.color(ContextCompat.getColor(context, R.color.colorAccent), lastSeven)
    )
}
