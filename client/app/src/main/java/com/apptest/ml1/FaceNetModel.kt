package com.apptest.ml1

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.io.FileInputStream

class FaceNetModel(context: Context) {

    private var interpreter: Interpreter

    init {
        // ✅ Load model properly from assets using FileChannel (IMPORTANT FIX)
        val fileDescriptor = context.assets.openFd("facenet.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        val modelBuffer: ByteBuffer = fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )

        modelBuffer.order(ByteOrder.nativeOrder())

        // ✅ Add interpreter options
        val options = Interpreter.Options()
        options.setNumThreads(4)

        interpreter = Interpreter(modelBuffer, options)

        // 🔥 CRITICAL FIX (your error fix)
        interpreter.allocateTensors()
    }

    fun getEmbedding(input: ByteBuffer): FloatArray {

        val output = Array(1) { FloatArray(192) }

//        // Optional safety check
//        if (interpreter == null) {
//            throw IllegalStateException("Interpreter not initialized")
//        }
        input.rewind() // VERY IMPORTANT

        interpreter.run(input, output)

        return output[0]
    }

    fun close() {
        interpreter.close()
    }
}