package com.alokkumar.onlinevotingapp.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PollResultViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _results = MutableStateFlow<List<Candidate>>(emptyList())
    val results: StateFlow<List<Candidate>> = _results

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun startListening(pollId: String) {
        _isLoading.value = true
        listenerRegistration?.remove()

        viewModelScope.launch {
            try {
                val candidateSnapshot = db.collection("polls")
                    .document(pollId)
                    .collection("candidates")
                    .get().await()

                val candidateList = candidateSnapshot.documents.mapNotNull {
                    val id = it.id
                    val name = it.getString("name") ?: return@mapNotNull null
                    val party = it.getString("party") ?: "Independent"
                    Triple(id, name, party)
                }

                listenerRegistration = db.collection("votes")
                    .whereEqualTo("pollId", pollId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener

                        val voteCounts = snapshot.documents
                            .mapNotNull { it.getString("candidateId") }
                            .groupingBy { it }
                            .eachCount()

                        val liveResults = candidateList.map { (id, name, party) ->
                            Candidate(name = name, party = party, votes = voteCounts[id] ?: 0)
                        }.sortedByDescending { it.votes }

                        _results.value = liveResults
                        _isLoading.value = false
                    }

            } catch (e: Exception) {
                Log.e("PollResultViewModel", "Error: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}