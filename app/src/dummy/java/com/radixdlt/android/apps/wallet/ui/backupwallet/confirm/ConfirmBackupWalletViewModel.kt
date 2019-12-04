package com.radixdlt.android.apps.wallet.ui.backupwallet.confirm

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputLayout

class ConfirmBackupWalletViewModel : ViewModel() {

    private val _confirmBackupWalletAction = MutableLiveData<ConfirmBackupWalletAction>()
    val confirmBackupWalletAction: LiveData<ConfirmBackupWalletAction>
        get() = _confirmBackupWalletAction

    private val _pastedMnemonic = MutableLiveData<Array<String>>()
    val pastedMnemonic: LiveData<Array<String>>
        get() = _pastedMnemonic

    private val _shuffledMnemonic = MutableLiveData<MutableList<String>>()
    val shuffledMnemonic: LiveData<MutableList<String>>
        get() = _shuffledMnemonic

    private val _undoLastWord = MutableLiveData<String>()
    val undoLastWord: LiveData<String>
        get() = _undoLastWord

    private lateinit var mnemonic: String

    init {
        _pastedMnemonic.value = emptyArray()
        _undoLastWord.value = ""
    }

    fun showMnemonic(args: ConfirmBackupWalletFragmentArgs) {
        mnemonic = args.mnemonic
        // Note: We do not shuffle in the dummy flavor due to Espresson no
        // scrolling correctly and views not being visible cannot be clicked
        _shuffledMnemonic.value = mnemonic
            .split(" ")
            .toMutableList()
    }

    private fun buildMnemonicFromFields(layout: ConstraintLayout): String {
        var mnemonic = ""
        layout.children.forEach {
            if (it is TextInputLayout && !it.editText?.text.isNullOrEmpty()) {
                it.editText?.apply {
                    mnemonic += "$text"
                    if (tag != "12") {
                        mnemonic += " "
                    }
                }
            }
        }

        return mnemonic
    }

    fun undoButtonClick(layout: ConstraintLayout) {
        // Check first editText preventing it from throwing an error
        if (!(layout.getChildAt(3) as TextInputLayout).editText?.text.isNullOrEmpty()) {
            _undoLastWord.value = layout.children.last {
                it is TextInputLayout && !it.editText?.text.isNullOrEmpty()
            }.let {
                (it as TextInputLayout).editText?.text.toString()
            }
        }
    }

    fun confirmMnemonicBackupButtonClick(layout: ConstraintLayout) {
        val builtMnemonic = buildMnemonicFromFields(layout)
        if (mnemonic == builtMnemonic) {
            navigate()
        } else {
            showMnemonicError()
        }
    }

    private fun navigate() {
        _confirmBackupWalletAction.value = ConfirmBackupWalletAction.Navigate
    }

    private fun showMnemonicError() {
        _confirmBackupWalletAction.value = ConfirmBackupWalletAction.ShowMnemonicError
    }
}
