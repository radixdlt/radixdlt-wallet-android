package com.radixdlt.android.apps.wallet.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.radixdlt.android.apps.wallet.R
import org.jetbrains.anko.longToast
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

fun Fragment.longToast(@StringRes message: Int) = view?.let { activity?.longToast(message) }

fun Fragment.longToast(message: CharSequence) = view?.let { activity?.longToast(message) }

fun Fragment.getStatusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Fragment.getNavigationBarHeight(): Int {
    val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0 && !hasMenuKey) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

fun Snackbar.config(context: Context) {
    val params = this.view.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(12, 12, 12, 12)
    this.view.layoutParams = params

    this.view.background = context.getDrawable(R.drawable.bg_snackbar_error)

    ViewCompat.setElevation(this.view, 6f)
}

fun Fragment.showErrorSnackbarAboveNavigationView(@StringRes text: Int) {
    view?.let {
        val sb = Snackbar.make(
            it,
            it.context.getString(text),
            Snackbar.LENGTH_SHORT
        )

        sb.view.background = it.context.getDrawable(R.drawable.bg_snackbar_error)
        sb.anchorView = it.rootView.findViewById(R.id.navigation) as BottomNavigationView
        sb.show()
    }
}

fun Fragment.showErrorSnackbar(@StringRes text: Int) {
    view?.let {
        val sb = Snackbar.make(
            it,
            it.context.getString(text),
            Snackbar.LENGTH_SHORT
        )
        sb.view.background = it.context.getDrawable(R.drawable.bg_snackbar_error)
        sb.show()
    }
}

fun Fragment.showSnackbarAboveNavigationView(@StringRes text: Int) {
    view?.let {
        val sb = Snackbar.make(
            it,
            it.context.getString(text),
            Snackbar.LENGTH_SHORT
        )
        sb.anchorView = it.rootView.findViewById(R.id.navigation) as BottomNavigationView
        sb.show()
    }
}

fun Fragment.showSnackbarAboveNavigationView(text: String) {
    view?.let {
        val sb = Snackbar.make(
            it,
            text,
            Snackbar.LENGTH_SHORT
        )
        sb.anchorView = it.rootView.findViewById(R.id.navigation) as BottomNavigationView
        sb.show()
    }
}

fun Activity.showSuccessSnackbarAboveNavigationView(@StringRes text: Int) {
    val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
    val sb = Snackbar.make(
        bottomNavigationView,
        getString(text),
        Snackbar.LENGTH_SHORT
    )
    sb.view.background = getDrawable(R.drawable.bg_snackbar_success)
    sb.anchorView = bottomNavigationView
    sb.show()
}


fun Fragment.showSuccessSnackbarAboveNavigationView(@StringRes text: Int) {
    view?.let {
        val sb = Snackbar.make(
            it,
            it.context.getString(text),
            Snackbar.LENGTH_SHORT
        )
        sb.view.background = it.context.getDrawable(R.drawable.bg_snackbar_success)
        sb.anchorView = it.rootView.findViewById(R.id.navigation) as BottomNavigationView
        sb.show()
    }
}

fun Fragment.initialiseToolbar(toolbar: View, @StringRes text: Int) {
    view?.let {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar as Toolbar)
        setToolbarTitleAndUpEnabled(text)
        (activity as? AppCompatActivity)?.supportActionBar
            ?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }
}

fun Fragment.initialiseToolbar(@StringRes text: Int, enabled: Boolean = true) {
    view?.let {
        setToolbarTitleAndUpEnabled(text, enabled)
    }
}

private fun Fragment.setToolbarTitleAndUpEnabled(text: Int, enabled: Boolean = true) {
    (activity as? AppCompatActivity)?.supportActionBar?.title = getString(text)
    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
}

fun View.changeTILBackgroundColourToPurple(view: TextInputLayout, tagName: String) {
    view.boxBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary)
    view.editText?.setTextColor(ContextCompat.getColor(context, R.color.white))
    // below is necessary due to text not changing colour
    view.editText?.setText("")
    view.hint = ""
    view.editText?.setText(tagName)
}
