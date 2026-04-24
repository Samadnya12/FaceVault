package com.apptest.ml1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

class FaceOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var faces: List<Face> = listOf()

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    fun setFaces(faces: List<Face>) {
        this.faces = faces
        invalidate() // redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (face in faces) {
            val bounds = face.boundingBox
            canvas.drawRect(bounds, boxPaint)
        }
    }
}