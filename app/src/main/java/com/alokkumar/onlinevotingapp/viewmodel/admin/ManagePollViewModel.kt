package com.alokkumar.onlinevotingapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.PollDocument
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ManagePollViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _polls = MutableStateFlow<List<PollDocument>>(emptyList())
    val polls: StateFlow<List<PollDocument>> = _polls

    private var listener: ListenerRegistration? = null

    init {
        fetchPolls()
    }

    private fun fetchPolls() {
        listener = db.collection("polls").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val pollList = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: return@mapNotNull null
                val description = doc.getString("description") ?: ""
                PollDocument(doc.id, title, description)
            }
            _polls.value = pollList
        }
    }

    fun deletePoll(pollId: String, onResult: (Boolean, String) -> Unit) {
        db.collection("polls").document(pollId)
            .delete()
            .addOnSuccessListener { onResult(true, "Poll deleted successfully") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Unknown error") }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}