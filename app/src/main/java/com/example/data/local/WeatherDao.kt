package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Weather Cache
    @Query("SELECT * FROM cached_weather WHERE locationKey = :locationKey LIMIT 1")
    fun getWeatherByLocation(locationKey: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestWeather(): Flow<CachedWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: CachedWeatherEntity)

    // Alerts
    @Query("SELECT * FROM extreme_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<CachedAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<CachedAlertEntity>)

    @Query("DELETE FROM extreme_alerts")
    suspend fun clearAlerts()

    // Chat History
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    // Climate Trends
    @Query("SELECT * FROM climate_trends WHERE locationKey = :locationKey ORDER BY id ASC")
    fun getTrendsForLocation(locationKey: String): Flow<List<HistoricalTrendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrends(trends: List<HistoricalTrendEntity>)

    // App Settings
    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
