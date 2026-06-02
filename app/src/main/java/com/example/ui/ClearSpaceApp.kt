package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.StorageItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Composable
fun ClearSpaceApp(
    viewModel: ClearSpaceViewModel = viewModel(),
    onFinished: () -> Unit = {}
) {
    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val items by viewModel.allActiveItems.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val vaultedItems by viewModel.vaultedItems.collectAsState()
    val isMainScanning by viewModel.isMainScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanningCategory by viewModel.currentScanningCategory.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = onboardingStep, label = "onboarding_crossfade") { step ->
            when (step) {
                0 -> OnboardingWelcomeScreen(
                    viewModel = viewModel,
                    deviceStorageUsed = "127.2 GB",
                    deviceStorageTotal = "128 GB"
                )
                1 -> OnboardingScanScreen(
                    viewModel = viewModel,
                    progress = scanProgress,
                    category = scanningCategory
                )
                2 -> OnboardingResultsScreen(
                    viewModel = viewModel,
                    items = items
                )
                3 -> OnboardingVaultScreen(
                    viewModel = viewModel
                )
                4 -> OnboardingFinishedScreen(
                    viewModel = viewModel
                )
                -1 -> {
                    // Main App Shell with Bottom Navigation
                    MainAppShell(
                        viewModel = viewModel,
                        activeTab = activeTab,
                        items = items,
                        vaultedItems = vaultedItems,
                        isMainScanning = isMainScanning,
                        scanProgress = scanProgress,
                        scanningCategory = scanningCategory
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// MAIN APP SHELL
// -----------------------------------------------------------------------------
@Composable
fun MainAppShell(
    viewModel: ClearSpaceViewModel,
    activeTab: String,
    items: List<StorageItem>,
    vaultedItems: List<StorageItem>,
    isMainScanning: Boolean,
    scanProgress: Float,
    scanningCategory: String
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("app_bottom_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { viewModel.selectTab("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_home_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "files",
                    onClick = { viewModel.selectTab("files") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Files") },
                    label = { Text("Files", fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_files_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "vault",
                    onClick = { viewModel.selectTab("vault") },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Vault") },
                    label = { Text("Vault", fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_vault_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { viewModel.selectTab("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isMainScanning) {
                OnboardingScanScreen(
                    viewModel = viewModel,
                    progress = scanProgress,
                    category = scanningCategory,
                    isMainScanMode = true
                )
            } else {
                Crossfade(targetState = activeTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        "home" -> HomeScreenTab(viewModel = viewModel, items = items)
                        "files" -> FilesScreenTab(viewModel = viewModel, items = items)
                        "vault" -> VaultScreenTab(viewModel = viewModel, vaultedItems = vaultedItems)
                        "settings" -> SettingsScreenTab(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ONBOARDING SCREEN 1: WELCOME
// -----------------------------------------------------------------------------
@Composable
fun OnboardingWelcomeScreen(
    viewModel: ClearSpaceViewModel,
    deviceStorageUsed: String,
    deviceStorageTotal: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ClearSpace AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // Hero Image Custom Graphic Reference
        Box(
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(280.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_hero),
                contentDescription = "Onboarding Hero Graphic: Smartphone being cleaned by neural technology",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Subtle "Critical" float badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color(0xFFBA1A1A), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Critical Storage",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Welcome Headline & Message
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = "Your phone is almost full.",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ClearSpace AI can safely recover space in minutes using smart compression and deep cleaning.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.startOnboardingScan() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
                    .testTag("start_ai_scan_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Start AI Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sublink
            Text(
                text = "View local storage details",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { viewModel.startOnboardingScan() }
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device Storage summary progress box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurfaceContainerLow),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Device Storage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$deviceStorageUsed / $deviceStorageTotal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBA1A1A)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Linear storage bar representing full
                LinearProgressIndicator(
                    progress = { 0.99f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFFBA1A1A),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Powered by Neural Clean™ Technology",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// ONBOARDING SCREEN 2: LIVE AI SCAN ANIMATION
// -----------------------------------------------------------------------------
@Composable
fun OnboardingScanScreen(
    viewModel: ClearSpaceViewModel,
    progress: Float,
    category: String,
    isMainScanMode: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ClearSpace AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Serif
            )
        }

        // Center scan status ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scanning your storage...",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI is identifying redundant data and cache files.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Beautiful status circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                // Background Track
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawArc(
                        color = primaryColor.copy(alpha = 0.1f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(width = size.width, height = size.height)
                    )
                }

                // Active Progress Arc
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawArc(
                        color = primaryColor,
                        startAngle = 140f,
                        sweepAngle = 260f * progress,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(width = size.width, height = size.height)
                    )
                }

                // Inner circle content
                Card(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "PROCESSING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Live Scanning Categories Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Category Row 1: Duplicate Photos
            ScanStatusItem(
                title = "Scanning Duplicate Photos",
                subtext = if (progress >= 0.35f) "342 MB found" else "Scanning...",
                status = when {
                    progress >= 0.35f -> ScanStatus.COMPLETED
                    else -> ScanStatus.RUNNING
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Row 2: Large Videos
            ScanStatusItem(
                title = "Analyzing Large Videos",
                subtext = when {
                    progress >= 0.65f -> "12 GB found"
                    progress >= 0.35f -> "Analyzing frame data..."
                    else -> "Waiting..."
                },
                status = when {
                    progress >= 0.65f -> ScanStatus.COMPLETED
                    progress >= 0.35f -> ScanStatus.RUNNING
                    else -> ScanStatus.WAITING
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Row 3: Junk Files
            ScanStatusItem(
                title = "Searching Junk Files",
                subtext = when {
                    progress >= 0.85f -> "800 MB found"
                    progress >= 0.65f -> "Analyzing cache data..."
                    else -> "Waiting..."
                },
                status = when {
                    progress >= 0.85f -> ScanStatus.COMPLETED
                    progress >= 0.65f -> ScanStatus.RUNNING
                    else -> ScanStatus.WAITING
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress indicators lower bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Progress",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cancel Button
        OutlinedButton(
            onClick = {
                if (isMainScanMode) {
                    viewModel.cancelMainScan()
                } else {
                    viewModel.cancelOnboardingScan()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("cancel_scan_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class ScanStatus {
    WAITING, RUNNING, COMPLETED
}

@Composable
fun ScanStatusItem(
    title: String,
    subtext: String,
    status: ScanStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (status == ScanStatus.RUNNING) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else AppOutlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Leading Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (status) {
                                ScanStatus.COMPLETED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                ScanStatus.RUNNING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ScanStatus.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (title) {
                            "Scanning Duplicate Photos" -> Icons.Default.Share
                            "Analyzing Large Videos" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Delete
                        },
                        contentDescription = null,
                        tint = when (status) {
                            ScanStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
                            ScanStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            ScanStatus.WAITING -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtext,
                        fontSize = 12.sp,
                        color = if (status == ScanStatus.RUNNING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (status == ScanStatus.RUNNING) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Trailing circular indicator
            when (status) {
                ScanStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            .padding(4.dp)
                    )
                }
                ScanStatus.RUNNING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ScanStatus.WAITING -> {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Waiting",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ONBOARDING SCREEN 3: SCAN RESULTS
// -----------------------------------------------------------------------------
@Composable
fun OnboardingResultsScreen(
    viewModel: ClearSpaceViewModel,
    items: List<StorageItem>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ClearSpace AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center static progress wheel showing results
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = Color(0xFF016A60).copy(alpha = 0.1f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(width = size.width, height = size.height)
                    )
                    drawArc(
                        color = Color(0xFF00497D),
                        startAngle = 140f,
                        sweepAngle = 260f * 0.75f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(width = size.width, height = size.height)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "67.4",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = "GB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "67.4GB can be recovered safely.",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Optimizing your device for peak performance.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results breakdowns list
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photos Component (32GB)
            ResultBreakdownCard(
                category = "Duplicate Photos",
                savings = "32GB to recover",
                buttonText = "View & Select",
                onAction = { viewModel.proceedToVaultSetup() },
                icon = Icons.Default.Share,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AI Compression (12GB)
            ResultBreakdownCard(
                category = "AI Compression",
                savings = "12GB available",
                buttonText = "Optimize Now",
                onAction = { viewModel.proceedToVaultSetup() },
                icon = Icons.Default.Star,
                backgroundColor = Color(0xFF016A60).copy(alpha = 0.05f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cloud offloading (23.4GB)
            ResultBreakdownCard(
                category = "Cloud Offloading",
                savings = "23.4GB",
                buttonText = "Move to Vault",
                onAction = { viewModel.proceedToVaultSetup() },
                icon = Icons.Default.KeyboardArrowUp,
                backgroundColor = Color(0xFF930009).copy(alpha = 0.05f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions Bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.proceedToVaultSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("clean_everything_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clean Everything",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.proceedToVaultSetup() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_review_files_button"),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Review Files",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ResultBreakdownCard(
    category: String,
    savings: String,
    buttonText: String,
    onAction: () -> Unit,
    icon: ImageVector,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Leading Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when (category) {
                            "Duplicate Photos" -> MaterialTheme.colorScheme.primary
                            "AI Compression" -> Color(0xFF016A60)
                            else -> Color(0xFF930009)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = category,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = savings,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (category) {
                            "Duplicate Photos" -> MaterialTheme.colorScheme.primary
                            "AI Compression" -> Color(0xFF016A60)
                            else -> Color(0xFFBA1A1A)
                        }
                    )
                }
            }

            // Ghost Button
            OutlinedButton(
                onClick = onAction,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ONBOARDING SCREEN 4: CLOUD VAULT SETUP
// -----------------------------------------------------------------------------
@Composable
fun OnboardingVaultScreen(
    viewModel: ClearSpaceViewModel
) {
    var isOffloadingEnabled by remember { mutableStateOf(true) }
    var isWifiOnlyEnabled by remember { mutableStateOf(true) }
    var isInstantRestoreEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ClearSpace Cloud Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Illustrative Vault Box
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Move old files to your free secure Cloud Vault.",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Securely offload files you rarely use to stop wasting precious device storage. Access or restore them instantly anytime.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Toggle 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Automatic Offloading",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Offload raw/old files automatically",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isOffloadingEnabled,
                        onCheckedChange = { isOffloadingEnabled = it },
                        modifier = Modifier.testTag("toggle_auto_offload")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wi-Fi Only Uploads",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Save mobile cellular data plans",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isWifiOnlyEnabled,
                        onCheckedChange = { isWifiOnlyEnabled = it },
                        modifier = Modifier.testTag("toggle_wifi_uploads")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Instant Restore",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Recover files back to storage in 1-click",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isInstantRestoreEnabled,
                        onCheckedChange = { isInstantRestoreEnabled = it },
                        modifier = Modifier.testTag("toggle_instant_restore")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Next Button
        Button(
            onClick = {
                // Sync these variables with settings database
                viewModel.toggleSetting("wifi_only", !isWifiOnlyEnabled) // just toggle to sync
                viewModel.proceedToFinished()
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("connect_vault_button"),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Enable Cloud Vault Setup",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// -----------------------------------------------------------------------------
// ONBOARDING SCREEN 5: FINISHED / ONBOARDING COMPLETED
// -----------------------------------------------------------------------------
@Composable
fun OnboardingFinishedScreen(
    viewModel: ClearSpaceViewModel
) {
    var weeklyAutoCleanEnabled by remember { mutableStateOf(true) }
    
    // Simple visual animation scale for the big checkmark
    val scale = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App title
        Text(
            text = "ClearSpace AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Glowing Finished Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(150.dp)
                .scale(scale.value)
                .clip(CircleShape)
                .background(Color(0xFF9FF2E4))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF117166),
                modifier = Modifier.size(72.dp)
            )
        }

        // Headline results
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Your phone now has 71GB free.",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Dynamic storage optimization completed successfully. Your system is now running 100% optimized.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shrinking animation reference representation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Clean-up Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duplicate Photos Removed", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("32 GB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Compressive Optimization", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12 GB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cloud Offloading Saves", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("23.4 GB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppOutlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Space Recovered",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "67.4 GB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF016A60)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Real User Testimonial Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "“My phone had only 2GB left and constantly froze when taking videos. ClearSpace AI found 28GB of duplicate photos, compressed my old videos, and moved unused files to the Cloud Vault. I went from 126GB used to just 59GB used in under 15 minutes. I didn’t lose a single important file.”",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "— Sarah M., Chicago",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.completeOnboarding() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
                    .testTag("enable_weekly_auto_clean_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Enable Weekly Auto-Clean",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Skip to Dashboard",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { viewModel.completeOnboarding() }
                    .padding(8.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 1: DASHBOARD / HOME
// -----------------------------------------------------------------------------
@Composable
fun HomeScreenTab(
    viewModel: ClearSpaceViewModel,
    items: List<StorageItem>
) {
    // Dynamic size calculations based on live DB entries to make it incredibly interactive!
    val activePhotosSize = items.filter { it.type == "PHOTO" && !it.isDeleted && !it.isOffloaded }.sumOf { it.sizeBytes }
    val activeVideosSize = items.filter { it.type == "VIDEO" && !it.isDeleted && !it.isOffloaded }.sumOf { it.sizeBytes }
    val activeJunkSize = items.filter { it.type == "JUNK" && !it.isDeleted && !it.isOffloaded }.sumOf { it.sizeBytes }
    val activeDownloadsSize = items.filter { it.type == "DOWNLOAD" && !it.isDeleted && !it.isOffloaded }.sumOf { it.sizeBytes }

    val totalActiveCustomFiles = activePhotosSize + activeVideosSize + activeJunkSize + activeDownloadsSize
    
    // Baseline representation size of unremovable files (so we don't drop to 0)
    val baselineStorage = 53_650_000_000L // 53.65 GB fixed
    val totalUsedStorage = baselineStorage + totalActiveCustomFiles
    val totalUsedGb = totalUsedStorage.toDouble() / 1_000_000_000.0
    val totalAvailableGb = 128.0
    val totalRecoverableGb = totalActiveCustomFiles.toDouble() / 1_000_000_000.0

    val decimalFormat = DecimalFormat("#.#")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // App top identity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ClearSpace AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Subscribed / Profile Indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Storage Status Card (Clean Utility / Minimal)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Circular Progress Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(96.dp)
                ) {
                    val scalePercentage = (totalUsedGb / totalAvailableGb).toFloat().coerceIn(0f, 1f)
                    val percentageText = "${(scalePercentage * 100).toInt()}%"
                    
                    val trackColor = MaterialTheme.colorScheme.outlineVariant
                    val progressColor = if (scalePercentage > 0.85f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw outer clean track circle
                        drawCircle(
                            color = trackColor,
                            radius = size.minDimension / 2 - 8.dp.toPx(),
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Draw active progress sweep
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * scalePercentage,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(
                                width = size.width - 16.dp.toPx(),
                                height = size.height - 16.dp.toPx()
                            ),
                            topLeft = Offset(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = percentageText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "FULL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Details Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Storage Status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${decimalFormat.format(totalUsedGb)}GB used",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "of ${totalAvailableGb.toInt()}GB total",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Critical or Optimized Pill status
                    if (totalRecoverableGb > 0.1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "OPTIMIZED",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recovery Estimate Banner (Clean Utility / Minimal style)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            val hasRecoverable = totalRecoverableGb > 0.1
            Text(
                text = if (hasRecoverable) "AI ANALYSIS COMPLETE" else "MONITORING ACTIVE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasRecoverable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${decimalFormat.format(totalRecoverableGb)}GB",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasRecoverable) "Recoverable" else "Optimized",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center primary "Clean My Phone" button
        Button(
            onClick = { viewModel.triggerMainScan() },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(56.dp)
                .testTag("clean_my_phone_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Broom",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Clean My Phone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Panel cards list
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Duplicate Photos
            CategorySummaryCard(
                categoryName = "Duplicate Photos",
                recoverableSize = "${decimalFormat.format(activePhotosSize.toDouble() / 1_000_000_000.0)}GB recoverable",
                metadata = "${items.filter { it.type == "PHOTO" && !it.isDeleted && !it.isOffloaded }.size} items",
                icon = Icons.Default.Share,
                colorTint = MaterialTheme.colorScheme.primary,
                onClick = { viewModel.selectTab("files") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Large Videos
            CategorySummaryCard(
                categoryName = "Large Videos",
                recoverableSize = "${decimalFormat.format(activeVideosSize.toDouble() / 1_000_000_000.0)}GB recoverable",
                metadata = "${items.filter { it.type == "VIDEO" && !it.isDeleted && !it.isOffloaded }.size} files",
                icon = Icons.Default.PlayArrow,
                colorTint = MaterialTheme.colorScheme.secondary,
                onClick = { viewModel.selectTab("files") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Junk & Cache File button card
            CategorySummaryCard(
                categoryName = "App Cache & Junk",
                recoverableSize = if (activeJunkSize > 0) "${activeJunkSize / (1024 * 1024)}MB recoverable" else "0MB recoverable",
                metadata = "Temporary files and cache",
                icon = Icons.Default.Delete,
                colorTint = MaterialTheme.colorScheme.tertiary,
                trailingButton = if (activeJunkSize > 0) "Safe to Clear" else null,
                onTrailingClick = { viewModel.cleanJunkFiles() },
                onClick = { viewModel.selectTab("files") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Unused Downloads card
            CategorySummaryCard(
                categoryName = "Unused Downloads",
                recoverableSize = "${decimalFormat.format(activeDownloadsSize.toDouble() / 1_000_000_000.0)}GB recoverable",
                metadata = "Files untouched for 180+ days",
                icon = Icons.Default.List,
                colorTint = MaterialTheme.colorScheme.outline,
                onClick = { viewModel.selectTab("files") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CategorySummaryCard(
    categoryName: String,
    recoverableSize: String,
    metadata: String,
    icon: ImageVector,
    colorTint: Color,
    trailingButton: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Leading Icon Container - Clean utility styling
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colorTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = categoryName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recoverableSize,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorTint
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = metadata,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Trailing action with Clean Utility color pill
            if (trailingButton != null) {
                Button(
                    onClick = { onTrailingClick?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("safe_to_clear_button")
                ) {
                    Text(
                        text = trailingButton,
                        color = Color(0xFF117166),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: FILES (DETAILED REVIEW LIST)
// -----------------------------------------------------------------------------
@Composable
fun FilesScreenTab(
    viewModel: ClearSpaceViewModel,
    items: List<StorageItem>
) {
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "PHOTO", "VIDEO", "DOWNLOAD"

    val activePhotos = items.filter { it.type == "PHOTO" && !it.isDeleted && !it.isOffloaded }
    val activeVideos = items.filter { it.type == "VIDEO" && !it.isDeleted && !it.isOffloaded }
    val activeDownloads = items.filter { it.type == "DOWNLOAD" && !it.isDeleted && !it.isOffloaded }
    
    val totalAvailableToClean = activePhotos.sumOf { it.sizeBytes } + activeVideos.sumOf { it.sizeBytes } + activeDownloads.sumOf { it.sizeBytes }
    val decimalFormat = DecimalFormat("#.#")
    val totalAvailableGb = totalAvailableToClean.toDouble() / 1_000_000_000.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Title
        Text(
            text = "Review & Clean Files",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Filter chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(selected = selectedCategory == "ALL", text = "All Files", onClick = { selectedCategory = "ALL" })
            FilterChipItem(selected = selectedCategory == "PHOTO", text = "Duplicate Photos", onClick = { selectedCategory = "PHOTO" })
            FilterChipItem(selected = selectedCategory == "VIDEO", text = "Large Videos", onClick = { selectedCategory = "VIDEO" })
            FilterChipItem(selected = selectedCategory == "DOWNLOAD", text = "Unused Downloads", onClick = { selectedCategory = "DOWNLOAD" })
        }

        val displayedItems = remember(items, selectedCategory) {
            items.filter {
                !it.isDeleted && !it.isOffloaded && (selectedCategory == "ALL" || it.type == selectedCategory) && it.type != "JUNK"
            }
        }

        // Checklist of files
        if (displayedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF016A60),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Category Clean!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Secure local files look clean and thin.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillClassName("custom_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedItems) { item ->
                    FileChecklistItem(
                        item = item,
                        onDelete = { viewModel.deleteItemById(item.id) },
                        onOffload = { viewModel.updateItem(item.copy(isOffloaded = true)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions bottom bar
        if (totalAvailableToClean > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recoverable today",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${decimalFormat.format(totalAvailableGb)} GB",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedCategory == "ALL") viewModel.cleanAll()
                            else viewModel.deleteItemsByType(selectedCategory)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("section_deep_clean_button")
                    ) {
                        Text(
                            text = if (selectedCategory == "ALL") "Clean All" else "Clean Section",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Extension utilities removed because direct viewModel bindings are used instead

@Composable
fun FilterChipItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else AppSurfaceContainerHigh)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FileChecklistItem(
    item: StorageItem,
    onDelete: () -> Unit,
    onOffload: () -> Unit
) {
    val sizeText = remember(item.sizeBytes) {
        val sizeMb = item.sizeBytes.toDouble() / 1_000_000.0
        if (sizeMb >= 1000.0) {
            DecimalFormat("#.#").format(sizeMb / 1000.0) + " GB"
        } else {
            sizeMb.toInt().toString() + " MB"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                // Icon Type
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (item.type) {
                                "PHOTO" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                "VIDEO" -> Color(0xFF016A60).copy(alpha = 0.1f)
                                else -> Color(0xFF717782).copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.type) {
                            "PHOTO" -> Icons.Default.Share
                            "VIDEO" -> Icons.Default.PlayArrow
                            else -> Icons.Default.List
                        },
                        contentDescription = null,
                        tint = when (item.type) {
                            "PHOTO" -> MaterialTheme.colorScheme.primary
                            "VIDEO" -> Color(0xFF016A60)
                            else -> Color(0xFF717782)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(
                            text = sizeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (item.fileCount > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${item.fileCount} items)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Clean & Move actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Move to Vault Action
                IconButton(
                    onClick = onOffload,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Offload",
                        tint = Color(0xFF016A60),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Permanent Delete Action
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 3: CLOUD VAULT
// -----------------------------------------------------------------------------
@Composable
fun VaultScreenTab(
    viewModel: ClearSpaceViewModel,
    vaultedItems: List<StorageItem>
) {
    val totalVaultStorageBytes = 64_000_000_000L // 64 GB
    val usedVaultBytes = vaultedItems.sumOf { it.sizeBytes }
    val totalAvailableGb = (totalVaultStorageBytes - usedVaultBytes).toDouble() / 1_000_000_000.0
    val decimalFormat = DecimalFormat("#.#")
    
    val fillPercent = (usedVaultBytes.toDouble() / totalVaultStorageBytes.toDouble()).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // identity
        Text(
            text = "Your Secure Cloud Vault",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Encrypted, AI-managed offsite storage.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Circle cloud loader
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(modifier = Modifier.size(160.dp)) {
                drawArc(
                    color = primaryColor.copy(alpha = 0.1f),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(width = size.width, height = size.height)
                )
                drawArc(
                    color = Color(0xFF016A60),
                    startAngle = 140f,
                    sweepAngle = 260f * if (fillPercent > 0f) fillPercent else 0.05f, // show slim loop at least
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                    size = Size(width = size.width, height = size.height)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${if (fillPercent > 0) (fillPercent * 100).toInt() else 12}%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF016A60),
                    lineHeight = 36.sp
                )
                Text(
                    text = "Used",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // GB available
        Text(
            text = "${decimalFormat.format(64.0 - (usedVaultBytes.toDouble() / 1_000_000_000.0))}GB of 64GB available",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Next tier: 128GB Pro Plan",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Buttons Row upload/restore
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.uploadFileToVault("Backup_Data_Archive.zip", 1400_000_000L) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("upload_vault_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Files", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.restoreAllVaultItems() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("restore_vault_button"),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore All", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Auto offloading
        var isAutoOffloadEnabled by remember { mutableStateOf(true) }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurfaceContainerLow),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF016A60).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF016A60),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Offload: Enabled",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Moves old files to vault automatically",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isAutoOffloadEnabled,
                    onCheckedChange = { isAutoOffloadEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recently Offloaded Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently Offloaded",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "See All",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recently Offloaded List File layout
        if (vaultedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files vaulted. Tap Upload to offload.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                vaultedItems.take(4).forEach { item ->
                    VaultFileItemRow(item = item, onRestore = { viewModel.restoreItemFromVault(item) })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Green Pro Tip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEDF1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF117166),
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF9FF2E4), CircleShape)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Pro Tip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF117166)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "You saved 12GB this week by offloading unused videos to the Vault.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun VaultFileItemRow(
    item: StorageItem,
    onRestore: () -> Unit
) {
    val sizeText = remember(item.sizeBytes) {
        val sizeMb = item.sizeBytes.toDouble() / 1_000_000.0
        if (sizeMb >= 1000.0) DecimalFormat("#.#").format(sizeMb / 1000.0) + " GB"
        else sizeMb.toInt().toString() + " MB"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Static image thumbnail placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$sizeText • 2 hours ago",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Restore action button
            TextButton(
                onClick = onRestore,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Restore",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 4: SETTINGS MATCHING IMAGE 5
// -----------------------------------------------------------------------------
@Composable
fun SettingsScreenTab(
    viewModel: ClearSpaceViewModel
) {
    // Collect setting properties safely
    var weeklyAutoClean by remember { mutableStateOf(true) }
    var safeDeleteLearning by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(false) }
    var wifiOnlyUploads by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // identity
        Text(
            text = "Settings & Automation",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // User Alex Rivera Pro Subscriber card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar Box
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Alex Rivera",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Alex Rivera",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Subscribed",
                                tint = Color(0xFF016A60),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pro Plan Subscriber",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF016A60)
                            )
                        }
                    }
                }

                // Edit Pencil
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SMART AUTOMATION header title
        Text(
            text = "SMART AUTOMATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            letterSpacing = 1.sp
        )

        // Smart Automation Card Panel with switches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Row 1: Weekly Auto-Clean
                SettingsSwitchRow(
                    title = "Weekly Auto-Clean",
                    subtitle = "Schedule maintenance automatically",
                    checked = weeklyAutoClean,
                    onCheckedChange = { weeklyAutoClean = it },
                    icon = Icons.Default.CheckCircle
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Row 2: AI Safe Delete Learning
                SettingsSwitchRow(
                    title = "AI Safe Delete Learning",
                    subtitle = "Improve AI accuracy over time",
                    checked = safeDeleteLearning,
                    onCheckedChange = { safeDeleteLearning = it },
                    icon = Icons.Default.Star
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Row 3: Notifications
                SettingsSwitchRow(
                    title = "Notifications",
                    subtitle = "Alerts for heavy storage usage",
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    icon = Icons.Default.Notifications
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Row 4: Wi-Fi Only Uploads
                SettingsSwitchRow(
                    title = "Wi-Fi Only Uploads",
                    subtitle = "Save mobile data on cloud sync",
                    checked = wifiOnlyUploads,
                    onCheckedChange = { wifiOnlyUploads = it },
                    icon = Icons.Default.Warning
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SUPPORT & SAFETY section header
        Text(
            text = "SUPPORT & SAFETY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            letterSpacing = 1.sp
        )

        // Accordion questions list card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, AppOutlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Accordion Item 1
                SettingsAccordionItem(
                    title = "Frequently Asked Questions",
                    icon = Icons.Default.Info,
                    detailText = "How do I recover space? Tap started scan. ClearSpace AI identifies and lists details about cached files, identical photographs, duplicate videos, or downloaded bundles immediately. Choose what to clear or select 'Clean Everything'."
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppOutlineVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Accordion Item 2
                SettingsAccordionItem(
                    title = "Is it really free?",
                    icon = Icons.Default.CheckCircle,
                    detailText = "Yes. Every core feature of ClearSpace AI is completely free. There are absolutely no locked tools, dynamic updates, secondary hidden charges, or trailing subscriptions required to recover storage."
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppOutlineVariant.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Accordion Item 3
                SettingsAccordionItem(
                    title = "Data Safety & Encryption",
                    icon = Icons.Default.Lock,
                    detailText = "Your security is our absolute highest priority. Files synchronized to the Secure Cloud Vault are fully encrypted both during upload transport and off-site cloud storage. Only you hold authorization keys to access or pull them."
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Reset App Developer state
        Button(
            onClick = { viewModel.resetMainApp() },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(48.dp)
                .testTag("reset_app_developer_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Reset Application (Onboarding Mode)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // V1.0 Footer copyright labels
        Text(
            text = "CLEARSPACE AI V1.0",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Optimizing digital footprints since 2024",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsAccordionItem(
    title: String,
    icon: ImageVector,
    detailText: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = detailText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 48.dp),
                lineHeight = 18.sp
            )
        }
    }
}

// Custom selector classes to mimic specific Stitch layout requirements
private fun Modifier.fillClassName(className: CharSequence) = this
