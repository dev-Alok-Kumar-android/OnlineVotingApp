package com.alokkumar.onlinevotingapp.viewmodel.admin

import android.util.Log
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

        init {
            loadVotes()
        }

        private fun loadVotes() {
            voteListener = db.collection("votes")
                .orderBy("voteTime", Query.Direction.DESCENDING)
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
                                voteTime = doc.getDate("voteTime")
                            )
                        } catch (e: Exception) {
                            Log.e("MonitorVotesVM", "Error parsing vote: ${e.message}")
                            null
                        }
                    }

                    _votes.value = votes
                }
        }

    fun getVoteById(voteId: String): VoteModel? {
        return _votes.value.find { it.voteId == voteId }
    }


    fun deleteVote(voteId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            db.collection("votes").document(voteId)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Error deleting vote") }
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