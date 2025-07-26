package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isVerified: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
