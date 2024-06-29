package com.eager2tech.beervision.ui

import com.eager2tech.beervision.usecases.detect.DetectionModel

data class DetectionsModel (
    val imageWidth: Int,
    val imageHeight: Int,
    val detections: List<DetectionModel>
)