/*package com.example.smarthome

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Use this for drawable resources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption // Import the enum

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the theme. You can change ThemeOption.Light to Dark or Pink to test
            SmartHomeTheme(selectedTheme = ThemeOption.Light) {
                WelcomeScreen {
                    // Mark welcome screen as shown
                    markWelcomeShown()
                    // Navigate to Login Activity
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish() // Finish WelcomeActivity so user can't go back to it
                }
            }
        }
    }

    private fun markWelcomeShown() {
        val sharedPref = getSharedPreferences("SmartHomePrefs", Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("has_seen_welcome", true)
            apply()
        }
    }
}

@Composable
fun WelcomeScreen(onGetStartedClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround // Distribute space
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Add some space at the top

            // App Title
            Text(
                text = "Welcome to Your Smart Home",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Placeholder for an image/icon (Replace R.drawable.ic_smart_home_logo with your actual drawable)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your logo/image
                contentDescription = "Smart Home Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(vertical = 24.dp),
                contentScale = ContentScale.Fit
            )

            // Description Text
            Text(
                text = "Control and manage your home devices easily and efficiently.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Spacer(modifier = Modifier.weight(1f)) // Push button towards bottom

            // Get Started Button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium // Use theme shape
            ) {
                Text("Get Started", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp)) // Add some space at the bottom
        }
    }
}

// Basic Preview (Requires a drawable named ic_launcher_foreground or change the painterResource)
@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SmartHomeTheme(selectedTheme = ThemeOption.Light) {
        WelcomeScreen {}
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenPreviewDark() {
    SmartHomeTheme(selectedTheme = ThemeOption.Dark) {
        WelcomeScreen {}
    }
}
*/
