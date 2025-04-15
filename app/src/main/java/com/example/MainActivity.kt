package com.example.smarthome

import android.annotation.SuppressLint
// import android.content.Intent
import androidx.navigation.NavGraph.Companion.findStartDestination
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent // Import AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith // Use togetherWith infix function
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi // Needed for Pager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager // Import Pager
import androidx.compose.foundation.pager.PagerState      // Import PagerState
import androidx.compose.foundation.pager.rememberPagerState // Import rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.automirrored.filled.ArrowBack // Not needed now
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
import androidx.compose.ui.graphics.Color // Keep for specific cases if needed, but prefer theme colors
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch // For launching coroutines
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            // Using Light theme as requested, ensure your ThemeOption enum/logic exists
            SmartHomeTheme(selectedTheme = ThemeOption.Light) {
                MainAppScaffold(auth)
            }
        }
    }
}

// --- AppBottomNavigationBar (Keep as is or refine theme colors if needed) ---
@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    navController: NavHostController,
    onAddClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        // Use theme colors
        containerColor = MaterialTheme.colorScheme.surface, // Or surfaceContainer for elevation effect
        contentColor = MaterialTheme.colorScheme.onSurface,
        actions = {
            NavigationBarItem(
                selected = currentRoute == AppDestinations.HOME,
                onClick = { /* ... navigation logic ... */ },
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                label = { Text("Home") },
                alwaysShowLabel = false,
                // Customize colors for better blend if needed
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // Subtle indicator
                )
            )
            NavigationBarItem(
                selected = currentRoute == AppDestinations.STATS,
                onClick = { /* ... navigation logic ... */ },
                icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Statistics") },
                label = { Text("Stats") },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // Subtle indicator
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary, // Keep primary for FAB action
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp) // Slightly more elevation
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    )
}

// --- AppDestinations (No Room Detail) ---
object AppDestinations {
    const val HOME = "home"
    const val STATS = "stats"
    const val ADD_DEVICE = "add_device"
}

// --- MainAppScaffold (Remove Room Detail Nav) ---
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScaffold(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        AppDestinations.HOME,
        AppDestinations.STATS,
        AppDestinations.ADD_DEVICE
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute ?: AppDestinations.HOME,
                    navController = navController,
                    onAddClick = { navController.navigate(AppDestinations.ADD_DEVICE) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Base background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME,
            modifier = Modifier.padding(paddingValues) // Apply padding from Scaffold
        ) {
            composable(AppDestinations.HOME) {
                HomeScreen(auth = auth) // Pass auth, no navigation callback needed
            }
            composable(AppDestinations.STATS) { StatsUsageScreen() }
            composable(AppDestinations.ADD_DEVICE) { AddDeviceScreen() }
            // No RoomDetailScreen composable needed here
        }
    }
}

// --- HomeScreen Modifications ---
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class) // Needed for Pager & AnimatedContent
@Composable
fun HomeScreen(auth: FirebaseAuth) {
    val currentDate = remember {
        // More welcoming date format
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }
    val userName = auth.currentUser?.displayName?.substringBefore(" ") ?: "User" // Get first name if available

    val rooms = remember { listOf("Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage") }
    val globalTemperature = "18°C"
    val globalHumidity = "65%"

    // State to track the *selected* room. Null means no room is selected.
    var selectedRoomName by remember { mutableStateOf<String?>(null) }
    val pagerState = rememberPagerState { rooms.size }
    val coroutineScope = rememberCoroutineScope()

    // --- Synchronization Effects for Pager and Selection ---
    // --- Synchronization Effects for Pager and Selection ---

    // Effect 1: Syncs Selection -> Pager Scroll (Keep as is)
    LaunchedEffect(selectedRoomName) {
        selectedRoomName?.let { room ->
            val index = rooms.indexOf(room)
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }
        // If selectedRoomName becomes null (deselected), you might want to scroll pager to 0
        // but since it will be hidden, it might not matter visually.
        // else if (pagerState.currentPage != 0) {
        //     pagerState.animateScrollToPage(0) // Optional reset
        // }
    }

    // Effect 2: Syncs Pager Swipe -> Selection (Modify this one)
    // Add selectedRoomName as a key AND check if it's non-null inside
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, selectedRoomName) {
        // *** Only run the update logic IF a room is already selected ***
        // This prevents the initial load (where selectedRoomName is null) from
        // immediately setting selectedRoomName based on the default pager page (0).
        if (selectedRoomName != null && !pagerState.isScrollInProgress) {
            val currentPagerRoom = rooms.getOrNull(pagerState.currentPage)
            // Update selectedRoomName only if the settled page is different
            // from the currently selected room (meaning the user swiped the pager).
            if (selectedRoomName != currentPagerRoom) {
                selectedRoomName = currentPagerRoom
            }
        }
    }

    // --- Main Layout: LazyColumn allows scrolling if content overflows ---
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background
            .padding(horizontal = 16.dp), // Consistent horizontal padding
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp) // More bottom padding needed due to bottom bar height
    ) {
        // --- Header (Always Visible) ---
        item {
            HomeHeader(userName = userName, date = currentDate)
            Spacer(modifier = Modifier.height(28.dp)) // Adjusted spacing
        }

        // --- Room Selector (Always Visible) ---
        item {
            Text(
                text = "Rooms", // Simple title for the section
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium, // Slightly less heavy than Bold
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant // Subtle title color
            )
            RoomSelector(
                rooms = rooms,
                selectedRoom = selectedRoomName, // Pass the selected room for highlighting
                onRoomSelected = { roomName ->
                    // If clicking the *already selected* room, deselect it
                    selectedRoomName = if (selectedRoomName == roomName) null else roomName
                }
            )
            Spacer(modifier = Modifier.height(28.dp)) // Adjusted spacing
        }

        // --- Conditional Content Area (Animated) ---
        item {
            // AnimatedContent switches between the two states below
            AnimatedContent(
                targetState = selectedRoomName, // Animate based on whether a room is selected (null vs non-null)
                label = "ContentAreaSwitcher",
                transitionSpec = {
                    // Define transitions for a smoother appearance/disappearance
                    if (targetState != null && initialState == null) {
                        // Content appearing (Pager)
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    } else if (targetState == null && initialState != null){
                        // Content disappearing (Pager hiding, default appearing)
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    } else {
                        // No change or simultaneous change (shouldn't happen often here)
                        fadeIn() togetherWith fadeOut()
                    } using SizeTransform(clip = false) // Prevent clipping during size change
                }
            ) { currentSelectedRoom ->
                if (currentSelectedRoom != null) {
                    // --- STATE 1: Room Selected - Show Pager ---
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            // Let the content determine height, up to a point? Or fixed height?
                            // Using wrapContentHeight() allows flexibility but might jump around.
                            // A fixed height might be smoother visually. Let's try semi-fixed.
                            .heightIn(min = 300.dp, max = 500.dp), // Allow some flexibility
                        pageSpacing = 16.dp, // Space between pages
                        contentPadding = PaddingValues(vertical = 8.dp) // Padding above/below pager content
                    ) { pageIndex ->
                        val roomNameForPage = rooms[pageIndex]
                        key(roomNameForPage) { // Ensure state resets correctly if needed per page
                            RoomControls(roomName = roomNameForPage) // Pass the room name
                        }
                    }
                } else {
                    // --- STATE 2: No Room Selected - Show Default Widgets ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Overview", // Title for this section
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // --- Global Conditions ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp) // Slightly less space
                        ) {
                            InfoWidget(
                                icon = Icons.Filled.Thermostat,
                                label = "Temperature",
                                value = globalTemperature,
                                modifier = Modifier.weight(1f)
                            )
                            InfoWidget(
                                icon = Icons.Filled.WaterDrop, // Corrected icon name
                                label = "Humidity",
                                value = globalHumidity,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))

                        // --- Quick Scenes ---
                        Text(
                            text = "Quick Scenes", // Title for this section
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp), // Adjust spacing
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item { SceneButton(icon = Icons.Default.WbSunny, label = "Morning", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.WorkOutline, label = "Away", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.Weekend, label = "Relax", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.Bedtime, label = "Night", onClick = { /* TODO */ }) }
                        }
                        Spacer(modifier = Modifier.height(16.dp)) // Final spacer in this column
                    }
                }
            }
        }
    }
}


// --- RoomSelector (Styled) ---
@Composable
fun RoomSelector(
    rooms: List<String>,
    selectedRoom: String?, // Current selection state from HomeScreen
    onRoomSelected: (String) -> Unit // Callback to HomeScreen
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp) // Small edge padding for the row
    ) {
        items(rooms) { room ->
            val isSelected = selectedRoom == room
            val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer // Use theme color
            else MaterialTheme.colorScheme.surfaceVariant // Theme color for unselected
            val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
            val targetBorderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant // Subtle border
            val targetFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium // Adjust weight

            Surface(
                modifier = Modifier
                    // Consider minWidth if names vary greatly, or keep fixed width
                    .widthIn(min = 110.dp) // Allow some flexibility
                    .height(45.dp)
                    .clickable { onRoomSelected(room) }, // Notify HomeScreen of click
                shape = RoundedCornerShape(12.dp), // Consistent rounded corners
                color = targetContainerColor, // Apply theme color
                contentColor = targetContentColor, // Apply theme color for text/icon
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp, // Slightly thicker border if selected
                    color = targetBorderColor
                ),
                shadowElevation = if (isSelected) 4.dp else 1.dp // More shadow if selected
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp), // Adjust padding
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Optional: Add an icon? E.g., based on room name
                    // Icon(Icons.Default.Chair, contentDescription = null, modifier = Modifier.size(16.dp))
                    // Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = room,
                        style = MaterialTheme.typography.labelLarge, // Slightly larger label style
                        fontWeight = targetFontWeight, // Apply dynamic font weight
                        // Color is inherited from Surface contentColor
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// --- HomeHeader (Styled) ---
@Composable
fun HomeHeader(userName: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp), // Reduced top padding slightly
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
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Subtle background
                tint = MaterialTheme.colorScheme.primary // Use primary color for accent
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    // More welcoming greeting
                    text = "Hello, $userName!",
                    style = MaterialTheme.typography.headlineSmall, // Slightly smaller headline
                    fontWeight = FontWeight.Normal, // Less heavy greeting
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = date, // Formatted date
                    style = MaterialTheme.typography.bodyMedium, // Standard body style
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Subtle date color
                )
            }
        }
        IconButton(onClick = { /* TODO: Settings Action */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Subtle icon color
            )
        }
    }
}


// --- InfoWidget (Styled) ---
@Composable
fun InfoWidget(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(IntrinsicSize.Min), // Adjust height
        shape = RoundedCornerShape(16.dp), // Slightly more rounded
        // Use a slightly different surface color for visual separation
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Minimal elevation
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp) // Adjusted padding
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                // Use secondary or primary color for icon accent
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp) // Slightly larger icon
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Subtle label color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium, // Good size for value
                fontWeight = FontWeight.SemiBold, // Slightly less heavy than Bold
                color = MaterialTheme.colorScheme.onSurface // Clear value color
            )
        }
    }
}


// --- SceneButton (Styled) ---
@Composable
fun SceneButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Using FilledTonalButton for a subtle but contained look
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp), // Standard height
        shape = RoundedCornerShape(12.dp), // Consistent rounding
        contentPadding = PaddingValues(horizontal = 12.dp), // Adjust padding if needed
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer, // Use theme color
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp) // Minimal elevation
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium, // Good style for button labels
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// --- RoomControls (Contains the Pager Item Content) ---
@Composable
fun RoomControls(roomName: String) {
    // State using remember(key) - resets when swiping *away* and *back* to the same room
    // For persistent state within the session, hoist this state to HomeScreen or a ViewModel.
    var temperature by remember(roomName) { mutableStateOf(20f) }
    var isLightOn by remember(roomName) { mutableStateOf(true) }
    var isDoorLocked by remember(roomName) { mutableStateOf(true) } // Example state

    // Column containing controls for *one* room, displayed within the Pager
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the space given by the Pager
            .padding(horizontal = 4.dp), // Add horizontal padding within the page
        verticalArrangement = Arrangement.spacedBy(16.dp), // Space between control cards
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Optional: Display room name subtly within the controls area
        /* Text(
             roomName,
             style = MaterialTheme.typography.titleSmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             modifier = Modifier.padding(bottom = 8.dp)
         ) */

        // --- Temperature Control Card ---
        ControlCard(title = "Temperature", icon = Icons.Default.Thermostat) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineMedium, // Larger display for temp
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = temperature,
                    onValueChange = { newValue ->
                        temperature = newValue
                        // TODO: Update actual device/backend for roomName
                    },
                    valueRange = 10f..30f, // Adjusted range example
                    steps = 19, // (30-10)/1 = 20 steps -> 19 divisions
                    colors = SliderDefaults.colors( // Use theme colors for slider
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        }

        // --- Light Control Card ---
        ControlCard(title = "Main Light", icon = Icons.Default.Lightbulb) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (isLightOn) "On" else "Off",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isLightOn,
                    onCheckedChange = { newState ->
                        isLightOn = newState
                        // TODO: Update actual device/backend for roomName
                    },
                    colors = SwitchDefaults.colors( // Use theme colors for switch
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        }

        // --- Door Control Card (Conditional) ---
        if (roomName == "Garage" || roomName == "Office") { // Example condition
            ControlCard(title = "Security Lock", icon = Icons.Default.Lock) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isDoorLocked) "Locked" else "Unlocked",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Using toggle buttons for lock/unlock can be clearer
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                isDoorLocked = true
                                // TODO: Send lock command for roomName
                            },
                            enabled = !isDoorLocked,
                            border = BorderStroke(1.dp, if (!isDoorLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) { Text("Lock") }
                        OutlinedButton(
                            onClick = {
                                isDoorLocked = false
                                // TODO: Send unlock command for roomName
                            },
                            enabled = isDoorLocked,
                            border = BorderStroke(1.dp, if (isDoorLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) { Text("Unlock") }
                    }
                }
            }
        }
        // Add more ControlCards based on roomName or device types in that room...
    }
}


// --- ControlCard (Styled) ---
@Composable
fun ControlCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Consistent rounding
        // Use a slightly elevated surface color for cards
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Minimal elevation
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) { // Standard padding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = title,
                    // Use primary or secondary color for the icon accent
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp) // Standard icon size
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium, // Clear title style
                    fontWeight = FontWeight.SemiBold, // Good weight for card titles
                    color = MaterialTheme.colorScheme.onSurface // Ensure readability
                )
            }
            Spacer(modifier = Modifier.height(12.dp)) // Space before content
            // Inject the specific control content (Slider, Switch, etc.)
            content()
        }
    }
}


// --- Other Helpers (Keep as is or ensure they use theme colors) ---
enum class DeviceType { Light, Thermostat, DoorLock, Generic }
// data class DeviceState(...)

// --- Placeholder Screens (Keep as is) ---
@Composable fun StatsUsageScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Stats Screen") } }
@Composable fun ProfileScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Profile Screen") } }
@Composable fun AddDeviceScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Add Device Screen") } }
