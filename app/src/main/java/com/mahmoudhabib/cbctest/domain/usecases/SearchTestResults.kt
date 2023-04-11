package com.mahmoudhabib.cbctest.domain.usecases

import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo
import kotlinx.coroutines.flow.Flow

class SearchTestResults(private val repo: LocalRepo) {
    operator fun invoke(searchWord:String): Flow<List<TestResult>> {
        return repo.searchHistory(searchWord)
    }
}