package com.alokkumar.onlinevotingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val fireStore = Firebase.firestore

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onResult(true, "Logged in successfully")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun signup(
        email: String,
        name: String,
        phoneNumber: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val uId = auth.currentUser?.uid ?: ""
                val userModel = UserModel(uId, name, email, phoneNumber)
                fireStore.collection("users").document(uId).set(userModel).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage)
            }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, "Reset email sent")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage)
            }
        }
    }
}
