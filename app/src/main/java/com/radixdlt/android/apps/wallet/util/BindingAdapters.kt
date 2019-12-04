package com.radixdlt.android.apps.wallet.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.biometrics.BiometricsAuthenticationResult
import com.radixdlt.android.apps.wallet.helper.TextFormatHelper
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.change.ChangePinAuthenticationViewModel
import com.radixdlt.android.apps.wallet.ui.dialog.authentication.pin.setup.SetupPinAuthenticationViewModel
import com.radixdlt.android.apps.wallet.ui.launch.pin.LaunchPinViewModel
import com.radixdlt.android.apps.wallet.ui.send.payment.pin.PaymentPinViewModel
import com.radixdlt.android.apps.wallet.ui.send.payment.status.PaymentStatusState
import com.radixdlt.android.apps.wallet.ui.main.settings.pin.AuthenticatePinViewModel
import com.radixdlt.android.apps.wallet.ui.layout.KeyPadView
import org.jetbrains.anko.px2dip

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

    text = TextFormatHelper.normal(
        TextFormatHelper.color(ContextCompat.getColor(context, R.color.radixBlueGrey2), firstPart),
        TextFormatHelper.color(ContextCompat.getColor(context, R.color.colorAccent), lastSeven)
    )
}

@BindingAdapter("focusBehaviour", "hint")
fun TextInputLayout.bindFocusBehaviour(editText: EditText, hint: String) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            if (!editText.text.isNullOrEmpty()) {
                boxBackgroundColor = ContextCompat.getColor(context, android.R.color.transparent)
                editText.setTextColor(ContextCompat.getColor(context, R.color.materialGrey900))
                this.hint = hint
            }
        } else {
            if (!editText.text.isNullOrEmpty()) {
                val text = editText.text.toString()
                changeTILBackgroundColourToPurple(this, text)
            }
        }
    }
}

@BindingAdapter("mnemonicWord")
fun EditText.bindMnemonicWord(mnemonic: Array<String>) {
    if (mnemonic.isEmpty()) return
    requestFocus()
    setText(mnemonic[tag.toString().toInt() - 1])
    clearFocus()

    hideKeyboard(this)
}

@BindingAdapter("mnemonicList", "chosenMnemonicWord")
fun ChipGroup.bindAddChip(mnemonicList: MutableList<String>, layout: ConstraintLayout) {
    for (index in mnemonicList.indices) {
        val tagName = mnemonicList[index]
        val chip = Chip(context)
        val paddingDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f,
            context.resources.displayMetrics
        ).toInt()
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
        chip.text = tagName

        chip.setOnClickListener {
            for (view in layout.children) {
                if (view is TextInputLayout && view.editText?.text.isNullOrEmpty()) {
                    changeTILBackgroundColourToPurple(view, tagName)
                    break
                }
            }
            removeView(chip)
        }

        addView(chip)
    }
}

@Suppress("unused")
@BindingAdapter("undoLastWord", "layout", "chipGroup")
fun AppCompatImageButton.bindUndo(
    undoLastWord: String,
    layout: ConstraintLayout,
    chipGroup: ChipGroup
) {
    if ((layout.getChildAt(3) as TextInputLayout).editText?.text.isNullOrEmpty()) {
        return
    }

    layout.children.last {
        it is TextInputLayout && !it.editText?.text.isNullOrEmpty()
    }.also {
        val til = it as TextInputLayout
        addChip(layout, chipGroup, undoLastWord)
        resetTIlBackgroundColour(til, it)
    }
}

private fun resetTIlBackgroundColour(til: TextInputLayout, it: TextInputLayout) {
    til.editText?.setText("")
    til.boxBackgroundColor = ContextCompat.getColor(it.context, android.R.color.transparent)
    til.editText?.setTextColor(ContextCompat.getColor(it.context, R.color.materialGrey900))
    til.hint = til.editText?.tag.toString()
}

private fun addChip(layout: ConstraintLayout, chipGroup: ChipGroup, mnemonicWord: String) {
    val ctx = layout.context
    val chip = Chip(ctx)
    val paddingDp = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        10f,
        layout.context.resources.displayMetrics
    ).toInt()
    chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp)
    chip.text = mnemonicWord
    chip.setOnClickListener {
        for (view in layout.children) {
            if (view is TextInputLayout && view.editText?.text.isNullOrEmpty()) {
                it.changeTILBackgroundColourToPurple(view, mnemonicWord)
                break
            }
        }
        chipGroup.removeView(chip)
    }

    chipGroup.addView(chip)
}

@BindingAdapter("pinSetupState")
fun TextView.bindPinSetupState(state: SetupPinAuthenticationViewModel.SetupPinState?) {
    text = when (state) {
        SetupPinAuthenticationViewModel.SetupPinState.SET -> {
            context.getString(R.string.setup_pin_dialog_set_pin_header)
        }
        SetupPinAuthenticationViewModel.SetupPinState.CONFIRM -> {
            context.getString(R.string.setup_pin_dialog_confirm_pin_header)
        }
        else -> return
    }
}

@BindingAdapter("pinChangeState")
fun TextView.bindPinSetupState(state: ChangePinAuthenticationViewModel.ChangePinState?) {
    text = when (state) {
        ChangePinAuthenticationViewModel.ChangePinState.CURRENT -> {
            context.getString(R.string.change_pin_dialog_enter_current_pin_header)
        }
        ChangePinAuthenticationViewModel.ChangePinState.NEW -> {
            context.getString(R.string.change_pin_dialog_new_pin_header)
        }
        ChangePinAuthenticationViewModel.ChangePinState.CONFIRM -> {
            context.getString(R.string.change_pin_dialog_confirm_pin_header)
        }
        else -> return
    }
}

@BindingAdapter("pinClear")
fun KeyPadView.bindPinClear(pinLength: Int) {
    if (pinLength == 0) clearDigits()
}

@BindingAdapter("pinCheck")
fun CheckBox.bindPinCheck(pinLength: Int) {
    val tagInt = (tag as String).toInt()
    isChecked = when (pinLength) {
        1 -> tagInt == 1
        2 -> tagInt in 1..2
        3 -> tagInt in 1..3
        4 -> tagInt in 1..4
        else -> false
    }
}

@BindingAdapter("pinError")
fun LinearLayout.bindPinError(state: SetupPinAuthenticationViewModel.SetupPinState?) {
    if (state == SetupPinAuthenticationViewModel.SetupPinState.ERROR) {
        shakeAnimation()
        vibrate(context)
    }
}

@BindingAdapter("pinError")
fun LinearLayout.bindPinError(state: ChangePinAuthenticationViewModel.ChangePinState?) {
    if (state == ChangePinAuthenticationViewModel.ChangePinState.ERROR) {
        shakeAnimation()
        vibrate(context)
    }
}

@BindingAdapter("pinError")
fun LinearLayout.bindPinError(state: PaymentPinViewModel.PaymentPinState?) {
    if (state == PaymentPinViewModel.PaymentPinState.ERROR) {
        shakeAnimation()
        vibrate(context)
    }
}

@BindingAdapter("pinError")
fun LinearLayout.bindPinError(state: LaunchPinViewModel.LaunchPinState?) {
    if (state == LaunchPinViewModel.LaunchPinState.ERROR) {
        shakeAnimation()
        vibrate(context)
    }
}

@BindingAdapter("pinError")
fun LinearLayout.bindPinError(state: AuthenticatePinViewModel.AuthenticatePinState) {
    if (state == AuthenticatePinViewModel.AuthenticatePinState.ERROR) {
        shakeAnimation()
        vibrate(context)
    }
}

private fun LinearLayout.shakeAnimation() {
    val values = arrayOf(0, 200, -200, 200, -200, 120, -120, 48, -48, 0)
        .map {
            context.px2dip(it)
        }
        .toFloatArray()
    val oa1 = ObjectAnimator
        .ofFloat(this, "translationX", *values)
        .setDuration(600)
    oa1.start()
}

private fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(400)
    }
}

@BindingAdapter("biometricsResult")
fun TextView.bindBiometricsResult(result: BiometricsAuthenticationResult?) {
    text = when (result) {
        BiometricsAuthenticationResult.Success -> ""
        BiometricsAuthenticationResult.Failure -> {
            context.getString(R.string.payment_biometrics_dialog_unable_to_authenticate)
        }
        is BiometricsAuthenticationResult.Error -> when (result.code) {
            BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT,
            BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT -> {
                context.getString(R.string.payment_biometrics_dialog_too_many_attempts)
            }
            else -> ""
        }
        else -> ""
    }
}
