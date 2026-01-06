package com.example.studentmanagementapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studentmanagementapp.data.entity.Student

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<Student>)

    @Update
    suspend fun update(student: Student)

    @Query("SELECT COUNT(*) FROM students WHERE registrationNumber = :registration")
    suspend fun isStudentRegistrationExists(registration: String): Int

    @Query("SELECT studentId FROM students WHERE registrationNumber = :registration LIMIT 1")
    suspend fun getStudentIdByRegistration(registration: String): Long?

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE studentId IN (:ids)")
    suspend fun getStudentsByIds(ids: List<Long>): List<Student>

    @Query("SELECT * FROM students WHERE studentId = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("DELETE FROM students WHERE registrationNumber = :regNo")
    suspend fun deleteByRegistration(regNo: String)
}
