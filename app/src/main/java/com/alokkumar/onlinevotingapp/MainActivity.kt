package com.alokkumar.onlinevotingapp

import android.app.KeyguardManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alokkumar.onlinevotingapp.ui.theme.OnlineVotingAppTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            OnlineVotingAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Firebase.auth.addAuthStateListener {
                        val user = it.currentUser
                        Log.d("StartupAuth", "Auth state: ${if (user != null) "User is logged in: ${user.email}" else "No user session (null)" }")
                    }
                    AppNavigation(Modifier
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background))
                }
            }
        }
    }
}

@Preview(showBackground = true,showSystemUi = true)
@Composable
private fun AppPreview() {
    AppNavigation()
}