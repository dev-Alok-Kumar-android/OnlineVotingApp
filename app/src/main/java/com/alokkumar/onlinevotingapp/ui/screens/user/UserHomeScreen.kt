package com.alokkumar.onlinevotingapp.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.PollModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val pollModels = remember { mutableStateListOf<PollModel>() }
    var userName by remember { mutableStateOf("") }

    var isVerified by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .get().addOnSuccessListener { doc ->
                isVerified = doc.getBoolean("verified") ?: false
                isLoading = false
            }
    }

    var searchQuery by remember { mutableStateOf("") }
    val onActiveChange: (Boolean) -> Unit = { isActive ->
        if (!isActive) {
            searchQuery = ""
        }
    }
    val filteredPolls = remember(searchQuery, pollModels) {
        if (searchQuery.isBlank()) {
            pollModels
        } else {
            pollModels.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    val active = searchQuery.isNotBlank()

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name") ?: ""
                }
        }
    }

    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("polls")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val updatedPollModels = snapshot.documents.map { doc ->
                    PollModel(
                        id = doc.id,
                        title = doc.getString("title") ?: "Untitled",
                        description = doc.getString("description") ?: ""
                    )
                }

                pollModels.clear()
                pollModels.addAll(updatedPollModels)
            }

        onDispose {
            listenerRegistration.remove()
        }
    }

    Scaffold(
//        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        Modifier.clickable(
                            onClick = {
                                if (auth.currentUser != null)
                                    navController.navigate("${Routes.USER_PROFILE}/${auth.currentUser?.uid}")
                            }
                        )
                    ) {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "    " + userName.ifBlank { " - User" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

    ) { it ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        else if (!isVerified) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentAlignment = Alignment.Center
            ) {
                Text("Your email is not verified")
                Text("Ask the admin to verify your email", modifier = Modifier.padding(22.dp).align(
                    Alignment.BottomCenter
                ))
            }
            return@Scaffold
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {


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
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { /* handle search if needed */ },
                            expanded = active,
                            onExpandedChange = onActiveChange,
                            placeholder = { Text("Search polls...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    },
                    expanded = active,
                    onExpandedChange = onActiveChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = SearchBarDefaults.inputFieldShape,
                    tonalElevation = SearchBarDefaults.TonalElevation,
                    shadowElevation = SearchBarDefaults.ShadowElevation,
                    windowInsets = SearchBarDefaults.windowInsets,
                    content = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            items(filteredPolls) { poll ->
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
                                        Text(
                                            poll.title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            poll.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.alpha(0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                    items(pollModels) { poll ->
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
                                Text(
                                    poll.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1
                                )
                                Text(
                                    poll.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.alpha(0.7f),
                                    maxLines = 2
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End

                            ) {
                                Button(
                                    onClick = {
                                        navController.navigate("vote_screen/${poll.id}")
                                    },
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text("Vote")
                                }
                                Button(
                                    onClick = {
                                        navController.navigate("result_screen/${poll.id}")
                                    },
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text("Results")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}