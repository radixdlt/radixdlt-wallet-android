package com.radixdlt.android.apps.wallet.ui.layout

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.radixdlt.android.apps.wallet.R
import java.util.ArrayList

class KeyPadView : FrameLayout, View.OnClickListener {
    private var ctx: Context? = null
    private val num = ArrayList<TextView>()
    private val line = ArrayList<View>()
    var digits: String = ""
        private set
    private var textLengthLimit = 6
    var textSize = 12f
        private set
    var customTextSize = 12f
        private set
    private var textColor = Color.BLACK
    var backgroundRes = R.drawable.keypad_background
        private set
    var imageResource = R.drawable.ic_arrow_back_24
        private set
    private var customString: String? = ""
    private var fontFaceString: String? = ""
    private var decimalString: String? = ""
    var typeface: Typeface? = null
        private set
    var customTextView: TextView? = null
    var imageResourceView: ImageView? = null
        private set
    private var deleteLayout: FrameLayout? = null
    var textGetListener: OnNumberTextListener? = null
        private set

    var customButtonListener: OnCustomButtonListener? = null
        private set

    constructor(context: Context) : super(context) {
        initialise(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialise(context, attrs)
    }

    fun setOnNumberTextListener(textGetListener: OnNumberTextListener) {
        this.textGetListener = textGetListener
        setup()
    }

    fun setOnCustomButtonListener(customButtonListener: OnCustomButtonListener) {
        this.customButtonListener = customButtonListener
        setup()
    }

    fun setBackgroundRes(BackgroundResource: Int): KeyPadView {
        this.backgroundRes = BackgroundResource
        setup()
        return this@KeyPadView
    }

    fun setImageRes(ImageResource: Int): KeyPadView {
        this.imageResource = ImageResource
        setup()
        return this@KeyPadView
    }

    fun setFontFace(FontFaceString: String): KeyPadView {
        this.fontFaceString = FontFaceString
        typeface = Typeface.createFromAsset(ctx!!.assets, this.fontFaceString)
        setup()
        return this@KeyPadView
    }

    private fun initialise(context: Context, attrs: AttributeSet?) {
        this.ctx = context
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.KeyPadView, 0, 0)
        digits = attributes.getString(R.styleable.KeyPadView_keypad_digits) ?: ""
        textLengthLimit = attributes.getInt(R.styleable.KeyPadView_keypad_text_limit, 5)
        textSize = attributes.getDimension(R.styleable.KeyPadView_keypad_text_size, 12.0f)
        customTextSize = attributes.getDimension(R.styleable.KeyPadView_keypad_custom_text_size, 12.0f)
        textColor = attributes.getColor(R.styleable.KeyPadView_keypad_text_color, Color.BLACK)
        backgroundRes =
            attributes.getResourceId(
                R.styleable.KeyPadView_keypad_background_resource,
                R.drawable.keypad_background
            )
        imageResource =
            attributes.getResourceId(
                R.styleable.KeyPadView_keypad_image_resource,
                R.drawable.ic_arrow_back_24
            )
        fontFaceString = attributes.getString(R.styleable.KeyPadView_keypad_font_path)
        decimalString = attributes.getString(R.styleable.KeyPadView_keypad_decimal)
        customString = attributes.getString(R.styleable.KeyPadView_keypad_custom)

        val v = LayoutInflater.from(context).inflate(R.layout.keypad_view, this, false)
        num.add(v.findViewById(R.id.one))
        num.add(v.findViewById(R.id.two))
        num.add(v.findViewById(R.id.three))
        num.add(v.findViewById(R.id.four))
        num.add(v.findViewById(R.id.five))
        num.add(v.findViewById(R.id.six))
        num.add(v.findViewById(R.id.seven))
        num.add(v.findViewById(R.id.eight))
        num.add(v.findViewById(R.id.nine))
        num.add(v.findViewById(R.id.zero))
        customTextView = v.findViewById(R.id.custom)
        imageResourceView = v.findViewById(R.id.delete)
        deleteLayout = v.findViewById(R.id.delete_layout)

        typeface = if (fontFaceString == null) {
            Typeface.DEFAULT
        } else {
            Typeface.createFromAsset(context.assets, fontFaceString)
        }

        setup()
        addView(v)
    }

    private fun View.addCircleRipple() = with(TypedValue()) {
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            this,
            true
        )
        setBackgroundResource(resourceId)
    }

    private fun setup() {

        for (textView in num) {
            textView.setOnClickListener(this)
            textView.textSize = textSize
            textView.setTextColor(textColor)
            textView.addCircleRipple()
            textView.typeface = typeface
        }

        if (customString != null) {
            customTextView?.apply {
                visibility = View.VISIBLE
                setOnClickListener(this@KeyPadView)
                textSize = customTextSize
                setTextColor(this@KeyPadView.textColor)
                typeface = this@KeyPadView.typeface
                text = customString
                addCircleRipple()
            }
        }

        deleteLayout!!.setOnClickListener(this)
        deleteLayout!!.addCircleRipple()
        imageResourceView!!.setImageResource(imageResource)
    }

    fun clearDigits() {
        digits = ""
    }

    fun getTextLengthLimit(): Int {
        return textLengthLimit
    }

    fun setTextLengthLimit(TextLengthLimit: Int): KeyPadView {
        this.textLengthLimit = TextLengthLimit
        setup()
        return this@KeyPadView
    }

    fun setTextSize(TextSize: Int): KeyPadView {
        this.textSize = TextSize.toFloat()
        setup()
        return this@KeyPadView
    }

    fun setCustomTextSize(TextSize: Int): KeyPadView {
        this.customTextSize = TextSize.toFloat()
        setup()
        return this@KeyPadView
    }

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(TextColor: Int): KeyPadView {
        this.textColor = TextColor
        setup()
        return this@KeyPadView
    }

    override fun onClick(view: View) {
        if (view is TextView && view.tag == "custom") {
            customButtonListener?.customButtonListener()
        } else if (view is TextView && digits.length < textLengthLimit) {
            if (validateTextView(view)) return
            if (decimalString != null && digits == "0" && view.text == "0") {
                digits = view.text.toString()
            } else {
                digits += view.text
            }
            textGetListener?.numberTextListener(digits)
        } else if (view is FrameLayout && digits.isNotEmpty()) {
            digits = digits.substring(0, digits.length - 1)
            textGetListener?.numberTextListener(digits)
        }
    }

    private fun validateTextView(view: TextView): Boolean {
        if (view.text == "," && (digits.contains(",") || digits.isEmpty())) return true
        if (digits.contains(",") && digits.split(",").size > 1 && digits.split(",")[1].length == 2) return true

        if (view.text == "." && (digits.contains(".") || digits.isEmpty())) return true
        if (digits.contains(".") && digits.split(".").size > 1 && digits.split(".")[1].length == 2) return true

        if (decimalString != null && digits == "0" && view.text == "0") return true

        return false
    }
}

interface OnNumberTextListener {
    fun numberTextListener(text: String)
}

interface OnCustomButtonListener {
    fun customButtonListener()
}
