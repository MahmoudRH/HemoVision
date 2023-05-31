package com.mahmoudhabib.cbctest.domain.usecases

import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo

class SaveTestResult(private val repo: LocalRepo) {

    suspend operator fun invoke(testResult: TestResult): Long{
       return repo.saveNewTestResult(testResult)
    }
}