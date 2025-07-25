package com.alokkumar.onlinevotingapp.ui.screens.common


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.Candidate
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CandidateListSection(
    candidates: List<Candidate>,
    pollId: String,
    navController: NavController,
    db: FirebaseFirestore,
    modifier: Modifier = Modifier
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
                    modifier = modifier.fillMaxWidth()
                        .clickable{
                            navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${pollId}/${candidate.id}")
                                  },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = modifier.padding(12.dp)) {
                        Text("Name: ${candidate.name}", fontSize = 18.sp)
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
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = MaterialTheme.typography.titleMedium.fontWeight)) {
                            append(candidate.name)
                        }
                        append(" '")
                    }
                )

            },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("polls").document(pollId)
                        .collection("candidates").document(candidate.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Candidate deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
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
