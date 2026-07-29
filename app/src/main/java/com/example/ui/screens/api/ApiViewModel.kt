package com.example.ui.screens.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApiRepository()
    private val dao = AppDatabase.getDatabase(application).savedItemDao()

    private val _request = MutableStateFlow(ApiRequestModel())
    val request: StateFlow<ApiRequestModel> = _request.asStateFlow()

    private val _response = MutableStateFlow<ApiResponseModel?>(null)
    val response: StateFlow<ApiResponseModel?> = _response.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateMethod(method: HttpMethod) {
        _request.value = _request.value.copy(method = method)
    }

    fun updateUrl(url: String) {
        _request.value = _request.value.copy(url = url)
    }

    fun updateAuthType(authType: AuthType) {
        _request.value = _request.value.copy(authType = authType)
    }

    fun updateAuthToken(token: String) {
        _request.value = _request.value.copy(authToken = token)
    }

    fun updateAuthCredentials(username: String, password: String) {
        _request.value = _request.value.copy(authUsername = username, authPassword = password)
    }

    fun updateApiKey(keyName: String, keyValue: String) {
        _request.value = _request.value.copy(apiKeyName = keyName, apiKeyValue = keyValue)
    }

    fun updateBodyType(bodyType: BodyType) {
        _request.value = _request.value.copy(bodyType = bodyType)
    }

    fun updateBodyContent(content: String) {
        _request.value = _request.value.copy(bodyContent = content)
    }

    fun addHeader(key: String = "", value: String = "") {
        val current = _request.value.headers.toMutableList()
        current.add(KeyValue(key, value))
        _request.value = _request.value.copy(headers = current)
    }

    fun updateHeader(index: Int, key: String, value: String, enabled: Boolean) {
        val current = _request.value.headers.toMutableList()
        if (index in current.indices) {
            current[index] = KeyValue(key, value, enabled)
            _request.value = _request.value.copy(headers = current)
        }
    }

    fun removeHeader(index: Int) {
        val current = _request.value.headers.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _request.value = _request.value.copy(headers = current)
        }
    }

    fun addQueryParam(key: String = "", value: String = "") {
        val current = _request.value.queryParams.toMutableList()
        current.add(KeyValue(key, value))
        _request.value = _request.value.copy(queryParams = current)
    }

    fun updateQueryParam(index: Int, key: String, value: String, enabled: Boolean) {
        val current = _request.value.queryParams.toMutableList()
        if (index in current.indices) {
            current[index] = KeyValue(key, value, enabled)
            _request.value = _request.value.copy(queryParams = current)
        }
    }

    fun removeQueryParam(index: Int) {
        val current = _request.value.queryParams.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _request.value = _request.value.copy(queryParams = current)
        }
    }

    fun sendRequest() {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.executeRequest(_request.value)
            _response.value = res
            _isLoading.value = false

            // Save to history in Room
            try {
                dao.insertItem(
                    SavedItemEntity(
                        category = "API",
                        title = "${_request.value.method} ${_request.value.name}",
                        subtitle = _request.value.url,
                        contentData = "${_request.value.method} | ${_request.value.url} | Status: ${res.statusCode}"
                    )
                )
            } catch (e: Exception) {
                // Ignore save errors
            }
        }
    }

    fun loadTemplate(templateName: String) {
        when (templateName) {
            "JSONPlaceholder TODOs" -> {
                _request.value = ApiRequestModel(
                    name = "JSONPlaceholder TODOs",
                    method = HttpMethod.GET,
                    url = "https://jsonplaceholder.typicode.com/todos/1"
                )
            }
            "Postman Echo GET" -> {
                _request.value = ApiRequestModel(
                    name = "Postman Echo GET",
                    method = HttpMethod.GET,
                    url = "https://postman-echo.com/get?test=devforge"
                )
            }
            "Postman Echo POST JSON" -> {
                _request.value = ApiRequestModel(
                    name = "Postman Echo POST JSON",
                    method = HttpMethod.POST,
                    url = "https://postman-echo.com/post",
                    bodyType = BodyType.RAW_JSON,
                    bodyContent = """{"app": "DevForge", "platform": "Android", "status": "active"}"""
                )
            }
            "GitHub Public User API" -> {
                _request.value = ApiRequestModel(
                    name = "GitHub User Profile",
                    method = HttpMethod.GET,
                    url = "https://api.github.com/users/octocat"
                )
            }
        }
    }

    fun importCurl(curlCommand: String) {
        try {
            var method = HttpMethod.GET
            var url = ""
            val headers = mutableListOf<KeyValue>()
            var bodyContent = ""
            var bodyType = BodyType.NONE

            val tokens = curlCommand.split(" ")
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i].trim()
                when {
                    token == "-X" || token == "--request" -> {
                        if (i + 1 < tokens.size) {
                            val m = tokens[i + 1].replace("'", "").replace("\"", "").uppercase()
                            method = try { HttpMethod.valueOf(m) } catch (e: Exception) { HttpMethod.GET }
                            i++
                        }
                    }
                    token == "-H" || token == "--header" -> {
                        if (i + 1 < tokens.size) {
                            var headerStr = tokens[i + 1]
                            // Combine quoted tokens if spaced
                            if (headerStr.startsWith("'") || headerStr.startsWith("\"")) {
                                var j = i + 1
                                val sb = StringBuilder()
                                while (j < tokens.size) {
                                    sb.append(tokens[j]).append(" ")
                                    if (tokens[j].endsWith("'") || tokens[j].endsWith("\"")) break
                                    j++
                                }
                                headerStr = sb.toString().trim().replace("'", "").replace("\"", "")
                                i = j
                            } else {
                                headerStr = headerStr.replace("'", "").replace("\"", "")
                            }
                            if (headerStr.contains(":")) {
                                val parts = headerStr.split(":", limit = 2)
                                headers.add(KeyValue(parts[0].trim(), parts[1].trim()))
                            }
                        }
                    }
                    token == "-d" || token == "--data" || token == "--data-raw" -> {
                        if (i + 1 < tokens.size) {
                            var dataStr = tokens[i + 1]
                            var j = i + 1
                            val sb = StringBuilder()
                            while (j < tokens.size) {
                                sb.append(tokens[j]).append(" ")
                                j++
                            }
                            dataStr = sb.toString().trim()
                            if ((dataStr.startsWith("'") && dataStr.endsWith("'")) || (dataStr.startsWith("\"") && dataStr.endsWith("\""))) {
                                dataStr = dataStr.substring(1, dataStr.length - 1)
                            }
                            bodyContent = dataStr
                            bodyType = BodyType.RAW_JSON
                            if (method == HttpMethod.GET) method = HttpMethod.POST
                            break
                        }
                    }
                    token.startsWith("http://") || token.startsWith("https://") || token.startsWith("'http") || token.startsWith("\"http") -> {
                        url = token.replace("'", "").replace("\"", "")
                    }
                }
                i++
            }

            if (url.isNotBlank()) {
                _request.value = _request.value.copy(
                    method = method,
                    url = url,
                    headers = if (headers.isNotEmpty()) headers else listOf(KeyValue("Accept", "application/json")),
                    bodyType = bodyType,
                    bodyContent = bodyContent
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
    }
}
