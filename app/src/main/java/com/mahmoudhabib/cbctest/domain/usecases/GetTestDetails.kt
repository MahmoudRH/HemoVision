package com.mahmoudhabib.cbctest.domain.usecases

import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo

class GetTestDetails(private val repo: LocalRepo) {
    suspend operator fun invoke(testId:Int): TestResult {
        return repo.getTestDetails(testId)
    }
}