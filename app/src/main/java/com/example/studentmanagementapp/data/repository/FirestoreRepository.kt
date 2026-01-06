package com.example.studentmanagementapp.data.repository

import android.util.Log
import com.example.studentmanagementapp.data.entity.Attendance
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.entity.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

data class FirestoreStudent(
    val studentId: String = "",
    val name: String = "",
    val contactNumber: String = "",
    val registrationNumber: String = "",
    val isRepeater: Boolean = false,
    val createdAt: Long = 0L
)

data class FirestoreCourse(
    val courseId: String = "",
    val courseName: String = "",
    val courseCode: String = "",
    val creditHours: Int = 0,
    val instructorName: String = "",
    val semesterNumber: Int = 0
)

data class FirestoreEnrollment(
    val enrollmentId: String = "",
    val studentId: String = "",
    val courseId: String = ""
)

data class FirestoreAttendance(
    val attendanceId: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val date: Date = Date(),
    val isPresent: Boolean = false
)

data class FirestoreDataBundle(
    val students: List<FirestoreStudent>,
    val courses: List<FirestoreCourse>,
    val enrollments: List<FirestoreEnrollment>,
    val attendance: List<FirestoreAttendance>
)

class FirestoreRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    private val studentsCollection = firestore.collection("Students")
    private val coursesCollection = firestore.collection("Courses")
    private val enrollmentsCollection = firestore.collection("Enrollments")
    private val attendanceCollection = firestore.collection("Attendance")

    init {
        firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }

    suspend fun saveStudent(student: Student) {
        val payload = FirestoreStudent(
            studentId = student.studentId.toString(),
            name = student.name,
            contactNumber = student.contactNumber,
            registrationNumber = student.registrationNumber,
            isRepeater = student.isRepeater,
            createdAt = student.createdAt
        )
        runCatching {
            studentsCollection.document(payload.studentId)
                .set(payload, SetOptions.merge())
                .await()
        }.onFailure { Log.e("FirestoreSave", "Failed to save student ${payload.studentId}", it) }
            .getOrThrow()
    }

    suspend fun deleteStudent(studentId: Long) {
        val id = studentId.toString()
        studentsCollection.document(id).delete().await()
        deleteEnrollmentsForStudent(id)
        deleteAttendanceForStudent(id)
    }

    private suspend fun deleteEnrollmentsForStudent(studentId: String) {
        val snapshot = enrollmentsCollection.whereEqualTo("studentId", studentId).get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    private suspend fun deleteAttendanceForStudent(studentId: String) {
        val snapshot = attendanceCollection.whereEqualTo("studentId", studentId).get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    suspend fun saveCourse(course: Course) {
        val payload = FirestoreCourse(
            courseId = course.courseId.toString(),
            courseName = course.courseName,
            courseCode = course.courseCode,
            creditHours = course.creditHours,
            instructorName = course.instructorName,
            semesterNumber = course.semesterNumber
        )
        runCatching {
            coursesCollection.document(payload.courseId)
                .set(payload, SetOptions.merge())
                .await()
        }.onFailure { Log.e("FirestoreSave", "Failed to save course ${payload.courseId}", it) }
            .getOrThrow()
    }

    // NEW: Delete course by ID
    suspend fun deleteCourse(courseId: Long) {
        val id = courseId.toString()
        // Delete course document
        coursesCollection.document(id).delete().await()
        // Delete associated enrollments
        val snapshot = enrollmentsCollection.whereEqualTo("courseId", id).get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
        // Optionally, delete attendance records
        val attendanceSnapshot = attendanceCollection.whereEqualTo("courseId", id).get().await()
        val attendanceBatch = firestore.batch()
        attendanceSnapshot.documents.forEach { attendanceBatch.delete(it.reference) }
        attendanceBatch.commit().await()
    }

    // NEW: Update course
    suspend fun updateCourse(course: Course) {
        val payload = FirestoreCourse(
            courseId = course.courseId.toString(),
            courseName = course.courseName,
            courseCode = course.courseCode,
            creditHours = course.creditHours,
            instructorName = course.instructorName,
            semesterNumber = course.semesterNumber
        )
        runCatching {
            coursesCollection.document(payload.courseId)
                .set(payload, SetOptions.merge())
                .await()
        }.onFailure { Log.e("FirestoreSave", "Failed to update course ${payload.courseId}", it) }
            .getOrThrow()
    }

    suspend fun saveEnrollment(enrollment: Enrollment) {
        val payload = FirestoreEnrollment(
            enrollmentId = enrollment.enrollmentId.toString(),
            studentId = enrollment.studentOwnerId.toString(),
            courseId = enrollment.courseOwnerId.toString()
        )
        runCatching {
            enrollmentsCollection.document(payload.enrollmentId)
                .set(payload, SetOptions.merge())
                .await()
        }.onFailure { Log.e("FirestoreSave", "Failed to save enrollment ${payload.enrollmentId}", it) }
            .getOrThrow()
    }

    suspend fun saveAttendance(attendance: Attendance) {
        val payload = FirestoreAttendance(
            attendanceId = attendance.attendanceId.toString(),
            studentId = attendance.studentOwnerId.toString(),
            courseId = attendance.courseOwnerId.toString(),
            date = attendance.date,
            isPresent = attendance.isPresent
        )
        runCatching {
            attendanceCollection.document(payload.attendanceId)
                .set(payload, SetOptions.merge())
                .await()
        }.onFailure { Log.e("FirestoreSave", "Failed to save attendance ${payload.attendanceId}", it) }
            .getOrThrow()
    }

    suspend fun fetchAllData(): FirestoreDataBundle {
        return runCatching {
            val students = studentsCollection.get().await().toObjects(FirestoreStudent::class.java)
            val courses = coursesCollection.get().await().toObjects(FirestoreCourse::class.java)
            val enrollments = enrollmentsCollection.get().await().toObjects(FirestoreEnrollment::class.java)
            val attendance = attendanceCollection.get().await().toObjects(FirestoreAttendance::class.java)
            FirestoreDataBundle(students, courses, enrollments, attendance)
        }.onFailure { Log.e("FirestoreFetch", "Failed to fetch data", it) }
            .getOrThrow()
    }

    suspend fun waitForPendingWrites() {
        firestore.waitForPendingWrites().await()
    }
}
