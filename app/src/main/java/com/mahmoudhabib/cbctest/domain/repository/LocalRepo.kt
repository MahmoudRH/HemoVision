package com.mahmoudhabib.cbctest.domain.repository

import com.mahmoudhabib.cbctest.domain.model.TestResult
import kotlinx.coroutines.flow.Flow

interface LocalRepo {

    fun getAllHistoryItems(): Flow<List<TestResult>>

    fun searchHistory(searchWord: String): Flow<List<TestResult>>

    suspend fun getTestDetails(testId: Int): TestResult?

    suspend fun saveNewTestResult(testResult: TestResult) : Long

    suspend fun deleteTestResult(testResult: TestResult)

}