package com.alokkumar.onlinevotingapp.viewmodel.admin.polls

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class AddOrEditPollViewModel : ViewModel() {
    private val db = Firebase.firestore

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    private val _candidates = mutableStateListOf<Candidate>()
    val candidates: List<Candidate> get() = _candidates

    private var pollListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null

    fun loadPoll(pollId: String, context: Context) {
        val pollDocRef = db.collection("polls").document(pollId)

        pollListener?.remove()
        candidateListener?.remove()

        pollListener = pollDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Failed to load polls: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            snapshot?.let {
                title = it.getString("title") ?: ""
                description = it.getString("description") ?: ""
            }
        }

        candidateListener = pollDocRef.collection("candidates")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Failed to load candidates: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let {
                    _candidates.clear()
                    for (doc in it.documents) {
                        val candidate = Candidate(
                            id = doc.id,
                            candidateName = doc.getString("candidateName") ?: "",
                            party = doc.getString("party") ?: "",
                            agenda = doc.getString("agenda") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                        )
                        _candidates.add(candidate)
                    }
                }
            }
    }

    fun savePoll(
        pollId: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (title.isBlank()) {
            Toast.makeText(context, "Title can't be empty", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val data = mapOf("title" to title, "description" to description)

        if (pollId != null) {
            db.collection("polls").document(pollId).update(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "PollModel updated", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    isLoading = false
                }
        } else {
            db.collection("polls").add(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "PollModel added", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Creation failed", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    isLoading = false
                }
        }
    }

    fun deleteCandidate(
        context: Context,
        db: FirebaseFirestore,
        pollId: String,
        candidateId: String,
        candidateName: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val candidateRef = db.collection("polls")
            .document(pollId)
            .collection("candidates")
            .document(candidateId)

        // Step 1: Delete associated votes
        db.collection("votes")
            .whereEqualTo("pollId", pollId)
            .whereEqualTo("candidateId", candidateId)
            .get()
            .addOnSuccessListener { voteSnapshots ->
                val batch = db.batch()

                voteSnapshots.documents.forEach { voteDoc ->
                    batch.delete(voteDoc.reference)
                }

                // Step 2: Delete the candidate
                batch.delete(candidateRef)

                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Candidate '$candidateName' and related votes deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        onError(e)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching votes: ${e.message}", Toast.LENGTH_SHORT).show()
                onError(e)
            }
    }


    override fun onCleared() {
        pollListener?.remove()
        candidateListener?.remove()
        super.onCleared()
    }
}
