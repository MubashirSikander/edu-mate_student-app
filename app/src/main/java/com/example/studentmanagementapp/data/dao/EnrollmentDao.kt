package com.example.studentmanagementapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studentmanagementapp.data.entity.Enrollment

@Dao
interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(enrollment: Enrollment): Long

    @Query("SELECT * FROM enrollments WHERE courseOwnerId = :courseId")
    fun getEnrollmentsByCourse(courseId: Long): LiveData<List<Enrollment>>

    @Query("SELECT * FROM enrollments WHERE courseOwnerId = :courseId")
    suspend fun getEnrollmentsListByCourse(courseId: Long): List<Enrollment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(enrollments: List<Enrollment>)

    @Query("DELETE FROM enrollments WHERE studentOwnerId = :studentId")
    suspend fun deleteEnrollmentsByStudentId(studentId: Long)

    @Query("DELETE FROM enrollments WHERE studentOwnerId = :studentId AND courseOwnerId = :courseId")
    suspend fun deleteEnrollment(studentId: Long, courseId: Long)

    // NEW: Delete enrollments by course
    @Query("DELETE FROM enrollments WHERE courseOwnerId = :courseId")
    suspend fun deleteEnrollmentsByCourseId(courseId: Long)
}
