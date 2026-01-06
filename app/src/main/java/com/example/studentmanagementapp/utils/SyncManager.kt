package com.example.studentmanagementapp.utils

import com.example.studentmanagementapp.data.repository.StudentRepository

class SyncManager(private val repository: StudentRepository) {
    suspend fun forceSync(): Boolean {
        return repository.syncWithFirestore()
    }
}
