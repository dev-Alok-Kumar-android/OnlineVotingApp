package com.alokkumar.onlinevotingapp.viewmodel.user

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.Candidate
import com.alokkumar.onlinevotingapp.model.PollModel
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
    var currentPollModel by mutableStateOf(PollModel())
        private set


    fun loadCandidates(pollId: String) {
        voteDocId = "${userId}_${pollId}"

        // Clear old data first
        candidates.clear()
        selectedCandidateId = null
        selectedCandidateName = ""
        hasVoted = false

        // Step 1: Load Poll
        db.collection("polls").document(pollId)
            .get()
            .addOnSuccessListener { poll ->
                val title = poll.getString("title") ?: ""
                val description = poll.getString("description") ?: ""
                currentPollModel = PollModel(id = pollId, title = title, description = description)

                // Step 2: Load candidates under that poll
                db.collection("polls").document(pollId).collection("candidates")
                    .get()
                    .addOnSuccessListener { result ->
                        val fetchedCandidates = result.map { doc ->
                            Candidate(
                                id = doc.id,
                                candidateName = doc.getString("candidateName") ?: "",
                                party = doc.getString("party") ?: "",
                                agenda = doc.getString("agenda") ?: "",
                                pollModel = currentPollModel,
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                            )
                        }
                        candidates.addAll(fetchedCandidates)
                        Log.d("VoteViewModel", "Loaded candidates: ${candidates.size}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("VoteViewModel", "Error loading candidates: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("VoteViewModel", "Error loading poll info: ${e.message}")
            }

        // Step 3: Check if already voted
        db.collection("votes").document(voteDocId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    hasVoted = true
                    selectedCandidateName = doc.getString("candidateName") ?: "___"
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

            // Step 1: Get voter candidateName from /users/{userId}
            userDocRef.get().addOnSuccessListener { userDoc ->
                val voterName = userDoc.getString("name") ?: "Anonymous"

                // Step 2: Get polls title
                pollDocRef.get().addOnSuccessListener { pollDoc ->
                    val pollModelTitle = pollDoc.getString("title") ?: "Unknown PollModel"

                    // Step 3: Get candidate candidateName
                    candidateDocRef.get().addOnSuccessListener { candidateDoc ->
                        val candidateName =
                            candidateDoc.getString("candidateName") ?: "Unknown Candidate"

                        // Step 4: Create VoteModel and save
                        val vote = VoteModel(
                            voteId = voteDocId,
                            voterName = voterName,
                            candidateName = candidateName,
                            candidateId = selectedCandidateId!!,
                            userId = userId,
                            pollId = pollId,
                            pollTitle = pollModelTitle,
                            timestamp = Timestamp.Companion.now().toDate()
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