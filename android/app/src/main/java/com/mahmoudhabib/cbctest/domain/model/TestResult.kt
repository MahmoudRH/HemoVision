package com.mahmoudhabib.cbctest.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "testResults")
data class TestResult(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "redBloodCells") val redBloodCells: Int ,
    @ColumnInfo(name = "whiteBloodCells") val whiteBloodCells: Int ,
    @ColumnInfo(name = "platelets") val platelets: Int ,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "originalImagePath") val originalImagePath: String,
    @ColumnInfo(name = "resultImagePath") val resultImagePath: String,
    @ColumnInfo(name = "abnormalities") val abnormalities:String =""

)
