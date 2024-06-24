package com.eager2tech.beervision.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.util.AttributeSet
import android.view.View


class BoundingBoxOverlay: View {
    private var detections: DetectionsModel? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

    init {
        paint.color = Color.RED
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f); // Set border width to 1px
    }

    fun setDetections(detections: DetectionsModel?) {
        this.detections = detections
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        detections ?. let {
            canvas.drawColor(Color.TRANSPARENT)
            for (d in it.detections) {
                canvas.drawRect(d.x1, d.y1, d.x2, d.y2, paint)
            }
        }
    }
}