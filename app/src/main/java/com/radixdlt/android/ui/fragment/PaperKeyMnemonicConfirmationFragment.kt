package com.radixdlt.android.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.radixdlt.android.R
import com.radixdlt.android.ui.activity.MainActivity
import com.radixdlt.android.util.EmptyTextWatcher
import kotlinx.android.synthetic.main.fragment_paper_key_mnemonic_confirmation.*
import org.jetbrains.anko.startActivity

class PaperKeyMnemonicConfirmationFragment : Fragment() {

    private var firstRandomWordVerified: Boolean = false
    private var secondRandomWordVerified: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_paper_key_mnemonic_confirmation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar as Toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Paper Key"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(
                ContextCompat.getDrawable(activity!!, R.drawable.ic_close)
        )
        setHasOptionsMenu(true)

        val mnemonicStringArray = arguments?.getStringArray("mnemonicStringArray")!!

        finishButton.setOnClickListener {
            // TODO: create private key and store it in Vault and disable auto lock for show case purposes
            if (firstRandomWordVerified && secondRandomWordVerified) {
                activity!!.finishAffinity()
                activity!!.startActivity<MainActivity>()
            }
        }

        val firstRandomWordPosition = (0..11).random()
        val secondRandomWordPosition = randomSecondNumberNotFirst(firstRandomWordPosition)

        mnemonicFirstWordEditView.hint = "Word #${firstRandomWordPosition + 1}"
        mnemonicSecondWordEditView.hint = "Word #${secondRandomWordPosition + 1}"

        mnemonicFirstWordEditView.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(firstRandomWord: Editable?) {
                firstRandomWordVerified = if (firstRandomWord.toString() == mnemonicStringArray[firstRandomWordPosition]) {
                    mnemonicFirstWordEditView.setTextColor(ContextCompat.getColor(activity!!, R.color.green))
                    true
                } else {
                    mnemonicFirstWordEditView.setTextColor(ContextCompat.getColor(activity!!, R.color.materialDarkGrey))
                    false
                }
            }
        })

        mnemonicSecondWordEditView.addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(secondRandomWord: Editable?) {
                secondRandomWordVerified = if (secondRandomWord.toString() == mnemonicStringArray[secondRandomWordPosition]) {
                    mnemonicSecondWordEditView.setTextColor(ContextCompat.getColor(activity!!, R.color.green))
                    true
                } else {
                    mnemonicSecondWordEditView.setTextColor(ContextCompat.getColor(activity!!, R.color.materialDarkGrey))
                    false
                }
            }
        })
    }

    private fun randomSecondNumberNotFirst(firstRandomWord: Int): Int {
        val secondRandomWord = (0..11).random()
        if (secondRandomWord != firstRandomWord) {
            return secondRandomWord
        }

        return randomSecondNumberNotFirst(firstRandomWord)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity!!.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
