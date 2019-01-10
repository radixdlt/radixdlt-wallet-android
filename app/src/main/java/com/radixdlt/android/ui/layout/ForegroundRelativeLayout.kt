package com.radixdlt.android.ui.layout

/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.radixdlt.android.R

class ForegroundRelativeLayout : RelativeLayout {

    private var mForegroundSelector: Drawable? = null
    private var mRectPadding: Rect? = null
    private var mUseBackgroundPadding = false

    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0)
        : super(context, attrs, defStyle) {

        val a =
            context.obtainStyledAttributes(attrs, R.styleable.ForegroundRelativeLayout, defStyle, 0)

        val d = a.getDrawable(R.styleable.ForegroundRelativeLayout_android_foreground)
        if (d != null) {
            foreground = d
        }

        a.recycle()

        if (this.background is NinePatchDrawable) {
            val npd = this.background as NinePatchDrawable
            mRectPadding = Rect()
            if (npd.getPadding(mRectPadding!!)) {
                mUseBackgroundPadding = true
            }
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (mForegroundSelector != null && mForegroundSelector!!.isStateful) {
            mForegroundSelector!!.state = drawableState
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (mForegroundSelector != null) {
            if (mUseBackgroundPadding) {
                mForegroundSelector!!.setBounds(
                    mRectPadding!!.left, mRectPadding!!.top, w - mRectPadding!!.right,
                    h - mRectPadding!!.bottom
                )
            } else {
                mForegroundSelector!!.setBounds(0, 0, w, h)
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (mForegroundSelector != null) {
            mForegroundSelector!!.draw(canvas)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mForegroundSelector
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (mForegroundSelector != null) mForegroundSelector!!.jumpToCurrentState()
    }

    override fun setForeground(drawable: Drawable?) {
        if (mForegroundSelector !== drawable) {
            if (mForegroundSelector != null) {
                mForegroundSelector!!.callback = null
                unscheduleDrawable(mForegroundSelector)
            }

            mForegroundSelector = drawable

            if (drawable != null) {
                setWillNotDraw(false)
                drawable.callback = this
                if (drawable.isStateful) {
                    drawable.state = drawableState
                }
            } else {
                setWillNotDraw(true)
            }
            requestLayout()
            invalidate()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        if (mForegroundSelector != null) {
            mForegroundSelector!!.setHotspot(x, y)
        }
    }
}
