package com.alokkumar.onlinevotingapp.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.viewmodel.admin.MonitorVotesViewModel

@Composable
fun MonitorVotesScreen(navController: NavController, viewModel: MonitorVotesViewModel = viewModel()) {
    val votes by viewModel.votes.collectAsState()
    val context = LocalContext.current
    var voteToDelete by remember { mutableStateOf<String?>(null) }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Monitor Votes", fontSize = 24.sp, modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(votes) { vote ->

//                val pollTitle by viewModel.getPollTitle(vote.pollId).collectAsState()

                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clickable{
                            navController.navigate("${Routes.VOTE_DETAIL}/${vote.voteId}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💈 Poll: ${vote.pollTitle}", fontSize = 18.sp)
                        Text("🧑 ${vote.voterName} ➝ 🗳️ ${vote.candidateName}", fontSize = 18.sp)
                        Text("🕒 ${viewModel.formatTime(vote.voteTime)}", fontSize = 14.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                voteToDelete = vote.voteId
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Vote")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    voteToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { voteToDelete = null },
            title = { Text("Delete Vote") },
            text = { Text("Are you sure you want to delete this vote?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteVote(id, onSuccess = {
                        Toast.makeText(context, "Vote deleted", Toast.LENGTH_SHORT).show()
                        voteToDelete = null},onError = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    })
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { voteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
