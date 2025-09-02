package com.apol.myapplication

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable

class TextDrawable(context: Context, private val text: String) : Drawable() {
    private val paint = Paint()

    init {
        paint.color = Color.WHITE
        paint.textSize = 64f
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val x = bounds.centerX().toFloat()
        val y = bounds.centerY() - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(text, x, y, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}