package com.example.expensetrackerfinalfull

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var data: List<Pair<String, Double>> = emptyList()
    private val palette = intArrayOf(
        Color.parseColor("#8E24AA"),
        Color.parseColor("#3949AB"),
        Color.parseColor("#00897B"),
        Color.parseColor("#F4511E"),
        Color.parseColor("#6D4C41"),
        Color.parseColor("#2E7D32"),
    )

    fun setData(entries: List<Pair<String, Double>>) {
        data = entries
        invalidate()
    }

    fun getColors(): IntArray = palette

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return
        val total = data.sumOf { it.second }
        val size = min(width, height) * 0.8f
        val left = (width - size)/2
        val top = (height - size)/2
        rect.set(left, top, left+size, top+size)
        var start = -90f
        data.forEachIndexed { i, entry ->
            val sweep = (entry.second / total * 360f).toFloat()
            paint.color = palette[i % palette.size]
            canvas.drawArc(rect, start, sweep, true, paint)
            start += sweep
        }
    }
}

// Small round color bullet for legend
class ColorSwatchDrawable(private val color: Int) : android.graphics.drawable.ShapeDrawable() {
    init {
        paint.color = color
        intrinsicWidth = 28
        intrinsicHeight = 28
        shape = object : android.graphics.drawable.shapes.Shape() {
            override fun draw(canvas: Canvas, paint: Paint) {
                val r = min(canvas.width, canvas.height)/2f
                canvas.drawCircle(r, r, r, paint)
            }
        }
    }
}
