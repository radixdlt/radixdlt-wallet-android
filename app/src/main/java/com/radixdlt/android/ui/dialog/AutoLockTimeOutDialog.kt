package com.radixdlt.android.ui.dialog

import android.app.Activity
import com.radixdlt.android.util.QueryPreferences
import mobi.upod.timedurationpicker.TimeDurationPicker

class AutoLockTimeOutDialog : TimeDurationPickerAppCompatDialogFragment() {

    override fun getInitialDuration(): Long {
        return QueryPreferences.getPrefAutoLockTimeOut(activity!!)
    }

    override fun setTimeUnits(): Int {
        return TimeDurationPicker.HH_MM_SS
    }

    override fun onDurationSet(view: TimeDurationPicker, duration: Long) {
        QueryPreferences.setPrefAutoLockTimeOut(view.context, duration)
        sendResult(Activity.RESULT_OK)
    }

    private fun sendResult(resultCode: Int) {
        if (targetFragment == null) return

        targetFragment!!.onActivityResult(targetRequestCode, resultCode, null)
    }

    companion object {
        fun newInstance(): AutoLockTimeOutDialog {
            return AutoLockTimeOutDialog()
        }
    }
}
