package com.radixdlt.android.apps.wallet.ui.layout

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class NonBreakingForwardSlashTextView : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val editable = editableText ?: return
        val charSequence = breakManually(editable)
        text = charSequence
    }

    private fun breakManually(text: CharSequence): CharSequence {
        val width = width - paddingLeft - paddingRight
        // Can't break with a width of 0.
        if (width == 0) return text
        val editable = SpannableStringBuilder(text)
        //creates an array with the width of each character
        val widths = FloatArray(editable.length)
        val p = paint
        p.getTextWidths(editable.toString(), widths)
        var currentWidth = 0.0f
        var position = 0
        var insertCount = 0
        val initialLength = editable.length
        while (position < initialLength) {
            currentWidth += widths[position]
            val curChar = editable[position + insertCount]
            if (curChar == '\n') {
                currentWidth = 0.0f
            } else if (currentWidth > width) {
                editable.insert(position + insertCount, "\n")
                insertCount++
                currentWidth = widths[position]
            }
            position++
        }
        return editable.toString()
    }
}
