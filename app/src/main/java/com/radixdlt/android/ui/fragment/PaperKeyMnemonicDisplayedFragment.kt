package com.radixdlt.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.radixdlt.android.R
import com.radixdlt.client.core.crypto.RadixECKeyPairs
import io.github.novacrypto.bip39.MnemonicGenerator
import io.github.novacrypto.bip39.SeedCalculator
import io.github.novacrypto.bip39.Words
import io.github.novacrypto.bip39.wordlists.English
import kotlinx.android.synthetic.main.fragment_paper_key_mnemonic_displayed.*
import java.security.SecureRandom

class PaperKeyMnemonicDisplayedFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_paper_key_mnemonic_displayed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar as Toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Paper Key"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mnemonic = generateMnemonic()
        mnemonicTextView.text = mnemonic

        val mnemonicStringArray = mnemonic.split(" ").toTypedArray()

        nextButton.setOnClickListener {
            val bundle = Bundle().also { it.putStringArray("mnemonicStringArray", mnemonicStringArray) }
            findNavController().navigate(
                    R.id.action_navigation_paper_key_mnemonic_displayed_to_navigation_paper_key_mnemonic_confirmation,
                    bundle
            )
        }
    }

    private fun generateMnemonic(): String {
        val sb = StringBuilder()
        val entropy = ByteArray(Words.TWELVE.byteLength())
        SecureRandom().nextBytes(entropy)
        MnemonicGenerator(English.INSTANCE)
                .createMnemonic(entropy) { sb.append(it) }

        return sb.toString()
    }

    fun generateSeed(mnemonic: String): ByteArray {
        return SeedCalculator().calculateSeed(mnemonic, "")
    }

    fun generatePrivateKey(seed: ByteArray): ByteArray {
        return RadixECKeyPairs
                .newInstance()
                .generateKeyPairFromSeed(seed).privateKey
    }
}
