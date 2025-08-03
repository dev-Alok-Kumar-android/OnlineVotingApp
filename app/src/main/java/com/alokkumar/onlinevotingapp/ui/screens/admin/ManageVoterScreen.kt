package com.alokkumar.onlinevotingapp.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.model.UserModel
import com.alokkumar.onlinevotingapp.viewmodel.user.UsersViewModel

@Composable
fun ManageVoterScreen(
    navController: NavController,
    usersViewModel: UsersViewModel = viewModel()
) {
    val voters by usersViewModel.users
    val loadingIds = usersViewModel.loadingIds
    val isLoading by usersViewModel.isLoading
    val errorMessage by usersViewModel.errorMessage

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Manage Voters", style = MaterialTheme.typography.headlineSmall)

        if (errorMessage != null) {
            LaunchedEffect(errorMessage) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                usersViewModel.errorMessage.value = null
            }
        }

        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(voters) { user ->
                val loading = user.uid in loadingIds
                VoterCard(user, loading,
                    onToggleVerify = { usersViewModel.toggleVerification(user) },
                    onDelete = { usersViewModel.deleteUser(user) }
                )
            }
        }
    }
}


@Composable
fun VoterCard(
    user: UserModel,
    loading: Boolean,
    onToggleVerify: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (loading) "Loading..." else if (user.verified) "Unverify" else "Verify")
                }

                Button(
                    onClick = onDelete,
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (loading) "Loading..." else "Delete")
                }
            }
        }
    }
}
