package com.mahmoudhabib.cbctest.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoudhabib.cbctest.domain.usecases.TestResultsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val useCases: TestResultsUseCases) : ViewModel() {

    private val _homeState = mutableStateOf<HomeState>(HomeState())
    val homeState: State<HomeState> = _homeState

    private var getTestResultsJob: Job? = null

    init {
        loadTestResults()
    }

    private fun loadTestResults() {
        getTestResultsJob?.cancel() // cancel if already running
        getTestResultsJob = useCases.getAllTestResults().onEach {
            _homeState.value = homeState.value.copy(
                testResults = it,
                isListLoading = false,
                isListEmpty = it.isEmpty()
            )
        }.launchIn(viewModelScope)
    }
}