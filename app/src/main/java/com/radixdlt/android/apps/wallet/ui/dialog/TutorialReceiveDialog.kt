package com.radixdlt.android.apps.wallet.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.radixdlt.android.R
import com.radixdlt.android.apps.wallet.util.QueryPreferences
import kotlinx.android.synthetic.main.dialog_tutorial_receive.*

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
        QueryPreferences.setPrefTutorialReceiveShown(view.context, true)
        toolbarDialog.setNavigationIcon(R.drawable.ic_close)
        toolbarDialog.setNavigationContentDescription(R.string.tutorial_receive_dialog_content_description_close_button)
        toolbarDialog.setNavigationOnClickListener { dismiss() }
        setReceiveButtonOnClickListener()
    }

    private fun setReceiveButtonOnClickListener() {
        receiveButton.setOnClickListener {
            val receiveRadixDialog = ReceiveRadixDialog.newInstance()
            fragmentManager?.apply {
                receiveRadixDialog.show(this, null)
            }
            dismiss()
        }
    }
}
