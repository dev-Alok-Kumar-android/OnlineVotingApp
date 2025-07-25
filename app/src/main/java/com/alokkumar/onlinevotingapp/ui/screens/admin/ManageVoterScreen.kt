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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alokkumar.onlinevotingapp.model.VoterDocument
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManageVoterScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val voterList = remember { mutableStateListOf<VoterDocument>() }
    val loadingUsers = remember { mutableStateListOf<String>() } // List of docIds being updated

    // Load voters from Firestore
    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener

            voterList.clear()
            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: ""
                val voterId = doc.getString("uid") ?: ""
                val isVerified = doc.getBoolean("isVerified") ?: false

                voterList.add(VoterDocument(doc.id, name, voterId, isVerified))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Voters", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(voterList) { user ->
                val isUserLoading = user.docId in loadingUsers

                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(2f)
                        ) {
                            Text("Name: ${user.name}", fontWeight = FontWeight.SemiBold)
                            Text("ID: ${user.voterId}")
                        }
                        Button(
                            onClick = {
                                loadingUsers.add(user.docId)
                                db.collection("users")
                                    .document(user.docId)
                                    .update("isVerified", !user.isVerified)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnCompleteListener {
                                        loadingUsers.remove(user.docId)
                                    }
                            },
                            enabled = !isUserLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isVerified) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isUserLoading) "Loading..." else if (user.isVerified) "Unverify" else "Verify")
                        }
                    }
                }
            }
        }
    }
}