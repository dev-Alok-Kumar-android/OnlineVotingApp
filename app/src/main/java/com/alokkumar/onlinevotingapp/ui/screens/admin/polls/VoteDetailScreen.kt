package com.alokkumar.onlinevotingapp.ui.screens.admin.polls

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val vote = viewModel.getVoteById(voteId)

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
                        viewModel.deleteVote(voteId,
                            onSuccess = { navController.popBackStack() },
                            onError = {

                            }
                        )
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        // You can implement edit screen later
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
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
            Text("Poll ID: ${vote?.pollId}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vote Time: ${viewModel.formatTime(vote?.voteTime)}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vote ID: ${vote?.voteId}")
        }
    }
}