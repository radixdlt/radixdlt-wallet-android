package com.radixdlt.android.apps.wallet.util

import android.os.Build
import android.view.View
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import org.jetbrains.anko.toast

fun Number?.getOrdinal(): String? {
    if (this == null) {
        return null
    }

    val format = "{0,ordinal}"

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        android.icu.text.MessageFormat.format(format, this)
    } else {
        com.ibm.icu.text.MessageFormat.format(format, this)
    }
}

fun View.doOnLayout(onLayout: (View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (onLayout(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}

/**
 * Extension method to set View's margin.
 */
fun View.setConstraintLayoutMargin(left: Int, top: Int, right: Int, bottom: Int) {
    val lp = layoutParams
    lp?.let {
        (lp as ConstraintLayout.LayoutParams).setMargins(left, top, right, bottom)
        layoutParams = lp
    }
}

fun Fragment.toast(@StringRes message: Int) = view?.let { activity?.toast(message) }

fun Fragment.toast(message: CharSequence) = view?.let { activity?.toast(message) }
