package com.alokkumar.onlinevotingapp.screens.user

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.model.Poll
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Composable
fun UserHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val polls = remember { mutableStateListOf<Poll>() }
    var userName by remember { mutableStateOf("") }

    // Fetch user name once
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: ""
                }
        }
    }

    // Real-time listener for polls with cleanup
    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("polls")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val updatedPolls = snapshot.documents.map { doc ->
                    Poll(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        description = doc.getString("description") ?: ""
                    )
                }

                polls.clear()
                polls.addAll(updatedPolls)
            }

        onDispose {
            listenerRegistration.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome, ${userName.ifBlank { "User" }}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Button(
            onClick = {
                showLogoutDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Logout")
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        navController.navigate("auth") {
                            popUpTo("user_home") { inclusive = true }
                        }
                        showLogoutDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )

        }

        suspend fun logOut() {
            coroutineScope {

                FirebaseAuth.getInstance().signOut()
                if (Firebase.auth.currentUser == null) {
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    navController.navigate("auth") {
                        popUpTo("user_home") { inclusive = true }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Available Polls", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(polls) { poll ->
                Card(
                    modifier = Modifier
                        .clickable {
                            navController.navigate("poll_actions/${poll.id}")
                        }
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(poll.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            poll.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }
            }
        }
    }
}
