package com.alokkumar.onlinevotingapp.viewmodel.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.Candidate
import com.alokkumar.onlinevotingapp.model.Poll
import com.alokkumar.onlinevotingapp.model.VoteModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VoteViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _votes = MutableStateFlow<List<VoteModel>>(emptyList())
    val votes: StateFlow<List<VoteModel>> = _votes
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    var candidates = mutableStateListOf<Candidate>()
        private set

    var selectedCandidateId by mutableStateOf<String?>(null)
    var selectedCandidateName by mutableStateOf("")
    var hasVoted by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)
    private var voteDocId = ""


    fun loadCandidates(pollId: String) {
        voteDocId = "${userId}_${pollId}"
        var currentPoll by mutableStateOf(Poll())

        db.collection("polls").get().addOnSuccessListener { result ->
            val poll = result.documents.find { it.id == pollId }
            val title = poll?.getString("title") ?: ""
            val description = poll?.getString("description") ?: ""
            currentPoll = Poll(id = pollId, title = title, description = description)
        }

        // Fetch candidates
        db.collection("polls")
            .document(pollId)
            .collection("candidates")
            .get()
            .addOnSuccessListener { result ->
                candidates.clear()
                for (doc in result) {
                    val candidate = Candidate(
                        id = doc.id,
                        name = doc.getString("voterName") ?: "",
                        party = doc.getString("party") ?: "",
                        agenda = doc.getString("agenda") ?: "",
                        poll = currentPoll,
                        timestamp = Timestamp.Companion.now()
                    )
                    candidates.add(candidate)
                }
            }

        // Check if already voted
        db.collection("votes").document(voteDocId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    hasVoted = true
                    selectedCandidateName = doc.getString("name") ?: ""
                }
            }
    }


    fun submitVote(pollId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val selected = candidates.find { it.id == selectedCandidateId }

        if (selected != null && !hasVoted && !isSubmitting) {
            isSubmitting = true

            val userDocRef = db.collection("users").document(userId)
            val pollDocRef = db.collection("polls").document(pollId)
            val candidateDocRef =
                pollDocRef.collection("candidates").document(selectedCandidateId!!)

            // Step 1: Get voter name from /users/{userId}
            userDocRef.get().addOnSuccessListener { userDoc ->
                val voterName = userDoc.getString("name") ?: "Anonymous"

                // Step 2: Get polls title
                pollDocRef.get().addOnSuccessListener { pollDoc ->
                    val pollTitle = pollDoc.getString("title") ?: "Unknown Poll"

                    // Step 3: Get candidate name
                    candidateDocRef.get().addOnSuccessListener { candidateDoc ->
                        val candidateName =
                            candidateDoc.getString("voterName") ?: "Unknown Candidate"

                        // Step 4: Create VoteModel and save
                        val vote = VoteModel(
                            voteId = voteDocId,
                            voterName = voterName,
                            candidateName = candidateName,
                            userId = userId,
                            pollId = pollId,
                            pollTitle = pollTitle,
                            voteTime = Timestamp.Companion.now().toDate()
                        )

                        db.collection("votes").document(voteDocId)
                            .set(vote)
                            .addOnSuccessListener {
                                // Step 5: Increment vote count
                                candidateDocRef.update("votes", FieldValue.increment(1))
                                    .addOnSuccessListener {
                                        hasVoted = true
                                        selectedCandidateName = candidateName
                                        isSubmitting = false
                                        onSuccess()
                                    }
                                    .addOnFailureListener {
                                        isSubmitting = false
                                        onFailure("Vote saved, but failed to update count.")
                                    }
                            }
                            .addOnFailureListener {
                                isSubmitting = false
                                onFailure("Failed to submit vote: ${it.message}")
                            }

                    }.addOnFailureListener {
                        isSubmitting = false
                        onFailure("Failed to get candidate info.")
                    }

                }.addOnFailureListener {
                    isSubmitting = false
                    onFailure("Failed to get polls info.")
                }

            }.addOnFailureListener {
                isSubmitting = false
                onFailure("Failed to get voter info.")
            }
        }
    }
}