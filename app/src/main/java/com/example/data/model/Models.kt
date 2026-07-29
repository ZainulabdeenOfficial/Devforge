package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HttpMethod {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS
}

enum class AuthType {
    NONE, BEARER, BASIC, API_KEY, JWT
}

enum class BodyType {
    NONE, RAW_JSON, XML, PLAIN_TEXT, FORM_DATA
}

data class KeyValue(
    val key: String,
    val value: String,
    val enabled: Boolean = true
)

data class ApiRequestModel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "New Request",
    val method: HttpMethod = HttpMethod.GET,
    val url: String = "https://jsonplaceholder.typicode.com/todos/1",
    val headers: List<KeyValue> = listOf(KeyValue("Accept", "application/json")),
    val queryParams: List<KeyValue> = emptyList(),
    val authType: AuthType = AuthType.NONE,
    val authToken: String = "",
    val authUsername: String = "",
    val authPassword: String = "",
    val apiKeyName: String = "X-API-Key",
    val apiKeyValue: String = "",
    val bodyType: BodyType = BodyType.NONE,
    val bodyContent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ApiResponseModel(
    val statusCode: Int = 0,
    val statusText: String = "",
    val durationMs: Long = 0,
    val sizeBytes: Long = 0,
    val headers: List<KeyValue> = emptyList(),
    val body: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "API", "REGEX", "JSON", "BOOKMARK", "HISTORY"
    val title: String,
    val subtitle: String,
    val contentData: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

data class RegexPreset(
    val title: String,
    val pattern: String,
    val description: String,
    val sampleText: String,
    val category: String
)

data class HttpStatusCodeModel(
    val code: Int,
    val name: String,
    val category: String, // 1xx, 2xx, 3xx, 4xx, 5xx
    val description: String,
    val commonCauses: String,
    val devTip: String
)

data class GitHubRepoModel(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val starsCount: Int,
    val forksCount: Int,
    val openIssuesCount: Int,
    val language: String?,
    val htmlUrl: String,
    val updatedAt: String
)

data class GitHubUserModel(
    val login: String,
    val name: String?,
    val avatarUrl: String,
    val bio: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val company: String?,
    val location: String?,
    val blog: String?
)

data class DevTutorial(
    val id: String,
    val title: String,
    val category: String, // REST, GraphQL, Regex, OAuth, JWT, Security
    val readTime: String,
    val summary: String,
    val fullContent: String
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val codeSnippet: String? = null
)
