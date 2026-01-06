package com.example.studentmanagementapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "enrollments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentOwnerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Course::class,
            parentColumns = ["courseId"],
            childColumns = ["courseOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Enrollment(
    @PrimaryKey(autoGenerate = true) val enrollmentId: Long = 0,
    val studentOwnerId: Long = 0,
    val courseOwnerId: Long = 0
)
