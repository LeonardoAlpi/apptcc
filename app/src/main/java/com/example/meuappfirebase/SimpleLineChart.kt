package com.example.meuappfirebase

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class SimpleLineChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dataPoints: List<Int> = emptyList()
    private var labels: List<String> = emptyList()
    private var animatedFraction: Float = 0f

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        strokeWidth = 2f
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCFFFFFF")
        textSize = 28f
        textAlign = Paint.Align.RIGHT
    }
    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**
     * Esta é a função que está causando o erro.
     * Garantimos aqui que ela espera uma lista de Inteiros (List<Int>).
     */
    fun setData(points: List<Int>, newLabels: List<String>) {
        dataPoints = points
        labels = newLabels
        startAnimation()
    }

    private fun startAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                this@SimpleLineChart.animatedFraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val paddingLeft = 90f
        val paddingRight = 50f
        val paddingTop = 50f
        val paddingBottom = 80f
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        val maxVal = (dataPoints.maxOrNull() ?: 1).coerceAtLeast(1)

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = paddingTop + chartHeight * i / gridLines
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, paintGrid)
            val labelValue = maxVal - (maxVal * i / gridLines)
            canvas.drawText("$labelValue", paddingLeft - 20f, y + 10f, paintText)
        }

        val gapX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else 0f

        val pointsXY = dataPoints.mapIndexed { idx, valor ->
            val x = paddingLeft + gapX * idx
            val normalizedY = if (maxVal > 0) valor.toFloat() / maxVal else 0f
            val y = paddingTop + chartHeight * (1 - normalizedY * animatedFraction)
            PointF(x, y)
        }

        if (pointsXY.size < 2) return

        val linePath = Path()
        val fillPath = Path()
        fillPath.moveTo(pointsXY.first().x, height - paddingBottom)
        fillPath.lineTo(pointsXY.first().x, pointsXY.first().y)
        linePath.moveTo(pointsXY.first().x, pointsXY.first().y)

        for (i in 0 until pointsXY.size - 1) {
            val currentPoint = pointsXY[i]
            val nextPoint = pointsXY[i + 1]
            val controlPointX = (currentPoint.x + nextPoint.x) / 2
            linePath.cubicTo(controlPointX, currentPoint.y, controlPointX, nextPoint.y, nextPoint.x, nextPoint.y)
            fillPath.cubicTo(controlPointX, currentPoint.y, controlPointX, nextPoint.y, nextPoint.x, nextPoint.y)
        }

        fillPath.lineTo(pointsXY.last().x, height - paddingBottom)
        fillPath.close()

        paintFill.shader = LinearGradient(0f, paddingTop, 0f, height - paddingBottom, Color.parseColor("#40FFFFFF"), Color.parseColor("#00FFFFFF"), Shader.TileMode.CLAMP)
        canvas.drawPath(fillPath, paintFill)
        canvas.drawPath(linePath, paintLine)

        pointsXY.forEach { point ->
            canvas.drawCircle(point.x, point.y, 10f, paintCircle)
        }

        if (labels.isNotEmpty()) {
            val textPaintBottom = Paint(paintText).apply {
                textAlign = Paint.Align.CENTER
                textSize = 28f
            }
            val numLabelsToShow = if (labels.size > 7) 6 else labels.size
            if (numLabelsToShow > 1) {
                for (i in 0 until numLabelsToShow) {
                    val dataIndex = (i * (labels.size - 1f) / (numLabelsToShow - 1f)).toInt()
                    val label = labels[dataIndex]
                    val x = paddingLeft + gapX * dataIndex
                    canvas.drawText(label, x, height - paddingBottom + 40f, textPaintBottom)
                }
            }
        }
    }
}