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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.R
import com.alokkumar.onlinevotingapp.Routes
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
            items(pollList) { poll ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clickable{ navController.navigate("${Routes.ADD_OR_EDIT_POLL}/${poll.docId}") },
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
                                    showDeleteConfirmationDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }

                            IconButton(onClick = {
                                navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${poll.docId}")
                            }) {
                                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_person_add_alt_1_24),
                                    contentDescription = "Add Candidate")
                            }
                            if (showDeleteConfirmationDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmationDialog = false },
                                    title = { Text("Delete Poll") },
                                    text = {
                                        Text(
                                            buildAnnotatedString {
                                                append("Delete candidate ' ")
                                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = MaterialTheme.typography.titleMedium.fontWeight)) {
                                                    append(poll.title)
                                                }
                                                append(" '")
                                            }
                                        )
                                       },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            db.collection("polls").document(poll.docId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Poll deleted $it", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            showDeleteConfirmationDialog = false
                                        }) {
                                            Text("Delete", color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                                            Text("Cancel"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

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