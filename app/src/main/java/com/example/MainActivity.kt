package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.api.ApiTesterScreen
import com.example.ui.screens.api.ApiViewModel
import com.example.ui.screens.code.CodeToolsScreen
import com.example.ui.screens.code.CodeToolsViewModel
import com.example.ui.screens.github.GitHubDashboardScreen
import com.example.ui.screens.github.GitHubViewModel
import com.example.ui.screens.home.HomeDashboardScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.more.MoreToolsScreen
import com.example.ui.screens.more.MoreViewModel
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.DevCyanPrimary
import com.example.ui.theme.DevForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val moreViewModel: MoreViewModel = viewModel()
            val isDarkTheme by moreViewModel.isDarkTheme.collectAsStateWithLifecycle()

            DevForgeTheme(darkTheme = isDarkTheme) {
                DevForgeMainApp(moreViewModel = moreViewModel)
            }
        }
    }
}

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun DevForgeMainApp(moreViewModel: MoreViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("devforge_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showOnboarding by remember {
        mutableStateOf(sharedPrefs.getBoolean("is_first_install", true))
    }

    val homeViewModel: HomeViewModel = viewModel()
    val apiViewModel: ApiViewModel = viewModel()
    val codeToolsViewModel: CodeToolsViewModel = viewModel()
    val gitHubViewModel: GitHubViewModel = viewModel()

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, "nav_home"),
        NavItem("API Tools", Icons.Default.Api, "nav_api"),
        NavItem("Code Tools", Icons.Default.Code, "nav_code"),
        NavItem("GitHub", Icons.Default.Source, "nav_github"),
        NavItem("More", Icons.Default.MoreHoriz, "nav_more")
    )

    if (showOnboarding) {
        OnboardingScreen(
            onOnboardingComplete = {
                sharedPrefs.edit().putBoolean("is_first_install", false).apply()
                showOnboarding = false
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = DevCyanPrimary,
                                indicatorColor = DevCyanPrimary
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeDashboardScreen(
                            viewModel = homeViewModel,
                            onNavigateToTab = { tabIndex -> selectedTab = tabIndex }
                        )
                        1 -> ApiTesterScreen(viewModel = apiViewModel)
                        2 -> CodeToolsScreen(viewModel = codeToolsViewModel)
                        3 -> GitHubDashboardScreen(viewModel = gitHubViewModel)
                        4 -> MoreToolsScreen(
                            viewModel = moreViewModel,
                            onReopenOnboarding = { showOnboarding = true }
                        )
                    }
                }
            }
        }
    }
}
