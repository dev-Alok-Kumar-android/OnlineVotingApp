package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp

data class UserModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val verified: Boolean = false,
    val deleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)
