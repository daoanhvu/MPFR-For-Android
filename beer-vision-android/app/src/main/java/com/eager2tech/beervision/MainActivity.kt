package com.eager2tech.beervision

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.eager2tech.beervision.databinding.MainCamLayoutBinding
import com.eager2tech.beervision.network.BeerVisionAPI
import com.eager2tech.beervision.usecases.detect.DetectAPIService
import com.eager2tech.beervision.usecases.detect.ImageAnalyzer
//import com.google.ai.client.generativeai.GenerativeModel
//import com.google.api.gax.core.FixedCredentialsProvider
//import com.google.auth.oauth2.ServiceAccountCredentials
//import com.google.cloud.vision.v1.ImageAnnotatorClient
//import com.google.cloud.vision.v1.ImageAnnotatorSettings
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainCamLayoutBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImage: ImageView
    private var imageCapture: ImageCapture? = null
//    private var imageAnalysis ImageAnalysis? = null
    private lateinit var detectAPIService: DetectAPIService
    private lateinit var imageAnalyzer: ImageAnalyzer

//    private lateinit var generativeModel: GenerativeModel
//    private lateinit var imageAnnotatorClient: ImageAnnotatorClient

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRE_PERMISSIONS && !it.value) {
                permissionGranted = false
            }
        }

        if (!permissionGranted) {
            Toast.makeText(baseContext, "Permission request denied.", Toast.LENGTH_SHORT).show()
        } else {
            startCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainCamLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detectAPIService = BeerVisionAPI.getRetrofit().create(DetectAPIService::class.java)

//        imageAnnotatorClient = createImageAnnotatorClient()

        imageAnalyzer = ImageAnalyzer(detectAPIService).also { it ->
            it.detectResults.observeForever { result ->
                // TODO: Update UI here
                val numberPerson = result.detections.count { item -> item.className == "person" }
                if (numberPerson > 0) {
                    binding.tvNotice.text = "There are ${numberPerson} people here!"
                } else {
                    binding.tvNotice.text = ""
                }
//                val prompt = Promp
//                generativeModel.
                binding.bbOverlay.setDetections(result)
                Log.i("MainActivity", result.toString())
            }
        }

//        generativeModel = GenerativeModel("generative-text-v1/models/gemini-1.5",
//            apiKey = com.eager2tech.beervision.BuildConfig.GEMINI_API_KEY)

        if (allPermissionGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons
        binding.btnImageCapture.setOnClickListener { takePhoto() }
        binding.btnVideCapture.setOnClickListener { captureVideo() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

//    private fun createImageAnnotatorClient(): ImageAnnotatorClient {
//        val keyStore = KeyStore.getInstance("AndroidKeyStore")
//        keyStore.load(null)
//        val inputStream = resources.openRawResource(R.raw.beer_vision_credentials)
//        val credentials = ServiceAccountCredentials.fromStream(inputStream)
//
//        val imageAnnotatorSettings = ImageAnnotatorSettings
//            .newBuilder()
//            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
//            .build()
//        return ImageAnnotatorClient.create(imageAnnotatorSettings)
//    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Use to bind the lifecycle of camera to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyer = ImageAnalysis
                .Builder()
//                .setTargetResolution(Size(640, 480)) // Adjust resolution as needed
//                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), this.imageAnalyzer)
                }

            // Send back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyer)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRE_PERMISSIONS)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun allPermissionGranted() = REQUIRE_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRE_PERMISSIONS = mutableListOf(
             android.Manifest.permission.CAMERA,
             android.Manifest.permission.RECORD_AUDIO
         ).apply {
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                 add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
             }
         }.toTypedArray()
    }
}
