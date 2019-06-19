package com.radixdlt.android.apps.wallet.ui.layout

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.DrawableCompat
import com.radixdlt.android.R
import java.util.ArrayList
import java.util.StringTokenizer

class CenterIconButton : AppCompatButton {

    private var textBoundsRect: Rect? = null
    @ColorInt
    private var tintColor = Color.TRANSPARENT
    private var mLeftPadding: Int = 0
    private var mRightPadding: Int = 0

    private val textWidth: Int
        get() {
            if (textBoundsRect == null) {
                textBoundsRect = Rect()
            }
            val paint = paint
            val text = divideText()
            paint.getTextBounds(text, 0, text.length, textBoundsRect)
            return textBoundsRect!!.width()
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CenterIconButton)
            tintColor =
                typedArray.getColor(R.styleable.CenterIconButton_drawableTint, Color.TRANSPARENT)
            val defaultDrawablePadding =
                resources.getDimension(R.dimen.ceter_icon_default_drawable_padding)
            val drawablePadding = typedArray.getDimension(
                R.styleable.CenterIconButton_android_drawablePadding,
                defaultDrawablePadding
            ).toInt()
            compoundDrawablePadding = drawablePadding

            updateTint()
            typedArray.recycle()
        }
        mLeftPadding = paddingLeft
        mRightPadding = paddingRight
    }

    private fun updateTint() {
        if (tintColor != Color.TRANSPARENT) {
            val drawables = compoundDrawables
            if (drawables.size != DRAWABLES_LENGTH) return

            val wrappedDrawables = arrayOfNulls<Drawable>(DRAWABLES_LENGTH)
            for (i in 0 until DRAWABLES_LENGTH) {
                val drawable = drawables[i]
                if (drawable != null) {
                    val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
                    DrawableCompat.setTint(wrappedDrawable, tintColor)
                    wrappedDrawables[i] = wrappedDrawable
                }
            }
            setCompoundDrawablesWithIntrinsicBounds(
                wrappedDrawables[DRAWABLE_LEFT_POSITION],
                wrappedDrawables[DRAWABLE_TOP_POSITION],
                wrappedDrawables[DRAWABLE_RIGHT_POSITION],
                wrappedDrawables[DRAWABLE_BOTTOM_POSITION]
            )
        }
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
        @DrawableRes left: Int,
        @DrawableRes top: Int,
        @DrawableRes right: Int,
        @DrawableRes bottom: Int
    ) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        updatePadding()
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
        updatePadding()
    }

    override fun setText(text: CharSequence, type: TextView.BufferType) {
        super.setText(text, type)
        updatePadding()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePadding(w)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        mLeftPadding = left
        mRightPadding = right
        updatePadding()
    }

    private fun updatePadding(width: Int = measuredWidth) {
        if (width == 0) return

        val compoundDrawables = compoundDrawables
        if (compoundDrawables.isEmpty() || compoundDrawables.size != DRAWABLES_LENGTH) return

        val leftDrawable = compoundDrawables[DRAWABLE_LEFT_POSITION]
        val rightDrawable = compoundDrawables[DRAWABLE_RIGHT_POSITION]
        if (leftDrawable == null && rightDrawable == null) return

        val textWidth = textWidth
        val iconPadding = Math.max(compoundDrawablePadding, 1)
        val paddingSize: Int

        paddingSize = if (leftDrawable != null && rightDrawable != null) {
            (width - leftDrawable.intrinsicWidth -
                rightDrawable.intrinsicWidth - textWidth - iconPadding * 4) / 2
        } else if (leftDrawable != null) {
            (width - leftDrawable.intrinsicWidth - iconPadding * 2 - textWidth) / 2
        } else {
            (width - rightDrawable!!.intrinsicWidth - iconPadding * 2 - textWidth) / 2
        }

        super.setPadding(
            Math.max(mLeftPadding, paddingSize),
            paddingTop,
            Math.max(paddingSize, mRightPadding),
            paddingBottom
        )
    }

    private fun divideText(): String {
        val text = text.toString()
        if (text.isEmpty()) {
            return ""
        }
        val list = ArrayList<String>()
        val tokenizer = StringTokenizer(text,
            DELIMITERS, false)
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken())
        }
        if (list.size == 1) {
            return if (isAllCaps) list[0].toUpperCase() else list[0]
        }
        var longPart = list[0]
        for (i in 0 until list.size - 1) {
            if (list[i + 1].length > list[i].length) {
                longPart = list[i + 1]
            }
        }

        return if (isAllCaps) longPart.toUpperCase() else longPart
    }

    override fun isAllCaps(): Boolean {
        val method = transformationMethod ?: return false

        return method.javaClass.simpleName == "AllCapsTransformationMethod"
    }

    companion object {
        private const val DELIMITERS = "\n"
        private const val DRAWABLE_LEFT_POSITION = 0
        private const val DRAWABLE_TOP_POSITION = 1
        private const val DRAWABLE_RIGHT_POSITION = 2
        private const val DRAWABLE_BOTTOM_POSITION = 3
        private const val DRAWABLES_LENGTH = 4
    }
}
