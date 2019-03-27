package com.radixdlt.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import kotlinx.android.synthetic.main.fragment_paper_key_welcome.*

class PaperKeyWelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_paper_key_welcome, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nextButton.setOnClickListener {
            findNavController().navigate(
                    R.id.action_navigation_paper_key_welcome_to_navigation_paper_key_mnemonic_displayed
            )
        }
    }
}
