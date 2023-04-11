package com.mahmoudhabib.cbctest.domain.usecases

import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo

class DeleteTestResult(private val repo: LocalRepo) {
    suspend operator fun invoke(testResult: TestResult) {
        repo.deleteTestResult(testResult)
    }
}