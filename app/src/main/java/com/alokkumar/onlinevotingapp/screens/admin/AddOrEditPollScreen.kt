package com.alokkumar.onlinevotingapp.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun AddOrEditPollScreen(navController: NavController, pollId: String?) {
    val db = Firebase.firestore
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf( pollId != null)}

    // Load existing poll if editing
    LaunchedEffect(pollId) {
        if (pollId != null) {
            isLoading = true
            db.collection("polls").document(pollId).get()
                .addOnSuccessListener { doc ->
                    title = doc.getString("title") ?: ""
                    description = doc.getString("description") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load poll", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (isEditMode) "Edit Poll" else "Add New Poll",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            singleLine = true,

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            onValueChange = { title = it },
            label = { Text("Poll Title") },
            modifier = Modifier.fillMaxWidth()
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
                    db.collection("polls").document(pollId!!).update(
                        mapOf(
                            "title" to title,
                            "description" to description
                        )
                    ).addOnSuccessListener {
                        Toast.makeText(context, "Poll updated", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
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
    }
}
