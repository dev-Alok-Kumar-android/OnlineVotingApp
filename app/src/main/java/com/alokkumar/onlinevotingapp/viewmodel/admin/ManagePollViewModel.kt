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
    var isLoading: Boolean by mutableStateOf(false)
    var errorMessage: String? by mutableStateOf(null)

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
        val pollRef = db.collection("polls").document(pollId)

        // Step 1: Delete associated votes
        db.collection("votes")
            .whereEqualTo("pollId", pollId)
            .get()
            .addOnSuccessListener { voteSnapshots ->
                val batch = db.batch()

                voteSnapshots.documents.forEach { voteDoc ->
                    batch.delete(voteDoc.reference)
                }

                // Step 2: Delete poll after votes
                batch.delete(pollRef)

                batch.commit()
                    .addOnSuccessListener {
                        onResult(true, "Poll and its votes deleted successfully")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to delete poll votes: ${e.message}")
                    }

            }.addOnFailureListener { e ->
                onResult(false, "Failed to fetch votes: ${e.message}")
            }
    }


    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}