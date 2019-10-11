package com.radixdlt.android.apps.wallet.ui.layout.edittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.radixdlt.android.apps.wallet.util.EmptyTextWatcher

class BackPressedEditText : TextInputEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            clearFocus()
        } else if (event.keyCode == KeyEvent.KEYCODE_SPACE) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onEditorAction(actionCode: Int) {
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            clearFocus()
        }
        super.onEditorAction(actionCode)
    }

    private fun init() {
        addTextChangedListener(object : TextWatcher by EmptyTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val textEntered = text?.toString() ?: ""

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    val replacedText = textEntered.replace(" ", "")
                    setText(replacedText)
                    setSelection(replacedText.length - 1)
                }
            }
        })
    }
}
