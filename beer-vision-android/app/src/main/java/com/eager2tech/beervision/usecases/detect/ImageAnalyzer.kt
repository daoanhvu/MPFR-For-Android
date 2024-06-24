package com.eager2tech.beervision.usecases.detect

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.eager2tech.beervision.ui.DetectionsModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class ImageAnalyzer(private val detectAPIService: DetectAPIService) : ImageAnalysis.Analyzer {
    val detectResults = MutableLiveData<DetectionsModel>()

    private fun convertImageProxyToBitmap(image: ImageProxy, quality: Int): ByteArray {
        val yBuffer = image.planes[0].buffer
        val vuBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer[nv21, 0, ySize]
        vuBuffer[nv21, ySize, vuSize]

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), quality, out)
        return out.toByteArray()
    }

    override fun analyze(imageProxy: ImageProxy) {
        val bytes = convertImageProxyToBitmap(imageProxy, 80)
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val request = bytes.toRequestBody(mediaType, 0, bytes.size)

        val requestImage = MultipartBody.Part
            .createFormData("file", "filename.jpg", request)
        val apiCall = detectAPIService.detect(requestImage)
        apiCall.enqueue(object: Callback<DetectResponseModel> {
            override fun onResponse(call: Call<DetectResponseModel>, response: Response<DetectResponseModel>) {
                response.body() ?. let { responseBody ->
                    detectResults.postValue(DetectionsModel(responseBody.detections))
                }
            }

            override fun onFailure(call: Call<DetectResponseModel>, t: Throwable) {
                Log.e("ImageAnalyzer::Detect", t.message.toString())
            }

        })

        imageProxy.close()
    }
}