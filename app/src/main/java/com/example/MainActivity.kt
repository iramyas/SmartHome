@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.smarthome

import android.annotation.SuppressLint
// import android.content.Intent // Not used
import androidx.navigation.NavGraph.Companion.findStartDestination // Needed for bottom nav logic
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler // Import BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            SmartHomeTheme(selectedTheme = ThemeOption.Dark) {
                MainAppScaffold(auth)
            }
        }
    }
}

// --- AppBottomNavigationBar (With Navigation Logic) ---
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        actions = {
            NavigationBarItem(
                selected = currentRoute == AppDestinations.HOME,
                onClick = {
                    // Navigate to Home, ensuring single top and restoring state
                    navController.navigate(AppDestinations.HOME) {
                        // Pop up to the start destination of the graph to avoid building up a large back stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Save state of the popped destinations
                        }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                label = { Text("Home") },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                selected = currentRoute == AppDestinations.STATS,
                onClick = {
                    // Navigate to Stats, ensuring single top and restoring state
                    navController.navigate(AppDestinations.STATS) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Statistics") },
                label = { Text("Stats") },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick, // Navigate to Add Device screen
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    )
}

// --- AppDestinations (Keep as is) ---
object AppDestinations {
    const val HOME = "home"
    const val STATS = "stats"
    const val ADD_DEVICE = "add_device"
    // No Room Detail destination needed if handled within HomeScreen state
}

// --- MainAppScaffold (Keep mostly as is) ---
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScaffold(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if the bottom bar should be shown based on the current route
    val showBottomBar = currentRoute in listOf(
        AppDestinations.HOME,
        AppDestinations.STATS
        // AddDeviceScreen might cover the bottom bar, or you might want it visible
        // Let's assume AddDeviceScreen is full-screen and doesn't need the bottom bar.
        // If you want the bottom bar on AddDeviceScreen, add it back to this list.
    )

    Scaffold(
        bottomBar = {
            // Only show the bottom bar on specified screens
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute ?: AppDestinations.HOME,
                    navController = navController,
                    onAddClick = {
                        // Navigate to Add Device, simple navigation as it's likely a separate flow
                        navController.navigate(AppDestinations.ADD_DEVICE) {
                            // Optionally add launchSingleTop = true if you don't want multiple add device screens
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME,
            // Apply padding ONLY if the bottom bar is shown, otherwise use no padding
            modifier = Modifier.padding(if (showBottomBar) paddingValues else PaddingValues(0.dp))
        ) {
            composable(AppDestinations.HOME) {
                // Pass navController to HomeScreen ONLY IF it needs to navigate elsewhere
                // In this case, it doesn't directly navigate, but manages internal state.
                HomeScreen(auth = auth)
            }
            composable(AppDestinations.STATS) {
                StatsUsageScreen() // Make sure this composable exists
            }
            composable(AppDestinations.ADD_DEVICE) {
                // Pass navController to allow AddDeviceScreen to navigate back or confirm
                AddDeviceScreen(navController = navController) // Make sure this composable exists
            }
        }
    }
}

// --- HomeScreen Modifications (Add BackHandler) ---
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(auth: FirebaseAuth) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }
    val userName = auth.currentUser?.displayName?.substringBefore(" ") ?: "Iram"

    val rooms = remember { listOf("Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage") }
    val globalTemperature = "18°C"
    val globalHumidity = "65%"

    var selectedRoomName by remember { mutableStateOf<String?>(null) }
    val pagerState = rememberPagerState { rooms.size }
    val coroutineScope = rememberCoroutineScope()

    // --- Back Handler ---
    // This will intercept the system back button press ONLY when a room is selected.
    // It sets the selected room back to null, showing the default home content,
    // instead of popping the entire HomeScreen from the navigation stack.
    BackHandler(enabled = selectedRoomName != null) {
        selectedRoomName = null // Go back to the default view
    }


    // --- Synchronization Effects
    LaunchedEffect(selectedRoomName) {
        selectedRoomName?.let { room ->
            val index = rooms.indexOf(room)
            if (index != -1 && index != pagerState.currentPage) {
                // Animate scroll smoothly
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress, selectedRoomName) {
        if (selectedRoomName != null && !pagerState.isScrollInProgress) {
            val currentPagerRoom = rooms.getOrNull(pagerState.currentPage)
            if (selectedRoomName != currentPagerRoom) {
                selectedRoomName = currentPagerRoom
            }
        }
    }

    // --- Main Layout (Keep as is) ---
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        // Adjust bottom padding carefully based on whether bottom bar is present or not.
        // Since padding is now handled by NavHost based on showBottomBar,
        // we might not need explicit large bottom padding here anymore.
        // Test this visually. Let's reduce it for now.
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp) // Reduced bottom padding
    ) {
        // --- Header ---
        item {
            HomeHeader(userName = userName, date = currentDate)
            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- Room Selector ---
        item {
            Text(
                text = "Rooms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RoomSelector(
                rooms = rooms,
                selectedRoom = selectedRoomName,
                onRoomSelected = { roomName ->
                    selectedRoomName = if (selectedRoomName == roomName) null else roomName
                }
            )
            Spacer(modifier = Modifier.height(28.dp))
        }

        // --- Conditional Content Area (Animated) ---
        item {
            AnimatedContent(
                targetState = selectedRoomName,
                label = "ContentAreaSwitcher",
                transitionSpec = {
                    if (targetState != null && initialState == null) {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    } else if (targetState == null && initialState != null){
                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                    } else {
                        fadeIn() togetherWith fadeOut()
                    } using SizeTransform(clip = false)
                }
            ) { currentSelectedRoom ->
                if (currentSelectedRoom != null) {
                    // --- STATE 1: Room Selected - Show Pager ---
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 300.dp, max = 500.dp), // Keep height constraints
                        pageSpacing = 16.dp,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { pageIndex ->
                        // Ensure correct room name is passed based on the page index
                        val roomNameForPage = rooms.getOrNull(pageIndex)
                        if (roomNameForPage != null) {
                            // Use key to help Compose manage state correctly during swipes
                            key(roomNameForPage) {
                                RoomControls(roomName = roomNameForPage)
                            }
                        } else {
                            // Placeholder or error view if index is out of bounds (shouldn't happen)
                            Text("Error: Room not found at index $pageIndex")
                        }
                    }
                } else {
                    // --- STATE 2: No Room Selected - Show Default Widgets ---
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            text = "Quick Scenes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            item { SceneButton(icon = Icons.Default.WbSunny, label = "Morning", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.WorkOutline, label = "Away", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.Weekend, label = "Relax", onClick = { /* TODO */ }) }
                            item { SceneButton(icon = Icons.Default.Bedtime, label = "Night", onClick = { /* TODO */ }) }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


// --- RoomSelector (Keep as is) ---
@Composable
fun RoomSelector(
    rooms: List<String>,
    selectedRoom: String?,
    onRoomSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(rooms) { room ->
            val isSelected = selectedRoom == room
            val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
            val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
            val targetBorderColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
            val targetFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

            Surface(
                modifier = Modifier
                    .widthIn(min = 110.dp)
                    .height(45.dp)
                    .clickable { onRoomSelected(room) },
                shape = RoundedCornerShape(12.dp),
                color = targetContainerColor,
                contentColor = targetContentColor,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = targetBorderColor
                ),
                shadowElevation = if (isSelected) 4.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = room,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = targetFontWeight,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// --- HomeHeader (Keep as is) ---
@Composable
fun HomeHeader(userName: String, date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hello, $userName!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = { /* TODO: Settings Action */ }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// --- InfoWidget (Keep as is) ---
@Composable
fun InfoWidget(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


// --- SceneButton (Keep as is) ---
@Composable
fun SceneButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// --- RoomControls (Keep mostly as is, ensure key usage) ---
@Composable
fun RoomControls(roomName: String) {
    // State remains scoped to the specific room instance within the pager page
    var temperature by remember(roomName) { mutableStateOf(20f) }
    var isLightOn by remember(roomName) { mutableStateOf(true) }
    var isDoorLocked by remember(roomName) { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperature Control Card
        ControlCard(title = "Temperature", icon = Icons.Default.Thermostat) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${temperature.toInt()}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = temperature,
                    onValueChange = { newValue -> temperature = newValue },
                    valueRange = 10f..30f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        }

        // Light Control Card
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
                    onCheckedChange = { newState -> isLightOn = newState },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        }

        // Door Control Card (Conditional)
        if (roomName == "Garage" || roomName == "Office") {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { isDoorLocked = true },
                            enabled = !isDoorLocked,
                            border = BorderStroke(1.dp, if (!isDoorLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) { Text("Lock") }
                        OutlinedButton(
                            onClick = { isDoorLocked = false },
                            enabled = isDoorLocked,
                            border = BorderStroke(1.dp, if (isDoorLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) { Text("Unlock") }
                    }
                }
            }
        }
    }
}


// --- ControlCard (Keep as is) ---
@Composable
fun ControlCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), // Slightly different surface
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Slightly more elevation
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null, // Decorative
                    tint = MaterialTheme.colorScheme.primary, // Use primary color for icon
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall, // Slightly smaller title
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Clear title color
                )
            }
            Spacer(modifier = Modifier.height(12.dp)) // Space before content
            // Provide the content composable defined in the lambda
            content()
        }
    }
}

// --- Placeholder Screens (You need to implement these) ---
@Composable
fun StatsUsageScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Statistics Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun AddDeviceScreen(navController: NavHostController) { // Pass NavController
    Scaffold( // Use Scaffold for potential TopAppBar
        topBar = {
            TopAppBar(
                title = { Text("Add New Device") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Navigate back
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Apply padding from Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text("Add Device Screen Content", style = MaterialTheme.typography.headlineMedium)
            // Add your device adding UI here
        }
    }
}
