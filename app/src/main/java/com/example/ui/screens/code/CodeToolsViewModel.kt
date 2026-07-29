package com.example.ui.screens.code

import androidx.lifecycle.ViewModel
import com.example.data.model.DevForgeData
import com.example.data.model.RegexPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class CodeToolsViewModel : ViewModel() {

    // --- JSON Tools State ---
    private val _jsonInput = MutableStateFlow("""{"name":"DevForge","version":1.0,"features":["API","JSON","Regex","GitHub"],"active":true}""")
    val jsonInput: StateFlow<String> = _jsonInput.asStateFlow()

    private val _jsonOutput = MutableStateFlow("")
    val jsonOutput: StateFlow<String> = _jsonOutput.asStateFlow()

    private val _jsonError = MutableStateFlow<String?>(null)
    val jsonError: StateFlow<String?> = _jsonError.asStateFlow()

    // --- Regex Playground State ---
    private val _regexPattern = MutableStateFlow("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
    val regexPattern: StateFlow<String> = _regexPattern.asStateFlow()

    private val _regexTestInput = MutableStateFlow("Test user contact: developer@devforge.io and admin@devforge.org or invalid-email@")
    val regexTestInput: StateFlow<String> = _regexTestInput.asStateFlow()

    private val _regexReplacePattern = MutableStateFlow("[REDACTED_EMAIL]")
    val regexReplacePattern: StateFlow<String> = _regexReplacePattern.asStateFlow()

    private val _regexFlagIgnoreCase = MutableStateFlow(true)
    val regexFlagIgnoreCase: StateFlow<Boolean> = _regexFlagIgnoreCase.asStateFlow()

    private val _regexFlagMultiline = MutableStateFlow(false)
    val regexFlagMultiline: StateFlow<Boolean> = _regexFlagMultiline.asStateFlow()

    private val _regexMatchResults = MutableStateFlow<List<String>>(emptyList())
    val regexMatchResults: StateFlow<List<String>> = _regexMatchResults.asStateFlow()

    private val _regexReplaceResult = MutableStateFlow("")
    val regexReplaceResult: StateFlow<String> = _regexReplaceResult.asStateFlow()

    // --- Code Editor State ---
    val availableLanguages = listOf(
        "Kotlin", "Java", "Python", "JavaScript", "TypeScript",
        "C", "C#", "Assembly", "C++", "Rust", "Go", "Swift",
        "Dart", "PHP", "Ruby", "HTML", "CSS", "SQL", "JSON", "YAML", "Shell"
    )

    private val _selectedLanguage = MutableStateFlow("Kotlin")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _codeEditorContent = MutableStateFlow(getTemplateForLanguage("Kotlin"))
    val codeEditorContent: StateFlow<String> = _codeEditorContent.asStateFlow()

    private val _codeFindQuery = MutableStateFlow("")
    val codeFindQuery: StateFlow<String> = _codeFindQuery.asStateFlow()

    private val _codeReplaceQuery = MutableStateFlow("")
    val codeReplaceQuery: StateFlow<String> = _codeReplaceQuery.asStateFlow()

    private val _codeExecutionOutput = MutableStateFlow<String?>(null)
    val codeExecutionOutput: StateFlow<String?> = _codeExecutionOutput.asStateFlow()

    init {
        formatJson()
        testRegex()
        executeCodeSimulation()
    }

    // --- Code Editor Actions ---
    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
        _codeEditorContent.value = getTemplateForLanguage(lang)
        executeCodeSimulation()
    }

    fun updateCodeEditorContent(code: String) {
        _codeEditorContent.value = code
        executeCodeSimulation()
    }

    fun updateCodeFindQuery(query: String) {
        _codeFindQuery.value = query
    }

    fun updateCodeReplaceQuery(query: String) {
        _codeReplaceQuery.value = query
    }

    fun performFindAndReplace() {
        if (_codeFindQuery.value.isNotEmpty()) {
            _codeEditorContent.value = _codeEditorContent.value.replace(_codeFindQuery.value, _codeReplaceQuery.value)
        }
    }

    fun executeCodeSimulation() {
        val lang = _selectedLanguage.value
        val code = _codeEditorContent.value

        // Check for basic syntax issues first
        val syntaxErr = checkSyntaxError(code)
        if (syntaxErr != null) {
            _codeExecutionOutput.value = syntaxErr
            return
        }

        val output = simulatePrintOutput(code, lang)
        _codeExecutionOutput.value = output
    }

    private fun checkSyntaxError(code: String): String? {
        var openBrace = 0
        var openParen = 0
        var openBracket = 0
        var doubleQuoteCount = 0

        for ((index, char) in code.withIndex()) {
            when (char) {
                '{' -> openBrace++
                '}' -> openBrace--
                '(' -> openParen++
                ')' -> openParen--
                '[' -> openBracket++
                ']' -> openBracket--
                '"' -> if (index == 0 || code[index - 1] != '\\') doubleQuoteCount++
            }
            if (openBrace < 0) return "SyntaxError: Unexpected closing brace '}' at position $index"
            if (openParen < 0) return "SyntaxError: Unexpected closing parenthesis ')' at position $index"
            if (openBracket < 0) return "SyntaxError: Unexpected closing bracket ']' at position $index"
        }
        if (openBrace > 0) return "SyntaxError: Unclosed brace '{' - missing '}'"
        if (openParen > 0) return "SyntaxError: Unclosed parenthesis '(' - missing ')'"
        if (openBracket > 0) return "SyntaxError: Unclosed bracket '[' - missing ']'"
        if (doubleQuoteCount % 2 != 0) return "SyntaxError: Unclosed string literal - missing double quote (\")"

        return null
    }

    private fun simulatePrintOutput(code: String, lang: String): String {
        val printTokens = when (lang) {
            "Kotlin", "Dart" -> listOf("println", "print")
            "Python" -> listOf("print")
            "JavaScript", "TypeScript" -> listOf("console.log", "console.info")
            "Java" -> listOf("System.out.println", "System.out.print")
            "C", "C++" -> listOf("printf", "std::cout")
            "C#" -> listOf("Console.WriteLine", "Console.Write")
            "PHP" -> listOf("echo", "print")
            "Ruby" -> listOf("puts", "print")
            "Go" -> listOf("fmt.Println", "fmt.Print")
            "Rust" -> listOf("println!", "print!")
            "Swift" -> listOf("print")
            "Shell" -> listOf("echo")
            "SQL" -> listOf("SELECT")
            else -> listOf("print", "println", "echo", "console.log")
        }

        val lines = code.lines()
        val outputs = mutableListOf<String>()
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("//") || trimmedLine.startsWith("#") || trimmedLine.startsWith(";")) continue
            for (token in printTokens) {
                if (trimmedLine.contains(token)) {
                    if (lang == "SQL" && token == "SELECT") {
                        outputs.add("id | repo_name | stars | language\n1  | DevForge  | 1250  | Kotlin")
                    } else if (lang == "C++" && token == "std::cout") {
                        val extracted = trimmedLine.substringAfter("<<").substringBefore(";").trim()
                        val cleaned = extracted.replace("std::endl", "").replace("\"", "").replace("'", "").trim()
                        if (cleaned.isNotBlank()) outputs.add(cleaned)
                    } else {
                        val extracted = trimmedLine.substringAfter("$token(").substringBeforeLast(")").trim()
                        if (extracted.isNotBlank() && extracted != trimmedLine) {
                            val cleanVal = extracted
                                .replace("\${appName}", "DevForge Studio")
                                .replace("\${languages.size}", "5")
                                .replace("\$appName", "DevForge Studio")
                                .replace("\"", "")
                                .replace("'", "")
                            outputs.add(cleanVal)
                        } else if (token == "echo") {
                            val echoText = trimmedLine.substringAfter("echo").replace(";", "").replace("\"", "").replace("'", "").trim()
                            if (echoText.isNotBlank()) outputs.add(echoText)
                        } else if (token == "puts") {
                            val putsText = trimmedLine.substringAfter("puts").replace("\"", "").replace("'", "").trim()
                            if (putsText.isNotBlank()) outputs.add(putsText)
                        }
                    }
                    break
                }
            }
        }

        return outputs.joinToString("\n")
    }

    private fun getTemplateForLanguage(lang: String): String {
        return when (lang) {
            "Kotlin" -> """fun main() {
    val appName = "DevForge Studio"
    println("Hello, ${'$'}appName!")
    
    val languages = listOf("Kotlin", "Java", "Python", "Rust", "Go")
    println("Supported Languages Count: ${'$'}{languages.size}")
}"""
            "Java" -> """public class Main {
    public static void main(String[] args) {
        System.out.println("Hello from DevForge Java Studio!");
        int result = calculateSum(15, 25);
        System.out.println("Sum Result: " + result);
    }

    private static int calculateSum(int a, int b) {
        return a + b;
    }
}"""
            "C" -> """#include <stdio.h>

int main() {
    printf("DevForge C Low-Level High Performance Studio\n");
    int a = 15, b = 35;
    printf("Sum: %d\n", a + b);
    return 0;
}"""
            "C#" -> """using System;

namespace DevForgeApp {
    class Program {
        static void Main(string[] args) {
            Console.WriteLine("Hello from DevForge C# .NET Studio!");
            int count = 10;
            Console.WriteLine($"Total Active Workers: {count}");
        }
    }
}"""
            "Assembly" -> """; DevForge Assembly (x86_64 / ARM) Template
section .data
    msg db 'Hello, DevForge Assembly Engine!', 0xA
    len equ $ - msg

section .text
    global _start

_start:
    mov eax, 4          ; sys_write
    mov ebx, 1          ; stdout
    mov ecx, msg        ; message pointer
    mov edx, len        ; message length
    int 0x80            ; system call

    mov eax, 1          ; sys_exit
    xor ebx, ebx
    int 0x80"""
            "Dart" -> """void main() {
  print('DevForge Dart & Flutter Studio');
  final tools = ['Compose', 'Flutter', 'Dart', 'Kotlin'];
  print('Active Developer Tools: ${'$'}{tools.join(", ")}');
}"""
            "PHP" -> """<?php
echo "DevForge PHP Web Engine\n";
${'$'}frameworks = ["Laravel", "Symfony", "WordPress"];
foreach (${'$'}frameworks as ${'$'}fw) {
    echo "Framework: ${'$'}fw\n";
}
?>"""
            "Ruby" -> """def dev_forge_greeting(user)
  puts "Welcome #{user} to DevForge Ruby Studio!"
end

dev_forge_greeting("Architect")"""
            "Python" -> """def fibonacci_series(n):
    a, b = 0, 1
    result = []
    for _ in range(n):
        result.append(a)
        a, b = b, a + b
    return result

print("Fibonacci Series:", fibonacci_series(8))
print("DevForge Python 3.11 Engine Ready")"""
            "JavaScript" -> """const devForge = {
    name: "DevForge",
    version: "1.0.0",
    features: ["API Tester", "Code Editor", "GitHub Cloner"]
};

console.log(`Welcome to ${'$'}{devForge.name} v${'$'}{devForge.version}`);
console.log("Active features:", devForge.features.join(", "));"""
            "TypeScript" -> """interface Developer {
    id: number;
    username: string;
    skills: string[];
}

const user: Developer = {
    id: 101,
    username: "mabideen",
    skills: ["Kotlin", "Android Compose", "TypeScript"]
};

console.log(`Developer Profile: ${'$'}{user.username}`);"""
            "C++" -> """#include <iostream>
#include <vector>

int main() {
    std::cout << "DevForge C++ High Performance Engine" << std::endl;
    std::vector<int> numbers = {10, 20, 30, 40};
    for (int num : numbers) {
        std::cout << "Value: " << num << std::endl;
    }
    return 0;
}"""
            "Rust" -> """fn main() {
    let project_name = "DevForge Rust Workspace";
    println!("Initializing {}", project_name);
    
    let numbers = vec![1, 2, 3, 4, 5];
    let sum: i32 = numbers.iter().sum();
    println!("Sum of elements: {}", sum);
}"""
            "Go" -> """package main

import "fmt"

func main() {
    fmt.Println("DevForge Go Microservice Engine")
    version := "v1.2.0"
    fmt.Println("Version:", version)
}"""
            "Swift" -> """import Foundation

struct AppConfig {
    let name: String
    let targetPlatform: String
}

let config = AppConfig(name: "DevForge", targetPlatform: "iOS / macOS")
print("App Configuration:", config.name, "for", config.targetPlatform)"""
            "HTML" -> """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>DevForge Live Code</title>
    <style>
        body { background: #0b1220; color: #00f2fe; font-family: monospace; padding: 20px; }
    </style>
</head>
<body>
    <h1>DevForge Web Studio</h1>
    <p>Multi-language editing on Android</p>
</body>
</html>"""
            "CSS" -> """.dev-forge-card {
    background-color: rgba(15, 23, 42, 0.8);
    border: 1px solid #00f2fe;
    border-radius: 12px;
    padding: 16px;
    color: #ffffff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}"""
            "SQL" -> """CREATE TABLE IF NOT EXISTS repositories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    repo_name TEXT NOT NULL,
    stars INT DEFAULT 0,
    language TEXT
);

INSERT INTO repositories (repo_name, stars, language) VALUES ('DevForge', 1250, 'Kotlin');
SELECT * FROM repositories WHERE stars > 500 ORDER BY stars DESC;"""
            "JSON" -> """{
  "project": "DevForge",
  "version": "1.0.0",
  "supportedLanguages": [
    "Kotlin", "Java", "Python", "JavaScript", "TypeScript",
    "C", "C#", "Assembly", "C++", "Rust", "Go", "Swift", "Dart", "PHP", "Ruby", "HTML", "CSS", "SQL", "JSON", "YAML", "Shell"
  ]
}"""
            "YAML" -> """version: '3.8'
services:
  devforge-api:
    image: devforge/api:latest
    ports:
      - "8080:8080"
    environment:
      - ENV=production
      - LOG_LEVEL=debug"""
            "Shell" -> """#!/bin/bash
echo "=== DevForge Shell Script Execution ==="
echo "Current Date: $(date)"
echo "Host Kernel: $(uname -r)"
echo "Deploying application artifacts..."
echo "Build completed successfully!" """
            else -> "// Write $lang code here..."
        }
    }

    // --- JSON Actions ---
    fun updateJsonInput(input: String) {
        _jsonInput.value = input
    }

    fun formatJson() {
        try {
            val trimmed = _jsonInput.value.trim()
            if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                _jsonOutput.value = jsonObject.toString(2)
                _jsonError.value = null
            } else if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                _jsonOutput.value = jsonArray.toString(2)
                _jsonError.value = null
            } else {
                _jsonError.value = "Invalid JSON structure. Must start with { or ["
            }
        } catch (e: Exception) {
            _jsonError.value = "JSON Error: ${e.localizedMessage}"
        }
    }

    fun minifyJson() {
        try {
            val trimmed = _jsonInput.value.trim()
            if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                _jsonOutput.value = jsonObject.toString()
                _jsonError.value = null
            } else if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                _jsonOutput.value = jsonArray.toString()
                _jsonError.value = null
            }
        } catch (e: Exception) {
            _jsonError.value = "Minify Error: ${e.localizedMessage}"
        }
    }

    // --- Regex Actions ---
    fun updateRegexPattern(pattern: String) {
        _regexPattern.value = pattern
        testRegex()
    }

    fun updateRegexTestInput(input: String) {
        _regexTestInput.value = input
        testRegex()
    }

    fun updateRegexReplacePattern(pattern: String) {
        _regexReplacePattern.value = pattern
        testRegex()
    }

    fun toggleIgnoreCase() {
        _regexFlagIgnoreCase.value = !_regexFlagIgnoreCase.value
        testRegex()
    }

    fun loadRegexPreset(preset: RegexPreset) {
        _regexPattern.value = preset.pattern
        _regexTestInput.value = preset.sampleText
        testRegex()
    }

    private fun testRegex() {
        try {
            val options = mutableSetOf<RegexOption>()
            if (_regexFlagIgnoreCase.value) options.add(RegexOption.IGNORE_CASE)
            if (_regexFlagMultiline.value) options.add(RegexOption.MULTILINE)

            val regex = Regex(_regexPattern.value, options)
            val matches = regex.findAll(_regexTestInput.value).map { it.value }.toList()
            _regexMatchResults.value = matches

            _regexReplaceResult.value = regex.replace(_regexTestInput.value, _regexReplacePattern.value)
        } catch (e: Exception) {
            _regexMatchResults.value = listOf("Invalid Regex Pattern: ${e.localizedMessage}")
            _regexReplaceResult.value = ""
        }
    }

    // --- Utility Functions ---
    fun encodeBase64(input: String): String {
        return try {
            android.util.Base64.encodeToString(input.toByteArray(), android.util.Base64.NO_WRAP)
        } catch (e: Exception) { "Error" }
    }

    fun decodeBase64(input: String): String {
        return try {
            String(android.util.Base64.decode(input, android.util.Base64.NO_WRAP))
        } catch (e: Exception) { "Invalid Base64 string" }
    }

    fun decodeJwt(jwt: String): Triple<String, String, String> {
        return try {
            val parts = jwt.split(".")
            if (parts.size >= 2) {
                val header = String(android.util.Base64.decode(parts[0], android.util.Base64.URL_SAFE))
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
                val signature = if (parts.size >= 3) parts[2] else "N/A"

                val prettyHeader = try { JSONObject(header).toString(2) } catch (e: Exception) { header }
                val prettyPayload = try { JSONObject(payload).toString(2) } catch (e: Exception) { payload }

                Triple(prettyHeader, prettyPayload, signature)
            } else {
                Triple("Invalid JWT Format", "Must contain at least header and payload separated by dots", "")
            }
        } catch (e: Exception) {
            Triple("Decode Error", e.localizedMessage ?: "Invalid JWT", "")
        }
    }

    fun generateHash(input: String, algorithm: String): String {
        return try {
            val md = MessageDigest.getInstance(algorithm)
            val bytes = md.digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) { "Error" }
    }

    fun encodeUrl(input: String): String = try { URLEncoder.encode(input, "UTF-8") } catch (e: Exception) { "" }
    fun decodeUrl(input: String): String = try { URLDecoder.decode(input, "UTF-8") } catch (e: Exception) { "" }

    fun generateUuid(): String = UUID.randomUUID().toString()

    fun convertEpochToDate(epochSeconds: Long): String {
        return try {
            val date = Date(epochSeconds * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) { "Invalid Epoch" }
    }

    fun generatePassword(length: Int = 16, includeSymbols: Boolean = true): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" + (if (includeSymbols) "!@#$%^&*()_+-=[]{}|;:,.<>?" else "")
        val random = Random()
        val sb = StringBuilder()
        for (i in 0 until length) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }
}
