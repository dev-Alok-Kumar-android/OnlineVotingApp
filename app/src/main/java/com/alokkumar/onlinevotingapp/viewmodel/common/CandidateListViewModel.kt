package com.alokkumar.onlinevotingapp.viewmodel.common

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.firestore.FirebaseFirestore

class CandidateListViewModel : ViewModel() {

    fun deleteCandidate(
        context: Context,
        db: FirebaseFirestore,
        pollId: String,
        candidateId: String,
        candidateName: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        db.collection("polls")
            .document(pollId)
            .collection("candidates")
            .document(candidateId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Candidate '$candidateName' deleted", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                onError(e)
            }
    }
}
