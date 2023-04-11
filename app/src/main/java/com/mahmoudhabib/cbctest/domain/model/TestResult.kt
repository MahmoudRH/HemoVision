package com.mahmoudhabib.cbctest.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity(tableName = "testResults")
data class TestResult(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "redBloodCells") val redBloodCells: Float = Random.nextFloat() * 100,
    @ColumnInfo(name = "whiteBloodCells") val whiteBloodCells: Float = Random.nextFloat() * 100,
    @ColumnInfo(name = "platelets") val platelets: Float = Random.nextFloat() * 100,
    @ColumnInfo(name = "date") val date: Long,
)
