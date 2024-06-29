package com.eager2tech.beervision.usecases.detect

data class DetectResponseModel(
    val serviceCode: Int,
    val detections: List<DetectionModel>
)