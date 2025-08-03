package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp


data class Candidate(
    val id: String = "",
    val name: String = "",
    val party: String = "",
    val agenda: String = "",
    val poll: Poll = Poll(),
    val votes: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)
