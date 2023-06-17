package com.mahmoudhabib.cbctest.presentation.screens.testDetails

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import com.mahmoudhabib.cbctest.presentation.PdfUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    private val useCases: TestResultsUseCases,
    private val savedStateHandle: SavedStateHandle,
    private val application: Application,
) :
    ViewModel() {
    val testDetails = mutableStateOf<TestResult?>(null)

    private val _isCreatingPdfFile = mutableStateOf<Boolean>(false)
    val isCreatingPdfFile: State<Boolean> = _isCreatingPdfFile

    private val _isPdfFileCreated = mutableStateOf<Boolean>(false)
    val isPdfFileCreated: State<Boolean> = _isPdfFileCreated

    private val _pdfFileUri = mutableStateOf<Uri?>(null)
    val pdfFileUri: State<Uri?> = _pdfFileUri

    private val _alertDialogState = mutableStateOf<Boolean>(false)
    val alertDialogState: State<Boolean> = _alertDialogState

    private val _isDeleteSuccess = mutableStateOf<Boolean>(false)
    val isDeleteSuccess: State<Boolean> = _isDeleteSuccess


    fun onEvent(event: TestDetailsEvent) {
        when (event) {
            is TestDetailsEvent.LoadTestDetails -> {
                savedStateHandle.get<Int>("testID")?.let { testID ->
                    viewModelScope.launch {
                        useCases.getTestDetails(testID)?.let {
                            testDetails.value = it
                        }
                    }
                }
            }

            is TestDetailsEvent.ShowDeleteConfirmationDialog -> {
                _alertDialogState.value = true
            }

            is TestDetailsEvent.DismissDeletion -> {
                _alertDialogState.value = false
            }

            is TestDetailsEvent.DeleteTestDetails -> {
                deleteTestResult()
                _alertDialogState.value = false
                _isDeleteSuccess.value = true
            }

            TestDetailsEvent.ShareTestDetails -> {
                testDetails.value?.let { test ->
                    _isCreatingPdfFile.value = true
                    _isPdfFileCreated.value = false
                    val pdfUri = PdfUtils.createReport(application.applicationContext, test)
                    _pdfFileUri.value = pdfUri
                    _isCreatingPdfFile.value = false
                    _isPdfFileCreated.value = true
                }
            }

            TestDetailsEvent.ClearPdf -> {
                _pdfFileUri.value = null
                _isPdfFileCreated.value = false
                _isCreatingPdfFile.value = false
            }
        }

    }

    private fun deleteTestResult() {
        viewModelScope.launch {
            testDetails.value?.let {
                useCases.deleteTestResult(it)
            }
        }
    }

}