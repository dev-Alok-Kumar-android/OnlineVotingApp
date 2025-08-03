package com.alokkumar.onlinevotingapp.viewmodel.admin

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewStatsViewModel : ViewModel() {
    private val firestore = Firebase.firestore

    val totalUsers = mutableIntStateOf(0)
    val verifiedUsers = mutableIntStateOf(0)
    val totalPolls = mutableIntStateOf(0)
    val totalVotes = mutableIntStateOf(0)

    val isLoading = mutableStateOf(true)
    val error = mutableStateOf<String?>(null)

    init {
        loadStats()
    }

    private fun loadStats() = viewModelScope.launch {
        try {
            isLoading.value = true
            error.value = null

            val users = firestore.collection("users").get().await().documents
            totalUsers.intValue = users.count { it.getBoolean("deleted") != true }
            verifiedUsers.intValue = users.count { it.getBoolean("verified") == true && it.getBoolean("deleted") != true }

            val polls = firestore.collection("polls").get().await().documents
            totalPolls.intValue = polls.size

            val votes = firestore.collection("votes").get().await().documents
            totalVotes.intValue = votes.size

        } catch (e: Exception) {
            error.value = e.localizedMessage
        } finally {
            isLoading.value = false
        }
    }
}