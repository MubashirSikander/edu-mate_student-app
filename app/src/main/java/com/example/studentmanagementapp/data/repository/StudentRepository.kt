package com.example.studentmanagementapp.data.repository

import android.util.Log
import com.example.studentmanagementapp.data.dao.AttendanceDao
import com.example.studentmanagementapp.data.dao.CourseDao
import com.example.studentmanagementapp.data.dao.EnrollmentDao
import com.example.studentmanagementapp.data.dao.StudentDao
import com.example.studentmanagementapp.data.entity.Attendance
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.entity.Student
import java.util.Calendar
import java.util.Date

class StudentRepository(
    private val studentDao: StudentDao,
    private val courseDao: CourseDao,
    private val enrollmentDao: EnrollmentDao,
    private val attendanceDao: AttendanceDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    data class AttendanceMarkPayload(
        val studentId: Long,
        val isPresent: Boolean
    )

    data class AttendanceBatchResult(
        val successCount: Int,
        val failures: List<String>
    )

    suspend fun addStudent(student: Student): Long {
        val id = studentDao.insert(student)
        val savedStudent = student.copy(studentId = id)
        firestoreRepository.saveStudent(savedStudent)
        return id
    }

    // For new students with email, password, and isCR
    suspend fun addStudent(
        name: String,
        contact: String,
        registration: String,
        email: String,
        password: String,
        isRepeater: Boolean,
        isCR: Boolean
    ): Long {
        val student = Student(
            name = name.trim(),
            contactNumber = contact.trim(),
            registrationNumber = registration.trim(),
            email = email.trim(),
            password = password.trim(),
            isRepeater = isRepeater,
            isCR = isCR
        )
        return addStudent(student)
    }

    suspend fun getStudentById(id: Long) =
        studentDao.getStudentById(id)

    suspend fun updateStudent(student: Student) {
        studentDao.update(student)
        firestoreRepository.saveStudent(student)
    }

    suspend fun isStudentExists(registration: String): Boolean {
        return studentDao.isStudentRegistrationExists(registration) > 0
    }
    fun getStudents() = studentDao.getAllStudents()
    suspend fun getStudentsByIds(ids: List<Long>) = studentDao.getStudentsByIds(ids)
    suspend fun deleteStudentByRegistration(registrationNumber: String) {
        // Step 1: Get the ID because Enrollment and Attendance tables use ID, not Reg No
        val studentId = studentDao.getStudentIdByRegistration(registrationNumber)

        if (studentId != null) {
            // Step 2: Delete Enrollments (using the ID we found)
            enrollmentDao.deleteEnrollmentsByStudentId(studentId)

            // Step 3: Delete Attendance (using the ID we found)
            attendanceDao.deleteAttendanceByStudentId(studentId)

            // Step 4: Finally, delete the Student using the Registration Number
            studentDao.deleteByRegistration(registrationNumber)
        }
        if (studentId != null) {
            firestoreRepository.deleteStudent(studentId)
        }
    }

    suspend fun addCourse(course: Course): Long {
        val id = courseDao.insert(course)
        val savedCourse = course.copy(courseId = id)
        // Sync course to the cloud for cross-device availability.
        firestoreRepository.saveCourse(savedCourse)
        return id
    }

    suspend fun deleteCourseByCode(code: String) {
        // Step 1: Find course ID
        val course = courseDao.getCourseByCode(code)
        course?.let {
            // Step 2: Delete related enrollments
            enrollmentDao.deleteEnrollmentsByCourseId(it.courseId)

            // Step 3: Delete attendance tied to the course
            attendanceDao.deleteAttendanceByCourse(it.courseId)

            // Step 4: Delete the course
            courseDao.delete(it)

            // Step 5: Delete from Firestore
            firestoreRepository.deleteCourse(it.courseId)
        }
    }

    suspend fun deleteEnrollment(studentId: Long, courseId: Long) {
        enrollmentDao.deleteEnrollment(studentId, courseId)
    }

    suspend fun updateCourse(
        courseId: Long,
        name: String,
        code: String,
        creditHours: Int,
        instructor: String,
        semester: Int
    ) {
        val existingCourse = courseDao.getCourseById(courseId)
        existingCourse?.let {
            val updatedCourse = it.copy(
                courseName = name.trim(),
                courseCode = code.trim(),
                creditHours = creditHours,
                instructorName = instructor.trim(),
                semesterNumber = semester
            )
            courseDao.update(updatedCourse)

            // Update in Firestore
            firestoreRepository.saveCourse(updatedCourse)
        }
    }
    suspend fun isCourseCodeExists(code: String): Boolean {
        return courseDao.isCourseCodeExists(code) > 0
    }
    fun getCourses() = courseDao.getAllCourses()
    suspend fun getCourseById(id: Long) = courseDao.getCourseById(id)

    suspend fun enrollStudent(enrollment: Enrollment) {
        val id = enrollmentDao.insert(enrollment)
        val savedEnrollment = enrollment.copy(enrollmentId = id)
        // Store enrollment remotely so attendance queries can work from Firestore cache.
        firestoreRepository.saveEnrollment(savedEnrollment)
    }
    fun getEnrollmentsForCourse(courseId: Long) = enrollmentDao.getEnrollmentsByCourse(courseId)
    suspend fun getEnrollmentsSnapshot(courseId: Long) = enrollmentDao.getEnrollmentsListByCourse(courseId)

    suspend fun saveAttendance(
        studentId: Long,
        courseId: Long,
        isPresent: Boolean,
        timestamp: Date
    ): Result<Attendance> {
        return runCatching {
            val dayStart = startOfDay(timestamp).time
            val dayEnd = endOfDay(timestamp).time

            // Avoid duplicate rows for the same student/date by checking the day boundaries.
            val existing = attendanceDao.getAttendanceForStudentOnDate(
                studentId = studentId,
                courseId = courseId,
                startOfDay = dayStart,
                endOfDay = dayEnd
            )

            val attendance = if (existing != null) {
                existing.copy(isPresent = isPresent, date = timestamp)
            } else {
                Attendance(
                    studentOwnerId = studentId,
                    courseOwnerId = courseId,
                    date = timestamp,
                    isPresent = isPresent
                )
            }

            val id = if (existing != null) {
                attendanceDao.update(attendance)
                existing.attendanceId
            } else {
                attendanceDao.insert(attendance)
            }

            val saved = attendance.copy(attendanceId = id)
            firestoreRepository.saveAttendance(saved)
            saved
        }
    }

    suspend fun saveAttendanceBatch(
        courseId: Long,
        payloads: List<AttendanceMarkPayload>,
        timestamp: Date
    ): AttendanceBatchResult {
        var successCount = 0
        val failures = mutableListOf<String>()

        payloads.forEach { payload ->
            val result = saveAttendance(
                studentId = payload.studentId,
                courseId = courseId,
                isPresent = payload.isPresent,
                timestamp = timestamp
            )
            result.onSuccess { successCount++ }
                .onFailure {
                    Log.e(
                        "AttendanceSave",
                        "Failed to save attendance for ${payload.studentId} in course $courseId",
                        it
                    )
                    failures.add(payload.studentId.toString())
                }
        }

        return AttendanceBatchResult(successCount, failures)
    }

    fun getAttendanceByCourse(courseId: Long) = attendanceDao.getAttendanceForCourse(courseId)
    suspend fun getAttendanceListForCourse(courseId: Long) = attendanceDao.getAttendanceListForCourse(courseId)

    suspend fun syncWithFirestore(): Boolean {
        return try {
            // Wait for any queued offline writes, then refresh Room with the cloud snapshot.
            firestoreRepository.waitForPendingWrites()
            val bundle = firestoreRepository.fetchAllData()
            val students = bundle.students.mapNotNull { it.toEntity() }
            val courses = bundle.courses.mapNotNull { it.toEntity() }
            val enrollments = bundle.enrollments.mapNotNull { it.toEntity() }
            val attendance = bundle.attendance.mapNotNull { it.toEntity() }

            studentDao.insertAll(students)
            courseDao.insertAll(courses)
            enrollmentDao.insertAll(enrollments)
            attendanceDao.insertAll(attendance)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()   // 👈 CRITICAL
            Log.e("SyncError", "Sync failed", ex)
            false
        }
    }

    private fun startOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun endOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    private fun FirestoreStudent.toEntity(): Student? {
        val id = studentId.toLongOrNull() ?: return null
        return Student(
            studentId = id,
            name = name,
            contactNumber = contactNumber,
            registrationNumber = registrationNumber,
            isRepeater = isRepeater,
            createdAt = if (createdAt > 0) createdAt else System.currentTimeMillis()
        )
    }

    private fun FirestoreCourse.toEntity(): Course? {
        val id = courseId.toLongOrNull() ?: return null
        return Course(
            courseId = id,
            courseName = courseName,
            courseCode = courseCode,
            creditHours = creditHours,
            instructorName = instructorName,
            semesterNumber = semesterNumber
        )
    }

    private fun FirestoreEnrollment.toEntity(): Enrollment? {
        val id = enrollmentId.toLongOrNull() ?: return null
        val student = studentId.toLongOrNull() ?: return null
        val course = courseId.toLongOrNull() ?: return null
        return Enrollment(
            enrollmentId = id,
            studentOwnerId = student,
            courseOwnerId = course
        )
    }

    private fun FirestoreAttendance.toEntity(): Attendance? {
        val id = attendanceId.toLongOrNull() ?: return null
        val student = studentId.toLongOrNull() ?: return null
        val course = courseId.toLongOrNull() ?: return null
        return Attendance(
            attendanceId = id,
            studentOwnerId = student,
            courseOwnerId = course,
            date = date,
            isPresent = isPresent
        )
    }
}
