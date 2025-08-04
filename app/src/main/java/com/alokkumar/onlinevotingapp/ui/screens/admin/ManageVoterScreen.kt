package com.alokkumar.onlinevotingapp.ui.screens.admin

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.UserModel
import com.alokkumar.onlinevotingapp.viewmodel.user.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageVoterScreen(
    navController: NavController,
    usersViewModel: UsersViewModel = viewModel(),
) {
    val voters by usersViewModel.users
    val loadingIds = usersViewModel.loadingButtons
    val isLoading by usersViewModel.isLoading
    val errorMessage by usersViewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Voters", style = MaterialTheme.typography.headlineSmall) },
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

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Spacer(Modifier.height(12.dp))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(voters) { user ->
                                val loading = user.uid in loadingIds
                                VoterCard(
                                    user, loading,
                                    onCardClick = { navController.navigate("${Routes.USER_PROFILE}/${user.uid}") },
                                    onToggleVerify = { usersViewModel.toggleVerification(user) },
                                    onToggleDelete = { usersViewModel.toggleDelete(user) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun VoterCard(
    user: UserModel,
    loading: Boolean,
    onCardClick: () -> Unit = {},
    onToggleVerify: () -> Unit,
    onToggleDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.name, style = MaterialTheme.typography.titleMedium)
            Text("Email: ${user.email}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onToggleVerify,
                    enabled = !loading,
                    colors = if (user.verified)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    else
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (loading) "Loading..." else if (user.verified) "Unverify" else "Verify",
                        color = if (user.verified) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                    )
                }

                Button(
                    onClick = onToggleDelete,
                    enabled = !loading,
                    colors = if (user.deleted)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    else
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (loading) "Loading..." else if (user.deleted) "Retrieve" else "Delete",
                        color = if (user.deleted) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}
