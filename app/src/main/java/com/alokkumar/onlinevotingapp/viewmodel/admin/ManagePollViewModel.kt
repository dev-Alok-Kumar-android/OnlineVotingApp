package com.alokkumar.onlinevotingapp.viewmodel.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.PollModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ManagePollViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _polls = MutableStateFlow<List<PollModel>>(emptyList())
    val polls: StateFlow<List<PollModel>> = _polls
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private var listener: ListenerRegistration? = null

    init {
        fetchPolls()
    }

    private fun fetchPolls() {
        listener = db.collection("polls").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val pollModelList = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val description = doc.getString("description") ?: ""
                PollModel(doc.id, title, description)
            }
            _polls.value = pollModelList
        }
    }

    fun deletePoll(pollId: String, onResult: (Boolean, String) -> Unit) {
        isLoading = true
        errorMessage = null

        val pollRef = db.collection("polls").document(pollId)

        // Step 1: Fetch and delete votes for the poll
        db.collection("votes")
            .whereEqualTo("pollId", pollId)
            .get()
            .addOnSuccessListener { voteSnapshots ->
                // Step 2: Fetch and delete candidates for the poll
                db.collection("polls").document(pollId)
                    .collection("candidates")
                    .get()
                    .addOnSuccessListener { candidateSnapshots ->
                        val batch = db.batch()

                        // Delete all votes
                        voteSnapshots.documents.forEach { voteDoc ->
                            batch.delete(voteDoc.reference)
                        }

                        // Delete all candidates
                        candidateSnapshots.documents.forEach { candidateDoc ->
                            batch.delete(candidateDoc.reference)
                        }

                        // Delete poll document
                        batch.delete(pollRef)

                        // Commit all deletes
                        batch.commit()
                            .addOnSuccessListener {
                                isLoading = false
                                onResult(true, "Poll, votes, and candidates deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = e.message
                                onResult(false, "Failed to delete poll: ${e.message}")
                            }

                    }.addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.message
                        onResult(false, "Failed to fetch candidates: ${e.message}")
                    }

            }.addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.message
                onResult(false, "Failed to fetch votes: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}