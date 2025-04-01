package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Temporarily disable login check for interface development
        /*
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        */

        setContent {
            SmartHomeTheme {
                MainScreen(auth)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(auth: FirebaseAuth) {
    var selectedScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hi ${auth.currentUser?.displayName ?: "User"}", color = Color(0xFFEFB8C8)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                actions = {
                    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
                    Text(text = currentDate, modifier = Modifier.padding(end = 16.dp), color = Color(0xFFEFB8C8))
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedScreen) { screen ->
                selectedScreen = screen
            }
        },
        containerColor = Color(0xFFFFF8E1) // Eggshell background color
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedScreen) {
                Screen.Home -> HomeScreen()
                Screen.Stats -> StatsScreen()
                Screen.Settings -> SettingsScreen()
                Screen.Notifications -> NotificationsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color(0xFFEFB8C8)
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            label = { Text("Stats") },
            selected = selectedScreen == Screen.Stats,
            onClick = { onScreenSelected(Screen.Stats) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
            label = { Text("Notifications") },
            selected = selectedScreen == Screen.Notifications,
            onClick = { onScreenSelected(Screen.Notifications) }
        )
    }
}

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Home!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF625b71),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoomButton("Kitchen", Icons.Default.Kitchen)
            RoomButton("Bedroom", Icons.Default.Bed)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RoomButton("Bathroom", Icons.Default.Bathtub)
            RoomButton("Living Room", Icons.Default.Living)
        }
    }
}

@Composable
fun RoomButton(roomName: String, icon: ImageVector) {
    FloatingActionButton(
        onClick = { /* Handle navigation to the specific room screen */ },
        shape = CircleShape,
        modifier = Modifier.size(100.dp),
        containerColor = Color(0xFFFFC1E3)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = roomName, tint = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = roomName,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun StatsScreen() {

}

@Composable
fun SettingsScreen() {
}

@Composable
fun NotificationsScreen() {
}

enum class Screen {
    Home, Stats, Settings, Notifications
}