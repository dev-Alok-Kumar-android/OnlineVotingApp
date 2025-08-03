package com.alokkumar.onlinevotingapp.viewmodel.admin.polls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddOrEditCandidateViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    var isSubmitting by mutableStateOf(false)
    private val _state = MutableStateFlow(Candidate())
    val state: StateFlow<Candidate> = _state

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onPartyChange(value: String) = _state.update { it.copy(party = value) }
    fun onAgendaChange(value: String) = _state.update { it.copy(agenda = value) }

    fun loadCandidate(pollId: String, candidateId: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("polls").document(pollId)
                    .collection("candidates").document(candidateId)
                    .get().await()

                _state.update {
                    it.copy(
                        name = doc.getString("voterName") ?: "",
                        party = doc.getString("party") ?: "",
                        agenda = doc.getString("agenda") ?: "",
                        votes = doc.getLong("votes")?.toInt() ?: 0
                    )
                }
            } catch (e: Exception) {
                // handle error externally via return or state if needed
            }
        }
    }

    fun submitCandidate(
        pollId: String,
        candidateId: String?,
        isEditMode: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val candidate = state.value
        if (candidate.name.isBlank() || candidate.party.isBlank() || candidate.agenda.isBlank()) {
            onFailure("Please fill in all fields")
            return
        }
        isSubmitting = true

        val candidateMap = hashMapOf(
            "voterName" to candidate.name,
            "party" to candidate.party,
            "agenda" to candidate.agenda,
            "votes" to candidate.votes
        )

        val candidateCollection = db.collection("polls")
            .document(pollId)
            .collection("candidates")

        val task = if (isEditMode) {
            candidateCollection.document(candidateId!!).update(candidateMap as Map<String, Any>)
        } else {
            candidateCollection.add(candidateMap)
        }

        task.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it.message ?: "Unknown error")
        }.addOnCompleteListener {
           isSubmitting = false
        }
    }
}
