package com.alokkumar.onlinevotingapp.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.viewmodel.admin.ViewStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewStatsScreen(navController: NavController, viewModel: ViewStatsViewModel = viewModel()) {
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val totalUsers by viewModel.totalUsers
    val verifiedUsers by viewModel.verifiedUsers
    val totalPolls by viewModel.totalPolls
    val totalVotes by viewModel.totalVotes

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📊 View Stats") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text("Error: $error", color = Color.Red)
                else -> {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(title = "👤 Total Users", count = totalUsers){
                            navController.navigate(Routes.MANAGE_VOTER)
                        }
                        StatCard(title = "✅ Verified Users", count = verifiedUsers){
                            navController.navigate(Routes.MANAGE_VOTER)
                        }
                        StatCard(title = "📮 Total Polls", count = totalPolls){
                            navController.navigate(Routes.MANAGE_POLLS)
                        }
                        StatCard(title = "🗳️ Total Votes", count = totalVotes){
                            navController.navigate(Routes.MONITOR_VOTES)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: Int, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("$count", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
