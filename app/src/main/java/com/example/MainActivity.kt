package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.presentation.HealthViewModel
import com.example.presentation.ViewModelFactory
import com.example.presentation.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Retrieve master dependencies Repository
        val app = application as HealthSyncApplication
        val repository = app.repository
        
        // Instantiate ViewModel
        val factory = ViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[HealthViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainRootNavigationContainer(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun MainRootNavigationContainer(viewModel: HealthViewModel) {
    val authState by viewModel.authState.collectAsState()
    
    // Simple state navigation framework
    // Tabs: "dashboard", "activity", "water", "sleep", "medication", "reports", "profile", "settings", "sos"
    var currentTab by remember { mutableStateOf("dashboard") }
    var isSplashLoading by remember { mutableStateOf(true) }

    // Brief welcoming splash screen
    LaunchedEffect(Unit) {
        delay(1200)
        isSplashLoading = false
    }

    if (isSplashLoading) {
        // --- HIGH FIDELITY SPLASH SCREEN ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Splash logo",
                    tint = TealAccent,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "HealthSync",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(color = TealAccent, strokeWidth = 3.dp)
            }
        }
    } else if (!authState.isLoggedIn) {
        // --- ONBOARDING & SIGN IN ---
        LoginScreen(viewModel = viewModel)
    } else {
        // --- MAIN APP SHELL SCANNED WRAPPERS ---
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                "dashboard" -> "Dashboard"
                                "activity" -> "Fitness Tracker"
                                "water" -> "Water Ledger"
                                "sleep" -> "Sleep Log"
                                "medication" -> "Medications"
                                "reports" -> "Health Analytics"
                                "profile" -> "My Metrics"
                                "settings" -> "Settings"
                                "sos" -> "Emergency Hub"
                                else -> "HealthSync"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    navigationIcon = {
                        // Quick toggle buttons
                        if (currentTab != "dashboard") {
                            IconButton(onClick = { currentTab = "dashboard" }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to dashboard", tint = Color.LightGray)
                            }
                        } else {
                            IconButton(onClick = { currentTab = "sos" }) {
                                Icon(Icons.Default.Emergency, contentDescription = "Panic SOS beacon", tint = Color.Red)
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { currentTab = "profile" },
                            modifier = Modifier.testTag("nav_profile_icon_button")
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile Details", tint = if (currentTab == "profile") TealAccent else Color.LightGray)
                        }
                        IconButton(
                            onClick = { currentTab = "settings" },
                            modifier = Modifier.testTag("nav_settings_icon_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings configuration", tint = if (currentTab == "settings") TealAccent else Color.LightGray)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        NavigationTabItem("dashboard", Icons.Default.Dashboard, "Home"),
                        NavigationTabItem("activity", Icons.Default.DirectionsRun, "Fitness"),
                        NavigationTabItem("water", Icons.Default.WaterDrop, "Water"),
                        NavigationTabItem("sleep", Icons.Default.Bedtime, "Sleep"),
                        NavigationTabItem("medication", Icons.Default.MedicalServices, "Alarms"),
                        NavigationTabItem("reports", Icons.Default.BarChart, "Trends")
                    )
                    
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentTab == item.route,
                            onClick = { currentTab = item.route },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = TealAccent,
                                indicatorColor = TealAccent,
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                    },
                    label = "tabTransition"
                ) { targetRoute ->
                    when (targetRoute) {
                        "dashboard" -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMetric = { tab -> currentTab = tab }
                        )
                        "activity" -> ActivityScreen(viewModel = viewModel)
                        "water" -> WaterScreen(viewModel = viewModel)
                        "sleep" -> SleepScreen(viewModel = viewModel)
                        "medication" -> MedicationScreen(viewModel = viewModel)
                        "reports" -> ReportsScreen(viewModel = viewModel)
                        "profile" -> ProfileScreen(viewModel = viewModel)
                        "settings" -> SettingsScreen(viewModel = viewModel)
                        "sos" -> SosScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
