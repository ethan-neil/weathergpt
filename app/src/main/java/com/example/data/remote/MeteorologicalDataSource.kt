package com.example.data.remote

import com.example.data.local.CachedAlertEntity
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.HistoricalTrendEntity
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object MeteorologicalDataSource {

    val AVAILABLE_LOCATIONS = listOf(
        LocationItem("Pune", "Maharashtra", "India", 18.5204, 73.8567, 560),
        LocationItem("Delhi NCR", "Delhi", "India", 28.6139, 77.2090, 216),
        LocationItem("Nagpur (Vidarbha)", "Maharashtra", "India", 21.1458, 79.0882, 310),
        LocationItem("Bengaluru", "Karnataka", "India", 12.9716, 77.5946, 920),
        LocationItem("Hyderabad", "Telangana", "India", 17.3850, 78.4867, 542),
        LocationItem("Bhubaneswar", "Odisha", "India", 20.2961, 85.8245, 45),
        LocationItem("Patna", "Bihar", "India", 25.5941, 85.1376, 53),
        LocationItem("Chennai", "Tamil Nadu", "India", 13.0827, 80.2707, 6),
        LocationItem("Kolkata", "West Bengal", "India", 22.5726, 88.3639, 9),
        LocationItem("Ludhiana", "Punjab", "India", 30.9010, 75.8573, 244),
        LocationItem("Ahmedabad", "Gujarat", "India", 23.0225, 72.5714, 53),
        LocationItem("Jaipur", "Rajasthan", "India", 26.9124, 75.7873, 431)
    )

    fun generateWeatherForLocation(location: LocationItem): CachedWeatherEntity {
        val baseTemp = when (location.name) {
            "Pune" -> 28
            "Delhi NCR" -> 33
            "Nagpur (Vidarbha)" -> 34
            "Bengaluru" -> 26
            "Hyderabad" -> 30
            "Bhubaneswar" -> 31
            "Patna" -> 32
            "Chennai" -> 32
            "Kolkata" -> 31
            "Ludhiana" -> 30
            "Ahmedabad" -> 35
            "Jaipur" -> 36
            else -> 29
        }

        val rainProb = when (location.name) {
            "Bhubaneswar", "Chennai", "Kolkata" -> 65
            "Pune", "Bengaluru" -> 40
            "Delhi NCR", "Ludhiana" -> 25
            else -> 20
        }

        val condition = if (rainProb > 50) {
            WeatherCondition("Rain", "Scattered Thunderstorms & Showers", "⛈️", "🌧️ Rain")
        } else if (rainProb > 30) {
            WeatherCondition("Clouds", "Partly Cloudy with Breeze", "⛅", "⛅ Cloudy")
        } else {
            WeatherCondition("Clear", "Sunny & Warm Skies", "☀️", "☀️ Sunny")
        }

        val hourlyList = listOf(
            HourlyForecast("Now", baseTemp, rainProb, condition.iconEmoji, 14),
            HourlyForecast("11:00", baseTemp + 2, rainProb, condition.iconEmoji, 16),
            HourlyForecast("14:00", baseTemp + 4, rainProb + 10, "⛅", 18),
            HourlyForecast("17:00", baseTemp + 1, rainProb + 15, "🌧️", 20),
            HourlyForecast("20:00", baseTemp - 2, rainProb - 5, "☁️", 12),
            HourlyForecast("23:00", baseTemp - 4, rainProb - 10, "🌙", 10)
        )

        val dailyList = listOf(
            DailyForecast("Today", "26 Aug", baseTemp - 5, baseTemp + 4, rainProb, condition.iconEmoji, condition.description, "Ideal for morning spraying. Postpone drying post 3 PM."),
            DailyForecast("Tomorrow", "27 Aug", baseTemp - 4, baseTemp + 3, rainProb + 10, "🌧️", "Intermittent Rains", "Maintain soil drainage in lower terraces."),
            DailyForecast("Thu", "28 Aug", baseTemp - 5, baseTemp + 5, 20, "⛅", "Partly Cloudy", "Good window for pesticide foliar application."),
            DailyForecast("Fri", "29 Aug", baseTemp - 3, baseTemp + 6, 15, "☀️", "Clear Sunny", "Optimum day for field harvesting and transport."),
            DailyForecast("Sat", "30 Aug", baseTemp - 4, baseTemp + 5, 30, "⛅", "Mild Breeze", "Favorable for sowing oilseeds and pulses."),
            DailyForecast("Sun", "31 Aug", baseTemp - 5, baseTemp + 4, 45, "⛈️", "Evening Thunderstorm", "Secure cattle indoors by late afternoon.")
        )

        val dialectMap = mapOf(
            "en" to DialectSummary(
                "en", "English", condition.description,
                "Mild day with scattered cloud coverage. Gentle breeze from South-West.",
                "Favorable for field operations in the morning. Keep harvested grains sheltered.",
                if (rainProb > 60) "Thunderstorm watch active for evening hours." else null
            ),
            "hi" to DialectSummary(
                "hi", "Hindi", "आंशिक रूप से बादल और हल्की धूप",
                "दिन में हल्की धूप और शाम को ठंडी हवा चलने की संभावना है।",
                "सुबह के समय कीटनाशक छिड़काव के लिए अनुकूल समय है। कटी फसल को ढककर रखें।",
                if (rainProb > 60) "शाम को गरज-चमक के साथ बारिश का अलर्ट।" else null
            ),
            "mr" to DialectSummary(
                "mr", "Marathi", "ढगाळ वातावरण व हलकी ऊन",
                "दिवसभर हलके ढग राहतील, संध्याकाळी गार वारा सुटेल.",
                "सकाळच्या वेळी औषध फवारणीसाठी योग्य वेळ. धान्य सुरक्षित जागी ठेवावे.",
                if (rainProb > 60) "सायंकाळी विजांच्या कडकडाटासह पावसाची शक्यता." else null
            ),
            "bn" to DialectSummary(
                "bn", "Bengali", "আংশিক মেঘলা ও রোদ",
                "সারা দিন হালকা মেঘ ও মৃদু হাওয়া থাকবে।",
                "সকালে সার ও ওষুধ স্প্রে করার জন্য ভালো সময়। ফসল শুকনো রাখুন।",
                if (rainProb > 60) "সন্ধ্যায় বজ্রবিদ্যুৎ সহ বৃষ্টির সতর্কতা।" else null
            ),
            "te" to DialectSummary(
                "te", "Telugu", "పాక్షికంగా మేఘావృతం",
                "పగటిపూట తేలికపాటి ఎండ, సాయంత్రం చల్లని గాలులు ఉంటాయి.",
                "ఉదయం పూట మందులు పిచికారీ చేయడానికి అనుకూలం.",
                if (rainProb > 60) "సాయంత్రం ఉరుములతో కూడిన వర్షం హెచ్చరిక." else null
            ),
            "ta" to DialectSummary(
                "ta", "Tamil", "பகுதி மேகமூட்டம்",
                "பகலில் மிதமான வெயில், மாலையில் குளிர்ந்த காற்று வீசும்.",
                "காலை வேளையில் பயிர்களுக்கு மருந்து தெளிக்க ஏற்றது.",
                if (rainProb > 60) "மாலையில் இடியுடன் கூடிய மழை எச்சரிக்கை." else null
            ),
            "gu" to DialectSummary(
                "gu", "Gujarati", "વાદળછાયું વાતાવરણ",
                "દિવસ દરમિયાન હળવા વાદળો અને સાંજ સુધીમાં ઠંડો પવન ફૂંકાશે.",
                "સવારના સમયે દવાનો છંટકાવ કરવો શ્રેષ્ઠ રહેશે.",
                if (rainProb > 60) "સાંજે ગાજવીજ સાથે વરસાદની ચેતવણી." else null
            ),
            "kn" to DialectSummary(
                "kn", "Kannada", "ಭಾಗಶಃ ಮೋಡ ಕವಿದ ವಾತಾವರಣ",
                "ಹಗಲಿನಲ್ಲಿ ಸಾಧಾರಣ ಬಿಸಿಲು, ಸಂಜೆ ತಂಪಾದ ಗಾಳಿ ಬೀಸಲಿದೆ.",
                "ಬೆಳಗಿನ ಜಾವ ಕೀಟನಾಶಕ ಸಿಂಪಡಣೆಗೆ ಸೂಕ್ತ ಸಮಯ.",
                if (rainProb > 60) "ಸಂಜೆ ಗುಡುಗು ಸಹಿತ ಮಳೆ ಎಚ್ಚರಿಕೆ." else null
            ),
            "pa" to DialectSummary(
                "pa", "Punjabi", "ਬੱਦਲਵਾਈ ਅਤੇ ਹਲਕੀ ਧੁੱਪ",
                "ਦਿਨ ਵੇਲੇ ਹਲਕੀ ਧੁੱਪ ਅਤੇ ਸ਼ਾਮ ਨੂੰ ਠੰਢੀ ਹਵਾ ਚੱਲਣ ਦੀ ਉਮੀਦ ਹੈ।",
                "ਸਵੇਰ ਦੇ ਸਮੇਂ ਸਪਰੇਅ ਕਰਨਾ ਫਸਲ ਲਈ ਲਾਹੇਵੰਦ ਰਹੇਗਾ।",
                if (rainProb > 60) "ਸ਼ਾਮ ਨੂੰ ਗਰਜ-ਚਮਕ ਨਾਲ ਮੀਂਹ ਦਾ ਅਲਰਟ।" else null
            )
        )

        val dialectJson = JSONObject()
        dialectMap.forEach { (k, v) ->
            val obj = JSONObject()
            obj.put("langCode", v.languageCode)
            obj.put("langName", v.languageName)
            obj.put("localizedCondition", v.localizedCondition)
            obj.put("folkSummary", v.folkSummary)
            obj.put("farmingAdvice", v.farmingAdvice)
            obj.put("emergencyNotice", v.emergencyNotice ?: "")
            dialectJson.put(k, obj)
        }

        val hourlyArray = JSONArray()
        hourlyList.forEach {
            val obj = JSONObject()
            obj.put("time", it.time)
            obj.put("tempC", it.tempC)
            obj.put("rainProb", it.rainProb)
            obj.put("conditionEmoji", it.conditionEmoji)
            obj.put("windKmh", it.windKmh)
            hourlyArray.put(obj)
        }

        val dailyArray = JSONArray()
        dailyList.forEach {
            val obj = JSONObject()
            obj.put("dayOfWeek", it.dayOfWeek)
            obj.put("date", it.date)
            obj.put("minTempC", it.minTempC)
            obj.put("maxTempC", it.maxTempC)
            obj.put("rainProb", it.rainProb)
            obj.put("conditionEmoji", it.conditionEmoji)
            obj.put("conditionText", it.conditionText)
            obj.put("farmingAdvisory", it.farmingAdvisory)
            dailyArray.put(obj)
        }

        return CachedWeatherEntity(
            locationKey = "${location.name}_${location.state}",
            locationName = location.name,
            state = location.state,
            currentTempC = baseTemp,
            feelsLikeC = baseTemp + 2,
            conditionMain = condition.main,
            conditionDescription = condition.description,
            conditionEmoji = condition.iconEmoji,
            humidityPercent = 64,
            windSpeedKmh = 14,
            windDirection = "SSW (205°)",
            rainfallProb = rainProb,
            uvIndex = 6,
            soilMoisturePercent = 58,
            gfsTempC = baseTemp,
            wrfTempC = baseTemp + 1,
            nwpSummary = "GFS 0.25° & WRF 3km high-resolution models indicate stable atmospheric boundary layer with 88% confidence.",
            dialectSummaryJson = dialectJson.toString(),
            hourlyJson = hourlyArray.toString(),
            dailyJson = dailyArray.toString(),
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun getInitialAlerts(): List<CachedAlertEntity> {
        return listOf(
            CachedAlertEntity(
                alertId = "alert_cyclone_001",
                title = "Cyclone 'Sagar' Low-Pressure Watch",
                severityStr = "ORANGE_ALERT",
                description = "Depression intensifying over Bay of Bengal. Squally winds reaching 55-65 km/h gusting to 75 km/h along coastal belts.",
                affectedArea = "Odisha, Coastal Andhra & West Bengal coast",
                validUntil = "Valid until 28 Aug 18:00 IST",
                safetyInstructionsRaw = "Fishermen strictly advised not to venture into deep sea||Farmers safeguard harvested crops under waterproof tarpaulins||Stay away from vulnerable electric poles and loose hoardings",
                audioSummary = "Orange alert for coastal belts. Squally winds up to 65 km/h expected. Fishermen avoid sea. Secure farm produce."
            ),
            CachedAlertEntity(
                alertId = "alert_thunderstorm_002",
                title = "Severe Convective Thunderstorm & Lightning",
                severityStr = "YELLOW_WATCH",
                description = "Isolated thunderstorm accompanied with cloud-to-ground lightning and gusty winds (30-40 km/h) likely over inland districts.",
                affectedArea = "Maharashtra, Telangana & North Karnataka",
                validUntil = "Valid for next 12 hours",
                safetyInstructionsRaw = "Take shelter in sturdy pucca buildings during lightning||Do not take shelter under solitary tall trees||Unplug sensitive agricultural motor pumps",
                audioSummary = "Yellow alert: Thunderstorm and lightning likely in afternoon. Stay indoors in sturdy structures."
            )
        )
    }

    fun getClimateTrendsForLocation(locationKey: String): List<HistoricalTrendEntity> {
        return listOf(
            HistoricalTrendEntity(locationKey = locationKey, monthName = "Apr", rainfallActualMm = 18f, rainfallNormalMm = 15f, tempActualC = 37.2f, tempNormalC = 36.5f, anomalyDescription = "+0.7°C Above Normal"),
            HistoricalTrendEntity(locationKey = locationKey, monthName = "May", rainfallActualMm = 42f, rainfallNormalMm = 35f, tempActualC = 38.5f, tempNormalC = 37.8f, anomalyDescription = "+20% Pre-monsoon showers"),
            HistoricalTrendEntity(locationKey = locationKey, monthName = "Jun", rainfallActualMm = 165f, rainfallNormalMm = 145f, tempActualC = 31.0f, tempNormalC = 31.5f, anomalyDescription = "Monsoon onset on schedule"),
            HistoricalTrendEntity(locationKey = locationKey, monthName = "Jul", rainfallActualMm = 280f, rainfallNormalMm = 250f, tempActualC = 27.4f, tempNormalC = 27.8f, anomalyDescription = "+12% Active Monsoon Surplus"),
            HistoricalTrendEntity(locationKey = locationKey, monthName = "Aug", rainfallActualMm = 210f, rainfallNormalMm = 195f, tempActualC = 26.8f, tempNormalC = 27.0f, anomalyDescription = "Consistent precipitation phase")
        )
    }
}
