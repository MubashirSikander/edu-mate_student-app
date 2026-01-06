package com.example.studentmanagementapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studentmanagementapp.data.dao.AttendanceDao
import com.example.studentmanagementapp.data.dao.CourseDao
import com.example.studentmanagementapp.data.dao.EnrollmentDao
import com.example.studentmanagementapp.data.dao.StudentDao
import com.example.studentmanagementapp.data.entity.Attendance
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Enrollment
import com.example.studentmanagementapp.data.entity.Student

@Database(
    entities = [Student::class, Course::class, Enrollment::class, Attendance::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun courseDao(): CourseDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_record_db"
                )
                    // We keep destructive migration to avoid crashes while adding Firestore-friendly fields.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
