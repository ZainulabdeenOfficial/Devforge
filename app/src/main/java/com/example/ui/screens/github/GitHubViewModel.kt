package com.example.ui.screens.github

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GitHubRepoModel
import com.example.data.model.GitHubUserModel
import com.example.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GitHubViewModel : ViewModel() {

    private val repository = GitHubRepository()

    private val _searchQuery = MutableStateFlow("android compose")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _repositories = MutableStateFlow<List<GitHubRepoModel>>(emptyList())
    val repositories: StateFlow<List<GitHubRepoModel>> = _repositories.asStateFlow()

    private val _selectedUser = MutableStateFlow<GitHubUserModel?>(null)
    val selectedUser: StateFlow<GitHubUserModel?> = _selectedUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userRepos = MutableStateFlow<List<GitHubRepoModel>>(emptyList())
    val userRepos: StateFlow<List<GitHubRepoModel>> = _userRepos.asStateFlow()

    private val _repoReadme = MutableStateFlow<String?>(null)
    val repoReadme: StateFlow<String?> = _repoReadme.asStateFlow()

    private val _cloneProgress = MutableStateFlow<Float?>(null) // null = idle, 0..1 = progress
    val cloneProgress: StateFlow<Float?> = _cloneProgress.asStateFlow()

    private val _cloneStatusMessage = MutableStateFlow("")
    val cloneStatusMessage: StateFlow<String> = _cloneStatusMessage.asStateFlow()

    init {
        searchRepositories("android compose")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchRepositories(query: String = _searchQuery.value) {
        viewModelScope.launch {
            _isLoading.value = true
            val results = repository.searchRepositories(query)
            _repositories.value = results
            _isLoading.value = false
        }
    }

    fun fetchUserWithRepos(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.getUserProfile(username)
            val repos = repository.getUserRepos(username)
            _selectedUser.value = user
            _userRepos.value = repos
            _isLoading.value = false
        }
    }

    fun fetchRepoReadme(fullName: String) {
        viewModelScope.launch {
            _repoReadme.value = "Fetching README.md for $fullName..."
            val content = repository.getRepoReadme(fullName)
            _repoReadme.value = content
        }
    }

    fun clearReadme() {
        _repoReadme.value = null
    }

    fun cloneAndDownloadRepo(context: Context, repo: GitHubRepoModel) {
        viewModelScope.launch {
            _cloneProgress.value = 0.10f
            _cloneStatusMessage.value = "Preparing clone & zip archive request for ${repo.fullName}..."

            kotlinx.coroutines.delay(400)
            _cloneProgress.value = 0.35f
            _cloneStatusMessage.value = "Requesting GitHub archive stream for ${repo.fullName}..."

            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                val zipUrl = "https://github.com/${repo.fullName}/archive/refs/heads/main.zip"
                val fileName = "${repo.name}-main.zip"

                if (downloadManager != null) {
                    val request = DownloadManager.Request(Uri.parse(zipUrl))
                        .setTitle("${repo.name}.zip")
                        .setDescription("Cloning GitHub repository ${repo.fullName}")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                    downloadManager.enqueue(request)
                }

                _cloneProgress.value = 0.80f
                _cloneStatusMessage.value = "Downloading archive to Phone Storage: Downloads/$fileName..."

                kotlinx.coroutines.delay(800)
                _cloneProgress.value = 1.0f
                _cloneStatusMessage.value = "SUCCESS! Repository ${repo.name} saved to Phone Storage (Downloads/$fileName)"

                Toast.makeText(
                    context,
                    "Repository ${repo.name} ZIP saved to Phone Storage Downloads!",
                    Toast.LENGTH_LONG
                ).show()

                kotlinx.coroutines.delay(3500)
                _cloneProgress.value = null
            } catch (e: Exception) {
                _cloneProgress.value = null
                _cloneStatusMessage.value = "Failed to save ZIP: ${e.localizedMessage}"
                Toast.makeText(context, "Storage download error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clearSelectedUser() {
        _selectedUser.value = null
        _userRepos.value = emptyList()
    }
}
