package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.graphics.Bitmap
import android.net.Uri


sealed class AddTestEvent() {
    class SetImageUri(val uri: Uri?) : AddTestEvent()
    class ChangeTitleText(val newText: String) : AddTestEvent()
    class ProcessImage(val bitmap: Bitmap) : AddTestEvent()

}
