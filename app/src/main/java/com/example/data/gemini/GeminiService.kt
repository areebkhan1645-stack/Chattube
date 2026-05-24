package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Model definition for visual filter parameters
data class AIFilterResult(
    val filterName: String,
    val hexColorOverlay1: String, // e.g. "#40FF00FF"
    val hexColorOverlay2: String, // e.g. "#20120A"
    val contrastDelta: Float,
    val stickerDescription: String,
    val description: String
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Check if API key is valid
    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateCaption(imagePromptDetails: String, vibe: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext "Living the $vibe energy! ✨📸 #chattube #viral #goodvibes"
        }

        val prompt = """
            Create a high-energy, engaging Snapchat and Instagram hybrid post caption based on these details: "$imagePromptDetails"
            The overall mood or vibe of the post is: "$vibe"
            
            Keep the caption punchy, cool, full of relevant emojis, and add 3 to 5 trending hashtags including #chattube.
            Do not include any intro, preambles, or markdown formatting. Just provide the raw caption text.
        """.trimIndent()

        try {
            // Build raw JSON payload manually using JSONObject
            val payload = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)
                
                // Add system instructions
                put("systemInstruction", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are an expert social media manager of Chattube, a Snapchat/Instagram combo. You write short, catchy, engaging, emoji-rich captions for Gen-Z users.")
                        })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.85)
                    put("maxOutputTokens", 150)
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Vibe: $vibe ✨ Chattube style! #vibe #trending #chattube"
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val textResponse = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                textResponse?.trim() ?: "Living my best life in ChatTube! ✨📸 #chattube #viral"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating caption", e)
            "Loving the $vibe energy! ✨📸 #chattube #viral #goodvibes"
        }
    }

    suspend fun generateCustomFilter(vibeKeyword: String): AIFilterResult = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            return@withContext getFallbackFilter(vibeKeyword)
        }

        val prompt = """
            Create a custom camera aesthetic filter JSON based on the vibe keyword: "$vibeKeyword".
            Return exactly a JSON object matching this schema:
            {
               "filterName": "String (Short cool name for the filter, under 18 chars)",
               "hexColorOverlay1": "String (8-digit ARGB hex color e.g. #30FF55AA with transparency built-in, represents main tint)",
               "hexColorOverlay2": "String (8-digit ARGB hex color e.g. #2000FFFF with transparency, represent gradient tint)",
               "contrastDelta": "Float (between 0.8 and 1.5, representing intensity)",
               "stickerDescription": "String (emoji or short symbol like 🪐 or ⚡️ or 🦄 representing a suitable top-right sticker)",
               "description": "String (short description of the aesthetic vibe generated)"
            }
            Do not include markdown or other decorators. Return valid raw JSON.
        """.trimIndent()

        try {
            val payload = JSONObject().apply {
                val contentsArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                    put("maxOutputTokens", 250)
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=${BuildConfig.GEMINI_API_KEY}")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getFallbackFilter(vibeKeyword)
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val textResponse = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                if (textResponse != null) {
                    val filterJson = JSONObject(textResponse)
                    AIFilterResult(
                        filterName = filterJson.getString("filterName"),
                        hexColorOverlay1 = filterJson.getString("hexColorOverlay1"),
                        hexColorOverlay2 = filterJson.getString("hexColorOverlay2"),
                        contrastDelta = filterJson.optDouble("contrastDelta", 1.0).toFloat(),
                        stickerDescription = filterJson.getString("stickerDescription"),
                        description = filterJson.getString("description")
                    )
                } else {
                    getFallbackFilter(vibeKeyword)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating filter", e)
            getFallbackFilter(vibeKeyword)
        }
    }

    private fun getFallbackFilter(vibeKeyword: String): AIFilterResult {
        return when (vibeKeyword.lowercase().trim()) {
            "cyberpunk", "cyber", "neon" -> AIFilterResult(
                filterName = "Neon Overdrive",
                hexColorOverlay1 = "#25FF00AA", // magenta tint
                hexColorOverlay2 = "#1500FFFF", // cyan gradient
                contrastDelta = 1.3f,
                stickerDescription = "⚡️",
                description = "High-voltage neon magenta & cyan wash for futuristic night snaps."
            )
            "retro", "vintage", "analog" -> AIFilterResult(
                filterName = "1995 Nostalgia",
                hexColorOverlay1 = "#20D4A373", // Warm sepia / orange
                hexColorOverlay2 = "#10603000", // Warm brown
                contrastDelta = 0.9f,
                stickerDescription = "📼",
                description = "Analog warm sepia grain with dusty highlights and classic camcorder overlay."
            )
            "dreamy", "ethereal", "angel" -> AIFilterResult(
                filterName = "Ethereal Dream",
                hexColorOverlay1 = "#18F7B5FF", // lavender
                hexColorOverlay2 = "#12E8C2CA", // pearlescent
                contrastDelta = 0.85f,
                stickerDescription = "✨",
                description = "Soft diffuse pastel glow with purple and pearlescent cloud sparkles."
            )
            "glare", "sun", "sunset" -> AIFilterResult(
                filterName = "Golden Hour",
                hexColorOverlay1 = "#30FFA700", // warm gold
                hexColorOverlay2 = "#15E53E20", // reddish orange
                contrastDelta = 1.15f,
                stickerDescription = "🌅",
                description = "Perpetually sun-drenched backlight. Turns any camera view into warm evening."
            )
            else -> AIFilterResult(
                filterName = "$vibeKeyword Lens",
                hexColorOverlay1 = "#1800E5FF", // subtle cyan
                hexColorOverlay2 = "#12A600FF", // subtle purple
                contrastDelta = 1.05f,
                stickerDescription = "🪄",
                description = "Dynamically generated $vibeKeyword aesthetic style with rich contrasts."
            )
        }
    }
}
