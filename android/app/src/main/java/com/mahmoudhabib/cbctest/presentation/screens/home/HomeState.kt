package com.mahmoudhabib.cbctest.presentation.screens.home

import com.mahmoudhabib.cbctest.domain.model.TestResult

data class HomeState(
    val testResults:List<TestResult> = emptyList(),
    val isListLoading: Boolean = true,
    val isListEmpty: Boolean = false
)
