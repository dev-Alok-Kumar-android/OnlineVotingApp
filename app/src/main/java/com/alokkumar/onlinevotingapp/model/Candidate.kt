package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp


data class Candidate(
    val id: String = "",
    val candidateName: String = "",
    val party: String = "",
    val agenda: String = "",
    val pollModel: PollModel = PollModel(),
    val votes: Int = 0,
    val timestamp: Timestamp = Timestamp.now()
)
