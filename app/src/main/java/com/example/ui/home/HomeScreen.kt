package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LocationItem
import com.example.data.remote.MeteorologicalDataSource
import com.example.ui.chat.WeatherGptChatScreen
import com.example.ui.components.*
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.WeatherViewModel

enum class MainTab(val title: String, val icon: @Composable () -> Unit) {
    FORECAST("Forecast", { Icon(Icons.Default.WbSunny, contentDescription = "Forecast") }),
    WEATHER_GPT("WeatherGPT AI", { Icon(Icons.Default.Chat, contentDescription = "WeatherGPT AI") }),
    RADAR_SATELLITE("Radar & Map", { Icon(Icons.Default.Radar, contentDescription = "Radar & Map") }),
    ALERTS("Early Alerts", { Icon(Icons.Default.Warning, contentDescription = "Early Alerts") }),
    CLIMATE("Climate", { Icon(Icons.Default.TrendingUp, contentDescription = "Climate") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.FORECAST) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val currentWeather by viewModel.currentWeather.collectAsState()
    val hourlyList by viewModel.hourlyList.collectAsState()
    val dailyList by viewModel.dailyList.collectAsState()
    val dialectSummary by viewModel.dialectSummary.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val climateTrends by viewModel.climateTrends.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isLowBandwidth by viewModel.isLowBandwidthMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isAwaitingAi by viewModel.isAwaitingAiResponse.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showLocationDialog = true }
                            .testTag("location_picker_trigger")
                    ) {
                        Text(
                            text = "🌤️ WeatherGPT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFFFB951))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${selectedLocation.name}, ${selectedLocation.state}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Location",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    IconButton(
                        onClick = { showLanguageDialog = true },
                        modifier = Modifier.testTag("language_picker_button")
                    ) {
                        Text(
                            text = selectedLanguage.nativeName.take(2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Low Bandwidth Toggle
                    IconButton(
                        onClick = { viewModel.toggleLowBandwidth(!isLowBandwidth) },
                        modifier = Modifier.testTag("low_bandwidth_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isLowBandwidth) Icons.Default.NetworkCheck else Icons.Default.SignalCellularAlt,
                            contentDescription = "Low Bandwidth Mode",
                            tint = if (isLowBandwidth) AlertOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dark Mode Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode(!isDarkMode) },
                        modifier = Modifier.testTag("dark_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_navigation"),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    MainTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = tab.icon,
                            label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Offline / Syncing Banner
            if (!isOnline || isSyncing || isLowBandwidth) {
                Surface(
                    color = if (!isOnline) Color(0xFF2C1E1A) else if (isLowBandwidth) Color(0xFF1E2A20) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (!isOnline) AlertOrange.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (!isOnline) Icons.Default.CloudOff else if (isLowBandwidth) Icons.Default.Speed else Icons.Default.Sync,
                                contentDescription = null,
                                tint = if (!isOnline) AlertOrange else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (!isOnline) "Offline Mode • Room Cached Database Active" else if (isLowBandwidth) "Low-Bandwidth Mode Active" else "Auto-Syncing meteorological datasets...",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!isOnline) AlertOrange else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!isOnline) {
                            Text(
                                text = "Auto-sync ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tab Contents
            when (selectedTab) {
                MainTab.FORECAST -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        // Dialect Weather Summary
                        item {
                            WeatherDialectSummaryCard(
                                tempC = currentWeather?.currentTempC ?: 28,
                                feelsLikeC = currentWeather?.feelsLikeC ?: 30,
                                conditionEmoji = currentWeather?.conditionEmoji ?: "⛅",
                                conditionDesc = currentWeather?.conditionDescription ?: "Partly Cloudy",
                                dialectSummary = dialectSummary,
                                locationName = "${selectedLocation.name}, ${selectedLocation.state}",
                                isSpeaking = isSpeaking,
                                onSpeakClick = { text -> viewModel.speak(text) }
                            )
                        }

                        // Agro & Environmental Metrics
                        item {
                            AgroMetricsGrid(
                                humidityPercent = currentWeather?.humidityPercent ?: 64,
                                windSpeedKmh = currentWeather?.windSpeedKmh ?: 14,
                                windDirection = currentWeather?.windDirection ?: "SSW (205°)",
                                rainfallProb = currentWeather?.rainfallProb ?: 40,
                                uvIndex = currentWeather?.uvIndex ?: 6,
                                soilMoisturePercent = currentWeather?.soilMoisturePercent ?: 58
                            )
                        }

                        // Hourly Forecast Row
                        if (hourlyList.isNotEmpty()) {
                            item {
                                HourlyForecastRow(hourlyList = hourlyList)
                            }
                        }

                        // NWP Numerical Prediction Model Banner
                        item {
                            NwpModelBanner(
                                gfsTempC = currentWeather?.gfsTempC ?: 28,
                                wrfTempC = currentWeather?.wrfTempC ?: 29,
                                nwpSummary = currentWeather?.nwpSummary ?: "GFS 0.25° & WRF 3km show stable boundary layer."
                            )
                        }

                        // 7-Day Agrarian Outlook
                        if (dailyList.isNotEmpty()) {
                            item {
                                DailyAgrarianForecastList(dailyList = dailyList)
                            }
                        }
                    }
                }

                MainTab.WEATHER_GPT -> {
                    WeatherGptChatScreen(
                        messages = chatMessages,
                        isAwaitingAi = isAwaitingAi,
                        isOnline = isOnline,
                        isLowBandwidth = isLowBandwidth,
                        selectedLanguage = selectedLanguage,
                        onSendMessage = { query -> viewModel.sendChatMessage(query) },
                        onSpeakMessage = { text -> viewModel.speak(text) },
                        onClearChat = { viewModel.clearChat() }
                    )
                }

                MainTab.RADAR_SATELLITE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        item {
                            RadarSatelliteView(locationName = "${selectedLocation.name}, ${selectedLocation.state}")
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "📡 Doppler Radar & INSAT-3DR Notes",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• Doppler radar scans hydrometeors within 150 km with 3km grid precision.\n• INSAT-3DR thermal infrared channel monitors deep convective clouds and cyclonic cloud spirals.\n• All frames are compressed locally for zero data consumption.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                MainTab.ALERTS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        item {
                            Text(
                                text = "Extreme Weather & Disaster Early Warnings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Early Warning System (EWS) integrated with IMD/NDMA protocols",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (alerts.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✅ No severe weather warnings active for your region.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                            }
                        } else {
                            items(alerts.size) { index ->
                                val alert = alerts[index]
                                AlertBannerCard(
                                    alert = alert,
                                    onSpeakClick = { text -> viewModel.speak(text) }
                                )
                            }
                        }
                    }
                }

                MainTab.CLIMATE -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        item {
                            ClimateTrendChart(
                                trends = climateTrends,
                                locationName = "${selectedLocation.name}, ${selectedLocation.state}"
                            )
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Historical Monsoon Trend Analysis",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "• 5-Month Seasonal Trend: Monsoon onset in June delivered normal cumulative rainfall.\n• July recorded a +12% surplus in soil water retention.\n• August forecast projects steady precipitation suitable for vegetative crop growth.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Location Picker Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Select Region / Location", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(MeteorologicalDataSource.AVAILABLE_LOCATIONS.size) { idx ->
                        val loc = MeteorologicalDataSource.AVAILABLE_LOCATIONS[idx]
                        val isCurrent = loc.name == selectedLocation.name
                        Surface(
                            onClick = {
                                viewModel.setLocation(loc)
                                showLocationDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("location_option_${loc.name.replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = loc.name,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${loc.state}, ${loc.country} (${loc.elevationM}m)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    // Multi-Language Picker Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Choose Language / भाषा निवडा", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(AppLanguage.values().size) { idx ->
                        val lang = AppLanguage.values()[idx]
                        val isCurrent = lang == selectedLanguage
                        Surface(
                            onClick = {
                                viewModel.setLanguage(lang)
                                showLanguageDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("language_option_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = lang.nativeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}
