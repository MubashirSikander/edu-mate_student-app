package com.example.studentmanagementapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studentmanagementapp.data.entity.Attendance

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance): Long

    @Query("SELECT * FROM attendance WHERE courseOwnerId = :courseId ORDER BY date DESC")
    fun getAttendanceForCourse(courseId: Long): LiveData<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE courseOwnerId = :courseId ORDER BY date DESC")
    suspend fun getAttendanceListForCourse(courseId: Long): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendance: List<Attendance>)

    @Query("DELETE FROM attendance WHERE studentOwnerId = :studentId")
    suspend fun deleteAttendanceByStudentId(studentId: Long)

    @Query(
        "SELECT * FROM attendance WHERE studentOwnerId = :studentId AND courseOwnerId = :courseId " +
            "AND date BETWEEN :startOfDay AND :endOfDay LIMIT 1"
    )
    suspend fun getAttendanceForStudentOnDate(
        studentId: Long,
        courseId: Long,
        startOfDay: Long,
        endOfDay: Long
    ): Attendance?

    @Update
    suspend fun update(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE courseOwnerId = :courseId")
    suspend fun deleteAttendanceByCourse(courseId: Long)
}
