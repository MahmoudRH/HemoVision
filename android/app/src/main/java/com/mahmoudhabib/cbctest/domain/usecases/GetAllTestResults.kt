package com.mahmoudhabib.cbctest.domain.usecases

import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo
import kotlinx.coroutines.flow.Flow

class GetAllTestResults(private val repo: LocalRepo) {
    operator fun invoke(): Flow<List<TestResult>> {
        return repo.getAllHistoryItems()
    }
}