package com.alokkumar.onlinevotingapp.model

data class PollModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val candidates: List<Candidate> = emptyList()
)
