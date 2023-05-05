package com.mahmoudhabib.cbctest.domain.model

import android.graphics.RectF

data class Recognition(
    val id: String,
    val title: String,
    val confidence: Float,
    val location: RectF,
    val detectedClass: Int,
)
