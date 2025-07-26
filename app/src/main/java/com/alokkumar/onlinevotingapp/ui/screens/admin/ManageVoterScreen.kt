package com.alokkumar.onlinevotingapp.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alokkumar.onlinevotingapp.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageVoterScreen() {
    val db = FirebaseFirestore.getInstance()
//    val auth = Firebase.auth
    val context = LocalContext.current
    val voterList = remember { mutableStateListOf<UserModel>() }
    val loadingUsers = remember { mutableStateListOf<String>() }
    var voterToDelete by remember { mutableStateOf<UserModel?>(null) }


    DisposableEffect(Unit) {
        val registration = db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                voterList.clear()
                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: ""
                    val isVerified = doc.getBoolean("isVerified") ?: false
                    val email = doc.getString("email") ?: ""
                    val isDeleted = doc.getBoolean("isDeleted") ?: false

                    voterList.add(
                        UserModel(
                            uid = doc.id,
                            name = name,
                            email = email,
                            isVerified = isVerified,
                            isDeleted = isDeleted,
                        )
                    )
                }
            }

        onDispose {
            registration.remove()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manage Voters",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(voterList) { user ->
                val isUserLoading = user.uid in loadingUsers

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Email: ${user.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    loadingUsers.add(user.uid)
                                    db.collection("users")
                                        .document(user.uid)
                                        .update("isVerified", !user.isVerified)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnCompleteListener {
                                            loadingUsers.remove(user.uid)
                                        }
                                },
                                enabled = !isUserLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isVerified) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isUserLoading) "Loading..." else if (user.isVerified) "Unverify" else "Verify",
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = { voterToDelete = user },
                                enabled = !isUserLoading,
                                colors = ButtonDefaults.buttonColors(
                                    if (user.isDeleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isUserLoading) "Loading..." else if (user.isDeleted) "Deleted" else "Delete",
                                    fontSize = 14.sp
                                )
                            }
                        }

                    }
                }
                if (voterToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { voterToDelete = null },
                        confirmButton = {
                            TextButton(onClick = {
                                val user = voterToDelete!!
                                loadingUsers.add(user.uid)
                                db.collection("users").document(user.uid)
                                    .update("isDeleted", true)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Deleted ${user.name}", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnCompleteListener {
                                        loadingUsers.remove(user.uid)
                                        voterToDelete = null
                                    }
                            }) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { voterToDelete = null }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Confirm Deletion") },
                        text = { Text("Are you sure you want to delete voter ${voterToDelete?.name}?") }
                    )
                }
            }
        }
    }
}
