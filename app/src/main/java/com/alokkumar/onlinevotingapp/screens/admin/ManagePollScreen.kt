package com.alokkumar.onlinevotingapp.screens.admin

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.model.PollDocument
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManagePollScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val pollList = remember { mutableStateListOf<PollDocument>() }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Fetch polls from Firestore
    LaunchedEffect(Unit) {
        db.collection("polls").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            pollList.clear()
            for (doc in snapshot.documents) {
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description") ?: ""
                pollList.add(PollDocument(doc.id, title, description))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manage Polls",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pollList) { poll ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = poll.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = poll.description)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                navController.navigate("add_or_edit_poll/${poll.docId}")
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }

                            IconButton(onClick = {
                                    showDeleteConfirmationDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }

                            if (showDeleteConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = false },
                                    title = { Text("Delete Poll") },
                                    text = { Text("Are you sure you want to delete this poll? This action cannot be undone.") },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            // Proceed to delete
                                            db.collection("polls").document(poll.docId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Poll deleted", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            showDeleteConfirmationDialog = false
                                        }) {
                                            Text("Delete")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }

                            IconButton(onClick = {
                                navController.navigate("add_candidate/${poll.docId}")
                            }) {
                                Icon(imageVector = ImageVector.vectorResource(id = com.alokkumar.onlinevotingapp.R.drawable.baseline_person_add_alt_1_24),
                                    contentDescription = "Add Candidate")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("add_or_edit_poll")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add New Poll")
        }
    }
}


@Preview
@Composable
private fun ManagePollScreenPreview() {
    ManagePollScreen(navController = NavController(LocalContext.current))
}