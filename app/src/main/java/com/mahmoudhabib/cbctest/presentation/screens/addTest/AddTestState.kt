package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.net.Uri

data class AddTestState(
    val selectedImageUri: Uri? = null,
    val titleText:String = "",
    val showImagePreview:Boolean  = false

    )
