package com.alokkumar.onlinevotingapp.model

data class VoterDocument(
    val docId: String,
    val name: String,
    val voterId: String,
    val isVerified: Boolean
)