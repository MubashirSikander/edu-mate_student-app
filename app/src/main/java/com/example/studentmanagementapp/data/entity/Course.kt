package com.example.studentmanagementapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val courseId: Long = 0,
    val courseName: String = "",
    val courseCode: String = "",
    val creditHours: Int = 0,
    val instructorName: String = "",
    val semesterNumber: Int = 0
)
