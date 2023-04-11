package com.mahmoudhabib.cbctest.domain.usecases

data class TestResultsUseCases(
    val getAllTestResults: GetAllTestResults,
    val searchTestResults: SearchTestResults,
    val saveTestResult: SaveTestResult,
    val deleteTestResult: DeleteTestResult,
    val getTestDetails: GetTestDetails,
)