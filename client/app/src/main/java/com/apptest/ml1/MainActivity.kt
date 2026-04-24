package com.apptest.ml1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Button
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
import android.view.View
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    val detector = FaceDetection.getClient()

    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var faceOverlay: FaceOverlay

    private lateinit var faceNetModel: FaceNetModel

    private var isProcessing = false
    private lateinit var etName: TextInputEditText
    private var isCameraStarted = false
    private val CAMERA_REQUEST_CODE = 100
    private lateinit var cameraProvider: ProcessCameraProvider
//    var detectedFaceEmbedding: FloatArray? = null

    var registeredEmbedding: FloatArray? = null
    var currentEmbedding: FloatArray? = null

    var isLoginMode = false
    var captureNextFrame = false
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    private lateinit var nameInputLayout: View // Add this for the layout container


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        registerButton = findViewById<Button>(R.id.btnRegister)
        loginButton = findViewById<Button>(R.id.btnLogin)


        registerButton.setOnClickListener {
//            isLoginMode = false
//            captureNextFrame = true
            val name = etName.text.toString().trim()
            if(name.isNotEmpty()){
                startScanning("REGISTER")
            }else{
                etName.error = "We need to know you ... Please enter your name !"
            }
            Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show()
        }

        loginButton.setOnClickListener {
//            isLoginMode = true
//            captureNextFrame = true
            val name = etName.text.toString().trim()
            if(name.isNotEmpty()){
                startScanning("LOGIN")
            }else{
                etName.error= "We need to know you ... Please enter your name !"
            }
        }


        viewFinder = findViewById(R.id.viewFinder)

        cameraExecutor = Executors.newSingleThreadExecutor()

        faceOverlay = findViewById(R.id.faceOverlay)

        faceNetModel = FaceNetModel(this)

        checkCameraPermission()


    }
    private fun startScanning(mode: String) {
        isLoginMode = (mode == "LOGIN")
        captureNextFrame = true // Get ready to grab a face

        // Show the camera UI
        viewFinder.visibility = View.VISIBLE
        faceOverlay.visibility = View.VISIBLE

        // Hide the buttons so they don't get clicked twice
        registerButton.visibility = View.GONE
        loginButton.visibility = View.GONE
        etName.visibility = View.GONE

        isProcessing=false

        // 3. FORCE RESTART CAMERA
//        if (isCameraStarted) return
//        isCameraStarted = true // Reset the flag so startCamera actually runs
        viewFinder.post {
            startCamera(forceRestart = true)
        }
    }

    private fun registerUser(embedding: FloatArray) {
        // 1. Get the name from your EditText FIRST
        val userId = etName.text.toString().trim()

        // 2. Validate the name before even starting the network request
        if (userId.isEmpty()) {
            etName.error = "Name is required"
            isProcessing = false // Reset this so the user can try again
            return
        }

        // 3. UI Feedback
        Toast.makeText(this, "Registering $userId...", Toast.LENGTH_SHORT).show()

        // 4. Create the request with the actual name
        val request = FaceRequest(user_id = userId, embedding = embedding.toList())

        RetrofitClient.api.register(request)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    isProcessing = false

                    if (response.isSuccessful) {
                        runOnUiThread {
                            // Stop Camera
                            if (::cameraProvider.isInitialized) {
                                cameraProvider.unbindAll()
                            }

                            // Hide UI
                            viewFinder.visibility = View.GONE
                            faceOverlay.visibility = View.GONE
                            registerButton.visibility = View.VISIBLE
                            loginButton.visibility = View.VISIBLE
                            etName.visibility = View.VISIBLE

                            // Move to Home
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            intent.putExtra("USER_NAME", userId) // Optional: Pass name to Home
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Register Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    isProcessing = false
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun loginUser(embedding: FloatArray) {
        val userId = etName.text.toString().trim()
        Toast.makeText(this, "Logging in as $userId...", Toast.LENGTH_SHORT).show()

        val request = FaceRequest(user_id = userId, embedding.toList())

        RetrofitClient.api.login(request)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                    isProcessing = false
                    if (response.isSuccessful) {
                        runOnUiThread {
                            // 1. Stop the CameraX provider
                            if (::cameraProvider.isInitialized) {
                                cameraProvider.unbindAll() // This physically turns the camera off
                            }
//                            // 2. Hide the camera UI
//                            viewFinder.visibility = View.GONE
//                            faceOverlay.visibility = View.GONE

                            // 3. Move to Home
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            intent.putExtra("USER_NAME", userId)
                            startActivity(intent)
                            finish()
                        }
                    }else{
                        isProcessing = false // Only release if it failed
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "No Match Found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    isProcessing = false
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })


    }

    // ✅ Permission Handling
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            requestPermissions(
//                arrayOf(Manifest.permission.CAMERA),
//                CAMERA_REQUEST_CODE
            startCamera()

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()

            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }else{
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }
    // ✅ Start CameraX
    private fun startCamera(forceRestart: Boolean = false) {

        if (isCameraStarted && !forceRestart) return
        isCameraStarted = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setTargetRotation(viewFinder.display.rotation) // Add this for better rotation handling
                .also {builder->
                    viewFinder.display?.rotation?.let { rotation ->
                        builder.setTargetRotation(rotation)
                    }
                }
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                Log.d("CAMERA", "Camera successfully bound")

            } catch (e: Exception) {
                Log.e("CAMERA", "Binding failed", e)
                isCameraStarted = false
            }

        }, ContextCompat.getMainExecutor(this))
        Log.d("CAMERA", "Camera started")
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to run the app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }
    // ✅ Process Frames
//    @OptIn(ExperimentalGetImage::class)
//    private fun processImageProxy(imageProxy: ImageProxy) {
//        Log.d("CAMERA_FLOW", "Received frame at ${System.currentTimeMillis()}")
//        val mediaImage = imageProxy.image
//        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//        if (mediaImage != null) {
//            val inputImage = InputImage.fromMediaImage(
//                mediaImage,
//                imageProxy.imageInfo.rotationDegrees
//            )
//
//            val rawBitmap = toBitmap(imageProxy)
//
//            val rotatedBitmap = if (rawBitmap != null) {
//                rotateBitmap(rawBitmap, imageProxy.imageInfo.rotationDegrees)
//            } else null
//
//            detectFaceLive(inputImage, rotatedBitmap)
//        }
//
//        imageProxy.close()
//    }

//    @OptIn(ExperimentalGetImage::class)
//    private fun processImageProxy(imageProxy: ImageProxy) {
//        val mediaImage = imageProxy.image ?: return
//
//        // Create InputImage directly from mediaImage (Efficient)
//        val inputImage = InputImage.fromMediaImage(
//            mediaImage,
//            imageProxy.imageInfo.rotationDegrees
//        )
//
//        detector.process(inputImage)
//            .addOnSuccessListener { faces ->
//                faceOverlay.setFaces(faces)
//
//                if (faces.isNotEmpty() && !isProcessing && captureNextFrame) {
//                    Log.d("DEBUG", "Face Found! Starting extraction...count = \${faces.size}")
//
//                    // ONLY NOW do we do the heavy lifting
//                    val rawBitmap = toBitmap(imageProxy)
//                    if (rawBitmap != null) {
//                        val rotatedBitmap = rotateBitmap(rawBitmap, imageProxy.imageInfo.rotationDegrees)
//
//                        val face = faces[0]
//                        val bounds = face.boundingBox
//
//                        try {
//                            val croppedFace = cropFace(rotatedBitmap, bounds)
//                            val resizedFace = resizeFace(croppedFace)
//                            val inputArray = convertBitmapToFloatArray(resizedFace)
//                            val embedding = faceNetModel.getEmbedding(inputArray)
//
//                            isProcessing = true
//                            captureNextFrame = false
//
//                            if (isLoginMode) loginUser(embedding) else registerUser(embedding)
//
//                        } catch (e: Exception) {
//                            Log.e("FACE_ERR", "Processing error: ${e.message}")
//                        }
//                    }
//                }
//                // CRITICAL: Close inside the listener or use a finally block
//                imageProxy.close()
//            }
//            .addOnFailureListener {
//                imageProxy.close()
//                Log.e("FACE", "Detection Failed")
//
//            }
//    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {

        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(inputImage)
            .addOnSuccessListener { faces ->

                faceOverlay.setFaces(faces)

                // 2. Only do heavy Bitmap work if we have a face AND the button was clicked
                if (faces.isNotEmpty() && captureNextFrame) {
                    // LOCK immediately to prevent multiple triggers
                    captureNextFrame = false
                    isProcessing = true

                    val face = faces[0]
                    val bounds = face.boundingBox

                    // Move heavy bitmap work to a background thread to prevent UI freeze
                    cameraExecutor.execute {
                        try {
                            val bitmap = toBitmap(imageProxy)
                            if (bitmap != null) {
                                val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
                                val cropped = cropFace(rotated, bounds)
                                val resized = resizeFace(cropped)
                                val inputBuffer = convertBitmapToByteBuffer(resized)
                                val embedding = faceNetModel.getEmbedding(inputBuffer)

                                // Jump back to UI thread for the Network Call/Toasts
                                runOnUiThread {
                                    if (isLoginMode) loginUser(embedding)
                                    else registerUser(embedding)
                                }
                            } else {
                                isProcessing = false // Reset if bitmap failed
                            }
                        } catch (e: Exception) {
                            Log.e("FACE_ERR", "Pipeline failed: ${e.message}")
                            runOnUiThread { isProcessing = false }
                        } finally {
                            imageProxy.close() // Close after background work is done
                        }
                    }
                } else {
                    imageProxy.close() // Close immediately if not capturing
                }
            }
            .addOnFailureListener {
                imageProxy.close()
            }
    }

    // ✅ Live Face Detection
//    private fun detectFaceLive(image: InputImage, bitmap: Bitmap?) {
//
//        detector.process(image)
//            .addOnSuccessListener { faces ->
//                faceOverlay.setFaces(faces)
//
//                if (faces.isNotEmpty() && bitmap != null && !isProcessing && captureNextFrame) {
//                    isProcessing = false
//                    val face = faces[0]
//                    val bounds = face.boundingBox
//
//                    val croppedFace = cropFace(bitmap, bounds)
//                    // Step 1: Resize
//                    val resizedFace = resizeFace(croppedFace)
//                    // Step 2: Convert to input tensor
//                    val inputArray = convertBitmapToFloatArray(resizedFace)
//
//                    // 🔥 NEW STEP
//                    val embedding = faceNetModel.getEmbedding(inputArray)
//                    Log.d("FACE", "Face Detected: ${faces.size}")
//                    Log.d("EMBEDDING", embedding.joinToString())
//
//                    // 🔥 CALL BACKEND
//                    sendToBackend(embedding, isLogin = isLoginMode) // register
//                    captureNextFrame = false
//                    //reset after delay
//                    viewFinder.postDelayed({ isProcessing = false}, 2000)
//                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener {
//                Log.e("FACE", "Detection Failed")
//            }
//    }
//    private fun embeddingToList(embedding: FloatArray): List<Float> {
//        return embedding.toList()0707

//    }

    private fun detectFaceLive(image: InputImage, bitmap: Bitmap?) {
        detector.process(image)
            .addOnSuccessListener { faces ->
                faceOverlay.setFaces(faces)

                Log.d("DEBUG", "faces size: ${faces.size}")
                Log.d("DEBUG", "bitmap null?: ${bitmap == null}")
                Log.d("DEBUG", "isProcessing: $isProcessing")
                Log.d("DEBUG", "captureNextFrame: $captureNextFrame")

                // Only proceed if we have a face, a bitmap, and the user actually clicked the button
                if (faces.isNotEmpty() && bitmap != null && !isProcessing && captureNextFrame) {

                    val face = faces[0]
                    val bounds = face.boundingBox

                    try {
                        val faceBitmap = Bitmap.createBitmap(
                            bitmap,
                            bounds.left.coerceAtLeast(0),
                            bounds.top.coerceAtLeast(0),
                            bounds.width().coerceAtMost(bitmap.width - bounds.left),
                            bounds.height().coerceAtMost(bitmap.height - bounds.top)
                        )

                        val croppedFace = cropFace(bitmap, bounds)
                        val resizedFace = resizeFace(croppedFace)

                        val inputArray =convertBitmapToByteBuffer(resizedFace)
                        val embedding = faceNetModel.getEmbedding(inputArray)

                        Log.d("EMBEDDING", embedding.joinToString())

                        // LOCK processing before network call
                        isProcessing = true
                        captureNextFrame = false // Consume the click event

                        if (isLoginMode) {
                            loginUser(embedding)
                        } else {
                            registerUser(embedding)
                        }

//                        sendToBackend(embedding, isLogin = isLoginMode)
                    } catch (e: Exception) {
                        Log.e("FACE", "Cropping/Embedding failed: ${e.message}")
                        isProcessing = false
                    }

                    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {

                        val size = 160
                        val inputBuffer = ByteBuffer.allocateDirect(4 * size * size * 3)
                        inputBuffer.order(java.nio.ByteOrder.nativeOrder())

                        val intValues = IntArray(size * size)
                        bitmap.getPixels(intValues, 0, size, 0, 0, size, size)

                        var pixel = 0
                        for (i in 0 until size) {
                            for (j in 0 until size) {
                                val value = intValues[pixel++]

                                inputBuffer.putFloat(((value shr 16 and 0xFF) / 255f))
                                inputBuffer.putFloat(((value shr 8 and 0xFF) / 255f))
                                inputBuffer.putFloat(((value and 0xFF) / 255f))
                            }
                        }

                        inputBuffer.rewind()
                        return inputBuffer
                    }
                }
            }
    }
//private fun detectFaceLive(image: InputImage, bitmap: Bitmap?) {
//    detector.process(image)
//        .addOnSuccessListener { faces ->
//            faceOverlay.setFaces(faces)
//
//            // DEBUG LOG: See if it's even finding a face
//            if (faces.isEmpty()) {
//                // Log.d("DEBUG", "No face detected in this frame")
//            } else {
//                Log.d("DEBUG", "FACE FOUND! Size: ${faces.size}")
//            }
//
//            if (faces.isNotEmpty() && !isProcessing && captureNextFrame) {
//                // Now, only if a face is found and we need to capture, we handle the bitmap
//                val rawBitmap = bitmap ?: return@addOnSuccessListener
//
//                val face = faces[0]
//                // ... rest of your embedding logic
//            }
//        }
//}
//



//    private fun sendToBackend(embedding: FloatArray, isLogin: Boolean) {
//
//        val request = FaceRequest(
//            user_id = "Samadnya",   // later make dynamic
//            embedding = embeddingToList(embedding)
//        )
//
//        val call: Call<ApiResponse> = if (isLogin) {
//            RetrofitClient.api.login(request)
//        } else {
//            RetrofitClient.api.register(request)
//        }
//
//        call.enqueue(object : retrofit2.Callback<ApiResponse> {
//
//            override fun onResponse(
//                call: retrofit2.Call<ApiResponse>,
//                response: retrofit2.Response<ApiResponse>
//            ) {
//                isProcessing = false
//                if (response.isSuccessful) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        response.body()?.message,
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//
//            override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
//                isProcessing = false
//                Toast.makeText(
//                    this@MainActivity,
//                    "Error: ${t.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }
private fun sendToBackend(embedding: FloatArray, isLogin: Boolean) {
    val request = FaceRequest("Samadnya", embedding.toList())
    val call = if (isLogin) RetrofitClient.api.login(request) else RetrofitClient.api.register(request)

    call.enqueue(object : retrofit2.Callback<ApiResponse> {
        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
            isProcessing = false // RELEASE LOCK
            if (response.isSuccessful) {
                Toast.makeText(this@MainActivity, "Success: ${response.body()?.message}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
            isProcessing = false // RELEASE LOCK
            Log.e("NETWORK", "Failed", t)
            Toast.makeText(this@MainActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}
    private fun cropFace(bitmap: Bitmap, bounds: android.graphics.Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            bounds.left.coerceAtLeast(0),
            bounds.top.coerceAtLeast(0),
            bounds.width().coerceAtMost(bitmap.width - bounds.left),
            bounds.height().coerceAtMost(bitmap.height - bounds.top)
        )
    }

//    ____________________MAY USE LATER ___________________________________________

//    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
//        val image = imageProxy.image ?: return null
//
//        val planes = image.planes
//        val buffer = planes[0].buffer
//        val bytes = ByteArray(buffer.remaining())
//        buffer.get(bytes)
//
//        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//    }
//_________________________________________________________________________________
//    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {
//        val image = imageProxy.image ?: return null
//        val inputImage = InputImage.fromMediaImage(
//            image,
//            imageProxy.imageInfo.rotationDegrees
//        )
//
//        return Bitmap.createBitmap(
//            inputImage.width,
//            inputImage.height,
//            Bitmap.Config.ARGB_8888
//        )
//    }
    @OptIn(ExperimentalGetImage::class)
    private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

        val image = imageProxy.image ?: return null

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height),
            100,
            out
        )

        val yuv = out.toByteArray()
        return BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
    }

    private fun resizeFace(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 112, 112, true)
    }
//
//    private fun convertBitmapToFloatArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
//
//        val input = Array(1) { Array(160) { Array(160) { FloatArray(3) } } }
//
//        for (y in 0 until 160) {
//            for (x in 0 until 160) {
//                val pixel = bitmap.getPixel(x, y)
//
//                val r = ((pixel shr 16) and 0xFF) / 255.0f
//                val g = ((pixel shr 8) and 0xFF) / 255.0f
//                val b = (pixel and 0xFF) / 255.0f
//
//                input[0][y][x][0] = r
//                input[0][y][x][1] = g
//                input[0][y][x][2] = b
//            }
//        }
//
//        return input
//    }
private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    // 1. Change size to match your model's requirement (112)
    val size = 112

    // This will now allocate exactly 150,528 bytes
    val inputBuffer = ByteBuffer.allocateDirect(4 * size * size * 3)
    inputBuffer.order(java.nio.ByteOrder.nativeOrder())

    val pixels = IntArray(size * size)

    // Ensure the bitmap being passed in is actually 112x112
    val resizedBitmap = if (bitmap.width != size || bitmap.height != size) {
        Bitmap.createScaledBitmap(bitmap, size, size, true)
    } else {
        bitmap
    }

    resizedBitmap.getPixels(pixels, 0, size, 0, 0, size, size)

    var pixelIndex = 0
    for (y in 0 until size) {
        for (x in 0 until size) {
            val pixel = pixels[pixelIndex++]

            // RGB extraction and normalization
            inputBuffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
            inputBuffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
            inputBuffer.putFloat(((pixel and 0xFF) / 255f))
        }
    }

    inputBuffer.rewind()
    return inputBuffer
}

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
//        matrix.postScale(-1f, 1f) ////uncomment if img flipped
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}