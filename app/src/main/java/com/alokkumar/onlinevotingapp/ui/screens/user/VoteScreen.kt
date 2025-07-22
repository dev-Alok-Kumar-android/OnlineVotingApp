package com.alokkumar.onlinevotingapp.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


@Composable
fun VoteScreen(navController: NavController, pollId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val candidates = remember { mutableStateListOf<Candidate>() }
    var selectedCandidateId by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }
    val selected = candidates.find { it.id == selectedCandidateId }
    var isSubmitting by remember { mutableStateOf(false) }
    var selectedCandidateName by remember { mutableStateOf("") }

    val userId = auth.currentUser?.uid ?: ""
    val voteDocId = "${userId}_${pollId}"

    // Fetch candidates and check vote
    LaunchedEffect(pollId) {
        // Fetch candidates
        db.collection("polls").document(pollId)
            .collection("candidates")
            .get()
            .addOnSuccessListener { result ->
                candidates.clear()
                for (doc in result) {
                    val candidate = Candidate(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        party = doc.getString("party") ?: "",
                        agenda = doc.getString("agenda") ?: ""
                    )
                    candidates.add(candidate)
                }
            }

        // Check if user already voted
        db.collection("votes").document(voteDocId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    hasVoted = true
                    selectedCandidateName = doc.getString("candidateName") ?: ""
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Vote", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (hasVoted) {
            Text(
                text = "You have already voted in this poll to $selectedCandidateName",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (candidates.isEmpty()){
            Text(
                text = "No candidates found",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(candidates) { candidate ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Name: ${candidate.name}", fontSize = 18.sp)
                        Text("Party: ${candidate.party}")
                        Text("Agenda: ${candidate.agenda}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                selectedCandidateId = candidate.id
                            },
                            enabled = !hasVoted && selectedCandidateId != candidate.id
                        ) {
                            Text(
                                if (selectedCandidateId == candidate.id)
                                    "Selected"
                                else "Select"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selected != null && !hasVoted && !isSubmitting) {
                    isSubmitting = true
                    db.collection("votes").document(voteDocId)
                        .set(
                            mapOf(
                                "pollId" to pollId,
                                "candidateId" to selectedCandidateId,
                                "candidateName" to selected.name,
                                "userId" to userId
                            )
                        )

                        .addOnSuccessListener {
                            isSubmitting = false
                            // 🔁 Increment candidate's vote count
                            db.collection("polls")
                                .document(pollId)
                                .collection("candidates")
                                .document(selectedCandidateId!!)
                                .update("votes", FieldValue.increment(1))

                                .addOnSuccessListener {
                                    Toast.makeText(context, "Vote submitted", Toast.LENGTH_SHORT).show()
                                    hasVoted = true
                                    selectedCandidateName = selected.name
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Vote recorded, but failed to update count", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            isSubmitting = false
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }

            },
            enabled = selectedCandidateId != null && !hasVoted && !isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isSubmitting) "Submitting..." else if (hasVoted) "Voted" else "Submit Vote")
        }
    }
}
