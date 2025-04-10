package com.example.smarthome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle

import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
//import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.LoginActivity

//import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*


enum class BottomNavScreen { Home, Add, Stats, Profile }

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

//        if (auth.currentUser == null) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//            return
//        }

        setContent {
            SmartHomeTheme(selectedTheme = ThemeOption.Light) {
                MainAppScaffold(auth)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScaffold(auth: FirebaseAuth) {
    var currentScreen by remember { mutableStateOf(BottomNavScreen.Home) }

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                onAddClick = { currentScreen = BottomNavScreen.Add }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                BottomNavScreen.Home -> HomeScreen(auth)
                BottomNavScreen.Stats -> StatsUsageScreen()
                BottomNavScreen.Profile -> ProfileScreen()
                BottomNavScreen.Add -> AddDeviceScreen()
            }
        }
    }
}
/*
@Composable
fun AppBottomNavigationBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit,
    onAddClick: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        //cutoutShape = CircleShape,
        actions = {
            IconButton(onClick = { onScreenSelected(BottomNavScreen.Home) }) {
                Icon(
                    Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (currentScreen == BottomNavScreen.Home) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onScreenSelected(BottomNavScreen.Stats) }) {
                Icon(
                    Icons.Outlined.BarChart,
                    contentDescription = "Statistics",
                    tint = if (currentScreen == BottomNavScreen.Stats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Device")
            }
        },
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    )
}*/

@Composable
fun AppBottomNavigationBar(
    currentScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit,
    onAddClick: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actions = {
            IconButton(onClick = { onScreenSelected(BottomNavScreen.Home) }) {
                Icon(
                    Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (currentScreen == BottomNavScreen.Home) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onScreenSelected(BottomNavScreen.Stats) }) {
                Icon(
                    Icons.Outlined.BarChart,
                    contentDescription = "Statistics",
                    tint = if (currentScreen == BottomNavScreen.Stats) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Device")
            }
        },
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    )
}

@Composable
fun HomeScreen(auth: FirebaseAuth) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            HomeHeader(auth.currentUser?.displayName ?: "User", currentDate)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HomeHeader(userName: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hi $userName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = { /* Navigate to settings */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsUsageScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Stats Screen")
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen")
    }
}

@Composable
fun AddDeviceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Add Device Screen")
    }
}
