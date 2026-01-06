package com.example.studentmanagementapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.db.AppDatabase
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.repository.StudentRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudentRepository
    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> get() = _selectedCourse

    val courses
        get() = repository.getCourses()

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudentRepository(db.studentDao(), db.courseDao(), db.enrollmentDao(), db.attendanceDao())
    }

    fun unenrollStudent(studentId: Long, courseId: Long) {
        viewModelScope.launch {
            repository.deleteEnrollment(studentId, courseId)
        }
    }

    fun addCourse(courseName: String, code: String, creditHours: Int, instructor: String, semester: Int, onResult: (String?) -> Unit) {
        if (courseName.isBlank() || code.isBlank() || instructor.isBlank()) onResult("VALID_DETAILS")
        viewModelScope.launch {
            if (repository.isCourseCodeExists(code)) {
                onResult("DUPLICATE_CODE")
                return@launch
            }
            repository.addCourse(
                Course(
                    courseName = courseName.trim(),
                    courseCode = code.trim(),
                    creditHours = creditHours,
                    instructorName = instructor.trim(),
                    semesterNumber = semester
                )
            )
            onResult(null)
        }
    }
    fun deleteCourseByCode(code: String) {
        viewModelScope.launch {
            repository.deleteCourseByCode(code)
        }
    }

    fun updateCourse(id: Long, name: String, code: String, creditHours: Int, instructor: String, semester: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.updateCourse(id, name, code, creditHours, instructor, semester)
        }
        onResult("UPDATED")
    }

    fun loadCourse(id: Long) {
        viewModelScope.launch {
            _selectedCourse.postValue(repository.getCourseById(id))
        }
    }

    suspend fun getCourse(id: Long) = repository.getCourseById(id)

    fun enrollStudent(studentId: Long, courseId: Long) {
        viewModelScope.launch {
            repository.enrollStudent(Enrollment(studentOwnerId = studentId, courseOwnerId = courseId))
        }
    }

    fun getEnrollments(courseId: Long) = repository.getEnrollmentsForCourse(courseId)
}
