package com.example.studentmanagementapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.entity.Attendance
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.data.repository.StudentRepository.AttendanceBatchResult
import com.example.studentmanagementapp.data.repository.StudentRepository.AttendanceMarkPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudentRepository
    val selectedCourseId = MutableLiveData<Long>()

    init {
        val db = com.example.studentmanagementapp.data.db.AppDatabase.getInstance(application)
        repository = StudentRepository(db.studentDao(), db.courseDao(), db.enrollmentDao(), db.attendanceDao())
    }

    fun getAttendanceByCourse(courseId: Long) = repository.getAttendanceByCourse(courseId)

    suspend fun saveAttendance(
        courseId: Long,
        payloads: List<AttendanceMarkPayload>,
        timestamp: Long
    ): AttendanceBatchResult {
        return withContext(Dispatchers.IO) {
            repository.saveAttendanceBatch(courseId, payloads, Date(timestamp))
        }
    }

    suspend fun getCourse(courseId: Long) = repository.getCourseById(courseId)

    fun loadEnrolledStudents(enrollments: List<Enrollment>, onLoaded: (List<Student>) -> Unit) {
        viewModelScope.launch {
            val studentIds = enrollments.map { it.studentOwnerId }
            if (studentIds.isEmpty()) {
                onLoaded(emptyList())
                return@launch
            }
            val students = repository.getStudentsByIds(studentIds)
            onLoaded(students)
        }
    }

    fun getAttendance(courseId: Long) = repository.getAttendanceByCourse(courseId)
    fun getEnrollments(courseId: Long) = repository.getEnrollmentsForCourse(courseId)
    suspend fun getAttendanceSnapshot(courseId: Long): List<Attendance> =
        repository.getAttendanceListForCourse(courseId)

    suspend fun getStudentsByIds(ids: List<Long>): List<Student> = withContext(Dispatchers.IO) {
        repository.getStudentsByIds(ids)
    }
}
