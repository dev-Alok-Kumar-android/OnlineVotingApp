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

    init { loadVotes() }

    private fun loadVotes() {
        voteListener = db.collection("votes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MonitorVotesVM", "Error listening to votes: ", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val allVotes = snapshot.documents.mapNotNull { doc ->
                    try {
                        VoteModel(
                            voteId = doc.id,
                            voterName = doc.getString("voterName") ?: "",
                            candidateName = doc.getString("candidateName") ?: "",
                            pollId = doc.getString("pollId") ?: "",
                            pollTitle = doc.getString("pollTitle") ?: "Unknown Poll",
                            userId = doc.getString("userId") ?: "",
                            timestamp = doc.getDate("timestamp")
                        )
                    } catch (e: Exception) {
                        Log.e("MonitorVotesVM", "Error parsing vote: ${e.message}")
                        null
                    }
                }

                _votes.value = allVotes
            }
    }

    fun deleteVote(voteId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val voteRef = db.collection("votes").document(voteId)

            voteRef.get().addOnSuccessListener { voteSnapshot ->
                val pollId = voteSnapshot.getString("pollId")
                val candidateId = voteSnapshot.getString("candidateId")

                if (pollId.isNullOrEmpty() || candidateId.isNullOrEmpty()) {
                    onError("Invalid vote data")
                    return@addOnSuccessListener
                }

                val candidateRef = db.collection("polls").document(pollId).collection("candidates")
                    .document(candidateId)

                db.runTransaction { transaction ->
                    val candidateSnap = transaction.get(candidateRef)
                    val currentVotes = candidateSnap.getLong("votes") ?: 0
                    val updatedVotes = (currentVotes - 1).coerceAtLeast(0)

                    transaction.update(candidateRef, "votes", updatedVotes)
                    transaction.delete(voteRef)
                }.addOnSuccessListener {
                    val userId = voteSnapshot.getString("userId")

                    if (!userId.isNullOrEmpty()) {
                        db.collection("polls").document(pollId).collection("voters")
                            .document(userId).update("hasVoted", false).addOnFailureListener { e ->
                                Log.e(
                                    "MonitorVotesVM", "Failed to reset hasVoted: ${e.message}"
                                )
                            }
                    }

                    onSuccess()
                }.addOnFailureListener { e ->
                    onError("Transaction failed: ${e.message}")
                }
            }.addOnFailureListener { e ->
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