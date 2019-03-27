package com.radixdlt.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.radixdlt.android.R
import com.radixdlt.android.ui.activity.MainActivity
import kotlinx.android.synthetic.main.fragment_paper_key_mnemonic_confirmation.*
import org.jetbrains.anko.startActivity

class PaperKeyMnemonicConfirmationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_paper_key_mnemonic_confirmation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finishButton.setOnClickListener {
            activity!!.finishAffinity()
            activity!!.startActivity<MainActivity>()
        }
    }
}
