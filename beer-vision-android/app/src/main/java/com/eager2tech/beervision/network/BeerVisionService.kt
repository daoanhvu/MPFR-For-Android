package com.eager2tech.beervision.network

import retrofit2.Retrofit

private const val BASE_URL = "http://10.0.0.2:10123/"

private val retrofit = Retrofit
    .Builder()
    .baseUrl(BASE_URL)
    .build()