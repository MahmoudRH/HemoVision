package com.mahmoudhabib.cbctest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mahmoudhabib.cbctest.domain.model.TestResult

@Database(entities = [TestResult::class], version = 2, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract val historyDao: HistoryDao
    companion object{  const val DATABASE_NAME = "HistoryDB" }

}