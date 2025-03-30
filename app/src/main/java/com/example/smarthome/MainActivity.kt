package com.example.smarthome

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.smarthome.ui.theme.SmartHomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHomeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedScreen) { screen ->
                selectedScreen = screen
            }
        }
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
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(ImageVector.vectorResource(id = R.drawable.ic_home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) }
        )
        NavigationBarItem(
            icon = { Icon(ImageVector.vectorResource(id = R.drawable.ic_stats), contentDescription = "Stats") },
            label = { Text("Stats") },
            selected = selectedScreen == Screen.Stats,
            onClick = { onScreenSelected(Screen.Stats) }
        )
        NavigationBarItem(
            icon = { Icon(ImageVector.vectorResource(id = R.drawable.ic_settings_24dp), contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedScreen == Screen.Settings,
            onClick = { onScreenSelected(Screen.Settings) }
        )
        NavigationBarItem(
            icon = { Icon(ImageVector.vectorResource(id = R.drawable.ic_notification), contentDescription = "Notifications") },
            label = { Text("Notifications") },
            selected = selectedScreen == Screen.Notifications,
            onClick = { onScreenSelected(Screen.Notifications) }
        )
    }
}

@Composable
fun HomeScreen() {
    // Your Home screen content
}

@Composable
fun StatsScreen() {
    // Your Stats screen content
}

@Composable
fun SettingsScreen() {
    // Your Settings screen content
}

@Composable
fun NotificationsScreen() {
    // Your Notifications screen content
}

enum class Screen {
    Home, Stats, Settings, Notifications
}


/*package com.example.smarthome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Redirect to LoginActivity if the user is not logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            SmartHomeTheme {
                MainScreen(
                    database = database,
                    auth = auth,
                    onLogout = {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    database: DatabaseReference,
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))

                // User profile section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = auth.currentUser?.email ?: "User",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Navigation items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == "Home",
                    onClick = {
                        currentScreen = "Home"
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Device Settings") },
                    label = { Text("Device Settings") },
                    selected = currentScreen == "Settings",
                    onClick = {
                        currentScreen = "Settings"
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Energy Stats") },
                    label = { Text("Energy Stats") },
                    selected = currentScreen == "Stats",
                    onClick = {
                        currentScreen = "Stats"
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Notifications") },
                    selected = currentScreen == "Notifications",
                    onClick = {
                        currentScreen = "Notifications"
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(Modifier.weight(1f))
                Divider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { onLogout() }
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Add refresh action */ }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "Home" -> SmartHomeDashboard(database)
                    "Settings" -> DeviceSettingsScreen(database)
                    "Stats" -> EnergyStatsScreen()
                    "Notifications" -> NotificationsScreen()
                }
            }
        }
    }
}

@Composable
fun SmartHomeDashboard(database: DatabaseReference) {
    var livingRoomLight by remember { mutableStateOf(false) }
    var bedroomLight by remember { mutableStateOf(false) }
    var kitchenLight by remember { mutableStateOf(false) }
    var fanSpeed by remember { mutableStateOf(50f) }
    var temperature by remember { mutableStateOf(22) }
    var humidity by remember { mutableIntStateOf(45) }

    // Animation for card alpha
    val infiniteTransition = rememberInfiniteTransition()
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Set up real-time listeners for Firebase data
    LaunchedEffect(Unit) {
        // Lights
        val lightingRef = database.child("lights")

        lightingRef.child("livingRoom").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                livingRoomLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        lightingRef.child("bedroom").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bedroomLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        lightingRef.child("kitchen").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kitchenLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fan
        database.child("devices/fan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fanSpeed = (snapshot.getValue(Int::class.java) ?: 50).toFloat()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Sensors
        val sensorsRef = database.child("sensors")

        sensorsRef.child("temperature").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                temperature = snapshot.getValue(Int::class.java) ?: 22
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        sensorsRef.child("humidity").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                humidity = snapshot.getValue(Int::class.java) ?: 45
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Main UI layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smart Home Dashboard",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Environment cards (temperature and humidity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(animatedAlpha)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Temperature",
                            tint = Color(0xFFE91E63)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Temperature", color = Color.Gray)
                        Text(text = "$temperature°C", color = Color.Black, fontSize = 22.sp)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(animatedAlpha)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Humidity",
                            tint = Color(0xFF03A9F4)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Humidity", color = Color.Gray)
                        Text(text = "$humidity%", color = Color.Black, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lights section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lighting Control",
                        color = Color.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DeviceSwitch("Living Room", livingRoomLight, Icons.Default.LightbulbCircle) { isOn ->
                        livingRoomLight = isOn
                        database.child("lights/livingRoom").setValue(isOn)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DeviceSwitch("Bedroom", bedroomLight, Icons.Default.Lightbulb) { isOn ->
                        bedroomLight = isOn
                        database.child("lights/bedroom").setValue(isOn)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    DeviceSwitch("Kitchen", kitchenLight, Icons.Default.Lightbulb) { isOn ->
                        kitchenLight = isOn
                        database.child("lights/kitchen").setValue(isOn)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fan control
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AirplanemodeActive,
                            contentDescription = "Fan",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fan Control",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = fanSpeed,
                        onValueChange = { newValue ->
                            fanSpeed = newValue
                            database.child("devices/fan").setValue(newValue.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Off", color = Color.Gray)
                        Text("${fanSpeed.toInt()}%", color = Color.Black, fontSize = 16.sp)
                        Text("Max", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceSwitch(
    name: String,
    isOn: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = if (isOn) Color(0xFFFFB300) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = name,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFB300),
                checkedTrackColor = Color(0xFFFFB300).copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DeviceSettingsScreen(database: DatabaseReference) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Device Settings",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("This screen would contain detailed settings for each device.")

                // Settings would go here
            }
        }
    }
}

@Composable
fun EnergyStatsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Energy Statistics",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("This screen would display energy usage statistics.")

                // Stats would go here
            }
        }
    }
}

@Composable
fun NotificationsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text("This screen would display system notifications.")

                // Notifications would go here
            }
        }
    }
}
/*
package com.example.smarthome
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // Redirect to LoginActivity if the user is not logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            SmartHomeTheme {
                MainScreen(
                    database = database,
                    auth = auth,
                    onLogout = {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    database: DatabaseReference,
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Home") }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                // User profile section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = auth.currentUser?.email ?: "User",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Divider()
                Spacer(Modifier.height(8.dp))
                // Navigation items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentScreen == "Home",
                    onClick = {
                        currentScreen = "Home"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Device Settings") },
                    label = { Text("Device Settings") },
                    selected = currentScreen == "Settings",
                    onClick = {
                        currentScreen = "Settings"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Energy Stats") },
                    label = { Text("Energy Stats") },
                    selected = currentScreen == "Stats",
                    onClick = {
                        currentScreen = "Stats"
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Notifications") },
                    selected = currentScreen == "Notifications",
                    onClick = {
                        currentScreen = "Notifications"
                        scope.launch { drawerState.close() }
                    }
                )
                Spacer(Modifier.weight(1f))
                Divider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { onLogout() }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {  TODO: Add refresh action  }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    "Home" -> SmartHomeDashboard(database)
                    "Settings" -> DeviceSettingsScreen(database)
                    "Stats" -> EnergyStatsScreen()
                    "Notifications" -> NotificationsScreen()
                }
            }
        }
    }
}
@Composable
fun SmartHomeDashboard(database: DatabaseReference) {
    var livingRoomLight by remember { mutableStateOf(false) }
    var bedroomLight by remember { mutableStateOf(false) }
    var kitchenLight by remember { mutableStateOf(false) }
    var fanSpeed by remember { mutableStateOf(50f) }
    var temperature by remember { mutableStateOf(22) }
    var humidity by remember { mutableStateOf(45) }
    // Animation for card alpha
    val infiniteTransition = rememberInfiniteTransition()
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    // Set up real-time listeners for Firebase data
    LaunchedEffect(Unit) {
        // Lights
        val lightingRef = database.child("lights")
        lightingRef.child("livingRoom").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                livingRoomLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        lightingRef.child("bedroom").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bedroomLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        lightingRef.child("kitchen").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kitchenLight = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        // Fan
        database.child("devices/fan").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fanSpeed = (snapshot.getValue(Int::class.java) ?: 50).toFloat()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        // Sensors
        val sensorsRef = database.child("sensors")
        sensorsRef.child("temperature").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                temperature = snapshot.getValue(Int::class.java) ?: 22
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        sensorsRef.child("humidity").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                humidity = snapshot.getValue(Int::class.java) ?: 45
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    // Main UI layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Smart Home Dashboard",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Environment cards (temperature and humidity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(animatedAlpha)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Temperature",
                            tint = Color(0xFFE91E63)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Temperature", color = Color.Gray)
                        Text(text = "$temperature°C", color = Color.Black, fontSize = 22.sp)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .alpha(animatedAlpha)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Humidity",
                            tint = Color(0xFF03A9F4)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Humidity", color = Color.Gray)
                        Text(text = "$humidity%", color = Color.Black, fontSize = 22.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Lights section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lighting Control",
                        color = Color.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DeviceSwitch("Living Room", livingRoomLight, Icons.Default.LightbulbCircle) { isOn ->
                        livingRoomLight = isOn
                        database.child("lights/livingRoom").setValue(isOn)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DeviceSwitch("Bedroom", bedroomLight, Icons.Default.Lightbulb) { isOn ->
                        bedroomLight = isOn
                        database.child("lights/bedroom").setValue(isOn)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    DeviceSwitch("Kitchen", kitchenLight, Icons.Default.Lightbulb) { isOn ->
                        kitchenLight = isOn
                        database.child("lights/kitchen").setValue(isOn)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Fan control
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AirplanemodeActive,
                            contentDescription = "Fan",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fan Control",
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = fanSpeed,
                        onValueChange = { newValue ->
                            fanSpeed = newValue
                            database.child("devices/fan").setValue(newValue.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Off", color = Color.Gray)
                        Text("${fanSpeed.toInt()}%", color = Color.Black, fontSize = 16.sp)
                        Text("Max", color = Color.Gray)
                    }
                }
            }
        }
    }
}
@Composable
fun DeviceSwitch(
    name: String,
    isOn: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = if (isOn) Color(0xFFFFB300) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isOn,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFB300),
                checkedTrackColor = Color(0xFFFFB300).copy(alpha = 0.5f)
            )
        )
    }
}
@Composable
fun DeviceSettingsScreen(database: DatabaseReference) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Device Settings",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("This screen would contain detailed settings for each device.")
                // Settings would go here
            }
        }
    }
}
@Composable
fun EnergyStatsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Energy Statistics",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("This screen would display energy usage statistics.")
                // Stats would go here
            }
        }
    }
}
@Composable
fun NotificationsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
                )
            )
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("This screen would display system notifications.")
                // Notifications would go here
            }
        }
    }
}*/