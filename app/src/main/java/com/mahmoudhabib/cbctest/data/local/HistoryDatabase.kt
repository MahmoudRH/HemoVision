package com.mahmoudhabib.cbctest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mahmoudhabib.cbctest.domain.model.TestResult
import kotlinx.coroutines.CoroutineScope

@Database(entities = [TestResult::class], version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao
    companion object{  const val DATABASE_NAME = "HistoryDB" }

}