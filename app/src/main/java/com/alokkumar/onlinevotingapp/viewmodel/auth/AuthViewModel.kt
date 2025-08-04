package com.alokkumar.onlinevotingapp.viewmodel.auth

import android.util.Log
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
    private val auth by lazy { Firebase.auth }
    private val fireStore by lazy { Firebase.firestore }
    val isLoggedIn = mutableStateOf(Firebase.auth.currentUser != null)
    val currentUserEmail = mutableStateOf(Firebase.auth.currentUser?.email ?: "")
    val loginLoading = mutableStateOf(false)
    val signupLoading = mutableStateOf(false)
    val loginError = mutableStateOf<String?>(null)
    val signupError = mutableStateOf<String?>(null)

    init {
        Firebase.auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            isLoggedIn.value = user != null
            currentUserEmail.value = user?.email ?: ""
            Log.d("AuthState", "User logged in: ${user != null}, UID: ${user?.uid}")
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) = viewModelScope
        .launch {
            loginLoading.value = true
            loginError.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                val user = auth.currentUser ?: throw Exception("User not found")
                val doc = fireStore.collection("users")
                    .document(user.uid).get().await()
                if (!doc.exists()) throw Exception("User record missing")
                val isVerified = doc.getBoolean("verified") ?: false
                val isDeleted = doc.getBoolean("deleted") ?: false
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

    fun signup(
        name: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: () -> Unit,
    ) = viewModelScope.launch {
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
                verified = false,
                deleted = false,
                createdAt = Timestamp.Companion.now()
            )
            fireStore.collection("users").document(uId).set(userModel).await()
            onSuccess()

        } catch (e: Exception) {
            if (e.localizedMessage?.contains("email address is already in use") == true) {
                val query = fireStore.collection("users")
                    .whereEqualTo("email", email)
                    .get().await()

                if (!query.isEmpty) {
                    val existing = query.documents[0]
                    val isDeleted = existing.getBoolean("deleted") ?: false
                    if (isDeleted) {
                        signupError.value =
                            "Account with this email is deleted. Contact support to recover."
                    } else {
                        signupError.value = "Email already in use. Try another email."
                    }
                } else {
                    signupError.value = "Email already in use. Try another email."
                }
            } else {
                signupError.value = e.localizedMessage
            }
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