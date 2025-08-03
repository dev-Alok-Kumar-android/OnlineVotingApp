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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.viewmodel.user.VoteViewModel


@Composable
fun VoteScreen(navController: NavController, pollId: String, viewModel: VoteViewModel = viewModel()) {
    val context = LocalContext.current

    val candidates = viewModel.candidates
    val selectedCandidateId = viewModel.selectedCandidateId
    val hasVoted = viewModel.hasVoted
    val selectedCandidateName = viewModel.selectedCandidateName
    val isSubmitting = viewModel.isSubmitting

    LaunchedEffect(pollId) {
        viewModel.loadCandidates(pollId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Vote", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (hasVoted) {
            Text(
                text = "You have already voted in this polls to $selectedCandidateName",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (candidates.isEmpty()) {
            Text(
                text = "No candidates found",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
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
                                viewModel.selectedCandidateId = candidate.id
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
                viewModel.submitVote(
                    pollId = pollId,
                    onSuccess = {
                        Toast.makeText(context, "Vote submitted", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = selectedCandidateId != null && !hasVoted && !isSubmitting,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                when {
                    isSubmitting -> "Submitting..."
                    hasVoted -> "Voted"
                    else -> "Submit Vote"
                }
            )
        }
    }
}

