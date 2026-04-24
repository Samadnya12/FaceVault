package com.apptest.ml1

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FacePreprocessor {

    fun preprocess(faceBitmap: Bitmap): ByteBuffer {

        val size = 160
        val resized = Bitmap.createScaledBitmap(faceBitmap, size, size, true)

        val inputBuffer = ByteBuffer.allocateDirect(4 * size * size * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(size * size)
        resized.getPixels(intValues, 0, size, 0, 0, size, size)

        var pixel = 0
        for (i in 0 until size) {
            for (j in 0 until size) {
                val value = intValues[pixel++]

                inputBuffer.putFloat(((value shr 16 and 0xFF) / 255f))
                inputBuffer.putFloat(((value shr 8 and 0xFF) / 255f))
                inputBuffer.putFloat(((value and 0xFF) / 255f))
            }
        }

        return inputBuffer
    }
}