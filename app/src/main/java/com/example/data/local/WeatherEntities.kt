package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val locationKey: String, // e.g. "Pune_Maharashtra"
    val locationName: String,
    val state: String,
    val currentTempC: Int,
    val feelsLikeC: Int,
    val conditionMain: String,
    val conditionDescription: String,
    val conditionEmoji: String,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val windDirection: String,
    val rainfallProb: Int,
    val uvIndex: Int,
    val soilMoisturePercent: Int,
    val gfsTempC: Int,
    val wrfTempC: Int,
    val nwpSummary: String,
    val dialectSummaryJson: String, // Stored serialized JSON of dialect summaries
    val hourlyJson: String,
    val dailyJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "extreme_alerts")
data class CachedAlertEntity(
    @PrimaryKey val alertId: String,
    val title: String,
    val severityStr: String,
    val description: String,
    val affectedArea: String,
    val validUntil: String,
    val safetyInstructionsRaw: String, // Delimited by ||
    val audioSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "general",
    val languageCode: String = "en"
)

@Entity(tableName = "climate_trends")
data class HistoricalTrendEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationKey: String,
    val monthName: String,
    val rainfallActualMm: Float,
    val rainfallNormalMm: Float,
    val tempActualC: Float,
    val tempNormalC: Float,
    val anomalyDescription: String
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
