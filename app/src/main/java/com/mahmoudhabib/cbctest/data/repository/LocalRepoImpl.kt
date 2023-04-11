package com.mahmoudhabib.cbctest.data.repository

import com.mahmoudhabib.cbctest.data.local.HistoryDao
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo
import kotlinx.coroutines.flow.Flow

class LocalRepoImpl(private val dao: HistoryDao) : LocalRepo {
    override fun getAllHistoryItems(): Flow<List<TestResult>> {
        return dao.getAllTestResults()
    }

    override fun searchHistory(searchWord: String): Flow<List<TestResult>> {
        return dao.searchHistory(searchWord)
    }

    override suspend fun getTestDetails(testId: Int): TestResult? {
        return dao.getTestDetails(testId)
    }

    override suspend fun saveNewTestResult(testResult: TestResult) {
        return dao.insertTestResult(testResult)
    }

    override suspend fun deleteTestResult(testResult: TestResult) {
        return dao.deleteTestResult(testResult)
    }
}