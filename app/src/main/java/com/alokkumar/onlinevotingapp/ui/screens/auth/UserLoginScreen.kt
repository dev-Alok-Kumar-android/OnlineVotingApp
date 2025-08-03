package com.alokkumar.onlinevotingapp.ui.screens.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alokkumar.onlinevotingapp.R
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.viewmodel.auth.AuthViewModel

@Composable
fun UserLoginScreen(modifier: Modifier = Modifier, navController: NavController,
                    authViewModel: AuthViewModel = viewModel()) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isLoginLoading by authViewModel.loginLoading
    val loginError by authViewModel.loginError

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(22.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            "Hello you need to login for vote",
            style = TextStyle(
                fontSize = 40.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )

        Image(
            painter = painterResource(R.drawable.baseline_person_24),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )

        Spacer(Modifier.height(30.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            isError = email.isNotBlank() && !isEmailValid,
            label = {
                Text("Enter Email")
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Enter Password")
            },
            isError = password.isNotBlank() && password.length <6,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password

            ),
            visualTransformation = PasswordVisualTransformation()

        )

        Spacer(Modifier.height(2.dp))

        TextButton(
            modifier = Modifier.align(Alignment.End),
            enabled = email.isNotBlank(),
            onClick = {
                authViewModel.resetPassword(email.trim()) { success, errorMessage ->
                    if (success) {
                        Toast.makeText(context, "Password reset link sent to your email", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
            Text("Forgot Password?")
        }

        Spacer(Modifier.height(10.dp))

        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@Button
            } else {
                authViewModel.login(email.trim(), password) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
                authViewModel.loginError.value?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
        },
            Modifier.fillMaxWidth().height(45.dp),
            enabled = !isLoginLoading) {
            Text(if (isLoginLoading) "Logging in..." else "Login")
        }

        loginError?.let {
            LaunchedEffect(it) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                authViewModel.loginError.value = null
            }
        }

        Spacer(Modifier.height(40.dp))

        TextButton(onClick = {
            navController.navigate(Routes.USER_REGISTRATION)
        }) {
            Text("Don't have any account, create one")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    UserLoginScreen(navController = NavController(LocalContext.current))
}