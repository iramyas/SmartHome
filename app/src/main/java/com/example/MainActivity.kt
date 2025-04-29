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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
//import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.IgnoreExtraProperties


import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.animation.core.animateDpAsState

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.theme.ThemeOption

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.first

import java.text.SimpleDateFormat
import java.util.*


data class Scene(val name: String, val icon: ImageVector)


enum class GeneralStatus { OK, Warning, Alarm }
enum class FireAlarmState { OK, Detected }


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
    private fun doorLockedKey(room: String) = booleanPreferencesKey("${room}_door_locked")
    private fun climateOnKey(room: String) = booleanPreferencesKey("${room}_climate_on")
    private fun targetTempKey(room: String) = floatPreferencesKey("${room}_target_temp")
    private fun humidityKey(room: String) = floatPreferencesKey("${room}_humidity")
    private fun blindPosKey(room: String) = intPreferencesKey("${room}_blind_pos")
    private fun fireAlarmStatusKey(room: String) = stringPreferencesKey("${room}_fire_alarm_status")
    private fun nightLightAutoKey(room: String) = booleanPreferencesKey("${room}_night_light_auto")
    private fun clapLightEnabledKey(room: String) = booleanPreferencesKey("${room}_clap_light_enabled")

    private fun generalStatusKey() = stringPreferencesKey("general_status")

    private fun deviceNamesKey(room: String) = stringPreferencesKey("${room}_device_names")


    val allRoomNames = listOf("Entrance", "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage")

    fun roomSettingsFlow(roomName: String): Flow<RoomSettings> = dataStore.data
        .map { preferences ->
            fun readFloat(key: Preferences.Key<Float>): Float? {
                val value = preferences[key]
                return when (value) {
                    is Float -> value
                    is String -> value.toFloatOrNull()
                    else -> null
                }
            }
            fun readBoolean(key: Preferences.Key<Boolean>, default: Boolean): Boolean {
                val value = preferences[key]
                return when(value) {
                    is Boolean -> value
                    is String -> value.equals("true", ignoreCase = true)
                    else -> default
                }
            }
            fun readInt(key: Preferences.Key<Int>, default: Int): Int {
                val value = preferences[key]
                return when(value) {
                    is Int -> value
                    is String -> value.toIntOrNull() ?: default
                    else -> default
                }
            }

            val deviceNamesJson = preferences[deviceNamesKey(roomName)]
            val deviceNamesList = if (deviceNamesJson != null) {
                deviceNamesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }


            RoomSettings(
                temperature = readFloat(tempKey(roomName)),
                isLightOn = readBoolean(lightKey(roomName), false),
                humidity = readFloat(humidityKey(roomName)),
                isDoorLocked = readBoolean(doorLockedKey(roomName), true),
                isClimateOn = readBoolean(climateOnKey(roomName), false),
                targetTemperature = readFloat(targetTempKey(roomName)) ?: 21f,
                blindPosition = readInt(blindPosKey(roomName), 50),
                fireAlarmStatus = try { FireAlarmState.valueOf(preferences[fireAlarmStatusKey(roomName)] ?: FireAlarmState.OK.name) } catch (e: Exception) { FireAlarmState.OK },
                isNightLightAuto = readBoolean(nightLightAutoKey(roomName), true),
                isClapLightEnabled = readBoolean(clapLightEnabledKey(roomName), false),
                generalStatus = try { GeneralStatus.valueOf(preferences[generalStatusKey()] ?: GeneralStatus.OK.name) } catch (e: Exception) { GeneralStatus.OK },
                deviceNames = deviceNamesList
            )
        }

//    val generalStatusFlow: Flow<GeneralStatus> = dataStore.data
//        .map { preferences ->
//            try { GeneralStatus.valueOf(preferences[generalStatusKey()] ?: GeneralStatus.OK.name) } catch (e: Exception) { GeneralStatus.OK }
//        }

    fun getAllRoomSettingsFlow(): Flow<Map<String, RoomSettings>> = dataStore.data
        .map { preferences ->
            val settingsMap = mutableMapOf<String, RoomSettings>()
            for (roomName in allRoomNames) {
                fun readFloat(key: Preferences.Key<Float>): Float? {
                    val value = preferences[key]
                    return when (value) {
                        is Float -> value
                        is String -> value.toFloatOrNull()
                        else -> null
                    }
                }
                fun readBoolean(key: Preferences.Key<Boolean>, default: Boolean): Boolean {
                    val value = preferences[key]
                    return when(value) {
                        is Boolean -> value
                        is String -> value.equals("true", ignoreCase = true)
                        else -> default
                    }
                }
                fun readInt(key: Preferences.Key<Int>, default: Int): Int {
                    val value = preferences[key]
                    return when(value) {
                        is Int -> value
                        is String -> value.toIntOrNull() ?: default
                        else -> default
                    }
                }

                val deviceNamesJson = preferences[deviceNamesKey(roomName)]
                val deviceNamesList = if (deviceNamesJson != null) {
                    deviceNamesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }

                settingsMap[roomName] = RoomSettings(
                    temperature = readFloat(tempKey(roomName)),
                    isLightOn = readBoolean(lightKey(roomName), false),
                    isDoorLocked = readBoolean(doorLockedKey(roomName), true),
                    isClimateOn = readBoolean(climateOnKey(roomName), false),
                    targetTemperature = readFloat(targetTempKey(roomName)) ?: 21f,
                    humidity = readFloat(humidityKey(roomName)),
                    blindPosition = readInt(blindPosKey(roomName), 50),
                    fireAlarmStatus = try { FireAlarmState.valueOf(preferences[fireAlarmStatusKey(roomName)] ?: FireAlarmState.OK.name) } catch (e: Exception) { FireAlarmState.OK },
                    isNightLightAuto = readBoolean(nightLightAutoKey(roomName), true),
                    isClapLightEnabled = readBoolean(clapLightEnabledKey(roomName), false),
                    generalStatus = try { GeneralStatus.valueOf(preferences[generalStatusKey()] ?: GeneralStatus.OK.name) } catch (e: Exception) { GeneralStatus.OK },
                    deviceNames = deviceNamesList
                )
            }
            settingsMap
        }


    suspend fun saveRoomSettings(roomName: String, settings: RoomSettings) {
        dataStore.edit { preferences ->
            settings.temperature?.let { preferences[tempKey(roomName)] = it } ?: preferences.remove(tempKey(roomName))
            preferences[lightKey(roomName)] = settings.isLightOn
            preferences[doorLockedKey(roomName)] = settings.isDoorLocked
            preferences[climateOnKey(roomName)] = settings.isClimateOn
            preferences[targetTempKey(roomName)] = settings.targetTemperature
            settings.humidity?.let { preferences[humidityKey(roomName)] = it } ?: preferences.remove(humidityKey(roomName))
            preferences[blindPosKey(roomName)] = settings.blindPosition
            preferences[fireAlarmStatusKey(roomName)] = settings.fireAlarmStatus.name
            preferences[nightLightAutoKey(roomName)] = settings.isNightLightAuto
            preferences[clapLightEnabledKey(roomName)] = settings.isClapLightEnabled
            preferences[deviceNamesKey(roomName)] = settings.deviceNames.joinToString(",")
        }
    }

//    suspend fun saveGeneralStatus(status: GeneralStatus) {
//        dataStore.edit { preferences ->
//            preferences[generalStatusKey()] = status.name
//        }
//    }
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var themePreferences: ThemePreferences
    private lateinit var database: DatabaseReference
    private lateinit var roomPreferences: RoomPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        themePreferences = ThemePreferences(applicationContext)
        roomPreferences = RoomPreferences(applicationContext)
        database= Firebase.database.reference
        initializeRoomsInFirebase(database)

        setContent {
            val currentTheme by themePreferences.selectedThemeFlow.collectAsState(initial = ThemeOption.Light)

            SmartHomeTheme(selectedTheme = currentTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainAppScaffold(
                        auth = auth,
                        database= database,
                        themePreferences = themePreferences,
                        roomPreferences = roomPreferences
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
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationBarItem(
                    selected = currentRoute == AppDestinations.HOME,
                    onClick = {
                        navController.navigate(AppDestinations.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors()
                )
                Spacer(Modifier.weight(1f))
                NavigationBarItem(
                    selected = currentRoute == AppDestinations.STATS,
                    onClick = {
                        navController.navigate(AppDestinations.STATS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Statistics") },
                    label = { Text("Stats") },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors()
                )
            }

            FloatingActionButton(
                onClick = {
                    onAddClick()
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp, 4.dp, 4.dp, 4.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
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
    database: DatabaseReference,
    themePreferences: ThemePreferences,
    roomPreferences: RoomPreferences
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        AppDestinations.HOME,
        AppDestinations.STATS
    )

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
                    database= database,
                    themePreferences = themePreferences,
                )
            }

            composable(AppDestinations.STATS) {
                StatsUsageScreen(roomPreferences = roomPreferences)
            }
            composable(AppDestinations.ADD_DEVICE) { AddDeviceScreen(navController = navController) }
        }
    }
}


@IgnoreExtraProperties
data class RoomSettings(
    val temperature: Float? = null,
    val isLightOn: Boolean = false,
    val humidity: Float? = null,
    val isDoorLocked: Boolean = true,
    val isClimateOn: Boolean = false,
    val targetTemperature: Float = 21f,
    val blindPosition: Int = 50,
    val fireAlarmStatus: FireAlarmState = FireAlarmState.OK,
    val isNightLightAuto: Boolean = true,
    val isClapLightEnabled: Boolean = false,
    val generalStatus: GeneralStatus = GeneralStatus.OK,
    val deviceNames: List<String> = emptyList()
) {
    constructor() : this(null, false, null, true, false, 21f, 50, FireAlarmState.OK, true, false, GeneralStatus.OK, emptyList())
}

fun defaultRoomSettings(): RoomSettings {
    return RoomSettings(
        temperature = 20f,
        isLightOn = false,
        humidity = 50f,
        isDoorLocked = true,
        isClimateOn = false,
        targetTemperature = 22f,
        blindPosition = 50,
        fireAlarmStatus = FireAlarmState.OK,
        isNightLightAuto = true,
        isClapLightEnabled = false,
        generalStatus = GeneralStatus.OK
    )
}
fun initializeRoomsInFirebase(database: DatabaseReference) {
    val rooms = listOf("Entrance", "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage")
    for (room in rooms) {
        val roomRef = database.child("rooms").child(room)
        roomRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                roomRef.setValue(defaultRoomSettings())
                    .addOnSuccessListener { Log.d("FirebaseInit", "Initialized $room with default settings.") }
                    .addOnFailureListener { e -> Log.e("FirebaseInit", "Failed to initialize $room.", e) }
            } else {
                Log.d("FirebaseInit", "$room already exists, skipping initialization.")
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseInit", "Failed to check existence of $room.", e)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    database: DatabaseReference,
    themePreferences: ThemePreferences,
) {
    val currentDate = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val userFirstName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }?.substringBefore(" ") ?: "Iram"
    val rooms = remember { listOf("Entrance", "Living Room", "Bedroom", "Kitchen", "Bathroom", "Office", "Garage") }
    val coroutineScope = rememberCoroutineScope()
    var selectedRoomIndex by remember { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { rooms.size })


    LaunchedEffect(selectedRoomIndex) {
        selectedRoomIndex?.let { index ->
            if (pagerState.currentPage != index) {
                if (!pagerState.isScrollInProgress) {
                    pagerState.animateScrollToPage(index)
                }
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->

                if (selectedRoomIndex != null && selectedRoomIndex != page) {
                    selectedRoomIndex = page
                }
            }
    }


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

        ThemeSelectionDialog(
            currentTheme = themePreferences.selectedThemeFlow.collectAsState(initial = ThemeOption.Light).value,
            onThemeSelected = { selectedTheme ->
                coroutineScope.launch { themePreferences.saveThemePreference(selectedTheme) }
                showThemeDialog = false
            },
            onDismissRequest = {
                showThemeDialog = false
            }
        )
    }

    BackHandler(enabled = selectedRoomIndex != null) {
        selectedRoomIndex = null
    }


    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HomeHeader(userName = userFirstName, date = currentDate, onSettingsClick = { showThemeDialog = true })
        Spacer(modifier = Modifier.height(16.dp))
        RoomSelector(
            rooms = rooms,
            selectedRoomIndex = selectedRoomIndex,
            onRoomSelected = { index ->
                val previouslySelected = selectedRoomIndex
                selectedRoomIndex = if (previouslySelected == index) null else index


                if (selectedRoomIndex != null && previouslySelected != index) {
                    coroutineScope.launch {
                        if (pagerState.currentPage != index) {
                            pagerState.scrollToPage(index)
                        }
                    }
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))


        AnimatedContent(
            targetState = selectedRoomIndex,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            transitionSpec = {

                if (targetState != null && initialState == null) {
                    slideInVertically(animationSpec = tween(400)) { height -> height } + fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(400))
                } else if (targetState == null && initialState != null) {
                    fadeIn(animationSpec = tween(400)) togetherWith (slideOutVertically(animationSpec = tween(400)) { height -> height } + fadeOut(animationSpec = tween(400)))
                } else {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                } using SizeTransform(clip = false)
            },
            label = "OverviewPagerSwitch"
        ) { currentSelectedRoomIndex ->
            if (currentSelectedRoomIndex == null) {

                OverviewContent(GeneralStatus.OK, globalTemperature, globalHumidity, scenes)
            } else {


                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    shape = RoundedCornerShape(2.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp),
                        pageSpacing = 1.dp,
                        verticalAlignment = Alignment.Top
                    ) { pageIndex ->
                        if (pageIndex >= 0 && pageIndex < rooms.size) {
                            val roomName = rooms[pageIndex]
                            key(roomName) {
                                RoomControls(
                                    roomName = roomName,
                                    database = database
                                )
                            }
                        } else {

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: Invalid room index $pageIndex")
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun OverviewContent(
    generalStatus: GeneralStatus,
    globalTemperature: String,
    globalHumidity: String,
    scenes: List<Scene>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GeneralStatusIndicator(status = generalStatus)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Global Climate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)

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
                    onClick = { }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GeneralStatusIndicator(status: GeneralStatus, modifier: Modifier = Modifier) {
    val statusColor = when (status) {
        GeneralStatus.OK -> MaterialTheme.colorScheme.primary
        GeneralStatus.Warning -> Color(0xFFFFA726)
        GeneralStatus.Alarm -> MaterialTheme.colorScheme.error
    }
    val statusIcon = when (status) {
        GeneralStatus.OK -> Icons.Filled.CheckCircleOutline
        GeneralStatus.Warning -> Icons.Filled.WarningAmber
        GeneralStatus.Alarm -> Icons.Filled.ErrorOutline
    }
    val statusText = when (status) {
        GeneralStatus.OK -> "All Systems Normal"
        GeneralStatus.Warning -> "Attention Required"
        GeneralStatus.Alarm -> "Alarm Active!"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = "Status: ${status.name}",
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
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
        "Entrance" -> Icons.Filled.SensorDoor
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
            val targetContainerColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, label = "RoomButtonContainer")
            val targetContentColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, label = "RoomButtonContent")
            val targetBorderColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "RoomButtonBorder")
            val targetElevation by animateDpAsState(if (isSelected) 4.dp else 1.dp, label = "RoomButtonElevation")


            val roomIcon = getRoomIcon(room)

            Surface(
                modifier = Modifier
                    .widthIn(min = 110.dp)
                    .height(45.dp)
                    .clickable {
                        onRoomSelected(index)
                    },
                shape = RoundedCornerShape(12.dp),
                color = targetContainerColor,
                contentColor = targetContentColor,
                border = BorderStroke(width = if (isSelected) 1.5.dp else 1.dp, color = targetBorderColor),
                shadowElevation = targetElevation
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
                    Text(
                        text = room,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        control()
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor
        )
    }
}



@Composable
fun RoomControls(
    roomName: String,
    database: DatabaseReference
) {
    val context = LocalContext.current
    val roomPreferences = remember { RoomPreferences(context) }
    var roomSettingsState by remember { mutableStateOf(RoomSettings()) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val roomRef = remember(roomName) { database.child("rooms").child(roomName) }

    LaunchedEffect(roomName) {
        roomPreferences.roomSettingsFlow(roomName).collectLatest { settings ->
            roomSettingsState = settings
        }
    }

    LaunchedEffect(roomName) {
        roomRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Optional: If you *want* Firebase to override DataStore (not the user's requirement here),
                // you would update roomSettingsState based on Firebase data here.
                // Since the requirement is for DataStore to be the primary source reflecting app state,
                // we won't update roomSettingsState from Firebase here. This listener just keeps the Firebase
                // data updated when DataStore changes trigger a Firebase write.
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    val imageResourceId = when (roomName) {
        "Entrance" -> R.drawable.ic_entrance_playstore
        "Living Room" -> R.drawable.ic_living_room_playstore
        "Bedroom" -> R.drawable.ic_bedroom_playstore
        "Kitchen" -> R.drawable.ic_kitchen_playstore
        "Bathroom" -> R.drawable.ic_bathroom_playstore
        "Office" -> R.drawable.ic_office_playstore
        "Garage" -> R.drawable.ic_garage_playstore
        else -> R.drawable.ic_default_playstore
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Image(
            painter = painterResource(id = imageResourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp)
        ) {
            Text("Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))


            SettingsRow(Icons.Filled.Lightbulb, "Lights") {
                Switch(
                    checked = roomSettingsState.isLightOn,
                    onCheckedChange = { isOn ->
                        val updatedSettings = roomSettingsState.copy(isLightOn = isOn)
                        roomSettingsState = updatedSettings // Optimistic UI update
                        coroutineScope.launch {
                            roomPreferences.saveRoomSettings(roomName, updatedSettings)
                            roomRef.child("isLightOn").setValue(isOn)
                        }
                    }
                )
            }
            Divider()

            roomSettingsState.temperature?.let { temp ->
                InfoRow(Icons.Filled.Thermostat, "Current Temperature", "${temp}°C")
                Divider()
            }

            roomSettingsState.humidity?.let { humidity ->
                InfoRow(Icons.Filled.WaterDrop, "Current Humidity", "${humidity}%")
                Divider()
            }

            Spacer(modifier = Modifier.height(24.dp))


            when (roomName) {
                "Entrance" -> {
                    Text("Entrance Specific", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsRow(Icons.Filled.Lock, "Door Lock") {
                        Switch(
                            checked = roomSettingsState.isDoorLocked,
                            onCheckedChange = { isLocked ->
                                val updatedSettings = roomSettingsState.copy(isDoorLocked = isLocked)
                                roomSettingsState = updatedSettings // Optimistic UI update
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("isDoorLocked").setValue(isLocked)
                                }
                            }
                        )
                    }
                    Divider()
                }
                "Living Room" -> {
                    Text("Living Room Specific", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsRow(Icons.Filled.AcUnit, "Climate System") {
                        Switch(
                            checked = roomSettingsState.isClimateOn,
                            onCheckedChange = { isOn ->
                                val updatedSettings = roomSettingsState.copy(isClimateOn = isOn)
                                roomSettingsState = updatedSettings // Optimistic UI update
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("isClimateOn").setValue(isOn)
                                }
                            }
                        )
                    }
                    Divider()


                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Temperature", style = MaterialTheme.typography.bodyMedium)
                            Text("${roomSettingsState.targetTemperature.toInt()}°C", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = roomSettingsState.targetTemperature,
                            onValueChange = { newTemp ->
                                roomSettingsState = roomSettingsState.copy(targetTemperature = newTemp)
                            },
                            onValueChangeFinished = {
                                val updatedSettings = roomSettingsState.copy(targetTemperature = roomSettingsState.targetTemperature)
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("targetTemperature").setValue(roomSettingsState.targetTemperature)
                                }
                            },
                            valueRange = 15f..30f,
                            steps = 14,
                            modifier = Modifier.fillMaxWidth().height(24.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                    }
                    Divider()


                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Blind Position", style = MaterialTheme.typography.bodyMedium)
                            Text("${roomSettingsState.blindPosition}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = roomSettingsState.blindPosition.toFloat(),
                            onValueChange = { newPos ->
                                roomSettingsState = roomSettingsState.copy(blindPosition = newPos.toInt())
                            },
                            onValueChangeFinished = {
                                val updatedSettings = roomSettingsState.copy(blindPosition = roomSettingsState.blindPosition)
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("blindPosition").setValue(roomSettingsState.blindPosition)
                                }
                            },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth().height(24.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                activeTickColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
                                inactiveTickColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    }
                    Divider()
                }
                "Bedroom" -> {
                    Text("Bedroom Specific", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsRow(Icons.Filled.AutoMode, "Night Light Auto") {
                        Switch(
                            checked = roomSettingsState.isNightLightAuto,
                            onCheckedChange = { isAuto ->
                                val updatedSettings = roomSettingsState.copy(isNightLightAuto = isAuto)
                                roomSettingsState = updatedSettings // Optimistic UI update
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("isNightLightAuto").setValue(isAuto)
                                }
                            }
                        )
                    }
                    Divider()
                    SettingsRow(Icons.Filled.Mic, "Clap Light Enabled") {
                        Switch(
                            checked = roomSettingsState.isClapLightEnabled,
                            onCheckedChange = { isEnabled ->
                                val updatedSettings = roomSettingsState.copy(isClapLightEnabled = isEnabled)
                                roomSettingsState = updatedSettings // Optimistic UI update
                                coroutineScope.launch {
                                    roomPreferences.saveRoomSettings(roomName, updatedSettings)
                                    roomRef.child("isClapLightEnabled").setValue(isEnabled)
                                }
                            }
                        )
                    }
                    Divider()
                }
                "Kitchen" -> {
                    Text("Kitchen Specific", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(
                        icon = if (roomSettingsState.fireAlarmStatus == FireAlarmState.OK) Icons.Filled.CheckCircleOutline else Icons.Filled.Warning,
                        label = "Fire Alarm Status",
                        value = roomSettingsState.fireAlarmStatus.name,
                        valueColor = if (roomSettingsState.fireAlarmStatus == FireAlarmState.OK) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                    Divider()
                }
                else -> {
                    Text("No specific controls for this room.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun HomeHeader(userName: String, date: String, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "User Avatar",
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Hello, $userName!",
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
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon, contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SceneButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp),
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(theme.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}


@Composable
fun StatsUsageScreen(roomPreferences: RoomPreferences) {
    val allRoomSettings by roomPreferences.getAllRoomSettingsFlow().collectAsState(initial = emptyMap())
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            "Current Status",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (allRoomSettings.isEmpty()) {
            StatusItem(icon = Icons.Filled.Info, text = "No room information available.")
        } else {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                var activeStatusFoundOverall = false // To check if ANY status is shown

                allRoomSettings.forEach { (roomName, settings) ->
                    var roomHasActiveStatus = false // To check if this specific room has status to show

                    // Determine if this room has any status we care about displaying
                    if (settings.isLightOn ||
                        (roomName in listOf("Entrance", "Garage") && (!settings.isDoorLocked || settings.isDoorLocked)) || // Include both locked/unlocked for Entrance/Garage
                        settings.isClimateOn ||
                        settings.isNightLightAuto ||
                        settings.isClapLightEnabled ||
                        settings.fireAlarmStatus != FireAlarmState.OK
                    ) {
                        roomHasActiveStatus = true
                    }

                    // Display room header only if there's something to show for this room
                    if (roomHasActiveStatus) {
                        Text(
                            roomName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Display Lights status if ON
                        if (settings.isLightOn) {
                            StatusItem(icon = Icons.Filled.Lightbulb, text = "$roomName light is ON")
                            activeStatusFoundOverall = true
                        }

                        // Display Door Lock status ONLY for Entrance and Garage
                        if (roomName == "Entrance" || roomName == "Garage") {
                            if (!settings.isDoorLocked) { // Display if unlocked
                                StatusItem(icon = Icons.Filled.LockOpen, text = "$roomName door is UNLOCKED", isWarning = true)
                                activeStatusFoundOverall = true
                            } else { // Display if locked
                                StatusItem(icon = Icons.Filled.Lock, text = "$roomName door is LOCKED", isPositive = true)
                                activeStatusFoundOverall = true
                            }
                        }

                        // Display Climate status if ON (assuming climate applies to rooms other than just Living Room if present in settings)
                        if (settings.isClimateOn) {
                            StatusItem(icon = Icons.Filled.AcUnit, text = "$roomName climate is ON", isPositive = true)
                            activeStatusFoundOverall = true
                        }

                        // Display Fire Alarm status if NOT OK (assuming only Kitchen has this based on previous context, but check for any room)
                        if (settings.fireAlarmStatus != FireAlarmState.OK) {
                            StatusItem(icon = Icons.Filled.Warning, text = "$roomName fire alarm: ${settings.fireAlarmStatus.name}", isWarning = true)
                            activeStatusFoundOverall = true
                        }

                        // Room specific statuses (keep these if they were intended)
                        when (roomName) {
                            "Bedroom" -> {
                                if (settings.isNightLightAuto) {
                                    StatusItem(icon = Icons.Filled.AutoMode, text = "Bedroom night light is AUTO", isPositive = true)
                                    activeStatusFoundOverall = true
                                }
                                if (settings.isClapLightEnabled) {
                                    StatusItem(icon = Icons.Filled.Mic, text = "Bedroom clap light is ENABLED", isPositive = true)
                                    activeStatusFoundOverall = true
                                }
                            }
                            // Add other room specific statuses here if needed from the original code
                        }
                        if (settings.deviceNames.isNotEmpty()) {
                            Text(
                                "Added Devices:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            settings.deviceNames.forEach { deviceName ->
                                StatusItem(icon = Icons.Filled.DeviceHub, text = deviceName)
                                activeStatusFoundOverall = true
                            }
                        }

                        // Add a divider after each room's statuses if that room had any active status displayed
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    }
                }

                // Display a message if no active statuses were found across all rooms
                if (!activeStatusFoundOverall) {
                    StatusItem(icon = Icons.Filled.CheckCircleOutline, text = "No active devices or features detected across all rooms.", isPositive = true)
                }
            }
        }
    }
}


@Composable
fun StatusItem(icon: ImageVector, text: String, isWarning: Boolean = false, isPositive: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isWarning -> MaterialTheme.colorScheme.error
                isPositive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                isWarning -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun AddDeviceScreen(navController: NavHostController) {
    val context = LocalContext.current
    val roomPreferences = remember { RoomPreferences(context) }
    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database.reference

    val rooms = remember { roomPreferences.allRoomNames }
    var selectedRoom by remember { mutableStateOf<String?>(null) }
    var deviceName by remember { mutableStateOf("") }
    var deviceType by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Text(
            "Add New Device",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text("Select Room", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rooms) { room ->
                FilterChip(
                    selected = room == selectedRoom,
                    onClick = { selectedRoom = if (selectedRoom == room) null else room },
                    label = { Text(room) },
                    leadingIcon = if (room == selectedRoom) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Text("Device Name", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("e.g., Living Room TV") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Device Type", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = deviceType,
            onValueChange = { deviceType = it },
            label = { Text("e.g., Apple TV, Washing Machine") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedRoom != null && deviceName.isNotBlank()) {
                    coroutineScope.launch {
                        val currentSettings = roomPreferences.roomSettingsFlow(selectedRoom!!).first()
                        val updatedDeviceNames = currentSettings.deviceNames.toMutableList()
                        updatedDeviceNames.add(deviceName)

                        val updatedSettings = currentSettings.copy(deviceNames = updatedDeviceNames)

                        roomPreferences.saveRoomSettings(selectedRoom!!, updatedSettings)

                        val roomRef = database.child("rooms").child(selectedRoom!!)
                        roomRef.child("deviceNames").setValue(updatedDeviceNames)


                        navController.popBackStack()

                    }
                } else {

                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedRoom != null && deviceName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Add Device", color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.height(16.dp))
        
        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        ) {
            Text("Cancel")
        }
    }
}
