package com.example.data.model

data class WeatherCondition(
    val main: String,
    val description: String,
    val iconEmoji: String,
    val simplifiedSymbol: String // E.g. "☀️ Sunny", "🌧️ Heavy Rain", "⛈️ Thunderstorm"
)

data class HourlyForecast(
    val time: String,
    val tempC: Int,
    val rainProb: Int,
    val conditionEmoji: String,
    val windKmh: Int
)

data class DailyForecast(
    val dayOfWeek: String,
    val date: String,
    val minTempC: Int,
    val maxTempC: Int,
    val rainProb: Int,
    val conditionEmoji: String,
    val conditionText: String,
    val farmingAdvisory: String
)

data class NWPModelInfo(
    val gfsTempC: Int,
    val wrfTempC: Int,
    val ecmwfRainProb: Int,
    val nwpConfidence: String, // "High (92%)", "Moderate (78%)"
    val modelSummary: String
)

data class DialectSummary(
    val languageCode: String,
    val languageName: String,
    val localizedCondition: String,
    val folkSummary: String,
    val farmingAdvice: String,
    val emergencyNotice: String? = null
)

data class ExtremeAlert(
    val id: String,
    val title: String,
    val severity: AlertSeverity,
    val description: String,
    val affectedArea: String,
    val validUntil: String,
    val safetyInstructions: List<String>,
    val audioSummary: String
)

enum class AlertSeverity {
    RED_WARNING,    // Severe (Cyclone, Flash Flood, Extreme Heatwave)
    ORANGE_ALERT,   // Be Prepared (Heavy Thunderstorms, High Wind)
    YELLOW_WATCH,   // Be Aware (Moderate Rain, Temperature Dip)
    GREEN_NORMAL    // Safe Conditions
}

data class ClimateTrend(
    val monthName: String,
    val rainfallActualMm: Float,
    val rainfallNormalMm: Float,
    val tempActualC: Float,
    val tempNormalC: Float,
    val anomalyDescription: String
)

data class LocationItem(
    val name: String,
    val state: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val elevationM: Int
)

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val localeTag: String
) {
    ENGLISH("en", "English", "English", "en-US"),
    HINDI("hi", "Hindi", "हिंदी", "hi-IN"),
    BENGALI("bn", "Bengali", "বাংলা", "bn-IN"),
    TELUGU("te", "Telugu", "తెలుగు", "te-IN"),
    TAMIL("ta", "Tamil", "தமிழ்", "ta-IN"),
    MARATHI("mr", "Marathi", "मराठी", "mr-IN"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "gu-IN"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "kn-IN"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", "pa-IN")
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "general", // "general", "crop_advisory", "disaster", "forecast", "nwp"
    val languageCode: String = "en",
    val isSpoken: Boolean = false
)
