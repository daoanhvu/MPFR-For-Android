package com.eager2tech.beervision.usecases.detect

import com.google.gson.annotations.SerializedName

data class DetectionModel(
    val className: String,
    @SerializedName("confidence") val confidence: Float,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)