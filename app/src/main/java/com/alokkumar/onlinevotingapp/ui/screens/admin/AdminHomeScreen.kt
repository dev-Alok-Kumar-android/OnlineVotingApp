package com.alokkumar.onlinevotingapp.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.R
import com.alokkumar.onlinevotingapp.Routes
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AdminHomeScreen(navController: NavController) {

    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WELCOME ADMIN",
            style = TextStyle(
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdminMenuItem(
                    title = "MANAGE VOTER",
                    iconRes = R.drawable.outline_person_raised_hand_24,
                    onClick = { navController.navigate("manage_voter") }
                )

                AdminMenuItem(
                    title = "MANAGE POLLS",
                    iconRes = R.drawable.outline_groups_24,
                    onClick = { navController.navigate("manage_polls") }
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdminMenuItem(
                    title = "VIEW STATS",
                    iconRes = R.drawable.outline_table_chart_24,
                    onClick = { }
                )

                AdminMenuItem(
                    title = "Monitor Votes",
                    iconRes = R.drawable.outline_visibility_24,
                    onClick = { }
                )
            }
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                AdminMenuItem(
                    title = "POLL RESULTS",
                    iconRes = R.drawable.baseline_bar_chart_24,
                    onClick = {  }
                )



                AdminMenuItem(
                    title = "LOGOUT",
                    iconRes = R.drawable.baseline_logout_24,
                    onClick = {
                        // Handle logout logic here
                        showLogoutDialog = true
                    }
                )
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Logout") },
                        text = { Text("Are you sure you want to logout?") },
                        confirmButton = {
                            TextButton(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Routes.AUTH) {
                                    popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                                }
                                showLogoutDialog = false
                            }) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AdminMenuItem(title: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .height(130.dp)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                .size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminHomeScreenPreview() {
    AdminHomeScreen(navController = NavController(LocalContext.current))
}