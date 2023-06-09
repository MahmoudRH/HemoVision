package com.mahmoudhabib.cbctest.presentation.screens.addTest

import android.net.Uri


sealed class AddTestEvent() {
    class SetImageUri(val uri: Uri?) : AddTestEvent()
    class ChangeTitleText(val newText: String) : AddTestEvent()
    class ProcessImage() : AddTestEvent()

}
