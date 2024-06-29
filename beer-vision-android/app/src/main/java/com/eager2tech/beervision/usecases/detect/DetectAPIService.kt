package com.eager2tech.beervision.usecases.detect

import okhttp3.MultipartBody
import retrofit2.Call
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DetectAPIService {
    @Multipart
    @POST("/detect")
    fun detect(@Part image: MultipartBody.Part): Call<DetectResponseModel>
}
