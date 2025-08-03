package com.alokkumar.onlinevotingapp.model

import com.google.firebase.Timestamp
import java.util.Date

data class VoteModel(
    val voteId: String = "",
    val voterName: String = "",
    val candidateName: String = "",
    val userId: String = "",
    val pollId: String = "",
    val pollTitle: String = "",
    val voteTime: Date? = Timestamp.now().toDate()
)
