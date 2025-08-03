package com.alokkumar.onlinevotingapp.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.R
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.model.PollDocument
import com.alokkumar.onlinevotingapp.viewmodel.admin.ManagePollViewModel

@Composable
fun ManagePollScreen(
    navController: NavController,
    viewModel: ManagePollViewModel = viewModel()
) {
    val context = LocalContext.current
    val pollList by viewModel.polls.collectAsState()
    var pollToDelete by remember { mutableStateOf<PollDocument?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manage Polls",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(pollList) { poll ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("${Routes.ADD_OR_EDIT_POLL}/${poll.docId}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = poll.title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = poll.description)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                pollToDelete = poll
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                            IconButton(onClick = {
                                navController.navigate("${Routes.ADD_OR_EDIT_CANDIDATE}/${poll.docId}")
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_person_add_alt_1_24),
                                    contentDescription = "Add Candidate"
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.navigate(Routes.ADD_OR_EDIT_POLL) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add New Poll")
        }
    }

    if (pollToDelete != null) {
        AlertDialog(
            onDismissRequest = { pollToDelete = null },
            title = { Text("Delete Poll") },
            text = {
                Text(buildAnnotatedString {
                    append("Delete polls ‘")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)) {
                        append(pollToDelete?.title ?: "")
                    }
                    append("’?")
                })
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePoll(pollToDelete!!.docId) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                    pollToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pollToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}