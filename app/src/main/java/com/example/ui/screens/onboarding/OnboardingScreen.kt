package com.example.ui.screens.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = "Welcome to DevForge",
            subtitle = "The All-in-One Mobile Developer Suite",
            description = "Empower your software engineering workflow right on your Android device. Test APIs, edit multi-language code, inspect GitHub repos, and converse with AI.",
            icon = Icons.Default.Terminal,
            accentColor = DevCyanPrimary
        ),
        OnboardingPageData(
            title = "Multi-Language Code Studio",
            subtitle = "15+ Programming Languages & Utilities",
            description = "Write and format Kotlin, Java, Python, JS, C++, Rust, Go, SQL and more. Features syntax highlighting, line numbering, templates, JSON beautifier, and Regex tester.",
            icon = Icons.Default.Code,
            accentColor = DevEmeraldSecondary
        ),
        OnboardingPageData(
            title = "GitHub Repo Explorer & Cloner",
            subtitle = "Search Any Developer & Clone Repos",
            description = "Lookup any GitHub username, view public repositories, inspect star ratings and forks, and clone/download source code ZIPs straight to your device.",
            icon = Icons.Default.Source,
            accentColor = DevPurpleTertiary
        ),
        OnboardingPageData(
            title = "Permissions & System Setup",
            subtitle = "Configure Device Capabilities",
            description = "DevForge requires Internet and Storage access to fetch GitHub repositories, execute network calls, and export code files securely.",
            icon = Icons.Default.Security,
            accentColor = DevCyanPrimary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Permission check states
    var storageGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) true
            else ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        )
    }

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: storageGranted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: notificationGranted
        }
    }

    Scaffold(
        modifier = modifier.testTag("onboarding_screen"),
        containerColor = DevBackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo_1785140580033),
                        contentDescription = "DevForge Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = "DevForge",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = { onOnboardingComplete() }) {
                        Text("SKIP", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIdx ->
                val page = pages[pageIdx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(page.accentColor.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                            .border(1.dp, page.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = page.title,
                            tint = page.accentColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        color = page.accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = page.subtitle.uppercase(),
                            color = page.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // If Page 4, show permissions status card
                    if (pageIdx == 3) {
                        Spacer(modifier = Modifier.height(20.dp))
                        GlassCard {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PermissionStatusRow(
                                    title = "Internet & Network Access",
                                    isGranted = true,
                                    subtitle = "Required for API Testing & GitHub API"
                                )
                                PermissionStatusRow(
                                    title = "Storage / Repository Export",
                                    isGranted = storageGranted,
                                    subtitle = "Used to save code snippets & cloned repo archives"
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    PermissionStatusRow(
                                        title = "App Notifications",
                                        isGranted = notificationGranted,
                                        subtitle = "Notifies when repo cloning or download completes"
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedButton(
                                    onClick = {
                                        val reqList = mutableListOf<String>()
                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                            reqList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                                            reqList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        } else {
                                            reqList.add(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        if (reqList.isNotEmpty()) {
                                            permissionLauncher.launch(reqList.toTypedArray())
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DevCyanPrimary)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DevCyanPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("GRANT PERMISSIONS", color = DevCyanPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { idx ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (pagerState.currentPage == idx) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == idx) DevCyanPrimary else Color.Gray.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accentColor
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "GET STARTED" else "CONTINUE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (pagerState.currentPage == pages.size - 1) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Surface(
            color = if (isGranted) DevEmeraldSecondary.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isGranted) DevEmeraldSecondary else Color.Red,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isGranted) "GRANTED" else "REQUIRED",
                    color = if (isGranted) DevEmeraldSecondary else Color.Red,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
