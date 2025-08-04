package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp
import java.util.Date

data class VoteModel(
    val voteId: String = "",
    val voterName: String = "",
    val candidateName: String = "",
    val candidateId: String = "",
    val userId: String = "",
    val pollId: String = "",
    val pollTitle: String = "",
    val timestamp: Date? = Timestamp.now().toDate()
)
