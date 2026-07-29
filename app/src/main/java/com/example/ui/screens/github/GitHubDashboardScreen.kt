package com.example.ui.screens.github

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.GitHubRepoModel
import com.example.data.model.GitHubUserModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubDashboardScreen(
    viewModel: GitHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val repos by viewModel.repositories.collectAsStateWithLifecycle()
    val userProfile by viewModel.selectedUser.collectAsStateWithLifecycle()
    val userRepos by viewModel.userRepos.collectAsStateWithLifecycle()
    val readmeContent by viewModel.repoReadme.collectAsStateWithLifecycle()
    val cloneProgress by viewModel.cloneProgress.collectAsStateWithLifecycle()
    val cloneStatusMessage by viewModel.cloneStatusMessage.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    var searchMode by remember { mutableIntStateOf(0) } // 0: Search Repos, 1: Search Username/User
    var userSearchQuery by remember { mutableStateOf("torvalds") }

    // Storage permission state
    var pendingCloneRepo by remember { mutableStateOf<GitHubRepoModel?>(null) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it } || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        if (granted && pendingCloneRepo != null) {
            viewModel.cloneAndDownloadRepo(context, pendingCloneRepo!!)
            pendingCloneRepo = null
        } else {
            showPermissionRationaleDialog = true
        }
    }

    fun handleCloneZip(repo: GitHubRepoModel) {
        pendingCloneRepo = repo
        val isStorageGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (isStorageGranted) {
            viewModel.cloneAndDownloadRepo(context, repo)
        } else {
            val req = mutableListOf<String>()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                req.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                req.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                req.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(req.toTypedArray())
        }
    }

    val quickFilters = listOf("android compose", "kotlin coroutines", "retrofit", "room database", "ktor", "flutter")
    val sampleUsers = listOf("torvalds", "google", "facebook", "square", "android", "jetbrains")

    Scaffold(
        modifier = modifier.testTag("github_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = { Text("GitHub Repo & User Explorer", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Mode Selector
            TabRow(
                selectedTabIndex = searchMode,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = searchMode == 0,
                    onClick = { searchMode = 0 },
                    text = { Text("Search Repositories", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = searchMode == 1,
                    onClick = { searchMode = 1 },
                    text = { Text("Search Developer / Org", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (searchMode == 0) {
                // Search Repositories Mode
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search public repositories (e.g., Kotlin, Compose)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DevCyanPrimary) },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchRepositories() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Search", tint = DevCyanPrimary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickFilters) { filter ->
                        FilterChip(
                            selected = query == filter,
                            onClick = {
                                viewModel.updateSearchQuery(filter)
                                viewModel.searchRepositories(filter)
                            },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }
            } else {
                // Search Username Mode
                OutlinedTextField(
                    value = userSearchQuery,
                    onValueChange = { userSearchQuery = it },
                    placeholder = { Text("Enter GitHub username or organization...") },
                    leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = DevEmeraldSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { if (userSearchQuery.isNotBlank()) viewModel.fetchUserWithRepos(userSearchQuery) }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Fetch User", tint = DevEmeraldSecondary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Sample User Suggestions
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleUsers) { u ->
                        SuggestionChip(
                            onClick = {
                                userSearchQuery = u
                                viewModel.fetchUserWithRepos(u)
                            },
                            label = { Text("@$u", fontSize = 12.sp, color = DevEmeraldSecondary) }
                        )
                    }
                }
            }

            // Clone / Download Progress Overlay
            if (cloneProgress != null) {
                GlassCard(borderColor = DevCyanPrimary) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CLONING REPOSITORY ARCHIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DevCyanPrimary)
                            Text("${(cloneProgress!! * 100).toInt()}%", fontWeight = FontWeight.Bold, color = DevEmeraldSecondary)
                        }
                        LinearProgressIndicator(
                            progress = { cloneProgress!! },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = DevCyanPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = cloneStatusMessage,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DevCyanPrimary)
                }
            } else {
                if (searchMode == 0) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(repos) { repo ->
                            RepoCard(
                                repo = repo,
                                onOpenUrl = { uriHandler.openUri(repo.htmlUrl) },
                                onViewOwner = {
                                    searchMode = 1
                                    userSearchQuery = repo.ownerLogin
                                    viewModel.fetchUserWithRepos(repo.ownerLogin)
                                },
                                onViewReadme = { viewModel.fetchRepoReadme(repo.fullName) },
                                onCloneRepo = { handleCloneZip(repo) }
                            )
                        }
                    }
                } else {
                    // User Repositories & Profile View
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (userProfile != null) {
                            item {
                                UserHeaderCard(
                                    user = userProfile!!,
                                    onOpenGithub = { uriHandler.openUri("https://github.com/${userProfile!!.login}") }
                                )
                            }
                            item {
                                Text(
                                    text = "PUBLIC REPOSITORIES (${userRepos.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DevCyanPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            items(userRepos) { repo ->
                                RepoCard(
                                    repo = repo,
                                    onOpenUrl = { uriHandler.openUri(repo.htmlUrl) },
                                    onViewOwner = {},
                                    onViewReadme = { viewModel.fetchRepoReadme(repo.fullName) },
                                    onCloneRepo = { handleCloneZip(repo) }
                                )
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Search any GitHub username above to explore their repos", color = Color.Gray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // README Preview Dialog
        if (readmeContent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearReadme() },
                title = { Text("Repository README.md", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = readmeContent!!,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearReadme() }) { Text("Close") }
                }
            )
        }

        // Storage Permission Request Rationale Dialog
        if (showPermissionRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionRationaleDialog = false },
                icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = DevCyanPrimary, modifier = Modifier.size(32.dp)) },
                title = { Text("Storage Permission Required", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "DevForge requires Storage access to download and save repository source code ZIP archives directly into your device's Phone Storage (Downloads folder).\n\nPlease allow Storage Permission to enable ZIP cloning.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionRationaleDialog = false
                            val req = mutableListOf<String>()
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                req.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                req.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                req.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            if (req.isNotEmpty()) {
                                permissionLauncher.launch(req.toTypedArray())
                            } else if (pendingCloneRepo != null) {
                                viewModel.cloneAndDownloadRepo(context, pendingCloneRepo!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary)
                    ) {
                        Text("ALLOW PERMISSION", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationaleDialog = false }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun UserHeaderCard(
    user: GitHubUserModel,
    onOpenGithub: () -> Unit
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.login,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(1.dp, DevCyanPrimary, CircleShape)
                    )
                    Column {
                        Text(user.name ?: user.login, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                        Text("@${user.login}", fontSize = 12.sp, color = DevCyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onOpenGithub,
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary)
                ) {
                    Text("PROFILE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            if (!user.bio.isNullOrBlank()) {
                Text(user.bio!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${user.publicRepos}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevCyanPrimary)
                    Text("Public Repos", fontSize = 10.sp, color = Color.Gray)
                }
                Column {
                    Text("${user.followers}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevEmeraldSecondary)
                    Text("Followers", fontSize = 10.sp, color = Color.Gray)
                }
                Column {
                    Text("${user.following}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DevPurpleTertiary)
                    Text("Following", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun RepoCard(
    repo: GitHubRepoModel,
    onOpenUrl: () -> Unit,
    onViewOwner: () -> Unit,
    onViewReadme: () -> Unit,
    onCloneRepo: () -> Unit
) {
    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onViewOwner() }
                ) {
                    AsyncImage(
                        model = repo.ownerAvatarUrl,
                        contentDescription = repo.ownerLogin,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = repo.ownerLogin,
                        fontSize = 12.sp,
                        color = DevCyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = DevEmeraldSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = repo.language ?: "Code",
                        color = DevEmeraldSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = repo.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = repo.description ?: "No description available",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Text("${repo.starsCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AltRoute, contentDescription = null, tint = DevCyanPrimary, modifier = Modifier.size(16.dp))
                    Text("${repo.forksCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Text("${repo.openIssuesCount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))

            // Action Buttons Row: README, CLONE / DOWNLOAD ZIP, OPEN GITHUB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReadme,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("README", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCloneRepo,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyanPrimary),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CLONE ZIP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                IconButton(
                    onClick = onOpenUrl,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open URL", tint = DevCyanPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
