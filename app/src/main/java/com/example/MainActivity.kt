@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)

package com.example.smarthome

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption

import com.google.firebase.auth.FirebaseAuth


import java.text.SimpleDateFormat
import java.util.*


data class Scene(val name: String, val icon: ImageVector)

private val Context.roomDataStore by preferencesDataStore(name = "room_prefs")
private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class ThemePreferences(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.settingsDataStore

    private object PreferencesKeys {
        val THEME_KEY = stringPreferencesKey("selected_theme")
    }

    val selectedThemeFlow: Flow<ThemeOption> = dataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_KEY]
            try {
                when (themeName) {
                    ThemeOption.Light.name -> ThemeOption.Light
                    ThemeOption.Dark.name -> ThemeOption.Dark
                    ThemeOption.Pink.name -> ThemeOption.Pink
                    else -> ThemeOption.Light
                }
            } catch (e: IllegalArgumentException) {
                Log.e("ThemePreferences", "Invalid theme name in DataStore: $themeName", e)
                ThemeOption.Light
            }
        }

    suspend fun saveThemePreference(theme: ThemeOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_KEY] = theme.name
        }
    }
}

class RoomPreferences(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.roomDataStore

    private fun tempKey(room: String) = floatPreferencesKey("${room}_temp")
    private fun lightKey(room: String) = booleanPreferencesKey("${room}_light")

    fun roomSettingsFlow(roomName: String): Flow<RoomSettings> = dataStore.data
        .map { preferences ->
            val tempPrefValue: Any? = preferences[tempKey(roomName)]
            val temperature: Float = when (tempPrefValue) {
                is Float -> tempPrefValue
                is String -> tempPrefValue.toFloatOrNull() ?: 20f
                else -> 20f
            }

            val lightPrefValue: Any? = preferences[lightKey(roomName)]
            val isLightOn: Boolean = when(lightPrefValue) {
                is Boolean -> lightPrefValue
                is String -> lightPrefValue.equals("true", ignoreCase = true)
                else -> false
            }


            RoomSettings(
                temperature = temperature,
                isLightOn = isLightOn
            )
        }

    suspend fun saveRoomSettings(roomName: String, settings: RoomSettings) {
        Log.d("RoomPreferences", "Saving settings for $roomName: Temp=${settings.temperature}, Light=${settings.isLightOn}")
        dataStore.edit { preferences ->
            preferences[tempKey(roomName)] = settings.temperature
            preferences[lightKey(roomName)] = settings.isLightOn
        }
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var roomPreferences: RoomPreferences
    private lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        roomPreferences = RoomPreferences(applicationContext)
        themePreferences = ThemePreferences(applicationContext)

        setContent {
            val currentTheme by themePreferences.selectedThemeFlow.collectAsState(initial = ThemeOption.Light)
            Log.d("MainActivity", "Applying theme: $currentTheme")

            SmartHomeTheme(selectedTheme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppScaffold(
                        auth = auth,
                        roomPreferences = roomPreferences,
                        themePreferences = themePreferences
                    )
                }
            }
        }
    }
}

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
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == AppDestinations.HOME,
            onClick = {
                Log.d("Navigation", "Home clicked")
                navController.navigate(AppDestinations.HOME) { }
            },
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home") },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors()
        )
        NavigationBarItem(
            selected = currentRoute == AppDestinations.STATS,
            onClick = {
                Log.d("Navigation", "Stats clicked")
                navController.navigate(AppDestinations.STATS) { }
            },
            icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Statistics") },
            label = { Text("Stats") },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors()
        )
        Spacer(Modifier.weight(1f))
        FloatingActionButton(
            onClick = {
                Log.d("Navigation", "Add FAB clicked")
                onAddClick()
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add")
        }
    }
}

object AppDestinations {
    const val HOME = "home"
    const val STATS = "stats"
    const val ADD_DEVICE = "add_device"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScaffold(
    auth: FirebaseAuth,
    roomPreferences: RoomPreferences,
    themePreferences: ThemePreferences
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        AppDestinations.HOME,
        AppDestinations.STATS
    )
    Log.d("MainAppScaffold", "Current route: $currentRoute, Show bottom bar: $showBottomBar")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute ?: AppDestinations.HOME,
                    navController = navController,
                    onAddClick = { navController.navigate(AppDestinations.ADD_DEVICE) { launchSingleTop = true } }
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.HOME,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            composable(AppDestinations.HOME) {
                HomeScreen(
                    auth = auth,
                    roomPreferences = roomPreferences,
                    themePreferences = themePreferences,
                )
            }
            composable(AppDestinations.STATS) { StatsUsageScreen() }
            composable(AppDestinations.ADD_DEVICE) { AddDeviceScreen(navController = navController) }
        }
    }
}

data class RoomSettings(
    val temperature: Float = 20f,
    val isLightOn: Boolean = false
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    roomPreferences: RoomPreferences,
    themePreferences: ThemePreferences,
) {
    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val userFirstName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }?.substringBefore(" ") ?: "Iram"
    val rooms = remember { listOf("Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage") }
    val coroutineScope = rememberCoroutineScope()
    var selectedRoomIndex by remember { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState(initialPage = selectedRoomIndex ?: 0, pageCount = { rooms.size })
    var showThemeDialog by remember { mutableStateOf(false) }
    val globalTemperature = "18°C"
    val globalHumidity = "65%"
    val scenes = remember { listOf(
        Scene("Morning", Icons.Default.WbSunny),
        Scene("Away", Icons.Default.WorkOutline),
        Scene("Relax", Icons.Default.Weekend),
        Scene("Night", Icons.Default.Bedtime)
    )}

    if (showThemeDialog) {
        val currentTheme by themePreferences.selectedThemeFlow.collectAsState(initial = ThemeOption.Light)
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { selectedTheme ->
                Log.d("HomeScreen", "Theme selected: $selectedTheme")
                coroutineScope.launch { themePreferences.saveThemePreference(selectedTheme) }
                showThemeDialog = false
            },
            onDismissRequest = {
                Log.d("HomeScreen", "Theme dialog dismissed")
                showThemeDialog = false
            }
        )
    }

    BackHandler(enabled = selectedRoomIndex != null) {
        Log.d("HomeScreen", "Back pressed, exiting room details")
        selectedRoomIndex = null
    }

    LaunchedEffect(selectedRoomIndex) {
        selectedRoomIndex?.let { index ->
            if (pagerState.currentPage != index) {
                Log.d("HomeScreen", "Scrolling pager to initial selected index: $index")
                pagerState.scrollToPage(index)
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                if (selectedRoomIndex != null) {
                    Log.d("HomeScreen", "Pager swiped to page: $page")
                    selectedRoomIndex = page
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HomeHeader(userName = userFirstName, date = currentDate, onSettingsClick = { Log.d("HomeScreen", "Settings icon clicked!"); showThemeDialog = true })
        Spacer(modifier = Modifier.height(16.dp))
        RoomSelector(
            rooms = rooms,
            selectedRoomIndex = selectedRoomIndex,
            onRoomSelected = { index ->
                Log.d("HomeScreen", "Room index $index selected by click")
                selectedRoomIndex = if (selectedRoomIndex == index) null else index
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        AnimatedContent(
            targetState = selectedRoomIndex,
            modifier = Modifier.fillMaxWidth().weight(1f),
            transitionSpec = {
                if (targetState != null && initialState == null) {
                    slideInVertically(tween(400)) { h -> h / 4 } + fadeIn(tween(400)) togetherWith
                            slideOutVertically(tween(400)) { h -> -h / 4 } + fadeOut(tween(400))
                } else if (targetState == null && initialState != null) {
                    slideInVertically(tween(400)) { h -> -h / 4 } + fadeIn(tween(400)) togetherWith
                            slideOutVertically(tween(400)) { h -> h / 4 } + fadeOut(tween(400))
                } else {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                } using SizeTransform(clip = false)
            },
            label = "OverviewPagerSwitch"
        ) { currentSelectedRoomIndex ->
            if (currentSelectedRoomIndex == null) {
                OverviewContent(globalTemperature, globalHumidity, scenes)
            } else {
                Log.d("HomeScreen", "Displaying Pager because selectedRoomIndex is $currentSelectedRoomIndex")
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 16.dp,
                    verticalAlignment = Alignment.Top
                ) { pageIndex ->
                    if (pageIndex >= 0 && pageIndex < rooms.size) {
                        val roomName = rooms[pageIndex]
                        Log.d("HomeScreen", "Pager composing page for: $roomName (Index $pageIndex)")
                        key(roomName) {
                            RoomControls(roomName = roomName, roomPreferences = roomPreferences)
                        }
                    } else {
                        Log.e("HomeScreen", "Invalid pageIndex in Pager (AnimatedContent): $pageIndex. Room count: ${rooms.size}")
                        Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: Invalid room index $pageIndex") }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewContent(
    globalTemperature: String,
    globalHumidity: String,
    scenes: List<Scene>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Global Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoWidget(icon = Icons.Filled.Thermostat, label = "Temperature", value = globalTemperature, modifier = Modifier.weight(1f))
            InfoWidget(icon = Icons.Filled.WaterDrop, label = "Humidity", value = globalHumidity, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = "Quick Scenes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
            items(scenes) { scene ->
                SceneButton(
                    icon = scene.icon,
                    label = scene.name,
                    onClick = { Log.d("OverviewContent", "Scene '${scene.name}' clicked") }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getRoomIcon(roomName: String): ImageVector {
    return when (roomName) {
        "Living Room" -> Icons.Filled.Weekend
        "Bedroom" -> Icons.Filled.Bed
        "Kitchen" -> Icons.Filled.Kitchen
        "Bathroom" -> Icons.Filled.Bathroom
        "Office" -> Icons.Filled.Work
        "Garage" -> Icons.Filled.Garage
        else -> Icons.Filled.Home
    }
}

@Composable
fun RoomSelector(
    rooms: List<String>,
    selectedRoomIndex: Int?,
    onRoomSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        itemsIndexed(rooms) { index, room ->
            val isSelected = index == selectedRoomIndex
            val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            val targetContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            val targetBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            val targetFontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            val roomIcon = getRoomIcon(room)

            Surface(
                modifier = Modifier
                    .widthIn(min = 110.dp)
                    .height(45.dp)
                    .clickable {
                        Log.d("RoomSelector", "Clicked on '$room' (Index $index)")
                        onRoomSelected(index)
                    },
                shape = RoundedCornerShape(12.dp),
                color = targetContainerColor,
                contentColor = targetContentColor,
                border = BorderStroke(width = if (isSelected) 1.5.dp else 1.dp, color = targetBorderColor),
                shadowElevation = if (isSelected) 4.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = roomIcon,
                        contentDescription = room,
                        tint = targetContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = room, style = MaterialTheme.typography.labelLarge, fontWeight = targetFontWeight, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun RoomControls(
    roomName: String,
    roomPreferences: RoomPreferences
) {
    val settings by roomPreferences.roomSettingsFlow(roomName).collectAsState(initial = RoomSettings())
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = roomName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = if (settings.isLightOn) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb, contentDescription = "Light", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Light", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = settings.isLightOn,
                    onCheckedChange = { newLightState ->
                        Log.d("RoomControls", "$roomName Light toggled: $newLightState")
                        coroutineScope.launch { roomPreferences.saveRoomSettings(roomName, settings.copy(isLightOn = newLightState)) }
                    },
                    colors = SwitchDefaults.colors()
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Thermostat, contentDescription = "Temperature", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Temperature: ${String.format("%.1f", settings.temperature)}°C", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = settings.temperature,
                    onValueChange = { newTemp ->
                        coroutineScope.launch {
                            val roundedTemp = (newTemp * 10).toInt() / 10f
                            if (settings.temperature != roundedTemp) {
                                Log.d("RoomControls", "$roomName Temp changed: $roundedTemp")
                                roomPreferences.saveRoomSettings(roomName, settings.copy(temperature = roundedTemp))
                            }
                        }
                    },
                    valueRange = 10f..30f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors()
                )
            }
        }
    }
}


@Composable
fun HomeHeader(userName: String, date: String, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = "User Avatar", modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Hello, $userName!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onSettingsClick) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoWidget(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SceneButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp), tonalElevation = 2.dp) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
            }
        }
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Theme", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                ThemeOption.values().filter { it != ThemeOption.System }.forEach { theme ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onThemeSelected(theme) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(theme.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}


@Composable
fun StatsUsageScreen() {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Text("Statistics Screen", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun AddDeviceScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Add Device Screen", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Done", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
