package com.alokkumar.onlinevotingapp.ui.screens.polls

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PollResultScreen(navController: NavController, pollId: String) {
    var results by remember { mutableStateOf<List<Pair<String, Int>>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("polls").document(pollId)
                .collection("candidates").get().await()
            results = snapshot.documents.map {
                val name = it.getString("name") ?: "Unknown"
                val votes = it.getLong("votes")?.toInt() ?: 0
                name to votes
            }
        } catch (e: Exception) {
            results = emptyList()
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "POLL RESULTS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (results.isNullOrEmpty()) {
            Text(
                text = "No results found.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        } else {
            results!!.sortedByDescending { it.second }.forEach { (name, votes) ->
                ResultRow(candidateName = name, voteCount = votes)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun ResultRow(candidateName: String, voteCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = candidateName, fontSize = 18.sp)
        Text(text = "$voteCount votes", fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}
