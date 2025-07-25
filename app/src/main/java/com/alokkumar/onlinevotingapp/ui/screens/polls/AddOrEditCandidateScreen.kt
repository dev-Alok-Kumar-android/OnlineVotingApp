package com.alokkumar.onlinevotingapp.ui.screens.polls

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AddOrEditCandidateScreen(
    navController: NavController,
    pollId: String,
    candidateId: String? = null
) {
    var name by remember { mutableStateOf("") }
    var party by remember { mutableStateOf("") }
    var agenda by remember { mutableStateOf("") }
    var votes by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val isEditMode = candidateId != null

    // Load candidate if in edit mode
    LaunchedEffect(Unit) {
        if (isEditMode) {
            try {
                val doc = db.collection("polls").document(pollId)
                    .collection("candidates").document(candidateId)
                    .get().await()

                name = doc.getString("name") ?: ""
                party = doc.getString("party") ?: ""
                agenda = doc.getString("agenda") ?: ""
                votes = doc.getLong("votes")?.toInt() ?: 0
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load candidate $e.message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isEditMode) "Edit Candidate" else "Add New Candidate",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Candidate Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = party,
            onValueChange = { party = it },
            label = { Text("Party Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = agenda,
            onValueChange = { agenda = it },
            label = { Text("Agenda") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || party.isBlank() || agenda.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isSubmitting = true

                val candidateMap = hashMapOf(
                    "name" to name,
                    "party" to party,
                    "agenda" to agenda,
                    "votes" to votes  // will be 0 in add mode, loaded value in edit mode
                )

                val candidateCollection = db.collection("polls")
                    .document(pollId)
                    .collection("candidates")

                if (isEditMode) {
                    candidateCollection.document(candidateId).update(candidateMap as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Candidate updated", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { isSubmitting = false }
                } else {
                    candidateCollection.add(candidateMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Candidate added", Toast.LENGTH_SHORT).show()
                            name = ""
                            party = ""
                            agenda = ""
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { isSubmitting = false }
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isSubmitting) "Submitting..." else "Submit")
        }
    }
}
