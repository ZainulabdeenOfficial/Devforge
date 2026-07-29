package com.example.data.repository

import com.example.data.model.GitHubRepoModel
import com.example.data.model.GitHubUserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class GitHubRepository {

    private val client = OkHttpClient()

    suspend fun searchRepositories(query: String): List<GitHubRepoModel> = withContext(Dispatchers.IO) {
        val searchQuery = if (query.isBlank()) "android kotlin language:kotlin" else query
        val url = "https://api.github.com/search/repositories?q=${java.net.URLEncoder.encode(searchQuery, "UTF-8")}&sort=stars&order=desc&per_page=20"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DevForge-Android-App")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                val json = JSONObject(body)
                val items = json.getJSONArray("items")
                val list = mutableListOf<GitHubRepoModel>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val owner = item.getJSONObject("owner")
                    list.add(
                        GitHubRepoModel(
                            id = item.getLong("id"),
                            name = item.getString("name"),
                            fullName = item.getString("full_name"),
                            description = item.optString("description", "No description provided"),
                            ownerLogin = owner.getString("login"),
                            ownerAvatarUrl = owner.getString("avatar_url"),
                            starsCount = item.getInt("stargazers_count"),
                            forksCount = item.getInt("forks_count"),
                            openIssuesCount = item.getInt("open_issues_count"),
                            language = item.optString("language", "Kotlin"),
                            htmlUrl = item.getString("html_url"),
                            updatedAt = item.optString("updated_at", "")
                        )
                    )
                }
                list
            } else {
                getFallbackRepos()
            }
        } catch (e: Exception) {
            getFallbackRepos()
        }
    }

    suspend fun getUserRepos(username: String): List<GitHubRepoModel> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/users/$username/repos?sort=updated&per_page=30"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DevForge-Android-App")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                val array = JSONArray(body)
                val list = mutableListOf<GitHubRepoModel>()
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val owner = item.getJSONObject("owner")
                    list.add(
                        GitHubRepoModel(
                            id = item.getLong("id"),
                            name = item.getString("name"),
                            fullName = item.getString("full_name"),
                            description = item.optString("description", "No description provided"),
                            ownerLogin = owner.getString("login"),
                            ownerAvatarUrl = owner.getString("avatar_url"),
                            starsCount = item.getInt("stargazers_count"),
                            forksCount = item.getInt("forks_count"),
                            openIssuesCount = item.getInt("open_issues_count"),
                            language = item.optString("language", "Code"),
                            htmlUrl = item.getString("html_url"),
                            updatedAt = item.optString("updated_at", "")
                        )
                    )
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRepoReadme(fullName: String): String = withContext(Dispatchers.IO) {
        val url = "https://raw.githubusercontent.com/$fullName/main/README.md"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DevForge-Android-App")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "# $fullName\n\nREADME file preview unavailable."
            } else {
                // Try master branch fallback
                val fallbackUrl = "https://raw.githubusercontent.com/$fullName/master/README.md"
                val fallbackReq = Request.Builder().url(fallbackUrl).build()
                val fallbackResp = client.newCall(fallbackReq).execute()
                if (fallbackResp.isSuccessful) {
                    fallbackResp.body?.string() ?: "# $fullName\n\nREADME content empty."
                } else {
                    "# $fullName\n\n### Open Source Repository\n\nThis repository is hosted on GitHub. Click **OPEN GITHUB** to inspect branches and commits on GitHub."
                }
            }
        } catch (e: Exception) {
            "# $fullName\n\nUnable to fetch README: ${e.localizedMessage}"
        }
    }

    suspend fun getUserProfile(username: String): GitHubUserModel? = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/users/$username"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "DevForge-Android-App")
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful && body.isNotEmpty()) {
                val json = JSONObject(body)
                GitHubUserModel(
                    login = json.getString("login"),
                    name = json.optString("name", username),
                    avatarUrl = json.getString("avatar_url"),
                    bio = json.optString("bio", "Mobile developer & open-source enthusiast"),
                    publicRepos = json.optInt("public_repos", 0),
                    followers = json.optInt("followers", 0),
                    following = json.optInt("following", 0),
                    company = json.optString("company", "Independent"),
                    location = json.optString("location", "Global"),
                    blog = json.optString("blog", "https://github.com/$username")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFallbackRepos(): List<GitHubRepoModel> {
        return listOf(
            GitHubRepoModel(
                id = 101,
                name = "architecture-samples",
                fullName = "android/architecture-samples",
                description = "A collection of samples to discuss and showcase different architectural tools and patterns for Android apps.",
                ownerLogin = "android",
                ownerAvatarUrl = "https://avatars.githubusercontent.com/u/3268959",
                starsCount = 43200,
                forksCount = 11800,
                openIssuesCount = 42,
                language = "Kotlin",
                htmlUrl = "https://github.com/android/architecture-samples",
                updatedAt = "2026-07-20"
            ),
            GitHubRepoModel(
                id = 102,
                name = "compose-samples",
                fullName = "android/compose-samples",
                description = "Official Jetpack Compose samples showcasing modern UI development in Android with Kotlin.",
                ownerLogin = "android",
                ownerAvatarUrl = "https://avatars.githubusercontent.com/u/3268959",
                starsCount = 18900,
                forksCount = 3800,
                openIssuesCount = 18,
                language = "Kotlin",
                htmlUrl = "https://github.com/android/compose-samples",
                updatedAt = "2026-07-22"
            ),
            GitHubRepoModel(
                id = 103,
                name = "retrofit",
                fullName = "square/retrofit",
                description = "A type-safe HTTP client for Android and the JVM.",
                ownerLogin = "square",
                ownerAvatarUrl = "https://avatars.githubusercontent.com/u/82592",
                starsCount = 42800,
                forksCount = 7200,
                openIssuesCount = 95,
                language = "Java/Kotlin",
                htmlUrl = "https://github.com/square/retrofit",
                updatedAt = "2026-07-15"
            ),
            GitHubRepoModel(
                id = 104,
                name = "coil",
                fullName = "coil-kt/coil",
                description = "Image loading for Android backed by Kotlin Coroutines.",
                ownerLogin = "coil-kt",
                ownerAvatarUrl = "https://avatars.githubusercontent.com/u/52394602",
                starsCount = 10200,
                forksCount = 740,
                openIssuesCount = 12,
                language = "Kotlin",
                htmlUrl = "https://github.com/coil-kt/coil",
                updatedAt = "2026-07-18"
            )
        )
    }
}
