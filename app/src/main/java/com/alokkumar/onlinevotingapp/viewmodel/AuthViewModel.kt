package com.alokkumar.onlinevotingapp.viewmodel

import android.util.Patterns
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

        private val auth = Firebase.auth
        private val fireStore = Firebase.firestore

        val loginLoading = mutableStateOf(false)
        val signupLoading = mutableStateOf(false)
        val loginError = mutableStateOf<String?>(null)
        val signupError = mutableStateOf<String?>(null)

        fun login(email: String, password: String, onSuccess: () -> Unit) = viewModelScope.launch {
            loginLoading.value = true
            loginError.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser ?: throw Exception("User not found")
                val doc = fireStore.collection("users").document(user.uid).get().await()
                if (!doc.exists()) throw Exception("User record missing")
                val isVerified = doc.getBoolean("isVerified") ?: false
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                when {
                    !isVerified -> throw Exception("Account not verified")
                    isDeleted -> throw Exception("Account deleted")
                    else -> onSuccess()
                }
            } catch (e: Exception) {
                loginError.value = e.localizedMessage
            } finally {
                loginLoading.value = false
            }
        }

        fun signup(name: String, email: String, phone: String,
                   password: String, onSuccess: () -> Unit) = viewModelScope.launch {
            signupLoading.value = true
            signupError.value = null
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val uId = auth.currentUser?.uid ?: throw Exception("Failed to get user UID")
                val userModel = UserModel(
                    uid = uId,
                    name = name,
                    email = email,
                    phone = phone,
                    isVerified = false,
                    isDeleted = false,
                    createdAt = Timestamp.now()
                )
                fireStore.collection("users").document(uId).set(userModel).await()
                onSuccess()
            } catch (e: Exception) {
                signupError.value = e.localizedMessage
            } finally {
                signupLoading.value = false
            }
        }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                onResult(false, "Enter a valid email")
                return@launch
            }
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, "Reset link sent to your email")
            } catch (e: Exception) {
                val errorMessage = when {
                    e.localizedMessage?.contains("no user record") == true -> "No account found with this email"
                    else -> e.localizedMessage ?: "Something went wrong"
                }
                onResult(false, errorMessage)
            }
        }
    }

}
