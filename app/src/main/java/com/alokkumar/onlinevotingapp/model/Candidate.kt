package com.alokkumar.onlinevotingapp.model

data class Candidate(
    val id: String = "",
    val name: String = "",
    val party: String = "",
    val agenda: String = "",
    val votes: Int = 0
)
