package com.alokkumar.onlinevotingapp.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.viewmodel.user.UsersViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: String,
    usersViewModel: UsersViewModel = viewModel(),
) {
    val user by usersViewModel.userProfile.collectAsState()
    val isLoading by usersViewModel.isLoading
    val btnLoading = user.uid in usersViewModel.loadingButtons
    val error by usersViewModel.errorMessage
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val currentEmail = auth.currentUser?.email ?: ""
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val isAdmin by remember { mutableStateOf(currentEmail == "admin@gmail.com") }
//    val isAdmin by remember { mutableStateOf(true) }; currentEmail

    LaunchedEffect(userId) {
        usersViewModel.listenToUser(userId)
    }

    LaunchedEffect(user) {
        user.let {
            name = it.name
            phone = it.phone
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = user.email,
                            onValueChange = { },
                            label = { Text("Email") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Name and phone fields (editable if current user or admin)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !(userId == currentUserId || isAdmin)
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = !(userId == currentUserId || isAdmin)
                        )

                        // Verified/Deleted status or buttons for admin
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAdmin) {
                                Button(
                                    enabled = !btnLoading,
                                    onClick = {
                                        usersViewModel.toggleVerification(user)
                                    },
                                    colors = if (user.verified)
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    else
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),

                                    ) {
                                    Text(
                                        text = if (btnLoading) "Loading..." else if (user.verified) "Unverify" else "Verify",
                                        color = if (user.verified) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                Button(
                                    enabled = !btnLoading,
                                    onClick = {
                                        usersViewModel.toggleDelete(user)
                                    },
                                    colors = if (user.deleted)
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    else
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                ) {
                                    Text(
                                        text = if (btnLoading) "Loading..." else if (user.deleted) "Retrieve" else "Delete",
                                        color = if (user.deleted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onError
                                    )
                                }
                            } else {
                                Text("Verified: ${user.verified}")
                                Text("Deleted: ${user.deleted}")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save button for current user or admin
                        if (userId == currentUserId || isAdmin) {
                            Button(
                                onClick = {
                                    usersViewModel.updateNameAndPhone(
                                        userId, name, phone,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Profile updated", Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }
}

