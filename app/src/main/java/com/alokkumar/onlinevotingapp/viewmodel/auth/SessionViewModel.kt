package com.alokkumar.onlinevotingapp.viewmodel.auth

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SessionViewModel : ViewModel() {
    private val auth = Firebase.auth

    val isLoggedIn = mutableStateOf(Firebase.auth.currentUser != null)
    val email = mutableStateOf(Firebase.auth.currentUser?.email ?: "")

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        isLoggedIn.value = user != null
        email.value = user?.email ?: ""
        Log.d("SessionViewModel", "AuthStateChanged -> LoggedIn=${isLoggedIn.value}, email=${email.value}")
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}