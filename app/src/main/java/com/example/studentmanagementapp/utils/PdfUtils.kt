package com.example.studentmanagementapp.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.studentmanagementapp.data.entity.Attendance
import com.example.studentmanagementapp.data.entity.Course
import com.example.studentmanagementapp.data.entity.Student
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    fun generateAttendancePdf(
        context: Context,
        course: Course,
        students: List<Student>,
        attendance: List<Attendance>
    ): File? {
        val document = PdfDocument()
        return try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val titlePaint = Paint().apply {
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val textPaint = Paint().apply { textSize = 14f }
            var yPos = 50f

            canvas.drawText("Attendance Sheet", 40f, yPos, titlePaint)
            yPos += 30f
            canvas.drawText("Course: ${course.courseName} (${course.courseCode})", 40f, yPos, textPaint)
            yPos += 20f
            canvas.drawText("Instructor: ${course.instructorName}", 40f, yPos, textPaint)
            yPos += 30f
            canvas.drawText("Student", 40f, yPos, titlePaint)
            canvas.drawText("Reg. No", 240f, yPos, titlePaint)
            canvas.drawText("Present", 440f, yPos, titlePaint)
            yPos += 20f

            attendance.forEach { record ->
                val student = students.find { it.studentId == record.studentOwnerId }
                canvas.drawText(student?.name ?: "Unknown", 40f, yPos, textPaint)
                canvas.drawText(student?.registrationNumber ?: "-", 240f, yPos, textPaint)
                canvas.drawText(if (record.isPresent) "Yes" else "No", 440f, yPos, textPaint)
                yPos += 18f
            }

            document.finishPage(page)

            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: return null
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "attendance_${course.courseCode}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { output ->
                document.writeTo(output)
            }
            file
        } catch (ex: Exception) {
            Toast.makeText(context, "Error creating PDF: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        } finally {
            document.close()
        }
    }
}
