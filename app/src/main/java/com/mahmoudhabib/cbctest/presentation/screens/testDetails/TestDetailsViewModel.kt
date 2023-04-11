package com.mahmoudhabib.cbctest.presentation.screens.testDetails

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    private val useCases: TestResultsUseCases,
    private val savedStateHandle: SavedStateHandle,
) :
    ViewModel() {
    val testDetails = mutableStateOf<TestResult?>(null)

    private val _isLoading = mutableStateOf<Boolean>(true)
    val isLoading: State<Boolean> = _isLoading

    private val _alertDialogState = mutableStateOf<Boolean>(false)
    val alertDialogState: State<Boolean> = _alertDialogState

    private val _isDeleteSuccess = mutableStateOf<Boolean>(false)
    val isDeleteSuccess: State<Boolean> = _isDeleteSuccess

    fun onEvent(event: TestDetailsEvent) {
        when (event) {
            is TestDetailsEvent.LoadTestDetails -> {
                savedStateHandle.get<Int>("testID")?.let { testID ->
                    viewModelScope.launch {
                        _isLoading.value = true
                        useCases.getTestDetails(testID)?.let {
                            testDetails.value = it
                        }
                        _isLoading.value = false
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