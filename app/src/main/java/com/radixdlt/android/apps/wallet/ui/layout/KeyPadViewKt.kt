package com.radixdlt.android.apps.wallet.ui.layout

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.radixdlt.android.R
import java.util.ArrayList

class KeyPadView : FrameLayout, View.OnClickListener, View.OnLongClickListener {
    private var ctx: Context? = null
    private val num = ArrayList<TextView>()
    private val line = ArrayList<View>()
    var digits: String = ""
        private set
    private var textLengthLimit = 6
    var textSize = 12f
        private set
    private var textColor = Color.BLACK
    var backgroundRes = R.drawable.keypad_background
        private set
    var imageResource = R.drawable.ic_backspace
        private set
    var clearImageResource = R.drawable.ic_close
        private set
    private var gridVisible = true
    private var gridBackgroundColor = Color.GRAY
    private var gridThickness = 3
    private var fontFaceString: String? = ""
    private var decimalString: String? = ""
    var typeface: Typeface? = null
        private set
    var imageResourceView: ImageView? = null
        private set
    private var clear: ImageView? = null
    private var decimal: TextView? = null
    private var deleteLayout: FrameLayout? = null
    private var clearLayout: FrameLayout? = null
    var textGetListener: OnNumberTextListener? = null
        private set

    constructor(context: Context) : super(context) {
        initialise(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialise(context, attrs)
    }

    fun setOnNumberTextListener(textGetListener: OnNumberTextListener) {
        this.textGetListener = textGetListener
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

    fun setClearImageRes(ClearImageResource: Int): KeyPadView {
        this.clearImageResource = ClearImageResource
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
        textColor = attributes.getColor(R.styleable.KeyPadView_keypad_text_color, Color.BLACK)
        backgroundRes =
            attributes.getResourceId(R.styleable.KeyPadView_keypad_background_resource, R.drawable.keypad_background)
        imageResource =
            attributes.getResourceId(R.styleable.KeyPadView_keypad_image_resource, R.drawable.ic_backspace)
        clearImageResource =
            attributes.getResourceId(R.styleable.KeyPadView_keypad_clear_image_resource, R.drawable.ic_close)
        gridVisible = attributes.getBoolean(R.styleable.KeyPadView_keypad_grid_visible, false)
        gridBackgroundColor = attributes.getColor(R.styleable.KeyPadView_keypad_grid_background_color, Color.GRAY)
        gridThickness = attributes.getDimension(R.styleable.KeyPadView_keypad_grid_line_thickness, 3f).toInt()
        fontFaceString = attributes.getString(R.styleable.KeyPadView_keypad_font_path)
        decimalString = attributes.getString(R.styleable.KeyPadView_keypad_decimal)

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
        line.add(v.findViewById(R.id.line1))
        line.add(v.findViewById(R.id.line2))
        line.add(v.findViewById(R.id.line3))
        line.add(v.findViewById(R.id.line4))
        line.add(v.findViewById(R.id.line5))
        line.add(v.findViewById(R.id.line6))
        line.add(v.findViewById(R.id.line7))
        line.add(v.findViewById(R.id.line8))
        line.add(v.findViewById(R.id.line9))
        line.add(v.findViewById(R.id.line10))
        line.add(v.findViewById(R.id.line11))
        line.add(v.findViewById(R.id.line0))
        line.add(v.findViewById(R.id.line00))
        line.add(v.findViewById(R.id.line000))
        imageResourceView = v.findViewById(R.id.delete)
        deleteLayout = v.findViewById(R.id.delete_layout)
        clearLayout = v.findViewById(R.id.clear_layout)
        clear = v.findViewById(R.id.clear)
        decimal = v.findViewById(R.id.comma)

        typeface = if (fontFaceString == null) {
            Typeface.DEFAULT
        } else {
            Typeface.createFromAsset(context.assets, fontFaceString)
        }
        setup()
        addView(v)
    }

    private fun View.addCircleRipple() = with(TypedValue()) {
        context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
        setBackgroundResource(resourceId)
    }

    private fun setup() {

        for (textView in num) {
            textView.setOnClickListener(this)
            textView.textSize = textSize
            textView.setTextColor(textColor)
//            textView.setBackgroundResource(backgroundRes)
            textView.addCircleRipple()
            textView.typeface = typeface
        }

        if (gridVisible) {
            for (view in line) {
                view.visibility = View.VISIBLE
                view.setBackgroundColor(gridBackgroundColor)
            }
            line[0].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[1].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[2].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[3].layoutParams = LinearLayout.LayoutParams(gridThickness, LayoutParams.MATCH_PARENT)
            line[4].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[5].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[6].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[7].layoutParams = LinearLayout.LayoutParams(gridThickness, LayoutParams.MATCH_PARENT)
            line[8].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[9].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[10].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)

            line[11].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[12].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
            line[13].layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, gridThickness)
        }

        deleteLayout!!.setOnClickListener(this)
        deleteLayout!!.setOnLongClickListener(this)
//        deleteLayout!!.setBackgroundResource(backgroundRes)
        deleteLayout!!.addCircleRipple()
        imageResourceView!!.setImageResource(imageResource)

//        clearLayout!!.setOnClickListener(this)
//        clearLayout!!.setBackgroundResource(backgroundRes)
//        clear!!.setImageResource(clearImageResource)

        if (decimalString != null) {
            clearLayout!!.visibility = View.GONE
            decimal!!.visibility = View.VISIBLE
            decimal!!.text = decimalString
            decimal!!.setOnClickListener(this)
            decimal!!.textSize = textSize
            decimal!!.setTextColor(textColor)
            decimal!!.setBackgroundResource(backgroundRes)
            decimal!!.typeface = typeface
        }
//        else {
//            decimal!!.visibility = View.GONE
//            clearLayout!!.visibility = View.VISIBLE
//            clearLayout!!.setOnClickListener(this)
//            clearLayout!!.setBackgroundResource(backgroundRes)
//            clear!!.setImageResource(clearImageResource)
//        }
        else {
            decimal!!.visibility = View.GONE
            clearLayout!!.visibility = View.VISIBLE
            clear!!.visibility = View.GONE
        }
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

    fun getTextColor(): Int {
        return textColor
    }

    fun setTextColor(TextColor: Int): KeyPadView {
        this.textColor = TextColor
        setup()
        return this@KeyPadView
    }

    fun isGridVisible(): Boolean {
        return gridVisible
    }

    fun setGridVisible(GridVisible: Boolean): KeyPadView {
        this.gridVisible = GridVisible
        setup()
        return this@KeyPadView
    }

    fun getGridBackgroundColor(): Int {
        return gridBackgroundColor
    }

    fun setGridBackgroundColor(GridBackgroundColor: Int): KeyPadView {
        this.gridBackgroundColor = GridBackgroundColor
        setup()
        return this@KeyPadView
    }

    fun getGridThickness(): Int {
        return gridThickness
    }

    fun setGridThickness(GridThickness: Int): KeyPadView {
        this.gridThickness = GridThickness
        setup()
        return this@KeyPadView
    }

    override fun onClick(view: View) {

        if (view is TextView && digits.length < textLengthLimit) {
            if (validateTextView(view)) return
            if (decimalString != null && digits == "0" && view.text == "0") {
                digits = view.text.toString()
            } else {
                digits += view.text
            }
        } else if (view is FrameLayout && digits.isNotEmpty()) {
            digits = if (view.getTag() != null && view.getTag().toString() == "clear") {
                ""
            } else {
                digits.substring(0, digits.length - 1)
            }
        }
        textGetListener?.let {
            it.numberTextListener(digits)
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (decimalString != null) {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            digits = ""
            textGetListener?.let {
                it.numberTextListener(digits)
            }
        }
        return false
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

//typealias TextGetListener = (text: String, digitsRemaining: Int) -> Unit

interface OnNumberTextListener {
    fun numberTextListener(text: String)
}
