package com.alokkumar.onlinevotingapp.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.viewmodel.admin.MonitorVotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorVotesScreen(
    navController: NavController,
    viewModel: MonitorVotesViewModel = viewModel(),
) {
    val votes by viewModel.votes.collectAsState()
    val context = LocalContext.current
    var voteToDelete by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val onActiveChange: (Boolean) -> Unit = { isActive ->
        if (!isActive) {
            searchQuery = ""
        }
    }
    val filteredVotes = remember(searchQuery, votes) {
        if (searchQuery.isBlank()) {
            votes
        } else {
            votes.filter {
                it.voteId.contains(searchQuery, ignoreCase = true) ||
                        it.voterName.contains(searchQuery, ignoreCase = true) ||
                        it.candidateName.contains(searchQuery, ignoreCase = true) ||
                        it.pollTitle.contains(searchQuery, ignoreCase = true) ||
                        viewModel.formatTime(it.timestamp).contains(searchQuery, ignoreCase = true)

            }
        }
    }
    val activeSearch = searchQuery.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor Votes", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { /* handle search if needed */ },
                        expanded = activeSearch,
                        onExpandedChange = onActiveChange,
                        placeholder = { Text("Search votes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = activeSearch,
                onExpandedChange = onActiveChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = SearchBarDefaults.inputFieldShape,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets,
                content = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(filteredVotes) { vote ->
                            Card(
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate("${Routes.VOTE_DETAIL}/${vote.voteId}")
                                    }
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        vote.pollTitle,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        "${vote.voterName} 🗳️ ${vote.candidateName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.alpha(0.7f)
                                    )
                                }
                            }
                        }
                    }
                },
            )

            // Votes UI
            when {
                viewModel.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                viewModel.errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }

                votes.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No votes found", fontSize = 18.sp)
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(votes) { vote ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .clickable { navController.navigate("${Routes.VOTE_DETAIL}/${vote.voteId}") },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💈 Poll: ${vote.pollTitle}", fontSize = 18.sp)
                                Text(
                                    "🧑 ${vote.voterName} ➝ 🗳️ ${vote.candidateName}",
                                    fontSize = 18.sp
                                )
                                Text("🕒 ${viewModel.formatTime(vote.timestamp)}", fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        voteToDelete = vote.voteId
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Vote"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    voteToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { voteToDelete = null },
            title = { Text("Delete Vote") },
            text = { Text("Are you sure you want to delete this vote?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteVote(id, onSuccess = {
                        Toast.makeText(context, "Vote deleted", Toast.LENGTH_SHORT).show()
                        voteToDelete = null
                    }, onError = {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        voteToDelete = null
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
