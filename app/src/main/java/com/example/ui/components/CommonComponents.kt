package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HttpMethod
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun MethodBadge(
    method: HttpMethod,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (method) {
        HttpMethod.GET -> MethodGet to Color.White
        HttpMethod.POST -> MethodPost to Color.Black
        HttpMethod.PUT -> MethodPut to Color.Black
        HttpMethod.DELETE -> MethodDelete to Color.White
        HttpMethod.PATCH -> MethodPatch to Color.Black
        HttpMethod.HEAD, HttpMethod.OPTIONS -> MethodOther to Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.2f))
            .border(1.dp, bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.name,
            color = bgColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StatusBadge(
    statusCode: Int,
    statusText: String = "",
    modifier: Modifier = Modifier
) {
    val color = when (statusCode) {
        in 200..299 -> DevEmeraldSecondary
        in 300..399 -> DevCyanPrimary
        in 400..499 -> Color(0xFFFF9800)
        in 500..599 -> Color(0xFFFF5252)
        else -> Color.Gray
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = if (statusText.isNotEmpty()) "$statusCode $statusText" else "$statusCode",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SyntaxCodeViewer(
    code: String,
    modifier: Modifier = Modifier,
    language: String = "code",
    maxLines: Int = Int.MAX_VALUE
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D121F))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DevCyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = language.uppercase(),
                        color = DevCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        isCopied = true
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCopied) DevEmeraldSecondary else DevCyanPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = if (isCopied) DevEmeraldSecondary else DevCyanPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCopied) "COPIED!" else "COPY CODE",
                            color = if (isCopied) DevEmeraldSecondary else DevCyanPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            val annotatedText = remember(code, language) {
                formatSyntaxHighlighting(code, language)
            }

            SelectionContainer {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(
                        text = annotatedText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = maxLines
                    )
                }
            }
        }
    }
}

fun formatSyntaxHighlighting(code: String, language: String): AnnotatedString {
    return buildAnnotatedString {
        if (code.isBlank()) {
            withStyle(SpanStyle(color = Color.Gray)) { append("// Empty body") }
            return@buildAnnotatedString
        }

        val langLower = language.lowercase()
        val keywordColor = Color(0xFFFF7B72)
        val stringColor = Color(0xFFA5D6FF)
        val numberColor = Color(0xFF7EE787)
        val commentColor = Color(0xFF8B949E)
        val defaultColor = Color(0xFFE6EDE3)

        val keywords = setOf(
            "fun", "val", "var", "class", "interface", "object", "import", "package", "return", "if", "else",
            "for", "while", "when", "is", "in", "try", "catch", "public", "private", "protected", "static",
            "void", "int", "float", "double", "char", "bool", "boolean", "def", "lambda", "using", "namespace",
            "struct", "enum", "fn", "let", "mut", "impl", "trait", "pub", "include", "define", "mov", "push",
            "pop", "add", "sub", "jmp", "call", "ret", "section", "global", "extern", "select", "from", "where",
            "insert", "update", "delete", "null", "true", "false", "system.out.println", "console.log", "printf"
        )

        // Basic tokenizer for syntax highlighting across multi-languages
        val lines = code.lines()
        lines.forEachIndexed { lineIdx, line ->
            var i = 0
            while (i < line.length) {
                val sub = line.substring(i)
                when {
                    // Comments
                    sub.startsWith("//") || sub.startsWith("#") || sub.startsWith(";") -> {
                        withStyle(SpanStyle(color = commentColor)) { append(sub) }
                        i = line.length
                    }
                    // Strings
                    sub.startsWith("\"") || sub.startsWith("'") -> {
                        val quote = sub[0]
                        val endIdx = line.indexOf(quote, i + 1)
                        if (endIdx != -1) {
                            val strLiteral = line.substring(i, endIdx + 1)
                            withStyle(SpanStyle(color = stringColor)) { append(strLiteral) }
                            i = endIdx + 1
                        } else {
                            withStyle(SpanStyle(color = stringColor)) { append(sub) }
                            i = line.length
                        }
                    }
                    // Words / Keywords
                    sub[0].isLetter() || sub[0] == '_' -> {
                        val wordMatch = Regex("^[a-zA-Z0-9_]+").find(sub)
                        val word = wordMatch?.value ?: sub.substring(0, 1)
                        if (keywords.contains(word.lowercase())) {
                            withStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold)) { append(word) }
                        } else {
                            withStyle(SpanStyle(color = defaultColor)) { append(word) }
                        }
                        i += word.length
                    }
                    // Numbers
                    sub[0].isDigit() -> {
                        val numMatch = Regex("^[0-9]+(\\.[0-9]+)?").find(sub)
                        val numStr = numMatch?.value ?: sub.substring(0, 1)
                        withStyle(SpanStyle(color = numberColor)) { append(numStr) }
                        i += numStr.length
                    }
                    else -> {
                        withStyle(SpanStyle(color = defaultColor)) { append(sub[0].toString()) }
                        i++
                    }
                }
            }
            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
