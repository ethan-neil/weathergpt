package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CachedAlertEntity
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.HistoricalTrendEntity
import com.example.data.local.WeatherDatabase
import com.example.data.model.*
import com.example.data.repository.WeatherRepository
import com.example.util.VoiceAssistantManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WeatherDatabase.getDatabase(application)
    private val repository = WeatherRepository(application, database)
    private val voiceManager = VoiceAssistantManager(application)

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val selectedLocation: StateFlow<LocationItem> = repository.selectedLocation
    val selectedLanguage: StateFlow<AppLanguage> = repository.selectedLanguage
    val isLowBandwidthMode: StateFlow<Boolean> = repository.isLowBandwidthMode
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking

    private val _isAwaitingAiResponse = MutableStateFlow(false)
    val isAwaitingAiResponse: StateFlow<Boolean> = _isAwaitingAiResponse.asStateFlow()

    val currentWeather: StateFlow<CachedWeatherEntity?> = repository.getCurrentWeatherFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val alerts: StateFlow<List<CachedAlertEntity>> = repository.getAlertsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val climateTrends: StateFlow<List<HistoricalTrendEntity>> = repository.getClimateTrendsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hourlyList: StateFlow<List<HourlyForecast>> = currentWeather.map { entity ->
        if (entity != null) repository.parseHourly(entity.hourlyJson) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyList: StateFlow<List<DailyForecast>> = currentWeather.map { entity ->
        if (entity != null) repository.parseDaily(entity.dailyJson) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dialectSummary: StateFlow<DialectSummary?> = combine(currentWeather, selectedLanguage) { entity, lang ->
        if (entity != null) repository.parseDialectSummary(entity.dialectSummaryJson, lang.code) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLocation(location: LocationItem) {
        repository.setLocation(location)
    }

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
    }

    fun toggleLowBandwidth(enabled: Boolean) {
        repository.toggleLowBandwidthMode(enabled)
    }

    fun toggleDarkMode(enabled: Boolean) {
        repository.toggleDarkMode(enabled)
    }

    fun speak(text: String) {
        voiceManager.speak(text, selectedLanguage.value)
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAwaitingAiResponse.value = true
            try {
                repository.sendUserMessage(text)
            } finally {
                _isAwaitingAiResponse.value = false
            }
        }
    }

    fun clearChat() {
        repository.clearChat()
    }

    fun triggerSync() {
        repository.triggerAutoSync()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
