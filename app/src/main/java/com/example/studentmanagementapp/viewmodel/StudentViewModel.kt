//package com.example.studentmanagementapp.viewmodel
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.studentmanagementapp.data.db.AppDatabase
//import com.example.studentmanagementapp.data.entity.Student
//import com.example.studentmanagementapp.data.repository.StudentRepository
//import com.example.studentmanagementapp.utils.SyncManager
//import kotlinx.coroutines.launch
//
//class StudentViewModel(application: Application) : AndroidViewModel(application) {
//    private val repository: StudentRepository
//    private val syncManager: SyncManager
//    val students
//        get() = repository.getStudents()
//
//    init {
//        val db = AppDatabase.getInstance(application)
//        repository = StudentRepository(db.studentDao(), db.courseDao(), db.enrollmentDao(), db.attendanceDao())
//        syncManager = SyncManager(repository)
//    }
//
//    fun addStudent(name: String, contact: String, registration: String, isRepeater: Boolean, onResult: (String?) -> Unit) {
//        if (name.isBlank() || contact.isBlank() || registration.isBlank()) {
//            onResult("VALID_DETAILS")
//            return
//        }
//
//        viewModelScope.launch {
//            if (repository.isStudentExists(registration)) {
//                onResult("DUPLICATE_REGISTRATION")
//                return@launch
//            }
//
//            repository.addStudent(
//                Student(
//                    name = name.trim(),
//                    contactNumber = contact.trim(),
//                    registrationNumber = registration.trim(),
//                    isRepeater = isRepeater
//                )
//            )
//
//            // 2. FIX: Notify UI that success happened (pass null or "SUCCESS")
//            onResult("SUCCESS")
//        }
//    }
//
//    fun deleteStudent(registrationNumber: String) {
//        viewModelScope.launch {
//            repository.deleteStudentByRegistration(registrationNumber)
//        }
//    }
//
//    fun syncNow(onResult: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            val success = syncManager.forceSync()
//            onResult(success)
//        }
//    }
//}
package com.example.studentmanagementapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.db.AppDatabase
import com.example.studentmanagementapp.data.entity.Student
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.utils.SyncManager
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository
    private val syncManager: SyncManager

    // Expose students as LiveData for UI to observe safely
    val students: LiveData<List<Student>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudentRepository(
            db.studentDao(),
            db.courseDao(),
            db.enrollmentDao(),
            db.attendanceDao()
        )
        syncManager = SyncManager(repository)
        students = repository.getStudents() // LiveData from Room
    }
    fun getStudentById(id: Long, onResult: (Student?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getStudentById(id))
        }
    }

    fun updateStudent(student: Student, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.updateStudent(student)
            onDone()
        }
    }


    fun addStudent(
        name: String,
        contact: String,
        registration: String,
        email: String,
        password: String,
        isRepeater: Boolean,
        isCR: Boolean,
        onResult: (String) -> Unit
    ) {
        if (name.isBlank() || contact.isBlank() || registration.isBlank() || email.isBlank() || password.isBlank()) {
            onResult("VALID_DETAILS")
            return
        }

        viewModelScope.launch {
            if (repository.isStudentExists(registration)) {
                onResult("DUPLICATE_REGISTRATION")
                return@launch
            }

            repository.addStudent(name, contact, registration, email, password, isRepeater, isCR)
            onResult("SUCCESS")
        }
    }

    fun deleteStudent(registrationNumber: String, onResult: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteStudentByRegistration(registrationNumber)
            onResult?.invoke()
        }
    }

    fun syncNow(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = syncManager.forceSync()
            onResult(success)
        }
    }
}
