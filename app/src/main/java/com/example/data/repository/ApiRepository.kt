package com.example.data.repository

import com.example.data.model.ApiRequestModel
import com.example.data.model.ApiResponseModel
import com.example.data.model.AuthType
import com.example.data.model.BodyType
import com.example.data.model.KeyValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun executeRequest(apiRequest: ApiRequestModel): ApiResponseModel = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            // Build URL with query params
            val urlBuilder = java.lang.StringBuilder(apiRequest.url.trim())
            val enabledParams = apiRequest.queryParams.filter { it.enabled && it.key.isNotBlank() }
            if (enabledParams.isNotEmpty()) {
                if (!apiRequest.url.contains("?")) {
                    urlBuilder.append("?")
                } else if (!apiRequest.url.endsWith("?") && !apiRequest.url.endsWith("&")) {
                    urlBuilder.append("&")
                }
                urlBuilder.append(enabledParams.joinToString("&") { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" })
            }

            val finalUrl = urlBuilder.toString()
            val requestBuilder = Request.Builder().url(finalUrl)

            // Add Headers
            val headerMap = mutableMapOf<String, String>()
            apiRequest.headers.filter { it.enabled && it.key.isNotBlank() }.forEach {
                headerMap[it.key] = it.value
            }

            // Auth headers
            when (apiRequest.authType) {
                AuthType.BEARER -> {
                    if (apiRequest.authToken.isNotBlank()) {
                        headerMap["Authorization"] = "Bearer ${apiRequest.authToken}"
                    }
                }
                AuthType.BASIC -> {
                    val credentials = "${apiRequest.authUsername}:${apiRequest.authPassword}"
                    val basic = "Basic " + android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)
                    headerMap["Authorization"] = basic
                }
                AuthType.API_KEY -> {
                    if (apiRequest.apiKeyName.isNotBlank() && apiRequest.apiKeyValue.isNotBlank()) {
                        headerMap[apiRequest.apiKeyName] = apiRequest.apiKeyValue
                    }
                }
                else -> {}
            }

            headerMap.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            // Body
            val requestBody = when (apiRequest.bodyType) {
                BodyType.RAW_JSON -> {
                    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                    apiRequest.bodyContent.toRequestBody(mediaType)
                }
                BodyType.XML -> {
                    val mediaType = "application/xml; charset=utf-8".toMediaTypeOrNull()
                    apiRequest.bodyContent.toRequestBody(mediaType)
                }
                BodyType.PLAIN_TEXT -> {
                    val mediaType = "text/plain; charset=utf-8".toMediaTypeOrNull()
                    apiRequest.bodyContent.toRequestBody(mediaType)
                }
                BodyType.FORM_DATA -> {
                    val formBuilder = okhttp3.FormBody.Builder()
                    // Parse form-data lines key=value
                    apiRequest.bodyContent.lines().forEach { line ->
                        if (line.contains("=")) {
                            val parts = line.split("=", limit = 2)
                            formBuilder.add(parts[0].trim(), parts[1].trim())
                        }
                    }
                    formBuilder.build()
                }
                BodyType.NONE -> null
            }

            // HTTP Method
            when (apiRequest.method) {
                com.example.data.model.HttpMethod.GET -> requestBuilder.get()
                com.example.data.model.HttpMethod.POST -> requestBuilder.post(requestBody ?: "".toRequestBody())
                com.example.data.model.HttpMethod.PUT -> requestBuilder.put(requestBody ?: "".toRequestBody())
                com.example.data.model.HttpMethod.PATCH -> requestBuilder.patch(requestBody ?: "".toRequestBody())
                com.example.data.model.HttpMethod.DELETE -> if (requestBody != null) requestBuilder.delete(requestBody) else requestBuilder.delete()
                com.example.data.model.HttpMethod.HEAD -> requestBuilder.head()
                com.example.data.model.HttpMethod.OPTIONS -> requestBuilder.method("OPTIONS", requestBody)
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            val responseBody = response.body?.string() ?: ""
            val size = responseBody.toByteArray().size.toLong()

            val responseHeaders = response.headers.map { KeyValue(it.first, it.second) }

            ApiResponseModel(
                statusCode = response.code,
                statusText = response.message,
                durationMs = duration,
                sizeBytes = size,
                headers = responseHeaders,
                body = responseBody,
                isSuccess = response.isSuccessful
            )
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            ApiResponseModel(
                statusCode = 0,
                statusText = "Error",
                durationMs = endTime - startTime,
                sizeBytes = 0,
                headers = emptyList(),
                body = "",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Network error occurred"
            )
        }
    }
}
