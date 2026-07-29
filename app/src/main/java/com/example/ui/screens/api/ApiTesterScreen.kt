package com.example.ui.screens.api

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.GlassCard
import com.example.ui.components.MethodBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyntaxCodeViewer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTesterScreen(
    viewModel: ApiViewModel,
    modifier: Modifier = Modifier
) {
    val request by viewModel.request.collectAsStateWithLifecycle()
    val response by viewModel.response.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Params, 1: Headers, 2: Auth, 3: Body, 4: Response
    var showCurlDialog by remember { mutableStateOf(false) }
    var curlInputText by remember { mutableStateOf("") }
    var showMethodMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("api_tester_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "API Tester",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showCurlDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Import cURL",
                            tint = DevCyanPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.loadTemplate("Postman Echo POST JSON") }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Load Demo Template",
                            tint = DevEmeraldSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // URL Bar & Method Selector Row
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Method Selector Dropdown Button
                        Box {
                            OutlinedButton(
                                onClick = { showMethodMenu = true },
                                modifier = Modifier.height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                MethodBadge(method = request.method)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = DevCyanPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showMethodMenu,
                                onDismissRequest = { showMethodMenu = false }
                            ) {
                                HttpMethod.values().forEach { method ->
                                    DropdownMenuItem(
                                        text = { MethodBadge(method = method) },
                                        onClick = {
                                            viewModel.updateMethod(method)
                                            showMethodMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // URL Input
                        OutlinedTextField(
                            value = request.url,
                            onValueChange = { viewModel.updateUrl(it) },
                            placeholder = { Text("https://api.example.com/v1/resource", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    // Send Request Button
                    Button(
                        onClick = { viewModel.sendRequest() },
                        enabled = !isLoading && request.url.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXECUTING...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SEND REQUEST", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs Header
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Params (${request.queryParams.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Headers (${request.headers.size})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Auth (${request.authType.name})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Body (${request.bodyType.name})", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = {
                        Text(
                            text = if (response != null) "Response (${response?.statusCode})" else "Response",
                            fontSize = 13.sp,
                            color = if (response != null) DevEmeraldSecondary else LocalContentColor.current
                        )
                    }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> QueryParamsTab(request, viewModel)
                    1 -> HeadersTab(request, viewModel)
                    2 -> AuthTab(request, viewModel)
                    3 -> BodyTab(request, viewModel)
                    4 -> ResponseTab(response, isLoading)
                }
            }
        }

        // cURL Import Dialog
        if (showCurlDialog) {
            AlertDialog(
                onDismissRequest = { showCurlDialog = false },
                title = { Text("Import cURL Command") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Paste a standard cURL command to populate method, URL, headers, and body:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = curlInputText,
                            onValueChange = { curlInputText = it },
                            placeholder = { Text("curl -X POST https://api.example.com -H \"Content-Type: application/json\" -d '{\"key\":\"val\"}'") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.importCurl(curlInputText)
                            showCurlDialog = false
                            curlInputText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary)
                    ) {
                        Text("IMPORT", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCurlDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun QueryParamsTab(request: ApiRequestModel, viewModel: ApiViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("QUERY PARAMETERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
            TextButton(onClick = { viewModel.addQueryParam() }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Param")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(request.queryParams) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.enabled,
                        onCheckedChange = { viewModel.updateQueryParam(index, item.key, item.value, it) }
                    )
                    OutlinedTextField(
                        value = item.key,
                        onValueChange = { viewModel.updateQueryParam(index, it, item.value, item.enabled) },
                        placeholder = { Text("Key") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    )
                    OutlinedTextField(
                        value = item.value,
                        onValueChange = { viewModel.updateQueryParam(index, item.key, it, item.enabled) },
                        placeholder = { Text("Value") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    )
                    IconButton(onClick = { viewModel.removeQueryParam(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun HeadersTab(request: ApiRequestModel, viewModel: ApiViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("HTTP HEADERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
            TextButton(onClick = { viewModel.addHeader() }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Header")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(request.headers) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.enabled,
                        onCheckedChange = { viewModel.updateHeader(index, item.key, item.value, it) }
                    )
                    OutlinedTextField(
                        value = item.key,
                        onValueChange = { viewModel.updateHeader(index, it, item.value, item.enabled) },
                        placeholder = { Text("Header Key") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    )
                    OutlinedTextField(
                        value = item.value,
                        onValueChange = { viewModel.updateHeader(index, item.key, it, item.enabled) },
                        placeholder = { Text("Header Value") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    )
                    IconButton(onClick = { viewModel.removeHeader(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthTab(request: ApiRequestModel, viewModel: ApiViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AUTHENTICATION TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthType.values().forEach { type ->
                FilterChip(
                    selected = request.authType == type,
                    onClick = { viewModel.updateAuthType(type) },
                    label = { Text(type.name, fontSize = 12.sp) }
                )
            }
        }

        when (request.authType) {
            AuthType.BEARER -> {
                OutlinedTextField(
                    value = request.authToken,
                    onValueChange = { viewModel.updateAuthToken(it) },
                    label = { Text("Bearer Token") },
                    placeholder = { Text("eyJhbGciOiJIUzI1Ni...") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                )
            }
            AuthType.BASIC -> {
                OutlinedTextField(
                    value = request.authUsername,
                    onValueChange = { viewModel.updateAuthCredentials(it, request.authPassword) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = request.authPassword,
                    onValueChange = { viewModel.updateAuthCredentials(request.authUsername, it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AuthType.API_KEY -> {
                OutlinedTextField(
                    value = request.apiKeyName,
                    onValueChange = { viewModel.updateApiKey(it, request.apiKeyValue) },
                    label = { Text("API Key Header / Query Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = request.apiKeyValue,
                    onValueChange = { viewModel.updateApiKey(request.apiKeyName, it) },
                    label = { Text("API Key Value") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                Text("No authentication header will be attached.", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun BodyTab(request: ApiRequestModel, viewModel: ApiViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BodyType.values().forEach { type ->
                FilterChip(
                    selected = request.bodyType == type,
                    onClick = { viewModel.updateBodyType(type) },
                    label = { Text(type.name, fontSize = 12.sp) }
                )
            }
        }

        if (request.bodyType != BodyType.NONE) {
            OutlinedTextField(
                value = request.bodyContent,
                onValueChange = { viewModel.updateBodyContent(it) },
                placeholder = { Text("Enter payload body content...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("This request does not include a body payload.", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ResponseTab(response: ApiResponseModel?, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DevCyanPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Executing HTTP request...", color = DevCyanPrimary, fontSize = 13.sp)
            }
        }
    } else if (response == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Send a request to view HTTP status, headers, and body response.", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(statusCode = response.statusCode, statusText = response.statusText)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${response.durationMs} ms", color = DevCyanPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text("${response.sizeBytes} B", color = DevEmeraldSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            if (response.errorMessage != null) {
                GlassCard(borderColor = Color.Red.copy(alpha = 0.5f)) {
                    Text("Error: ${response.errorMessage}", color = Color.Red, fontSize = 13.sp)
                }
            } else {
                Text("RESPONSE BODY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
                SyntaxCodeViewer(
                    code = response.body,
                    language = "json"
                )
            }
        }
    }
}
