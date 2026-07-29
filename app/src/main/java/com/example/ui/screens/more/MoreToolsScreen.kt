package com.example.ui.screens.more

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.DevForgeData
import com.example.data.model.HttpStatusCodeModel
import com.example.data.model.DevTutorial
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyntaxCodeViewer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreToolsScreen(
    viewModel: MoreViewModel,
    onReopenOnboarding: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: HTTP Codes, 1: AI Assistant, 2: Learning Hub, 3: Settings

    Scaffold(
        modifier = modifier.testTag("more_tools_screen"),
        topBar = {
            TopAppBar(
                title = { Text("More Developer Tools", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            ScrollableTabRow(selectedTabIndex = subTab, edgePadding = 0.dp, containerColor = Color.Transparent, divider = {}) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("HTTP Explorer", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("AI Assistant", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Learning Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = subTab == 3, onClick = { subTab = 3 }, text = { Text("Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold) })
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (subTab) {
                    0 -> HttpExplorerSection(viewModel)
                    1 -> AiAssistantSection(viewModel)
                    2 -> LearningHubSection()
                    3 -> SettingsSection(viewModel, onReopenOnboarding)
                }
            }
        }
    }
}

@Composable
fun HttpExplorerSection(viewModel: MoreViewModel) {
    val query by viewModel.httpQuery.collectAsStateWithLifecycle()
    val category by viewModel.httpCategory.collectAsStateWithLifecycle()
    val quizQuestion by viewModel.quizCurrentQuestion.collectAsStateWithLifecycle()
    val quizOptions by viewModel.quizOptions.collectAsStateWithLifecycle()
    val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val quizResult by viewModel.quizAnswerResult.collectAsStateWithLifecycle()

    var showQuizDialog by remember { mutableStateOf(false) }
    var selectedCodeDetail by remember { mutableStateOf<HttpStatusCodeModel?>(null) }

    val filteredCodes = DevForgeData.httpStatusCodes.filter { item ->
        (category == "ALL" || item.category.startsWith(category)) &&
                (query.isBlank() || item.code.toString().contains(query) || item.name.contains(query, ignoreCase = true))
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateHttpQuery(it) },
                placeholder = { Text("Search 200, 404, Unauthorized...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { showQuizDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DevEmeraldSecondary)
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("QUIZ", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "1xx", "2xx", "3xx", "4xx", "5xx").forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { viewModel.updateHttpCategory(cat) },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(filteredCodes) { codeItem ->
                GlassCard(onClick = { selectedCodeDetail = codeItem }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge(statusCode = codeItem.code, statusText = codeItem.name)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(codeItem.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }

        // Detail Dialog
        if (selectedCodeDetail != null) {
            val item = selectedCodeDetail!!
            AlertDialog(
                onDismissRequest = { selectedCodeDetail = null },
                title = { StatusBadge(statusCode = item.code, statusText = item.name) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary)
                        Text(item.description, fontSize = 13.sp)

                        Text("COMMON CAUSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)
                        Text(item.commonCauses, fontSize = 12.sp)

                        Text("DEVELOPER TIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevPurpleTertiary)
                        Text(item.devTip, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedCodeDetail = null }) { Text("Close") }
                }
            )
        }

        // Quiz Dialog
        if (showQuizDialog && quizQuestion != null) {
            AlertDialog(
                onDismissRequest = { showQuizDialog = false },
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("HTTP Status Quiz", fontWeight = FontWeight.Bold)
                        Text("Score: $quizScore", color = DevCyanPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "What does HTTP status code ${quizQuestion!!.code} stand for?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        quizOptions.forEach { option ->
                            Button(
                                onClick = { viewModel.submitQuizAnswer(option) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(option, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (quizResult != null) {
                            Text(
                                text = if (quizResult == true) "Correct! +10 Points" else "Incorrect! Correct: ${quizQuestion!!.name}",
                                color = if (quizResult == true) DevEmeraldSecondary else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.nextQuizQuestion() }) {
                        Text("NEXT QUESTION")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuizDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun AiAssistantSection(viewModel: MoreViewModel) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedAiRole.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    val samplePrompts = listOf(
        "Explain HTTP 401 vs 403 error",
        "Generate Regex for US Phone number",
        "Kotlin Coroutine Retry Function",
        "Jetpack Compose StateFlow example",
        "Room Database Entity pattern",
        "Explain JWT Token Claims"
    )

    val rolesWithModels = listOf(
        Triple("General Assistant", "General Tasks", "General Tasks"),
        Triple("Senior Architect", "Complex Architecture", "Complex Tasks"),
        Triple("Fast Explainer", "Quick Explanations", "Fast Tasks")
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // AI Header Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = DevCyanPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text("DevForge AI Assistant", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DevCyanPrimary)
                    Text("Interactive Multi-turn Mode", fontSize = 10.sp, color = DevEmeraldSecondary, fontFamily = FontFamily.Monospace)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DevEmeraldSecondary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, DevEmeraldSecondary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "FREE AI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevEmeraldSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.clearAiChat() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = "Clear Chat", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Chatbot Role Selector Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rolesWithModels) { (roleName, roleDesc, taskLabel) ->
                FilterChip(
                    selected = selectedRole == roleName,
                    onClick = { viewModel.selectAiRole(roleName) },
                    label = {
                        Column {
                            Text(roleName, fontSize = 11.sp, fontWeight = if (selectedRole == roleName) FontWeight.Bold else FontWeight.Normal)
                            Text(taskLabel, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevCyanPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = DevCyanPrimary
                    )
                )
            }
        }

        // Chat Message Stream
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isUser) DevCyanPrimary.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isUser) DevCyanPrimary else DevBorderDark,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUser) "DEVELOPER" else "AI ASSISTANT ($selectedRole)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) DevCyanPrimary else DevEmeraldSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = msg.message,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (!msg.codeSnippet.isNullOrBlank()) {
                                SyntaxCodeViewer(code = msg.codeSnippet, language = "kotlin")
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DevCyanPrimary, strokeWidth = 2.dp)
                Text("DevForge AI is generating response...", fontSize = 12.sp, color = DevCyanPrimary)
            }
        }

        // Prompt Suggestions Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(samplePrompts) { prompt ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                        viewModel.sendAiMessage(prompt)
                        inputText = ""
                    },
                    label = { Text(prompt, fontSize = 11.sp) },
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = DevBorderDark
                    )
                )
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask DevForge AI anything...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendAiMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DevCyanPrimary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
            }
        }
    }
}

@Composable
fun LearningHubSection() {
    var selectedTutorial by remember { mutableStateOf<DevTutorial?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        items(DevForgeData.devTutorials) { tutorial ->
            GlassCard(onClick = { selectedTutorial = tutorial }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DevCyanPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tutorial.category,
                                color = DevCyanPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(tutorial.readTime, fontSize = 11.sp, color = Color.Gray)
                    }

                    Text(tutorial.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(tutorial.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (selectedTutorial != null) {
        val t = selectedTutorial!!
        AlertDialog(
            onDismissRequest = { selectedTutorial = null },
            title = { Text(t.title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = t.fullContent,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedTutorial = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SettingsSection(
    viewModel: MoreViewModel,
    onReopenOnboarding: (() -> Unit)? = null
) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isBio by viewModel.isBiometricsEnabled.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("APP PREFERENCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Developer Dark Theme", fontSize = 14.sp)
                    Switch(checked = isDark, onCheckedChange = { viewModel.toggleDarkTheme(it) })
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biometric Security Lock", fontSize = 14.sp)
                    Switch(checked = isBio, onCheckedChange = { viewModel.toggleBiometrics(it) })
                }

                if (onReopenOnboarding != null) {
                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    OutlinedButton(
                        onClick = { onReopenOnboarding() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = DevCyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REOPEN ONBOARDING TOUR", color = DevCyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DATA MANAGEMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)

                OutlinedButton(
                    onClick = { viewModel.clearAppData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CLEAR ALL SAVED COLLECTIONS")
                }
            }
        }
    }
}
