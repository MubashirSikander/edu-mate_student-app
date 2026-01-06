package com.example.studentmanagementapp

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class StudentManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
    }
}
