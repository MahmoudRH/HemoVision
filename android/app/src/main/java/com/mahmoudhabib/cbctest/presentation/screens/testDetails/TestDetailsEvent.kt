package com.mahmoudhabib.cbctest.presentation.screens.testDetails

sealed class TestDetailsEvent {
    object LoadTestDetails : TestDetailsEvent()
    object ShowDeleteConfirmationDialog : TestDetailsEvent()
    object DeleteTestDetails : TestDetailsEvent()
    object DismissDeletion : TestDetailsEvent()
}