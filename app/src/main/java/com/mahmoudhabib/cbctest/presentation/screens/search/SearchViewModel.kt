package com.mahmoudhabib.cbctest.presentation.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val useCases: TestResultsUseCases,
) : ViewModel() {
    val searchWord = mutableStateOf("")
    var resultsList: List<TestResult> = emptyList()
    val isLoading = mutableStateOf<Boolean>(false)
    val isResultsListEmpty = mutableStateOf<Boolean>(false)

    private var searchJob: Job? = null
    fun search() {
        isLoading.value = true
        searchJob?.cancel()
        searchJob = useCases.searchTestResults(searchWord.value.trim()).onEach {
            resultsList = it
            isLoading.value = false
            isResultsListEmpty.value = it.isEmpty()
        }.launchIn(viewModelScope)
    }
}