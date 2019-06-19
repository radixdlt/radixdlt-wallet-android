package com.radixdlt.android.apps.wallet.ui.dialog

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerDialog

/**
 * Base class for implementing a time duration picker dialog fragment as described in the
 * [Pickers](https://developer.android.com/guide/topics/ui/controls/pickers.html) guide in the
 * android documentation.
 *
 * You need to implement #onDurationSet in your derived class. You can override #getInitialDuration if you want to
 * provide an initial duration to be set when the dialog starts.
 *
 * @see TimeDurationPickerDialog
 *
 * @see TimeDurationPicker
 */
abstract class TimeDurationPickerAppCompatDialogFragment : AppCompatDialogFragment(),
    TimeDurationPickerDialog.OnDurationSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        return TimeDurationPickerDialog(activity, this, getInitialDuration(), setTimeUnits())
    }

    /**
     * The duration to be shown as default value when the dialog appears.
     * @return the default duration in milliseconds.
     */
    protected open fun getInitialDuration(): Long {
        return 0
    }

    protected open fun setTimeUnits(): Int {
        return TimeDurationPicker.HH_MM_SS
    }
}
