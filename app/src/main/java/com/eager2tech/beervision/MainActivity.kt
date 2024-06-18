package com.eager2tech.beervision

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.eager2tech.beervision.databinding.MainCamLayoutBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainCamLayoutBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImage: ImageView
    private val REQUEST_CODE_CAMERA = 101

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

    private fun startCamera() {}

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRE_PERMISSIONS)
    }

    private fun takePhoto() { }

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
