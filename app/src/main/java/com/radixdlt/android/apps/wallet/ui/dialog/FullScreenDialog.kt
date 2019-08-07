package com.radixdlt.android.apps.wallet.ui.dialog

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.radixdlt.android.R

open class FullScreenDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }
}
