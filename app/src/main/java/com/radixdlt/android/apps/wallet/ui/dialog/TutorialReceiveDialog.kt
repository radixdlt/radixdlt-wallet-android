package com.radixdlt.android.apps.wallet.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.radixdlt.android.apps.wallet.R
import com.radixdlt.android.apps.wallet.ui.receive.ReceiveActivity
import com.radixdlt.android.apps.wallet.util.Pref
import com.radixdlt.android.apps.wallet.util.Pref.defaultPrefs
import com.radixdlt.android.apps.wallet.util.Pref.set
import kotlinx.android.synthetic.main.dialog_tutorial_receive.*
import org.jetbrains.anko.startActivity

class TutorialReceiveDialog : FullScreenDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_tutorial_receive, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply { defaultPrefs()[Pref.TUTORIAL_RECEIVE] = true }
        toolbarDialog.setNavigationOnClickListener { dismiss() }
        setReceiveButtonOnClickListener()
    }

    private fun setReceiveButtonOnClickListener() {
        receiveButton.setOnClickListener {
            dismiss()
            activity?.startActivity<ReceiveActivity>()
        }
    }
}
