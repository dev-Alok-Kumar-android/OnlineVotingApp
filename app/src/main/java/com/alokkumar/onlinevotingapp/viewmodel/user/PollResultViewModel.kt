package com.alokkumar.onlinevotingapp.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

        listenerRegistration = db.collection("polls")
            .document(pollId)
            .collection("candidates")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e("PollResultVM", "Listen failed: ${error?.message}")
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val candidates = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("candidateName")
                    val party = doc.getString("party")
                    val agenda = doc.getString("agenda")
                    val id = doc.id
                    val votes = doc.getLong("votes")?.toInt() ?: 0

                    if (name != null && party != null) {
                        Candidate(
                            id = id,
                            candidateName = name,
                            party = party,
                            agenda = agenda ?: "",
                            votes = votes
                        )
                    } else null
                }.sortedByDescending { it.votes }

                _results.value = candidates
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}