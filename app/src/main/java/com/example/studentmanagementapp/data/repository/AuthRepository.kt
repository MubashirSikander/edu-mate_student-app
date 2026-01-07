package com.example.studentmanagementapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class AuthProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CR"
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return runCatching {
            auth.signInWithEmailAndPassword(email, password).await().user
                ?: throw IllegalStateException("Login failed: missing user")
        }.onFailure { Log.e("AuthRepository", "Login failed for $email", it) }
    }

    suspend fun signup(name: String, email: String, password: String): Result<FirebaseUser> {
        return runCatching {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
                ?: throw IllegalStateException("Signup failed: missing user")
            saveProfile(AuthProfile(uid = user.uid, name = name, email = email))
            user
        }.onFailure { Log.e("AuthRepository", "Signup failed for $email", it) }
    }

    suspend fun saveProfile(profile: AuthProfile) {
        runCatching {
            firestore.collection("Users")
                .document(profile.uid)
                .set(profile)
                .await()
        }.onFailure { Log.e("AuthRepository", "Failed to persist profile ${profile.uid}", it) }
            .getOrThrow()
    }

    suspend fun fetchProfile(uid: String): AuthProfile? {
        return runCatching {
            firestore.collection("Users").document(uid).get().await().toObject<AuthProfile>()
        }.onFailure { Log.e("AuthRepository", "Failed to fetch profile $uid", it) }
            .getOrNull()
    }

    fun logout() {
        auth.signOut()
    }

    fun currentUser(): FirebaseUser? = auth.currentUser
}
