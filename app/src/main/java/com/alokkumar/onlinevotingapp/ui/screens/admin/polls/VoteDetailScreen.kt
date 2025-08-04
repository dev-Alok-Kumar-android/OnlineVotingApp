package com.alokkumar.onlinevotingapp.ui.screens.admin.polls

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alokkumar.onlinevotingapp.viewmodel.admin.MonitorVotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteDetailScreen(
    navController: NavHostController,
    voteId: String,
    modifier: Modifier = Modifier
) {
    val viewModel: MonitorVotesViewModel = viewModel()
    val votes by viewModel.votes.collectAsState()
    val vote = votes.find { it.voteId == voteId }
    val context = LocalContext.current
    var delete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vote Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        delete = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {

                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (vote == null) {
                Text("Vote not found", modifier = Modifier.padding(16.dp))
            }

            Text("Voter Name: ${vote?.voterName}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Candidate Name: ${vote?.candidateName}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vote Time: ${viewModel.formatTime(vote?.timestamp)}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vote ID: ${vote?.voteId}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("PollModel ID: ${vote?.pollId}")
        }
    }

    if (delete) {
       AlertDialog(
           onDismissRequest = {
               delete = false
           },
           title = { Text("Delete Vote") },
           text = { Text("Are you sure you want to delete this vote?") },
           confirmButton = {
               TextButton(onClick = {
                   viewModel.deleteVote(voteId, onSuccess = {
                       Toast.makeText(context, "Vote deleted", Toast.LENGTH_SHORT).show()
                       navController.popBackStack()
                   }, onError = {
                       Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                       delete = false
                   })
               }) {
                   Text("Delete", color = MaterialTheme.colorScheme.error)
               }
           },
           dismissButton = {
               TextButton(onClick = {
                   delete = false
               }) {
                   Text("Cancel")
               }
           }
       )
   }
}