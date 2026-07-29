package com.example.ui.screens.more

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DevForgeData
import com.example.data.model.HttpStatusCodeModel
import com.example.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoreViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = AiRepository()
    private val dao = AppDatabase.getDatabase(application).savedItemDao()

    // --- HTTP Status Explorer State ---
    private val _httpQuery = MutableStateFlow("")
    val httpQuery: StateFlow<String> = _httpQuery.asStateFlow()

    private val _httpCategory = MutableStateFlow("ALL") // ALL, 1xx, 2xx, 3xx, 4xx, 5xx
    val httpCategory: StateFlow<String> = _httpCategory.asStateFlow()

    // Quiz Mode
    private val _quizCurrentQuestion = MutableStateFlow<HttpStatusCodeModel?>(null)
    val quizCurrentQuestion: StateFlow<HttpStatusCodeModel?> = _quizCurrentQuestion.asStateFlow()

    private val _quizOptions = MutableStateFlow<List<String>>(emptyList())
    val quizOptions: StateFlow<List<String>> = _quizOptions.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizAnswerResult = MutableStateFlow<Boolean?>(null)
    val quizAnswerResult: StateFlow<Boolean?> = _quizAnswerResult.asStateFlow()

    // --- AI Assistant State ---
    private val _customGeminiKey = MutableStateFlow("")
    val customGeminiKey: StateFlow<String> = _customGeminiKey.asStateFlow()

    private val _selectedAiRole = MutableStateFlow("General Assistant") // "General Assistant", "Senior Architect", "Fast Explainer"
    val selectedAiRole: StateFlow<String> = _selectedAiRole.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                message = "Welcome to DevForge AI Assistant! I am configured with Gemini multi-turn conversation support. Ask me about Android, Kotlin Coroutines, Jetpack Compose, REST APIs, or Room DB."
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // --- Settings State ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isBiometricsEnabled = MutableStateFlow(false)
    val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled.asStateFlow()

    init {
        nextQuizQuestion()
    }

    fun updateCustomGeminiKey(key: String) {
        _customGeminiKey.value = key
    }

    fun selectAiRole(role: String) {
        _selectedAiRole.value = role
    }

    // HTTP Actions
    fun updateHttpQuery(query: String) { _httpQuery.value = query }
    fun updateHttpCategory(category: String) { _httpCategory.value = category }

    fun nextQuizQuestion() {
        val allCodes = DevForgeData.httpStatusCodes
        val current = allCodes.random()
        _quizCurrentQuestion.value = current

        val options = mutableListOf(current.name)
        val otherNames = allCodes.filter { it.code != current.code }.map { it.name }.shuffled().take(3)
        options.addAll(otherNames)
        _quizOptions.value = options.shuffled()
        _quizAnswerResult.value = null
    }

    fun submitQuizAnswer(selectedOption: String) {
        val correct = _quizCurrentQuestion.value?.name
        val isCorrect = selectedOption == correct
        _quizAnswerResult.value = isCorrect
        if (isCorrect) {
            _quizScore.value += 10
        }
    }

    // AI Chat Actions
    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(sender = "USER", message = prompt)
        val updatedHistory = _aiMessages.value + userMsg
        _aiMessages.value = updatedHistory

        viewModelScope.launch {
            _isAiLoading.value = true
            val aiMsg = aiRepository.generateAiResponse(
                chatHistory = updatedHistory,
                userCustomKey = _customGeminiKey.value,
                aiRole = _selectedAiRole.value
            )
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            ChatMessage(
                sender = "AI",
                message = "Chat history cleared. How can DevForge AI assist your workflow?"
            )
        )
    }

    // Settings Actions
    fun toggleDarkTheme(enabled: Boolean) { _isDarkTheme.value = enabled }
    fun toggleBiometrics(enabled: Boolean) { _isBiometricsEnabled.value = enabled }

    fun clearAppData() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }
}
