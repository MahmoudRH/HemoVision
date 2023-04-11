package com.mahmoudhabib.cbctest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mahmoudhabib.cbctest.domain.model.TestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert
    suspend fun insertTestResult(testResult: TestResult)

    @Delete
    suspend fun deleteTestResult(testResult: TestResult)

    @Query("SELECT * FROM testResults WHERE id = :testId LIMIT 1")
    suspend fun getTestDetails(testId: Int): TestResult

    @Query("SELECT * FROM testResults")
    fun getAllTestResults(): Flow<List<TestResult>>

    @Query("SELECT * FROM testResults WHERE title LIKE '%' || :searchWord || '%'")
    fun searchHistory(searchWord: String? = null): Flow<List<TestResult>>
}