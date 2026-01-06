package com.example.studentmanagementapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.studentmanagementapp.data.entity.Course

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    @Query("SELECT * FROM courses ORDER BY courseName ASC")
    fun getAllCourses(): LiveData<List<Course>>

    @Query("SELECT * FROM courses WHERE courseId = :id")
    suspend fun getCourseById(id: Long): Course?

    @Query("SELECT COUNT(*) FROM courses WHERE courseCode = :code")
    suspend fun isCourseCodeExists(code: String): Int

    @Query("SELECT * FROM courses WHERE courseCode = :code LIMIT 1")
    suspend fun getCourseByCode(code: String): Course?

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
}
