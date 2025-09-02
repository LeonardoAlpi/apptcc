package com.example.meuappfirebase

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * Esta é uma classe 'Drawable' customizada.
 * A função dela é desenhar um círculo colorido com as iniciais de um nome,
 * servindo como uma foto de perfil provisória (placeholder).
 */
class InitialsDrawable(
    private val initials: String,
    private val backgroundColor: Int
) : Drawable() {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f // Tamanho do texto
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val radius = bounds.width() / 2f

        // Desenha o círculo de fundo
        canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius, backgroundPaint)

        // Desenha o texto (iniciais) centralizado
        val yPos = bounds.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(initials, bounds.centerX().toFloat(), yPos, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
        backgroundPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
        backgroundPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}