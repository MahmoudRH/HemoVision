package com.mahmoudhabib.cbctest.data.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mahmoudhabib.cbctest.data.local.HistoryDao
import com.mahmoudhabib.cbctest.data.local.HistoryDatabase
import com.mahmoudhabib.cbctest.data.repository.LocalRepoImpl
import com.mahmoudhabib.cbctest.domain.model.TestResult
import com.mahmoudhabib.cbctest.domain.repository.LocalRepo
import com.mahmoudhabib.cbctest.domain.usecases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideDataBase(app: Application): HistoryDatabase {
        return Room.databaseBuilder(app, HistoryDatabase::class.java, HistoryDatabase.DATABASE_NAME)
            .addCallback(object :RoomDatabase.Callback(){
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    coroutineScope.launch {
                        val dao = provideHistoryDao(provideDataBase(app))
                        dao.insertTestResult(TestResult(title="first", date = System.currentTimeMillis()))
                        dao.insertTestResult(TestResult(title="second", date = System.currentTimeMillis()))
                        dao.insertTestResult(TestResult(title="third", date = System.currentTimeMillis()))
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: HistoryDatabase): HistoryDao {
        return database.historyDao
    }

    @Provides
    @Singleton
    fun provideLocalRepo(db: HistoryDatabase): LocalRepo {
        return LocalRepoImpl(db.historyDao)
    }

    @Provides
    @Singleton
    fun provideUseCases(repo: LocalRepo): TestResultsUseCases {
        return TestResultsUseCases(
            getAllTestResults = GetAllTestResults(repo = repo),
            searchTestResults = SearchTestResults(repo = repo),
            saveTestResult = SaveTestResult(repo = repo),
            deleteTestResult = DeleteTestResult(repo = repo),
            getTestDetails = GetTestDetails(repo = repo)
        )
    }
}