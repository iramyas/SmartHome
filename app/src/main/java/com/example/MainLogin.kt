
package com.example.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource // Use this for drawable resources
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.MainActivity
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption // Import the enum
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // 1. Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToMain()
            return // Skip the rest if already logged in
        }

        // 2. Check if it's the first launch
        if (isFirstLaunch()) {
            navigateToWelcome()
            return // Skip the rest if showing welcome screen
        }

        // 3. If not logged in and not first launch, show Login screen
        setContent {
            SmartHomeTheme(selectedTheme = ThemeOption.Light) { // Choose default theme here
                LoginScreen(
                    onLoginSuccess = { navigateToMain() },
                    onSignUpClick = { /* Handle navigation to Sign Up screen */ },
                    auth = auth
                )
            }
        }
    }

    private fun isFirstLaunch(): Boolean {
        val sharedPref = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE)
        // Default to true if the key doesn't exist (meaning it's the first launch)
        return !sharedPref.getBoolean("has_seen_welcome", false)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Prevent going back to Login/Welcome
    }

    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish() // Prevent going back to Login after Welcome
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    auth: FirebaseAuth
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()), // Allow scrolling if content overflows
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {

            // Logo or App Name
            // Replace R.drawable.ic_launcher_foreground with your actual drawable if you have one
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your logo/image
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp) // Adjust size as needed
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )
            // Or use Text:
            /* Text(
                text = "Smart Home Login",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            ) */


            Text(
                text = "Log in to your account",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )


            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim(); errorMessage = null },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = errorMessage != null // Show error state if message exists
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null // Show error state if message exists
            )

            // Error Message Display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null // Clear previous errors
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.message ?: "Login failed. Please try again."
                                // More specific error handling can be added here
                                // e.g., check for specific Firebase exceptions
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary // Spinner color on button
                    )
                } else {
                    Text("Log In", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forgot Password / Sign Up Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { /* TODO: Handle Forgot Password */ }) {
                    Text("Forgot Password?")
                }
                Text("|", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onSignUpClick) {
                    Text("Sign Up")
                }
            }
            Spacer(modifier = Modifier.height(32.dp)) // Space at the bottom
        }
    }
}


@Preview(showBackground = true, name = "Login Light")
@Composable
fun LoginScreenPreviewLight() {
    SmartHomeTheme(selectedTheme = ThemeOption.Light) {
        LoginScreen({}, {}, FirebaseAuth.getInstance()) // Pass dummy instance for preview
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Login Dark")
@Composable
fun LoginScreenPreviewDark() {
    SmartHomeTheme(selectedTheme = ThemeOption.Dark) {
        LoginScreen({}, {}, FirebaseAuth.getInstance()) // Pass dummy instance for preview
    }
}

@Preview(showBackground = true, name = "Login Pink")
@Composable
fun LoginScreenPreviewPink() {
    SmartHomeTheme(selectedTheme = ThemeOption.Pink) {
        LoginScreen({}, {}, FirebaseAuth.getInstance()) // Pass dummy instance for preview
    }
}