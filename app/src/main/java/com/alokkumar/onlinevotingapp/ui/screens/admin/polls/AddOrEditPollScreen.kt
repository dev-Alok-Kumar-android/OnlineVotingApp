package com.alokkumar.onlinevotingapp.ui.screens.admin.polls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.Candidate
import com.alokkumar.onlinevotingapp.viewmodel.admin.polls.AddOrEditPollViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

@Composable
fun AddOrEditPollScreen(
    navController: NavController, pollId: String?,
    viewModel: AddOrEditPollViewModel = viewModel(),
) {
    val context = LocalContext.current

    val title = viewModel.title
    val description = viewModel.description
    val isLoading = viewModel.isLoading
    val candidates = viewModel.candidates

    val isEditMode = pollId != null
    val scrollState = rememberScrollState()

    // Trigger load when pollId is available
    LaunchedEffect(pollId) {
        if (pollId != null) {
            viewModel.loadPoll(pollId, context)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = if (isEditMode) "Edit PollModel" else "Add New PollModel",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${pollId}")
                    }
                ) {
                    Text("Add Candidate")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.title = it },
                label = { Text("PollModel Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.description = it },
                label = { Text("PollModel Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.savePoll(pollId, context) {
                        navController.popBackStack()
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isEditMode) "Update PollModel" else "Create PollModel")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
                if (candidates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ){
                        Text(
                            text = "No Candidates Available, Please Add Candidate",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                CandidateListSection(
                    candidates = candidates,
                    pollId = pollId,
                    navController = navController,
                    db = Firebase.firestore
                )
            }
        }
    }
}



@Composable
fun CandidateListSection(
    candidates: List<Candidate>,
    pollId: String,
    navController: NavController,
    db: FirebaseFirestore,
    modifier: Modifier = Modifier,
    viewModel: AddOrEditPollViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedCandidateForDelete by remember { mutableStateOf<Candidate?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 500.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            items(candidates) { candidate ->
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${pollId}/${candidate.id}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = modifier.padding(12.dp)) {
                        Text("Name: ${candidate.candidateName}", fontSize = 18.sp)
                        Text("Party: ${candidate.party}")
                        Text("Agenda: ${candidate.agenda}")
                        Spacer(modifier = modifier.height(8.dp))
                        Row(
                            modifier = modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                selectedCandidateForDelete = candidate
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedCandidateForDelete?.let { candidate ->
        AlertDialog(
            onDismissRequest = { selectedCandidateForDelete = null },
            title = { Text("Delete Candidate") },
            text = {
                Text(
                    buildAnnotatedString {
                        append("Delete candidate ' ")
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                        ) {
                            append(candidate.candidateName)
                        }
                        append(" '?")
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCandidate(
                        context = context,
                        db = db,
                        pollId = pollId,
                        candidateId = candidate.id,
                        candidateName = candidate.candidateName
                    )
                    selectedCandidateForDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCandidateForDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
