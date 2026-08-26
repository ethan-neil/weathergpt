package com.example.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.GeminiWeatherService
import com.example.data.remote.MeteorologicalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WeatherRepository(
    private val context: Context,
    private val database: WeatherDatabase,
    private val geminiService: GeminiWeatherService = GeminiWeatherService(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val dao = database.weatherDao()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _selectedLocation = MutableStateFlow(MeteorologicalDataSource.AVAILABLE_LOCATIONS.first())
    val selectedLocation: StateFlow<LocationItem> = _selectedLocation.asStateFlow()

    private val _isLowBandwidthMode = MutableStateFlow(false)
    val isLowBandwidthMode: StateFlow<Boolean> = _isLowBandwidthMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        monitorNetworkConnectivity()
        seedInitialData()
    }

    private fun monitorNetworkConnectivity() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val isCurrentlyConnected = cm?.activeNetwork?.let { network ->
            cm.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: true
        _isOnline.value = isCurrentlyConnected

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                Log.d("WeatherRepository", "Network reconnected -> Triggering automated backup & sync")
                triggerAutoSync()
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                Log.d("WeatherRepository", "Network disconnected -> Operating in offline cache mode")
            }
        })
    }

    private fun seedInitialData() {
        scope.launch {
            val location = _selectedLocation.value
            val locationKey = "${location.name}_${location.state}"
            
            // Seed weather for all default locations
            MeteorologicalDataSource.AVAILABLE_LOCATIONS.forEach { loc ->
                val entity = MeteorologicalDataSource.generateWeatherForLocation(loc)
                dao.insertWeather(entity)
                val trends = MeteorologicalDataSource.getClimateTrendsForLocation("${loc.name}_${loc.state}")
                dao.insertTrends(trends)
            }

            // Seed alerts
            dao.insertAlerts(MeteorologicalDataSource.getInitialAlerts())

            // Initial greeting message in chat
            val welcomeMsg = ChatMessageEntity(
                text = "Namaste! I am WeatherGPT, your AI meteorologist & agrarian intelligence assistant. Ask me anything about forecasts, crop spraying safety, NWP model charts, or extreme weather alerts in any local language.",
                isUser = false,
                category = "general",
                languageCode = "en"
            )
            dao.insertChatMessage(welcomeMsg)
        }
    }

    fun setLocation(location: LocationItem) {
        _selectedLocation.value = location
        scope.launch {
            val entity = MeteorologicalDataSource.generateWeatherForLocation(location)
            dao.insertWeather(entity)
        }
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    fun toggleLowBandwidthMode(enabled: Boolean) {
        _isLowBandwidthMode.value = enabled
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun getCurrentWeatherFlow(): Flow<CachedWeatherEntity?> {
        return _selectedLocation.flatMapLatest { loc ->
            val key = "${loc.name}_${loc.state}"
            dao.getWeatherByLocation(key)
        }
    }

    fun getAlertsFlow(): Flow<List<CachedAlertEntity>> {
        return dao.getAllAlerts()
    }

    fun getChatMessagesFlow(): Flow<List<ChatMessageEntity>> {
        return dao.getAllChatMessages()
    }

    fun getClimateTrendsFlow(): Flow<List<HistoricalTrendEntity>> {
        return _selectedLocation.flatMapLatest { loc ->
            dao.getTrendsForLocation("${loc.name}_${loc.state}")
        }
    }

    suspend fun sendUserMessage(queryText: String) {
        val userEntity = ChatMessageEntity(
            text = queryText,
            isUser = true,
            languageCode = _selectedLanguage.value.code
        )
        dao.insertChatMessage(userEntity)

        // Generate response using Gemini or Offline Engine
        val loc = _selectedLocation.value
        val weatherEntity = dao.getWeatherByLocation("${loc.name}_${loc.state}").firstOrNull()
        val contextInfo = """
            Location: ${loc.name}, ${loc.state}, ${loc.country}
            Temperature: ${weatherEntity?.currentTempC ?: 28}°C (Feels like ${weatherEntity?.feelsLikeC ?: 30}°C)
            Condition: ${weatherEntity?.conditionDescription ?: "Partly Cloudy"}
            Rain Probability: ${weatherEntity?.rainfallProb ?: 35}%
            Humidity: ${weatherEntity?.humidityPercent ?: 65}%
            Wind: ${weatherEntity?.windSpeedKmh ?: 14} km/h (${weatherEntity?.windDirection ?: "SW"})
            NWP Confidence: ${weatherEntity?.nwpSummary ?: "High Confidence"}
        """.trimIndent()

        val aiResponse = geminiService.askWeatherGpt(
            userQuery = queryText,
            currentWeatherContext = contextInfo,
            activeLanguage = _selectedLanguage.value,
            isLowBandwidth = _isLowBandwidthMode.value
        )

        val botEntity = ChatMessageEntity(
            text = aiResponse,
            isUser = false,
            category = if (queryText.contains("spray") || queryText.contains("crop")) "crop_advisory" else "general",
            languageCode = _selectedLanguage.value.code
        )
        dao.insertChatMessage(botEntity)
    }

    fun triggerAutoSync() {
        scope.launch {
            _isSyncing.value = true
            try {
                // Refresh weather cache
                val loc = _selectedLocation.value
                val updated = MeteorologicalDataSource.generateWeatherForLocation(loc)
                dao.insertWeather(updated)
                dao.insertAlerts(MeteorologicalDataSource.getInitialAlerts())
                Log.d("WeatherRepository", "Auto-sync completed successfully.")
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Sync error: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearChat() {
        scope.launch {
            dao.clearChatHistory()
        }
    }

    // Helper parser for hourly and daily forecast from JSON
    fun parseHourly(jsonStr: String): List<HourlyForecast> {
        val list = mutableListOf<HourlyForecast>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    HourlyForecast(
                        time = obj.getString("time"),
                        tempC = obj.getInt("tempC"),
                        rainProb = obj.getInt("rainProb"),
                        conditionEmoji = obj.getString("conditionEmoji"),
                        windKmh = obj.getInt("windKmh")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    fun parseDaily(jsonStr: String): List<DailyForecast> {
        val list = mutableListOf<DailyForecast>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    DailyForecast(
                        dayOfWeek = obj.getString("dayOfWeek"),
                        date = obj.getString("date"),
                        minTempC = obj.getInt("minTempC"),
                        maxTempC = obj.getInt("maxTempC"),
                        rainProb = obj.getInt("rainProb"),
                        conditionEmoji = obj.getString("conditionEmoji"),
                        conditionText = obj.getString("conditionText"),
                        farmingAdvisory = obj.getString("farmingAdvisory")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    fun parseDialectSummary(jsonStr: String, langCode: String): DialectSummary? {
        return try {
            val root = JSONObject(jsonStr)
            val code = if (root.has(langCode)) langCode else "en"
            val obj = root.getJSONObject(code)
            DialectSummary(
                languageCode = obj.getString("langCode"),
                languageName = obj.getString("langName"),
                localizedCondition = obj.getString("localizedCondition"),
                folkSummary = obj.getString("folkSummary"),
                farmingAdvice = obj.getString("farmingAdvice"),
                emergencyNotice = if (obj.optString("emergencyNotice").isNotBlank()) obj.getString("emergencyNotice") else null
            )
        } catch (e: Exception) {
            null
        }
    }
}
