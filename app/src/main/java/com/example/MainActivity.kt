package com.example.smarthome

import android.annotation.SuppressLint
// import android.content.Intent
import androidx.navigation.NavGraph.Companion.findStartDestination
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // For back button
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight // Needed for FontWeight settings
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import java.text.SimpleDateFormat
import java.util.* // Needed for Date and Locale



class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            SmartHomeTheme(selectedTheme = ThemeOption.Light) {
                MainAppScaffold(auth)
            }
        }
    }
}


//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    navController: NavHostController,
    onAddClick: () -> Unit
) {
    BottomAppBar(
        // Combine modifiers: Set height first, then clip
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,

        // contentPadding = PaddingValues(horizontal = 8.dp)
        actions = {
            // Home Icon
            NavigationBarItem(
                selected = currentRoute == AppDestinations.HOME,
                onClick = {
                    if (currentRoute != AppDestinations.HOME) {
                        navController.navigate(AppDestinations.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                label = { Text("Home") },
                alwaysShowLabel = false // Hide label for unselected items
            )

            // Stats Icon
            NavigationBarItem(
                selected = currentRoute == AppDestinations.STATS,
                onClick = {
                    if (currentRoute != AppDestinations.STATS) {
                        navController.navigate(AppDestinations.STATS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Statistics") },
                label = { Text("Stats") },
                alwaysShowLabel = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(2.dp, 2.dp, 2.dp, 2.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    )
}

object AppDestinations {
    const val HOME = "home"
    const val STATS = "stats"
    const val ADD_DEVICE = "add_device"
    //const val PROFILE = "profile"
    const val ROOM_DETAIL = "room_detail"
    const val ROOM_NAME_ARG = "roomName"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScaffold(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState() // Use this for current route
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in listOf(
        AppDestinations.HOME,
        AppDestinations.STATS,
        AppDestinations.ADD_DEVICE // Keep showing on Add if it's a main tab
        // AppDestinations.PROFILE // Remove profile from list if it's not reachable via bottom bar
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute ?: AppDestinations.HOME, // Default
                    navController = navController,
                    onAddClick = { navController.navigate(AppDestinations.ADD_DEVICE) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(AppDestinations.HOME) {
                HomeScreen(
                    auth = auth,
                    onNavigateToRoom = { roomName ->
                        navController.navigate("${AppDestinations.ROOM_DETAIL}/$roomName")
                    }
                )
            }
            composable(AppDestinations.STATS) { StatsUsageScreen() }
            composable(AppDestinations.ADD_DEVICE) { AddDeviceScreen() }
            // composable(AppDestinations.PROFILE) { ProfileScreen() }

            composable(
                route = "${AppDestinations.ROOM_DETAIL}/{${AppDestinations.ROOM_NAME_ARG}}",
                arguments = listOf(navArgument(AppDestinations.ROOM_NAME_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val roomName = backStackEntry.arguments?.getString(AppDestinations.ROOM_NAME_ARG)
                RoomDetailScreen(
                    navController = navController,
                    roomName = roomName ?: "Unknown Room"
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    onNavigateToRoom: (String) -> Unit // Callback to navigate
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }
    val rooms = listOf("Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage")
    val globalTemperature = "18°C"
    val globalHumidity = "65%"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        // Add more bottom padding if needed due to shorter bottom bar potentially overlapping last item
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // --- Header ---
        item {
            HomeHeader(auth.currentUser?.displayName ?: "User", currentDate)
            // Increased spacing after header
            Spacer(modifier = Modifier.height(30.dp))
        }

        // --- Room Selector ---
        item {
            // Text(
            // text = "Select Room",
            // style = MaterialTheme.typography.titleMedium,
            // fontWeight = FontWeight.Medium,
            // modifier = Modifier.padding(bottom = 12.dp),
            // color = MaterialTheme.colorScheme.onSurfaceVariant
            // )
            RoomSelector(
                rooms = rooms,
                onRoomSelected = onNavigateToRoom // Use the navigation callback
            )
            // Increased spacing after Room Selector
            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- Global Conditions ---
        item {
            // Text(
            // text = "Current Conditions",
            // style = MaterialTheme.typography.titleMedium, // Slightly larger title
            // fontWeight = FontWeight.Medium,
            // modifier = Modifier.padding(bottom = 12.dp), // More padding below title
            // color = MaterialTheme.colorScheme.onSurfaceVariant
            // )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                InfoWidget(
                    icon = Icons.Filled.Thermostat,
                    label = "Temperature",
                    value = globalTemperature,
                    modifier = Modifier.weight(1f)
                )
                InfoWidget(
                    icon = Icons.Filled.WaterDrop,
                    label = "Humidity",
                    value = globalHumidity,
                    modifier = Modifier.weight(1f)
                )
            }
            // Increased spacing
            Spacer(modifier = Modifier.height(32.dp))
        }


        /*
        item {
            Text(
                text = "Favorites", // Title for this sub-section
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DeviceCard(...)
                DeviceCard(...)
                DeviceCard(...)
            }
            Spacer(modifier = Modifier.height(20.dp)) // Space after favorites
        }
        */

        // --- Quick Scenes ---
        item {
            // Text(
            // text = "shortcuts",
            // style = MaterialTheme.typography.titleMedium, // Slightly larger title
            // fontWeight = FontWeight.Medium,
            // modifier = Modifier.padding(bottom = 12.dp), // More padding below title
            // color = MaterialTheme.colorScheme.onSurfaceVariant
            // )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Slightly more space between buttons
                contentPadding = PaddingValues(horizontal = 2.dp) // Keep small edge padding
            ) {
                item { SceneButton(icon = Icons.Default.WbSunny, label = "Morning", onClick = { /* TODO */ }) }
                item { SceneButton(icon = Icons.Default.WorkOutline, label = "Away", onClick = { /* TODO */ }) }
                item { SceneButton(icon = Icons.Default.Weekend, label = "Relax", onClick = { /* TODO */ }) }
                item { SceneButton(icon = Icons.Default.Bedtime, label = "Night", onClick = { /* TODO */ }) }
            }
            // Spacer at the very end inside the LazyColumn might not be needed
            // if contentPadding.bottom is sufficient. Remove if desired.
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---Room Detail Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    navController: NavHostController,
    roomName: String
) {
    var temperature by remember { mutableStateOf(20f) }
    var isLightOn by remember { mutableStateOf(true) }
    var isDoorLocked by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(roomName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Go back
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp), // Add content padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Controls for $roomName", style = MaterialTheme.typography.headlineSmall)

            // --- Temperature Control ---
            ControlCard(title = "Temperature", icon = Icons.Default.Thermostat) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${temperature.toInt()}°C", style = MaterialTheme.typography.titleLarge)
                    Slider(
                        value = temperature,
                        onValueChange = { newValue ->
                            temperature = newValue
                            // TODO: Send temperature update to your backend/device
                        },
                        valueRange = 10f..40f,
                        steps = 29 // (40-10) / 1 = 30 steps, means 29
                    )
                }
            }

            // --- Light Control ---
            ControlCard(title = "Main Light", icon = Icons.Default.Lightbulb) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (isLightOn) "On" else "Off", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isLightOn,
                        onCheckedChange = { newState ->
                            isLightOn = newState
                            // TODO: Send light state update to your backend/device
                        }
                    )
                }
            }

            // --- Door Control ---
            if (roomName == "Garage" || roomName == "Front Door") {
                ControlCard(title = "Door Lock", icon = Icons.Default.Lock) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isDoorLocked) "Locked" else "Unlocked", style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                isDoorLocked = true
                                // TODO: Send lock command
                            }, enabled = !isDoorLocked) { Text("Lock") }
                            Button(onClick = {
                                isDoorLocked = false
                                // TODO: Send unlock command
                            }, enabled = isDoorLocked) { Text("Unlock") }
                        }
                        /* // Alternative: Using a Switch
                        Switch(
                            checked = isDoorLocked,
                            onCheckedChange = { newState ->
                                isDoorLocked = newState
                                // TODO: Send door lock state update
                            }
                        ) */
                    }
                }
            }

        }
    }
}

// Helper Composable for structuring controls in cards
@Composable
fun ControlCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Inject the specific control content here
            content()
        }
    }
}


// --- Helper Data Classes/Enums
enum class DeviceType { Light, Thermostat, DoorLock, Generic }

data class DeviceState(
    val icon: ImageVector,
    val name: String,
    val status: String, // e.g., "On", "Off", "21°C", "Locked"
    val type: DeviceType,
    val currentTemperature: Float? = null,
    val isLightOn: Boolean? = null,
    val isLocked: Boolean? = null
)


@Composable
fun RoomSelector(
    rooms: List<String>,
    selectedRoomInitial: String? = null,
    onRoomSelected: (String) -> Unit // Callback when a room is clicked
) {
    var selectedRoom by remember { mutableStateOf(selectedRoomInitial) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(rooms) { room ->
            val isSelected = selectedRoom == room
            Surface(
                modifier = Modifier
                    .width(130.dp)
                    .height(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { // Handle clicks on the room item
                        selectedRoom = room
                        onRoomSelected(room)
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFD7C1C3),
                border = BorderStroke(1.dp, Color(0xFF4A0010)),
                shadowElevation = 4.dp
                /*
                color = if (isSelected) Color(0xFFCCB5BA) else Color(0xFFEAD1DC),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Color(0xFFB3A0A4) else Color(0xFFD8C2C6)
                ),
                shadowElevation = if (isSelected) 4.dp else 2.dp
                */
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = room,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        // color = if (isSelected) Color.White else Color(0xFF6B4E57),
                        color = Color(0xFF000000),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(userName: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically, // Align items vertically
        horizontalArrangement = Arrangement.SpaceBetween // Space out children (User info vs Settings)
    ) {
        // Left side: User Avatar and Greeting
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle, // User avatar icon
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp) // Size of the avatar
                    .clip(CircleShape) // Clip to a circle
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Background color
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Icon tint
            )
            Spacer(modifier = Modifier.width(12.dp)) // Space between avatar and text
            // Column for Greeting and Date
            Column {
                Text(
                    text = "Hi $userName", // Greeting text
                    style = MaterialTheme.typography.titleLarge, // Style for greeting
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = date, // Current date text
                    style = MaterialTheme.typography.bodyMedium, // Style for date
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Right side: Settings Button
        IconButton(onClick = { /* TODO: Implement settings navigation */ }) {
            Icon(
                imageVector = Icons.Default.Settings, // Settings icon
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Icon color
            )
        }
    }
}
@Composable
fun InfoWidget(
    icon: ImageVector, // Icon to display (e.g., Thermostat, WaterDrop)
    label: String, // Text label (e.g., "Temperature")
    value: String, // Value to display (e.g., "23°C")
    modifier: Modifier = Modifier // Modifier for layout control (like weight)
) {
    Card(
        modifier = modifier
            .height(IntrinsicSize.Min), // Adjust height to content
        shape = RoundedCornerShape(12.dp), // Rounded corners for the card
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Background color
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Slight shadow
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Internal padding
                .fillMaxWidth(), // Fill width inside the card
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label, // Accessibility description
                tint = MaterialTheme.colorScheme.primary, // Use primary color for icon
                modifier = Modifier.size(20.dp) // Size of the icon
            )
            Spacer(modifier = Modifier.height(8.dp)) // Space between icon and label
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium, // Style for the label
                color = MaterialTheme.colorScheme.onSurfaceVariant // Color for the label
            )
            Spacer(modifier = Modifier.height(4.dp)) // Space between label and value
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium, // Style for the value (slightly larger)
                fontWeight = FontWeight.Bold, // Make value bold
                color = MaterialTheme.colorScheme.onSurface // Color for the value
            )
        }
    }
}
@Composable
fun DeviceCard(
    icon: ImageVector,
    name: String,
    status: String, // e.g., "On", "Off", "21°C"
    modifier: Modifier = Modifier,
    // Optional: Add later for interactive elements like toggles
    // isOn: Boolean? = null,
    // onToggle: ((Boolean) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(), // Make card take full width available in its container
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)), // Slightly transparent variant
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(IntrinsicSize.Min), // Ensure row adjusts height but columns can align
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Push elements apart
        ) {
            // Left side: Icon and Name/Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp) // Slightly larger icon
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) // Slightly faded status
                    )
                }
            }
            // Right side: Placeholder for potential controls (like a Switch)
            // if (isOn != null && onToggle != null) {
            // Switch(checked = isOn, onCheckedChange = onToggle)
            // } else {
            // Optional: Add a chevron or other indicator if clickable later
            // Icon(Icons.Filled.ChevronRight, contentDescription = null)
            // }
        }
    }
}

@Composable
fun SceneButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton( // Use OutlinedButton for a less prominent look than filled Button
        onClick = onClick,
        modifier = modifier.height(50.dp), // Give button a decent height
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium) // Use label style
        }
    }
}


// --- Placeholder Screens (Keep as is or enhance later) ---
@Composable fun StatsUsageScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Stats Screen") } }
@Composable fun ProfileScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Profile Screen") } }
@Composable fun AddDeviceScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Add Device Screen") } }
