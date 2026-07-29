package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: HomeViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedItems by viewModel.savedItems.collectAsStateWithLifecycle()

    var showQuickCreateDialog by remember { mutableStateOf(false) }

    if (showQuickCreateDialog) {
        AlertDialog(
            onDismissRequest = { showQuickCreateDialog = false },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = DevCyanPrimary) },
            title = { Text("Developer Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        "Choose a quick action to populate mock data or jump directly into developer tools:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.addSampleQuickData {
                                Toast.makeText(context, "Sample Developer Datasets inserted successfully!", Toast.LENGTH_SHORT).show()
                            }
                            showQuickCreateDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DevCyanPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = DevCyanPrimary)
                            Text("Insert Sample Developer Datasets", color = DevCyanPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            showQuickCreateDialog = false
                            onNavigateToTab(4) // AI Assistant tab
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmeraldSecondary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.Black)
                            Text("Launch AI Assistant Query", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showQuickCreateDialog = false
                            onNavigateToTab(1) // API Tester
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DevPurpleTertiary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Http, contentDescription = null, tint = DevPurpleTertiary)
                            Text("Test Custom REST Endpoint", color = DevPurpleTertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickCreateDialog = false }) {
                    Text("CLOSE", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("home_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DevCyanPrimary, DevEmeraldSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "DevForge Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "DevForge",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "All-in-One Mobile Developer Toolkit",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onNavigateToTab(4) }) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = DevCyanPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F1A2E),
                                    Color(0xFF162544),
                                    Color(0xFF0B1220)
                                )
                            )
                        )
                        .border(1.dp, DevCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DevCyanPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "v1.0.0 PRO TOOLKIT",
                                    color = DevCyanPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = DevEmeraldSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Elevate Your Mobile Engineering Workflow",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Postman-grade API testing, JSON & Regex playgrounds, GitHub insights, and 30+ developer utilities in your pocket.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToTab(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Http,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("API Tester", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigateToTab(4) },
                                colors = ButtonDefaults.buttonColors(containerColor = DevEmeraldSecondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ AI Assistant", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Saved Items",
                        value = "${savedItems.size}",
                        icon = Icons.Default.Bookmark,
                        color = DevCyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tools Ready",
                        value = "32+",
                        icon = Icons.Default.Construction,
                        color = DevEmeraldSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "AI Engine",
                        value = "Active",
                        icon = Icons.Default.AutoAwesome,
                        color = DevPurpleTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Launcher Grid Header
            item {
                Text(
                    text = "FEATURED DEVELOPER MODULES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DevCyanPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Launcher Grid Items
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolLauncherCard(
                            title = "API Tester",
                            subtitle = "REST, headers, auth, cURL import",
                            icon = Icons.Default.Api,
                            badgeColor = MethodPost,
                            onClick = { onNavigateToTab(1) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolLauncherCard(
                            title = "JSON Utilities",
                            subtitle = "Formatter, validator, diff, tree",
                            icon = Icons.Default.DataObject,
                            badgeColor = DevCyanPrimary,
                            onClick = { onNavigateToTab(2) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ToolLauncherCard(
                            title = "Regex Playground",
                            subtitle = "Live match, replace & cheat sheet",
                            icon = Icons.Default.FindInPage,
                            badgeColor = DevEmeraldSecondary,
                            onClick = { onNavigateToTab(2) },
                            modifier = Modifier.weight(1f)
                        )
                        ToolLauncherCard(
                            title = "GitHub Insights",
                            subtitle = "Trending repos, users, markdown",
                            icon = Icons.Default.Source,
                            badgeColor = DevPurpleTertiary,
                            onClick = { onNavigateToTab(3) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Pinned Quick Activity
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT SAVED ACTIVITY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${savedItems.size} Saved",
                            fontSize = 12.sp,
                            color = DevCyanPrimary
                        )
                    }

                    if (savedItems.isEmpty()) {
                        GlassCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No saved API requests or Regex snippets yet.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                                TextButton(onClick = {
                                    viewModel.addSampleQuickData {
                                        Toast.makeText(context, "Sample Developer Datasets inserted successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Load Sample Presets", color = DevCyanPrimary)
                                }
                            }
                        }
                    } else {
                        savedItems.take(4).forEach { item ->
                            GlassCard(
                                onClick = { onNavigateToTab(if (item.category == "API") 1 else 2) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (item.category == "API") MethodGet.copy(alpha = 0.2f)
                                                    else DevEmeraldSecondary.copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (item.category == "API") Icons.Default.Http else Icons.Default.Code,
                                                contentDescription = null,
                                                tint = if (item.category == "API") MethodGet else DevEmeraldSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = item.subtitle,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteItem(item.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Item",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ToolLauncherCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
