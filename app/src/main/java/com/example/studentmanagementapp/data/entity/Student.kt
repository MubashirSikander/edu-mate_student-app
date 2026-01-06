package com.example.studentmanagementapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val studentId: Long = 0,
    val name: String = "",
    val contactNumber: String = "",
    val registrationNumber: String = "",
    val email: String = "",
    val password: String = "",
    val isCR: Boolean = false,
    val isRepeater: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

