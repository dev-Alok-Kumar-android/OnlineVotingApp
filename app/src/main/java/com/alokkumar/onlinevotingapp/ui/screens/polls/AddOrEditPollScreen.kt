package com.alokkumar.onlinevotingapp.ui.screens.polls

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.DisposableEffect
import com.alokkumar.onlinevotingapp.ui.screens.common.CandidateListSection
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun AddOrEditPollScreen(navController: NavController, pollId: String?) {
    val db = Firebase.firestore
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isEditMode = pollId != null
    val candidates = remember { mutableStateListOf<Candidate>() }
    val scrollState = rememberScrollState()


    DisposableEffect(pollId) {
        if (pollId == null) return@DisposableEffect onDispose { }

        val pollDocRef = db.collection("polls").document(pollId)
        val pollListener = pollDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(context, "Failed to load poll: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@addSnapshotListener
            }

            snapshot?.let {
                title = it.getString("title") ?: ""
                description = it.getString("description") ?: ""
            }
        }

        val candidateListener = pollDocRef.collection("candidates")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Failed to load candidates: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                snapshot?.let {
                    candidates.clear()
                    for (doc in it.documents) {
                        val candidate = Candidate(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            party = doc.getString("party") ?: "",
                            agenda = doc.getString("agenda") ?: ""
                        )
                        candidates.add(candidate)
                    }
                }
            }

        onDispose {
            pollListener.remove()
            candidateListener.remove()
        }
    }
    Scaffold(
        topBar = {
            Row (modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = if (isEditMode) "Edit Poll" else "Add New Poll",
                    style = MaterialTheme.typography.titleLarge
                )
//                IconButton(
//                    onClick = {
                // Delete this poll
//
//                    }
//                ) {
//                Row{
//                    Icon(
//                        imageVector = Icons.Default.Delete,
//                        contentDescription = "Delete",
//                        tint = MaterialTheme.colorScheme.error
//                    )
//                    Text(text = "Delete", color = MaterialTheme.colorScheme.error)
//                }
//                }
            }

        },
        bottomBar = {
            Row (modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
            Button(
                onClick = {
                    navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${pollId}")
                },
            ) {
                Text("Add Candidate")
            }
            }
        },

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
                .padding(16.dp)
        )
        {

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
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
                onValueChange = { description = it },
                label = { Text("Poll Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title can't be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    if (isEditMode) {
                        db.collection("polls").document(pollId).update(
                            mapOf(
                                "title" to title,
                                "description" to description
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(context, "Poll updated", Toast.LENGTH_SHORT).show()
//                        navController.popBackStack()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }.addOnCompleteListener {
                            isLoading = false
                        }
                    } else {
                        db.collection("polls").add(
                            mapOf(
                                "title" to title,
                                "description" to description
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(context, "Poll added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Creation failed", Toast.LENGTH_SHORT).show()
                        }.addOnCompleteListener {
                            isLoading = false
                        }
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
                    db = db
                )
            }
        }
    }
}
