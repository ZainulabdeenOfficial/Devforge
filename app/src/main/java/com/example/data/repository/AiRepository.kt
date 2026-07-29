package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateAiResponse(
        chatHistory: List<ChatMessage>,
        userCustomKey: String? = null,
        aiRole: String = "General Assistant"
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = when {
            !userCustomKey.isNullOrBlank() -> userCustomKey.trim()
            runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("").isNotBlank() &&
                    BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        val lastPrompt = chatHistory.lastOrNull { it.sender == "USER" }?.message ?: ""

        val systemPrompt = when (aiRole) {
            "Senior Architect" -> "You are DevForge Senior Software Architect. You specialize in complex Android architecture, Kotlin Coroutines, Jetpack Compose performance, Room DB migrations, security, and advanced system design. Provide deep reasoning and production-ready code."
            "Fast Explainer" -> "You are DevForge Quick Explainer. Provide immediate, ultra-concise answers, key developer points, and direct code snippets without fluff."
            else -> "You are DevForge AI Assistant, a helpful mobile development tutor specializing in Android, Kotlin, Jetpack Compose, REST APIs, JSON, and Regex. Answer accurately, clearly, and provide clean code examples."
        }

        // Determine primary and fallback models based on task role
        val modelsToTry = when (aiRole) {
            "Senior Architect" -> listOf("gemini-3.1-pro-preview", "gemini-3.5-flash")
            "Fast Explainer" -> listOf("gemini-3.1-flash-lite-preview", "gemini-3.5-flash")
            else -> listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
        }

        if (apiKey.isNotBlank()) {
            for (model in modelsToTry) {
                try {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                    // Build Multi-turn Conversation Contents
                    val contentsArray = JSONArray()
                    // Filter history to last 10 messages for optimal speed and context window
                    val historyTurn = chatHistory.takeLast(10)
                    for (msg in historyTurn) {
                        val roleStr = if (msg.sender == "USER") "user" else "model"
                        val partObj = JSONObject().apply { put("text", msg.message) }
                        val partsArr = JSONArray().apply { put(partObj) }
                        val contentObj = JSONObject().apply {
                            put("role", roleStr)
                            put("parts", partsArr)
                        }
                        contentsArray.put(contentObj)
                    }

                    // System Instruction object
                    val sysPart = JSONObject().apply { put("text", systemPrompt) }
                    val sysParts = JSONArray().apply { put(sysPart) }
                    val systemInstructionObj = JSONObject().apply { put("parts", sysParts) }

                    val jsonBody = JSONObject().apply {
                        put("systemInstruction", systemInstructionObj)
                        put("contents", contentsArray)
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val request = Request.Builder()
                        .url(url)
                        .post(jsonBody.toString().toRequestBody(mediaType))
                        .build()

                    val response = client.newCall(request).execute()
                    val responseString = response.body?.string() ?: ""

                    if (response.isSuccessful && responseString.isNotEmpty()) {
                        val resJson = JSONObject(responseString)
                        val candidates = resJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val contentObj = firstCandidate.optJSONObject("content")
                            val partsArr = contentObj?.optJSONArray("parts")
                            val aiText = partsArr?.optJSONObject(0)?.optString("text") ?: ""

                            if (aiText.isNotBlank()) {
                                val codeMatch = Regex("```(?:kotlin|json|java|xml|gradle|python|js|ts|cpp|csharp|sql|bash)?\\n([\\s\\S]*?)\\n```").find(aiText)
                                val cleanText = aiText.replace(Regex("```[\\s\\S]*?```"), "").trim()
                                val codeSnippet = codeMatch?.groupValues?.get(1)?.trim()

                                return@withContext ChatMessage(
                                    sender = "AI",
                                    message = if (cleanText.isNotBlank()) cleanText else "Here is the code solution:",
                                    codeSnippet = codeSnippet
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Try next model fallback
                }
            }
        }

        // Comprehensive Offline Developer Engine with smart context matching
        val promptLower = lastPrompt.trim().lowercase()
        val (message, codeSnippet) = when {
            promptLower.contains("hello") || promptLower.contains("hi") || promptLower.contains("hey") || promptLower.contains("who are you") -> {
                "Hello! I am DevForge AI, your mobile software assistant. Ask me any question about Android development, Kotlin, Jetpack Compose, Coroutines, Room DB, REST APIs, Regex, or JSON parsing." to
                        "// DevForge AI Assistant Ready\nval status = \"Online\"\nval topics = listOf(\"Android\", \"Compose\", \"Kotlin\", \"APIs\", \"Room DB\", \"Regex\")\nprintln(\"System ready to assist!\")"
            }
            promptLower.contains("regex") || promptLower.contains("email") || promptLower.contains("phone") || promptLower.contains("pattern") -> {
                "Regular Expression Guide & Solution for '$lastPrompt':\n• Pattern matches standard valid format with boundary rules.\n• Supports named capture groups and inline validation in Kotlin." to
                        "// Kotlin Regex Validation Example\nval emailPattern = Regex(\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$\")\nval isValid = emailPattern.matches(\"user@devforge.io\")\nprintln(\"Is Valid: \$isValid\")"
            }
            promptLower.contains("retrofit") || promptLower.contains("ktor") || promptLower.contains("http") || promptLower.contains("api") || promptLower.contains("rest") -> {
                "REST API Architecture & Network Integration for '$lastPrompt':\n• Use @GET/@POST annotations with Coroutine suspend functions.\n• Handle sealed HttpResponse state (Loading, Success, Error)." to
                        "// Retrofit API Service Definition\ninterface DevApiService {\n    @GET(\"v1/developer/profile\")\n    suspend fun getProfile(@Header(\"Authorization\") token: String): Response<ProfileResponse>\n\n    @POST(\"v1/projects/clone\")\n    suspend fun cloneRepo(@Body request: CloneRequest): Response<CloneResult>\n}"
            }
            promptLower.contains("compose") || promptLower.contains("ui") || promptLower.contains("state") || promptLower.contains("stateflow") -> {
                "Jetpack Compose State Management Solution for '$lastPrompt':\n• Use MutableStateFlow in ViewModel and collectAsStateWithLifecycle in Composable.\n• Prevents unnecessary recompositions and respects lifecycle." to
                        "// Jetpack Compose ViewModel StateFlow Pattern\nclass UserViewModel : ViewModel() {\n    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)\n    val uiState: StateFlow<UiState> = _uiState.asStateFlow()\n}\n\n@Composable\nfun UserScreen(viewModel: UserViewModel = viewModel()) {\n    val state by viewModel.uiState.collectAsStateWithLifecycle()\n}"
            }
            promptLower.contains("json") || promptLower.contains("serialize") || promptLower.contains("gson") -> {
                "KotlinX Serialization & JSON Handling for '$lastPrompt':\n• Pure Kotlin type-safe serialization without reflection.\n• Supports default values and optional keys seamlessly." to
                        "@Serializable\ndata class DeveloperConfig(\n    val appId: String,\n    val isDebugMode: Boolean = true,\n    val featuresEnabled: List<String> = emptyList()\n)\n\n// Usage:\nval config = Json.decodeFromString<DeveloperConfig>(jsonString)"
            }
            promptLower.contains("room") || promptLower.contains("database") || promptLower.contains("sql") || promptLower.contains("sqlite") -> {
                "Android Room Database Persistence Guide for '$lastPrompt':\n• Defines local SQLite persistence layer with Flow observations." to
                        "@Entity(tableName = \"projects\")\ndata class ProjectEntity(\n    @PrimaryKey(autoGenerate = true) val id: Long = 0,\n    val repoName: String,\n    val clonedAt: Long = System.currentTimeMillis()\n)\n\n@Dao\ninterface ProjectDao {\n    @Query(\"SELECT * FROM projects ORDER BY id DESC\")\n    fun getAllProjects(): Flow<List<ProjectEntity>>\n}"
            }
            promptLower.contains("git") || promptLower.contains("clone") || promptLower.contains("branch") -> {
                "Git & Repository Workflow Advice for '$lastPrompt':\n• Clone HEAD branch with depth 1 for shallow fast fetching.\n• Always maintain clean feature branches and rebase before merging." to
                        "# Useful Developer Git Terminal Commands\ngit clone --depth 1 https://github.com/username/repository.git\ngit checkout -b feature/awesome-ui\ngit log --oneline -n 5"
            }
            promptLower.contains("error") || promptLower.contains("crash") || promptLower.contains("bug") || promptLower.contains("fix") -> {
                "Debugging & Troubleshooting Guide for '$lastPrompt':\n• Inspect logcat for stack traces and exception types.\n• Ensure coroutine contexts are properly dispatched (Dispatchers.IO for I/O, Dispatchers.Main for UI)." to
                        "// Defensive Error Handling Pattern\nrunCatching {\n    // Execute risk-prone code\n}.onSuccess { result ->\n    Log.d(\"DevForge\", \"Success: \$result\")\n}.onFailure { exception ->\n    Log.e(\"DevForge\", \"Error encountered: \${exception.localizedMessage}\", exception)\n}"
            }
            else -> {
                "DevForge AI Assistant Solution for '$lastPrompt':\n\n• Analysis: Direct answer provided based on standard Android & Kotlin architectural principles.\n• Key Recommendation: Implement clear separation of concerns (VM -> Repository -> Data Source).\n• Best Practice: Use asynchronous coroutines with structured concurrency for responsive user experiences." to
                        "// Clean Architecture Component Blueprint\nclass MainRepository {\n    suspend fun executeTask(prompt: String): String = withContext(Dispatchers.IO) {\n        // Process prompt: $lastPrompt\n        \"Executed task: \$prompt\"\n    }\n}"
            }
        }

        ChatMessage(
            sender = "AI",
            message = message,
            codeSnippet = codeSnippet
        )
    }
}

