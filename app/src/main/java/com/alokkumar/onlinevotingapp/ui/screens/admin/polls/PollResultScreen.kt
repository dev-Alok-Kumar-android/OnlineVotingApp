package com.alokkumar.onlinevotingapp.ui.screens.admin.polls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.viewmodel.admin.polls.PollResultViewModel

@Composable
fun PollResultScreen(
    navController: NavController,
    pollId: String,
    viewModel: PollResultViewModel = viewModel()
) {
    val results by viewModel.results.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Trigger fetch once
    LaunchedEffect(pollId) {
        viewModel.fetchResults(pollId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "POLL RESULTS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        when {
            loading -> CircularProgressIndicator()
            results.isNullOrEmpty() -> Text(
                text = "No results found.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            else -> {
                results!!.sortedByDescending { it.second }.forEach { (name, votes) ->
                    ResultRow(candidateName = name, voteCount = votes)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun ResultRow(candidateName: String, voteCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = candidateName, fontSize = 18.sp)
        Text(text = "$voteCount votes", fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}
