package com.alokkumar.onlinevotingapp.screens.user

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.model.CandidateResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ResultScreen(pollId: String, navController: NavController) {
    var results by remember { mutableStateOf<List<CandidateResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    // Set up real-time result updates with cleanup
    DisposableEffect(pollId) {
        var listenerRegistration: ListenerRegistration? = null
        var isCancelled = false

        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val candidateSnapshot = db.collection("polls")
                    .document(pollId)
                    .collection("candidates")
                    .get()
                    .await()

                if (isCancelled) return@launch

                val candidateList = candidateSnapshot.documents.mapNotNull {
                    val id = it.id
                    val name = it.getString("name") ?: return@mapNotNull null
                    val party = it.getString("party") ?: "Independent"
                    Triple(id, name, party)
                }

                listenerRegistration = db.collection("votes")
                    .whereEqualTo("pollId", pollId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener

                        val voteCounts = snapshot.documents
                            .mapNotNull { it.getString("candidateId") }
                            .groupingBy { it }
                            .eachCount()

                        val liveResults = candidateList.map { (id, name, party) ->
                            CandidateResult(name = name, party = party, votes = voteCounts[id] ?: 0)
                        }.sortedByDescending { it.votes }

                        results = liveResults
                        isLoading = false
                    }
            } catch (e: Exception) {
                Log.e("ResultScreen", "ResultScreen: ${e.message}",e)
                isLoading = false
            }
        }

        onDispose {
            isCancelled = true
            listenerRegistration?.remove()
            job.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Election Results", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (results.isEmpty()) {
                Text("No results found.")
            } else {
                LazyColumn {
                    items(results) { result ->
                        ResultCard(result)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}




@Composable
fun ResultCard(candidate: CandidateResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${candidate.name}", style = MaterialTheme.typography.titleMedium)
            Text("Party: ${candidate.party}")
            Text("Votes: ${candidate.votes}")
        }
    }
}
