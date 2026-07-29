package com.example.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.SavedItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.savedItemDao()

    val savedItems: StateFlow<List<SavedItemEntity>> = dao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePin(item: SavedItemEntity) {
        viewModelScope.launch {
            dao.updatePinnedStatus(item.id, !item.isPinned)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            dao.deleteItemById(id)
        }
    }

    fun addSampleQuickData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val samples = listOf(
                SavedItemEntity(
                    category = "API",
                    title = "GET Users List",
                    subtitle = "https://jsonplaceholder.typicode.com/users",
                    contentData = """{"method":"GET","url":"https://jsonplaceholder.typicode.com/users"}""",
                    isPinned = true
                ),
                SavedItemEntity(
                    category = "API",
                    title = "POST Create User",
                    subtitle = "https://reqres.in/api/users",
                    contentData = """{"method":"POST","url":"https://reqres.in/api/users","body":{"name":"DevForge User","job":"Android Developer"}}""",
                    isPinned = false
                ),
                SavedItemEntity(
                    category = "REGEX",
                    title = "Email Validation Pattern",
                    subtitle = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                    contentData = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                    isPinned = true
                ),
                SavedItemEntity(
                    category = "REGEX",
                    title = "URL Parser Regex",
                    subtitle = "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b",
                    contentData = "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b",
                    isPinned = false
                ),
                SavedItemEntity(
                    category = "JSON",
                    title = "App Config Blueprint",
                    subtitle = "DevForge Application Manifest Schema",
                    contentData = """{"appName":"DevForge","version":"1.0.0","environment":"production","activeModules":["API","JSON","Regex","GitHub"]}""",
                    isPinned = false
                ),
                SavedItemEntity(
                    category = "SQL",
                    title = "User & Auth Table Schema",
                    subtitle = "CREATE TABLE users (id INTEGER PRIMARY KEY...)",
                    contentData = "CREATE TABLE IF NOT EXISTS users (\n    id INTEGER PRIMARY KEY AUTOINCREMENT,\n    username TEXT NOT NULL UNIQUE,\n    email TEXT NOT NULL,\n    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n);",
                    isPinned = false
                ),
                SavedItemEntity(
                    category = "BOOKMARK",
                    title = "Jetpack Compose Samples",
                    subtitle = "github.com/android/compose-samples",
                    contentData = "https://github.com/android/compose-samples",
                    isPinned = true
                )
            )
            samples.forEach { dao.insertItem(it) }
            onComplete?.invoke()
        }
    }
}
