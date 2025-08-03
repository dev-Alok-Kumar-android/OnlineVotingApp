package com.alokkumar.onlinevotingapp.ui.screens.auth


import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.alokkumar.onlinevotingapp.Routes
import com.alokkumar.onlinevotingapp.viewmodel.auth.AuthViewModel

@Composable
fun UserRegistrationScreen(modifier: Modifier = Modifier,navController: NavController, authViewModel: AuthViewModel = viewModel() ) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.trim().length >= 6

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(22.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome! Register from here to vote...",
            style = TextStyle(
                fontSize = 40.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(30.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full voterName") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotBlank() && !isEmailValid,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            isError = phoneNumber.isNotBlank() && phoneNumber.length != 10,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotBlank() && !isPasswordValid,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotBlank() && confirmPassword != password,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(30.dp))

        val isSignupLoading by authViewModel.signupLoading

        Button(onClick = {
            when {
                email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank() || phoneNumber.isBlank() ->
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()

                password != confirmPassword ->
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()

                else -> {
                    authViewModel.signup(
                        name = name.trim(),
                        email = email.trim(),
                        phone = phoneNumber.trim(),
                        password = password
                    ) {
                        Toast.makeText(context, "Signup successful", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.USER_HOME) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                    authViewModel.signupError.value?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
            Modifier.fillMaxWidth().height(45.dp),
            enabled = !isSignupLoading) {
            Text(if (isSignupLoading) "Signing up..." else "Sign Up")
        }


        Spacer(Modifier.height(40.dp))

        TextButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Already have an account? Login")
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UserRegistrationScreenPreview() {
    UserRegistrationScreen(navController = NavController(LocalContext.current))
}