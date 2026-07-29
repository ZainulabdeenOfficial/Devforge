package com.example.ui.screens.code

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DevForgeData
import com.example.data.model.RegexPreset
import com.example.ui.components.GlassCard
import com.example.ui.components.SyntaxCodeViewer
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeToolsScreen(
    viewModel: CodeToolsViewModel,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Code Editor, 1: JSON Tools, 2: Regex Playground, 3: Utilities

    Scaffold(
        modifier = modifier.testTag("code_tools_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Multi-Language Code Studio", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sub tab bar
            ScrollableTabRow(
                selectedTabIndex = subTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    text = { Text("Code Editor", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    text = { Text("JSON Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = subTab == 2,
                    onClick = { subTab = 2 },
                    text = { Text("Regex Playground", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = subTab == 3,
                    onClick = { subTab = 3 },
                    text = { Text("Dev Utilities", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (subTab) {
                    0 -> CodeEditorSection(viewModel)
                    1 -> JsonToolsSection(viewModel)
                    2 -> RegexPlaygroundSection(viewModel)
                    3 -> DevUtilitiesSection(viewModel)
                }
            }
        }
    }
}

@Composable
fun CodeEditorSection(viewModel: CodeToolsViewModel) {
    val selectedLang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val codeContent by viewModel.codeEditorContent.collectAsStateWithLifecycle()
    val findQuery by viewModel.codeFindQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.codeReplaceQuery.collectAsStateWithLifecycle()
    val output by viewModel.codeExecutionOutput.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showFindReplace by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val lineCount = remember(codeContent) { codeContent.lines().size.coerceAtLeast(1) }

    // Auto-suggestion keywords map per language
    val languageKeywordsMap = remember {
        mapOf(
            "Kotlin" to listOf("fun ", "val ", "var ", "class ", "println(", "override ", "interface ", "import ", "when (", "StateFlow<", "collectAsStateWithLifecycle()"),
            "Java" to listOf("public ", "private ", "static ", "void ", "class ", "System.out.println(", "return ", "interface ", "import ", "Override"),
            "C" to listOf("printf(", "#include <stdio.h>", "int ", "return ", "struct ", "void ", "char ", "for (int i = 0;"),
            "C#" to listOf("Console.WriteLine(", "using System;", "namespace ", "class ", "static ", "public ", "async ", "string "),
            "Assembly" to listOf("mov ", "push ", "pop ", "section .data", "global _start", "int 0x80", "ret", "call ", "eax, "),
            "Python" to listOf("def ", "return ", "print(", "import ", "for i in range(", "class ", "lambda ", "if __name__ == '__main__':"),
            "JavaScript" to listOf("const ", "let ", "console.log(", "function ", "async ", "await ", "export ", "import "),
            "TypeScript" to listOf("interface ", "type ", "const ", "console.log(", "export ", "async ", "string[]"),
            "Dart" to listOf("void ", "main()", "print(", "final ", "class ", "import ", "Future<", "async "),
            "PHP" to listOf("echo ", "function ", "public ", "return ", "foreach (", "<?php", "class "),
            "Ruby" to listOf("def ", "puts ", "end", "class ", "attr_accessor ", "require "),
            "C++" to listOf("std::cout << ", "#include <iostream>", "int main()", "std::vector<", "return 0;"),
            "Rust" to listOf("fn ", "let mut ", "println!(", "use ", "pub struct ", "impl ", "match "),
            "Go" to listOf("func ", "fmt.Println(", "package main", "import ", "type ", "struct {"),
            "SQL" to listOf("SELECT ", "FROM ", "WHERE ", "INSERT INTO ", "UPDATE ", "DELETE ", "GROUP BY ", "ORDER BY ")
        )
    }

    val currentSuggestions = remember(codeContent, selectedLang) {
        val keywords = languageKeywordsMap[selectedLang] ?: listOf("println(", "return ", "val ", "fun ")
        val lastWord = codeContent.takeLastWhile { it.isLetterOrDigit() || it == '_' || it == '.' || it == '#' }.lowercase()
        if (lastWord.isBlank()) {
            keywords.take(6)
        } else {
            val matched = keywords.filter { it.lowercase().contains(lastWord) }
            if (matched.isNotEmpty()) matched else keywords.take(6)
        }
    }

    // Function to apply suggestion
    val applySuggestion: (String) -> Unit = { suggestion ->
        val lastWord = codeContent.takeLastWhile { it.isLetterOrDigit() || it == '_' || it == '.' || it == '#' }
        val newCode = if (lastWord.isNotEmpty() && suggestion.lowercase().startsWith(lastWord.lowercase())) {
            codeContent.dropLast(lastWord.length) + suggestion
        } else {
            codeContent + " " + suggestion
        }
        viewModel.updateCodeEditorContent(newCode)
    }

    if (isFullscreen) {
        // Fullscreen Editor Overlay Dialog with Keyboard Handling & Landscape Support
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070D18))
                    .statusBarsPadding()
                    .imePadding()
                    .padding(12.dp),
                color = Color(0xFF070D18)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fullscreen Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = DevCyanPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text(selectedLang.uppercase(), color = DevCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Text("FULLSCREEN CODE STUDIO", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { viewModel.executeCodeSimulation() },
                                colors = ButtonDefaults.buttonColors(containerColor = DevEmeraldSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Text("RUN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(codeContent))
                                Toast.makeText(context, "$selectedLang code copied!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DevCyanPrimary)
                            }
                            OutlinedButton(
                                onClick = { isFullscreen = false },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                            ) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("EXIT", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Autocomplete Suggestions Row
                    Text("SUGGESTIONS & AUTOCOMPLETE", fontSize = 10.sp, color = DevCyanPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(currentSuggestions) { kw ->
                            SuggestionChip(
                                onClick = { applySuggestion(kw) },
                                label = { Text(kw, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DevCyanPrimary) },
                                border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = DevCyanPrimary.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // Fullscreen Editor Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 350.dp, max = 500.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF020610))
                            .border(1.dp, DevCyanPrimary, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Line Numbers Gutter
                            Column(
                                modifier = Modifier
                                    .width(36.dp)
                                    .fillMaxHeight()
                                    .padding(end = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                repeat(lineCount) { idx ->
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(),
                                color = Color.Gray.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Editable Code Input
                            OutlinedTextField(
                                value = codeContent,
                                onValueChange = { viewModel.updateCodeEditorContent(it) },
                                modifier = Modifier.fillMaxSize(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = DevCyanPrimary,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }

                    if (output != null) {
                        GlassCard(borderColor = DevEmeraldSecondary) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("OUTPUT CONSOLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary, fontFamily = FontFamily.Monospace)
                                Text(text = output!!, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Language Selector Chips
        Text("SELECT PROGRAMMING LANGUAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.availableLanguages) { lang ->
                FilterChip(
                    selected = selectedLang == lang,
                    onClick = { viewModel.selectLanguage(lang) },
                    label = { Text(lang, fontSize = 12.sp, fontWeight = if (selectedLang == lang) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevCyanPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { viewModel.executeCodeSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmeraldSecondary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { showFindReplace = !showFindReplace },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FIND", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { isFullscreen = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DevCyanPrimary)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = DevCyanPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FULLSCREEN", fontSize = 11.sp, color = DevCyanPrimary)
                }
            }

            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(codeContent))
                    Toast.makeText(context, "$selectedLang code copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevCyanPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = DevCyanPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("COPY CODE", fontSize = 11.sp, color = DevCyanPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Find & Replace Drawer
        if (showFindReplace) {
            GlassCard(borderColor = DevCyanPrimary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = findQuery,
                            onValueChange = { viewModel.updateCodeFindQuery(it) },
                            placeholder = { Text("Find string...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        )
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { viewModel.updateCodeReplaceQuery(it) },
                            placeholder = { Text("Replace with...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        )
                    }
                    Button(
                        onClick = { viewModel.performFindAndReplace() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary)
                    ) {
                        Text("REPLACE ALL OCCURRENCES", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Auto-suggestions Bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("CODE SUGGESTIONS / AUTOCOMPLETE", fontSize = 10.sp, color = DevCyanPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(currentSuggestions) { kw ->
                    SuggestionChip(
                        onClick = { applySuggestion(kw) },
                        label = { Text(kw, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = DevCyanPrimary) },
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = DevCyanPrimary.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Code Editor Box with Line Numbers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070D18))
                .border(1.dp, DevBorderDark, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .width(36.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    repeat(lineCount) { idx ->
                        Text(
                            text = "${idx + 1}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray.copy(alpha = 0.6f),
                            lineHeight = 16.sp
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(),
                    color = Color.Gray.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Editable Code Input
                OutlinedTextField(
                    value = codeContent,
                    onValueChange = { viewModel.updateCodeEditorContent(it) },
                    modifier = Modifier.fillMaxSize(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = DevCyanPrimary,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        // Execution Output Console
        if (output != null) {
            GlassCard(borderColor = DevEmeraldSecondary) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("EXECUTION CONSOLE OUTPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary, fontFamily = FontFamily.Monospace)
                    Text(
                        text = output!!,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun JsonToolsSection(viewModel: CodeToolsViewModel) {
    val jsonInput by viewModel.jsonInput.collectAsStateWithLifecycle()
    val jsonOutput by viewModel.jsonOutput.collectAsStateWithLifecycle()
    val jsonError by viewModel.jsonError.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("RAW JSON INPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.formatJson() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("BEAUTIFY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.minifyJson() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("MINIFY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        OutlinedTextField(
            value = jsonInput,
            onValueChange = { viewModel.updateJsonInput(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(12.dp)
        )

        if (jsonError != null) {
            GlassCard(borderColor = Color.Red.copy(alpha = 0.5f)) {
                Text(jsonError!!, color = Color.Red, fontSize = 12.sp)
            }
        } else {
            Text("FORMATTED JSON OUTPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary, fontFamily = FontFamily.Monospace)
            SyntaxCodeViewer(code = jsonOutput, language = "json")
        }
    }
}

@Composable
fun RegexPlaygroundSection(viewModel: CodeToolsViewModel) {
    val pattern by viewModel.regexPattern.collectAsStateWithLifecycle()
    val testInput by viewModel.regexTestInput.collectAsStateWithLifecycle()
    val replacePattern by viewModel.regexReplacePattern.collectAsStateWithLifecycle()
    val matches by viewModel.regexMatchResults.collectAsStateWithLifecycle()
    val replaceResult by viewModel.regexReplaceResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("COMMON REGEX PRESETS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary, fontFamily = FontFamily.Monospace)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DevForgeData.regexPresets) { preset ->
                SuggestionChip(
                    onClick = { viewModel.loadRegexPreset(preset) },
                    label = { Text(preset.title, fontSize = 12.sp) }
                )
            }
        }

        OutlinedTextField(
            value = pattern,
            onValueChange = { viewModel.updateRegexPattern(it) },
            label = { Text("Regex Pattern") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        )

        OutlinedTextField(
            value = testInput,
            onValueChange = { viewModel.updateRegexTestInput(it) },
            label = { Text("Test Input Text") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
        )

        OutlinedTextField(
            value = replacePattern,
            onValueChange = { viewModel.updateRegexReplacePattern(it) },
            label = { Text("Replace Template") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MATCHES FOUND (${matches.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)
                matches.forEachIndexed { idx, match ->
                    Text("#${idx + 1}: $match", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = DevCyanPrimary)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.2f))

                Text("REPLACE PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevPurpleTertiary)
                Text(replaceResult, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun DevUtilitiesSection(viewModel: CodeToolsViewModel) {
    var selectedUtil by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = selectedUtil, edgePadding = 0.dp, containerColor = Color.Transparent, divider = {}) {
            Tab(selected = selectedUtil == 0, onClick = { selectedUtil = 0 }, text = { Text("Base64", fontSize = 12.sp) })
            Tab(selected = selectedUtil == 1, onClick = { selectedUtil = 1 }, text = { Text("JWT Decoder", fontSize = 12.sp) })
            Tab(selected = selectedUtil == 2, onClick = { selectedUtil = 2 }, text = { Text("Hash MD5/SHA", fontSize = 12.sp) })
            Tab(selected = selectedUtil == 3, onClick = { selectedUtil = 3 }, text = { Text("UUID & Epoch", fontSize = 12.sp) })
            Tab(selected = selectedUtil == 4, onClick = { selectedUtil = 4 }, text = { Text("Password & Color", fontSize = 12.sp) })
        }

        when (selectedUtil) {
            0 -> Base64Tool(viewModel)
            1 -> JwtDecoderTool(viewModel)
            2 -> HashGeneratorTool(viewModel)
            3 -> UuidEpochTool(viewModel)
            4 -> PasswordColorTool(viewModel)
        }
    }
}

@Composable
fun Base64Tool(viewModel: CodeToolsViewModel) {
    var text by remember { mutableStateOf("DevForge Mobile Engineering") }
    var encoded by remember { mutableStateOf("") }
    var decoded by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        encoded = viewModel.encodeBase64(text)
        decoded = viewModel.decodeBase64(encoded)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text Input") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("ENCODED BASE64", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary)
        SyntaxCodeViewer(code = encoded, language = "text")

        Text("DECODED VERIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)
        SyntaxCodeViewer(code = decoded, language = "text")
    }
}

@Composable
fun JwtDecoderTool(viewModel: CodeToolsViewModel) {
    var jwtInput by remember { mutableStateOf("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlIjoiZGV2ZWxvcGVyIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c") }
    var header by remember { mutableStateOf("") }
    var payload by remember { mutableStateOf("") }

    LaunchedEffect(jwtInput) {
        val res = viewModel.decodeJwt(jwtInput)
        header = res.first
        payload = res.second
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = jwtInput,
            onValueChange = { jwtInput = it },
            label = { Text("JWT Token String") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        )
        Text("JWT HEADER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary)
        SyntaxCodeViewer(code = header, language = "json")

        Text("JWT PAYLOAD CLAIMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)
        SyntaxCodeViewer(code = payload, language = "json")
    }
}

@Composable
fun HashGeneratorTool(viewModel: CodeToolsViewModel) {
    var input by remember { mutableStateOf("DevForge2026") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Text to Hash") },
            modifier = Modifier.fillMaxWidth()
        )

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("MD5: ${viewModel.generateHash(input, "MD5")}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = DevCyanPrimary)
                Text("SHA-1: ${viewModel.generateHash(input, "SHA-1")}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = DevEmeraldSecondary)
                Text("SHA-256: ${viewModel.generateHash(input, "SHA-256")}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = DevPurpleTertiary)
            }
        }
    }
}

@Composable
fun UuidEpochTool(viewModel: CodeToolsViewModel) {
    var uuid by remember { mutableStateOf(viewModel.generateUuid()) }
    var epochSec by remember { mutableStateOf((System.currentTimeMillis() / 1000).toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UUID v4 Generator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(onClick = { uuid = viewModel.generateUuid() }) { Text("GENERATE") }
                }
                Text(uuid, fontFamily = FontFamily.Monospace, color = DevCyanPrimary, fontSize = 13.sp)
            }
        }

        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Unix Timestamp / Epoch Converter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = epochSec,
                    onValueChange = { epochSec = it },
                    label = { Text("Epoch Seconds") },
                    modifier = Modifier.fillMaxWidth()
                )
                val formattedDate = viewModel.convertEpochToDate(epochSec.toLongOrNull() ?: 0L)
                Text("Formatted Date: $formattedDate", color = DevEmeraldSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun PasswordColorTool(viewModel: CodeToolsViewModel) {
    var password by remember { mutableStateOf(viewModel.generatePassword()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Password Generator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(onClick = { password = viewModel.generatePassword() }) { Text("GENERATE") }
                }
                Text(password, fontFamily = FontFamily.Monospace, color = DevCyanPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
