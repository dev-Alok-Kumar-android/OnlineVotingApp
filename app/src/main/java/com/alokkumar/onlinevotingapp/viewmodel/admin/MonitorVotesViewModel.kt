package com.alokkumar.onlinevotingapp.viewmodel.admin

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.VoteModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonitorVotesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _votes = MutableStateFlow<List<VoteModel>>(emptyList())
    val votes: StateFlow<List<VoteModel>> = _votes
    private var voteListener: ListenerRegistration? = null
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadVotes()
    }

    private fun loadVotes() {
        voteListener = db.collection("votes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MonitorVotesVM", "Error listening to votes: ", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val votes = snapshot.documents.mapNotNull { doc ->
                    try {
                        VoteModel(
                            voteId = doc.id,
                            voterName = doc.getString("voterName") ?: "",
                            candidateName = doc.getString("candidateName") ?: "",
                            pollId = doc.getString("pollId") ?: "",
                            pollTitle = doc.getString("pollTitle") ?: "Unknown PollModel",
                            userId = doc.getString("userId") ?: "",
                            timestamp = doc.getDate("timestamp")
                        )
                    } catch (e: Exception) {
                        Log.e("MonitorVotesVM", "Error parsing vote: ${e.message}")
                        null
                    }
                }

                _votes.value = votes
            }
    }


    fun deleteVote(voteId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val voteRef = db.collection("votes").document(voteId)

            // First, get the vote data
            voteRef.get()
                .addOnSuccessListener { voteSnapshot ->
                    val pollId = voteSnapshot.getString("pollId")
                    val candidateId = voteSnapshot.getString("candidateId")

                    if (pollId.isNullOrEmpty() || candidateId.isNullOrEmpty()) {
                        onError("Invalid vote data")
                        return@addOnSuccessListener
                    }

                    // Reference to candidate document
                    val candidateRef = db.collection("polls")
                        .document(pollId)
                        .collection("candidates")
                        .document(candidateId)

                    // Run a Firestore transaction to update vote count safely
                    db.runTransaction { transaction ->
                        val candidateSnap = transaction.get(candidateRef)
                        val currentVotes = candidateSnap.getLong("votes") ?: 0
                        val updatedVotes = (currentVotes - 1).coerceAtLeast(0) // Prevent negative

                        transaction.update(candidateRef, "votes", updatedVotes)
                        transaction.delete(voteRef)
                    }.addOnSuccessListener {
                        db.collection("votes").document(voteId).delete()
                        onSuccess()
                    }.addOnFailureListener { e ->
                        onError("Transaction failed: ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    onError("Failed to fetch vote data: ${e.message}")
                }
        }
    }

    fun formatTime(date: Date?): String {
        if (date == null) return "Time not recorded"
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }

    override fun onCleared() {
        super.onCleared()
        voteListener?.remove()
    }

}