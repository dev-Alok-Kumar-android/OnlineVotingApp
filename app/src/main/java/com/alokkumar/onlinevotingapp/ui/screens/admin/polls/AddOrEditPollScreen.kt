package com.alokkumar.onlinevotingapp.ui.screens.admin.polls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.ui.screens.common.CandidateListSection
import com.alokkumar.onlinevotingapp.viewmodel.admin.polls.AddOrEditPollViewModel
import com.google.firebase.Firebase
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
                    text = if (isEditMode) "Edit Poll" else "Add New Poll",
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
                label = { Text("Poll Title") },
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
                label = { Text("Poll Description") },
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
                Text(if (isEditMode) "Update Poll" else "Create Poll")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditMode) {
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
