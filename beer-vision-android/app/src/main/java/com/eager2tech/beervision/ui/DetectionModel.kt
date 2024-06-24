package com.eager2tech.beervision.ui

import com.eager2tech.beervision.usecases.detect.DetectionModel

data class DetectionsModel (
    val detections: List<DetectionModel>
)