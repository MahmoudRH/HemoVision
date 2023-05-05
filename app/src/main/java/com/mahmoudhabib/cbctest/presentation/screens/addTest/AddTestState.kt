package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.graphics.Bitmap

data class AddTestState(
    val titleText: String = "",
    val showImagePreview: Boolean = false,
    val selectedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val shouldNavigateToTestDetails: Boolean = false,
    val rowId:Int = -1
)
