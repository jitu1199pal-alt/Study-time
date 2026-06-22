package com.example

import android.app.Activity
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var prefs: StudyBlockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = StudyBlockPreferences(applicationContext)

        // Initialize Google Mobile Ads SDK (AdMob)
        AdmobManager.initialize(applicationContext)

        // Ask for runtime notification permissions on Android 13+ (required for stable foreground services)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Find if navigated directly from Block screen to the Study Tab
        val startInStudyTab = intent.getBooleanExtra("navigate_to_study", false)

        setContent {
            MyApplicationTheme {
                MainContainerScreen(
                    prefs = prefs,
                    startInStudyTab = startInStudyTab,
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

// Data class to easily list and visualize installed mobile apps
data class AppInfoModel(
    val label: String,
    val packageName: String,
    val isSystem: Boolean,
    val isChecked: Boolean = false,
    val icon: android.graphics.drawable.Drawable? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    prefs: StudyBlockPreferences,
    startInStudyTab: Boolean,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // Exactly 3 tabs: 0 -> Dashboard/Schedules, 1 -> Apps, 2 -> Help/Info
    var showBreakPopup by remember { mutableStateOf(false) }
    var tempBreakMinutes by remember { mutableStateOf(15f) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showAccessibilityConsentDialog by remember { mutableStateOf(false) }
    var activeAdState by remember { mutableStateOf<ActiveAdData?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }

    val activity = context as? Activity
    val triggerAdFlow: (Int) -> Unit = { minutes ->
        if (activity != null) {
            isAdLoading = true
            AdmobManager.loadInterstitial(
                context = context,
                adUnitId = null,
                onLoaded = { interstitialAd ->
                    isAdLoading = false
                    AdmobManager.showInterstitial(activity, interstitialAd) {
                        prefs.startEmergencyBreak(minutes)
                        isBlockActive = prefs.isBlockerActiveRightNow()
                        breakRemainingMinutes = prefs.getBreakRemainingMinutes()
                    }
                },
                onFailed = { loadError ->
                    isAdLoading = false
                    val adSec = if (minutes <= 20) 10 else if (minutes <= 30) 15 else 45
                    activeAdState = ActiveAdData(
                        adDuration = adSec,
                        adSecsRemaining = adSec,
                        pendingBreakMinutes = minutes,
                        adKey = (0..2).random()
                    )
                }
            )
        } else {
            val adSec = if (minutes <= 20) 10 else if (minutes <= 30) 15 else 45
            activeAdState = ActiveAdData(
                adDuration = adSec,
                adSecsRemaining = adSec,
                pendingBreakMinutes = minutes,
                adKey = (0..2).random()
            )
        }
    }
    
    // Live states
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)) }
    var isBlockActive by remember { mutableStateOf(prefs.isBlockerActiveRightNow()) }
    var breakRemainingMinutes by remember { mutableStateOf(prefs.getBreakRemainingMinutes()) }
    var studyAppsCount by remember { mutableStateOf(getInstalledStudyAppsCount(context, prefs)) }

    var currentTimeString by remember { mutableStateOf("") }
    var currentDayOfWeekString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(activeAdState != null) {
        if (activeAdState != null) {
            while (activeAdState != null && activeAdState!!.adSecsRemaining > 0) {
                delay(1000)
                val current = activeAdState
                if (current != null) {
                    activeAdState = current.copy(adSecsRemaining = current.adSecsRemaining - 1)
                }
            }
        }
    }

    // Live clock ticking and state polling
    LaunchedEffect(Unit) {
        while (true) {
            isServiceEnabled = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
            isBlockActive = prefs.isBlockerActiveRightNow()
            breakRemainingMinutes = prefs.getBreakRemainingMinutes()
            studyAppsCount = getInstalledStudyAppsCount(context, prefs)

            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR)
            val minute = cal.get(Calendar.MINUTE)
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.PM) "PM" else "AM"
            val h12 = if (hour == 0) 12 else hour
            currentTimeString = String.format("%02d:%02d %s", h12, minute, amPm)

            val day = cal.get(Calendar.DAY_OF_MONTH)
            val year = cal.get(Calendar.YEAR)
            val months = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            val monthName = months[cal.get(Calendar.MONTH)]
            currentDateString = String.format("%02d %s %d", day, monthName, year)

            val daysOfWeekH = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            currentDayOfWeekString = daysOfWeekH[cal.get(Calendar.DAY_OF_WEEK) - 1]

            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentTimeString,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Day: $currentDayOfWeekString",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981) // Matching preview emerald green color
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Date: $currentDateString",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showBreakPopup = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981) // Emerald Green to match the preview Exactly
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Break",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Take Break",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Moved from bottom to TOP of the APK
                    NavigationBar(
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(56.dp)
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Apps") },
                            label = { Text("Add Apps", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Info, contentDescription = "Help") },
                            label = { Text("Help", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AdmobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(
                    prefs = prefs,
                    isServiceEnabled = isServiceEnabled,
                    isBlockActive = isBlockActive,
                    breakRemainingMinutes = breakRemainingMinutes,
                    studyAppsCount = studyAppsCount,
                    onOpenSettings = { showPermissionsDialog = true },
                    onStatusRefresh = {
                        isServiceEnabled = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
                        isBlockActive = prefs.isBlockerActiveRightNow()
                        breakRemainingMinutes = prefs.getBreakRemainingMinutes()
                    },
                    onTriggerAd = triggerAdFlow
                )
                1 -> AppSelectorTab(
                    prefs = prefs,
                    onAppsUpdated = { studyAppsCount = getInstalledStudyAppsCount(context, prefs) }
                )
                2 -> InfoTab()
            }
        }
    }

    if (showBreakPopup) {
        AlertDialog(
            onDismissRequest = { showBreakPopup = false },
            title = {
                Text(
                    text = "Choose Break Duration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "During your emergency break, all mobile apps will be unlocked. Select a break duration from 5 to 60 minutes:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "${tempBreakMinutes.toInt()} Minutes",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Slider(
                        value = tempBreakMinutes,
                        valueRange = 5f..60f,
                        steps = 10,
                        onValueChange = { tempBreakMinutes = (Math.round(it / 5.0) * 5).toFloat() }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBreakPopup = false
                    triggerAdFlow(tempBreakMinutes.toInt())
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakPopup = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPermissionsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionsDialog = false },
            title = {
                Text(
                    text = "App Permissions Needed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Please grant the following permissions for the app to function properly. These are only used local-on-device to block distracting apps according to your rules:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Check live status of permissions
                    var isAccessibilityOn by remember {
                        mutableStateOf(isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java))
                    }
                    var isOverlayOn by remember {
                        mutableStateOf(if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            Settings.canDrawOverlays(context)
                        } else {
                            true
                        })
                    }
                    var isBatteryOn by remember {
                        mutableStateOf(if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
                        } else {
                            true
                        })
                    }
                    var isNotificationOn by remember {
                        mutableStateOf(if (android.os.Build.VERSION.SDK_INT >= 33) {
                            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        })
                    }

                    // Poll status periodically while dialog is visible so clicking setting dynamically updates the UI
                    LaunchedEffect(showPermissionsDialog) {
                        while (showPermissionsDialog) {
                            isAccessibilityOn = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
                            isOverlayOn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                Settings.canDrawOverlays(context)
                            } else {
                                true
                            }
                            isBatteryOn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
                            } else {
                                true
                            }
                            isNotificationOn = if (android.os.Build.VERSION.SDK_INT >= 33) {
                                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            } else {
                                true
                            }
                            delay(1000)
                        }
                    }

                    // Permission 0: App Info / Allow Restricted Settings Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "App Information Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Recommended Setup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "If you encounter a 'Restricted Settings' error when enabling the Accessibility Service, configure it via the three-dot menu in App Info.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // fallback in case of context resolve issues
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Open App Info Settings",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Permission 1: Accessibility Service Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Accessibility Service Permit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isAccessibilityOn) "Enabled (ON)" else "Disabled (OFF)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAccessibilityOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Required to identify when a blacklisted app or non-study app is opened so the lock interface can overlay.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (isAccessibilityOn) {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        context.startActivity(intent)
                                    } else {
                                        showAccessibilityConsentDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAccessibilityOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isAccessibilityOn) "View Settings" else "Enable Service",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Permission 2: Draw Over Other Apps Overlay Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Display Over Other Apps (Overlay)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isOverlayOn) "Granted (ON)" else "Denied (OFF)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOverlayOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Required to display the blocking screen and prevent access to distracting apps instantly.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        try {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                            context.startActivity(intent)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOverlayOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isOverlayOn) "View Settings" else "Enable Permission",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Permission 3: Battery Optimization bypass Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Battery Optimization (Battery Saver)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isBatteryOn) "Ignored (ON)" else "Optimizing (OFF)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isBatteryOn) Color(0xFF10B981) else Color(0xFFEAB308)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prevents Android from stopping your study blocker background service to save power (Google Play policy compliant).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                        try {
                                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBatteryOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isBatteryOn) "View Settings" else "Ignore Optimization",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Permission 4: Notification Card (Android 13+)
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Notification Permission",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isNotificationOn) "Granted (ON)" else "Denied (OFF)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isNotificationOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Required to show live blocking timers and persistent schedule status in your drawer.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNotificationOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (isNotificationOn) "View Settings" else "Enable Notifications",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPermissionsDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showAccessibilityConsentDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityConsentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Consent Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Prominent Disclosure (निजता घोषणा)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .maxHeight(300.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "English Required Disclosure:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "This application uses the Accessibility Service API purely to detect on-device when distracting or block-listed applications are launched or brought to the foreground during your Scheduled Study Focus Times.\n\n" +
                               "Why do we need this permit?\n" +
                               "1. To identify the active package name on your screen.\n" +
                               "2. To immediately minimize distracting apps and display the focus/study lock screen layout to protect your learning schedule.\n\n" +
                               "Data Collection & Privacy Policy Pledge:\n" +
                               "• We never collect, monitor, store, or transmit any of your personal data, web browser history, or usage records online or offline. Every check occurs strictly locally on your device for functional blocking only. No background telemetry or logs are ever uploaded.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "हिंदी महत्वपूर्ण निजता घोषणा:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "यह ऐप आपके निर्धारित अध्ययन के समय विचलित करने वाले ऐप्स को ब्लॉक करने के लिए Accessibility Service API (पहुंच सेवा) का उपयोग करता है।\n\n" +
                               "हमें इसकी आवश्यकता क्यों है?\n" +
                               "1. स्क्रीन पर सक्रिय ऐप के नाम (package name) की पहचान करने के लिए।\n" +
                               "2. ताकि जब आप कोई ध्यान भटकाने वाला ऐप खोलें, यह उसे तुरंत बंद करके स्टडी लॉक स्क्रीन दिखा सके।\n\n" +
                               "डेटा निजता और सुरक्षा प्रतिज्ञा:\n" +
                               "• हम आपका कोई भी निजी डेटा, इतिहास, या सर्च रिकॉर्ड न तो संग्रहित करते हैं और न ही किसी सर्वर पर भेजते हैं। सब कुछ सुरक्षित रूप से आपके फोन के अंदर ही संचालित होता है।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityConsentDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Handler
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981) // Emerald Consent Green
                    )
                ) {
                    Text("I Agree (सहमत हूँ)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAccessibilityConsentDialog = false }
                ) {
                    Text("No, Thanks (असहमत हूँ)")
                }
            }
        )
    }

    if (isAdLoading) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Loading Secure Ad...", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Connecting to AdMob servers to launch your break options...")
                }
            },
            confirmButton = {}
        )
    }

    activeAdState?.let { ad ->
        SimulatedAdOverlay(
            ad = ad,
            onFinishAd = {
                prefs.startEmergencyBreak(ad.pendingBreakMinutes)
                activeAdState = null
                isBlockActive = prefs.isBlockerActiveRightNow()
                breakRemainingMinutes = prefs.getBreakRemainingMinutes()
            },
            onAdRedirectMessage = { msg ->
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun DashboardTab(
    prefs: StudyBlockPreferences,
    isServiceEnabled: Boolean,
    isBlockActive: Boolean,
    breakRemainingMinutes: Int,
    studyAppsCount: Int,
    onOpenSettings: () -> Unit,
    onStatusRefresh: () -> Unit,
    onTriggerAd: (Int) -> Unit
) {
    var showBreakPopupAtDashboard by remember { mutableStateOf(false) }
    var dashboardBreakMinutes by remember { mutableStateOf(15f) }

    var isScheduleEnabled by remember { mutableStateOf(prefs.isScheduleEnabled) }
    var slots by remember { mutableStateOf(prefs.getTimeSlots()) }
    var editingSlotId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(isScheduleEnabled) {
        slots = prefs.getTimeSlots()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Essential accessibility warning card if permission not granted
        if (!isServiceEnabled) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x1FFF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Accessibility Service is Disabled",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enabling the Accessibility Service is absolutely required to block non-study apps. Please click the button below to turn it on.",
                            fontSize = 13.sp,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enable Accessibility Service", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Lock Core Status display
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBlockActive) Color(0x1F38BDF8) else Color(0x1F10B981)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isBlockActive) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isBlockActive) "Strict Study Lock is Active" else "Sandbox Mode (Unlocked)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isBlockActive) Color(0xFF38BDF8) else Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBlockActive) "Block mode is currently active: normal apps are closed" else "No restrictions active right now",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$studyAppsCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Allowed Study Apps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val activeSlotsCount = slots.filter { it.isEnabled }.size
                            val scheduleText = if (isScheduleEnabled) {
                                "$activeSlotsCount Active"
                            } else {
                                "Disabled"
                            }
                            Text(text = scheduleText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Active Schedules", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Break active banner
        if (breakRemainingMinutes > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F10B981)),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = "Break Active", tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Emergency Break: $breakRemainingMinutes mins left",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                fontSize = 13.sp
                            )
                        }
                        
                        OutlinedButton(
                            onClick = {
                                prefs.cancelEmergencyBreak()
                                onStatusRefresh()
                            },
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lock Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Table master toggle
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Enable Schedule Lock", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(text = "Enable scheduled blocking mode", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isScheduleEnabled,
                        onCheckedChange = {
                            isScheduleEnabled = it
                            prefs.isScheduleEnabled = it
                            onStatusRefresh()
                        }
                    )
                }
            }
        }

        // Table Header
        item {
            Text(
                text = "7-Slot Study Schedule Table",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Time slots list
        items(slots) { slot ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingSlotId = slot.id }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Schedule #${slot.id}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (slot.isEnabled && isScheduleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (slot.isEnabled && isScheduleEnabled) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Switch(
                            checked = slot.isEnabled,
                            onCheckedChange = { checked ->
                                val updated = slot.copy(isEnabled = checked)
                                prefs.saveTimeSlot(updated)
                                slots = prefs.getTimeSlots()
                                onStatusRefresh()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startStr = formatSlotTime12H(slot.startHour, slot.startMinute)
                        val endStr = formatSlotTime12H(slot.endHour, slot.endMinute)
                        Text(
                            text = "Duration: $startStr - $endStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (slot.isEnabled && isScheduleEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Time Slot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Emergency Break Presets Inside Dashboard Tab
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency Break Control",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instantly take a 5-60 minute temporary break during which all personal non-study apps are allowed.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intervals = listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)
                        intervals.forEach { min ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onTriggerAd(min)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$min Min",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(text = "Break", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showBreakPopupAtDashboard = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Break Icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Custom Break Duration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Premium Real AdMob Banner Ad
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sponsor Advertisement",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    AdmobBanner(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (showBreakPopupAtDashboard) {
        AlertDialog(
            onDismissRequest = { showBreakPopupAtDashboard = false },
            title = { Text("Custom Break Duration", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select the duration (5 to 60 minutes) to temporarily unlock all mobile apps:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${dashboardBreakMinutes.toInt()} Minutes",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = dashboardBreakMinutes,
                        valueRange = 5f..60f,
                        steps = 10,
                        onValueChange = { dashboardBreakMinutes = (Math.round(it / 5.0) * 5).toFloat() }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onTriggerAd(dashboardBreakMinutes.toInt())
                    showBreakPopupAtDashboard = false
                }) {
                    Text("Take Break")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakPopupAtDashboard = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (editingSlotId != null) {
        val slotToEdit = slots.find { it.id == editingSlotId }
        if (slotToEdit != null) {
            TimeSlotEditDialog(
                slot = slotToEdit,
                onDismiss = { editingSlotId = null },
                onSave = { updatedSlot ->
                    prefs.saveTimeSlot(updatedSlot)
                    slots = prefs.getTimeSlots()
                    onStatusRefresh()
                    editingSlotId = null
                }
            )
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How It Works (Instructions)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bulletPoints = listOf(
                        "1. **Select Apps**: Go to the 'Add Apps' tab and select the apps you need for studying (e.g., NCERT, YouTube, etc.).",
                        "2. **7 Time slots**: Configure up to 7 persistent time slots based on your study habits and daily routines.",
                        "3. **Lock Activation**: During scheduled times, only your allowed study apps are accessible, while distracting apps are blocked automatically.",
                        "4. **Emergency Breaks**: If you need to access other apps urgently, take a 5-60 minute ad-supported break from the top right menu to unlock all apps temporarily."
                    )
                    
                    bulletPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = point,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy Policy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔒 Security & Data Privacy Policy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand/Collapse",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This app is 100% offline and secure. We do not transmit, collect, or store any of your personal or browsing data on external servers.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1. Accessibility Service API:\nThis permission is used solely to identify the package names of active on-screen apps in real-time, allowing us to block distracting apps during study hours.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2. 100% Offline Integrity:\nAll layouts, schedules, and settings are fully persisted on-device. No telemetry, statistics, or logs are shared or synced with the cloud.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "3. Google Play Store Compliance:\nOur product complies with the Google Play Developer Distribution Agreement regarding sensitive user data and accessibility APIs.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view full disclosures...",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSelectorTab(
    prefs: StudyBlockPreferences,
    onAppsUpdated: () -> Unit
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfoModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showOnlySelected by remember { mutableStateOf(false) }

    // Query package manager inside a coroutine background thread
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        
        val apps = resolveInfos.map { resolveInfo ->
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val label = appInfo.loadLabel(pm).toString()
            val packageName = appInfo.packageName
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = try {
                appInfo.loadIcon(pm)
            } catch (e: Exception) {
                null
            }
            
            AppInfoModel(
                label = label,
                packageName = packageName,
                isSystem = isSystem,
                isChecked = prefs.isStudyApp(packageName),
                icon = icon
            )
        }.distinctBy { it.packageName }.sortedBy { it.label }

        installedApps = apps
        isLoading = false
    }

    val filteredApps = installedApps.filter {
        (it.label.contains(searchQuery, ignoreCase = true) || 
        it.packageName.contains(searchQuery, ignoreCase = true)) &&
        (!showOnlySelected || it.isChecked)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Select All / Deselect All / Study App Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = {
                    installedApps = installedApps.map { it.copy(isChecked = true) }
                    installedApps.forEach { prefs.addStudyApp(it.packageName) }
                    onAppsUpdated()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Select All", modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(3.dp))
                Text("Select All", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            OutlinedButton(
                onClick = {
                    installedApps = installedApps.map { it.copy(isChecked = false) }
                    installedApps.forEach { prefs.removeStudyApp(it.packageName) }
                    onAppsUpdated()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Deselect All", modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(3.3.dp))
                Text("Deselect All", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = {
                    showOnlySelected = !showOnlySelected
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showOnlySelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (showOnlySelected) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = "Study App Only",
                    modifier = Modifier.size(13.dp),
                    tint = if (showOnlySelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Study Apps Only",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showOnlySelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlySelected) {
                            "No study apps selected yet!\nPlease select apps above."
                        } else {
                            "No apps found matching search query."
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newChecked = !app.isChecked
                                installedApps = installedApps.map {
                                    if (it.packageName == app.packageName) it.copy(isChecked = newChecked) else it
                                }
                                if (newChecked) {
                                    prefs.addStudyApp(app.packageName)
                                } else {
                                    prefs.removeStudyApp(app.packageName)
                                }
                                onAppsUpdated()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                val bitmap = remember(app.packageName) {
                                    try {
                                        app.icon.toBitmap(
                                            width = 120,
                                            height = 120,
                                            config = android.graphics.Bitmap.Config.ARGB_8888
                                        ).asImageBitmap()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "${app.label} Icon",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = app.label, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = app.packageName, 
                                    fontSize = 11.sp, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                    overflow = TextOverflow.Ellipsis, 
                                    maxLines = 1
                                )
                            }
                            Checkbox(
                                checked = app.isChecked,
                                onCheckedChange = { isChecked ->
                                    installedApps = installedApps.map {
                                        if (it.packageName == app.packageName) it.copy(isChecked = isChecked) else it
                                    }
                                    if (isChecked) {
                                        prefs.addStudyApp(app.packageName)
                                    } else {
                                        prefs.removeStudyApp(app.packageName)
                                    }
                                    onAppsUpdated()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
}

fun formatSlotTime12H(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return String.format("%02d:%02d %s", h12, minute, amPm)
}

@Composable
fun TimeSlotEditDialog(
    slot: TimeSlot,
    onDismiss: () -> Unit,
    onSave: (TimeSlot) -> Unit
) {
    val initialStartH12 = if (slot.startHour % 12 == 0) 12 else slot.startHour % 12
    val initialStartAmPm = if (slot.startHour >= 12) "PM" else "AM"

    val initialEndH12 = if (slot.endHour % 12 == 0) 12 else slot.endHour % 12
    val initialEndAmPm = if (slot.endHour >= 12) "PM" else "AM"

    var startH12 by remember { mutableStateOf(initialStartH12) }
    var startMin by remember { mutableStateOf(slot.startMinute) }
    var startAmPm by remember { mutableStateOf(initialStartAmPm) }

    var endH12 by remember { mutableStateOf(initialEndH12) }
    var endMin by remember { mutableStateOf(slot.endMinute) }
    var endAmPm by remember { mutableStateOf(initialEndAmPm) }

    val hoursList = (1..12).map { String.format("%02d", it) }
    val minutesList = (0..59).map { String.format("%02d", it) }
    val amPmList = listOf("AM", "PM")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Study Schedule #${slot.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Start Time:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePartDropdown(
                            value = String.format("%02d", startH12),
                            options = hoursList,
                            label = "Hour",
                            onValueChange = { startH12 = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(":", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TimePartDropdown(
                            value = String.format("%02d", startMin),
                            options = minutesList,
                            label = "Minute",
                            onValueChange = { startMin = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        TimePartDropdown(
                            value = startAmPm,
                            options = amPmList,
                            label = "AM/PM",
                            onValueChange = { startAmPm = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column {
                    Text("End Time:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePartDropdown(
                            value = String.format("%02d", endH12),
                            options = hoursList,
                            label = "Hour",
                            onValueChange = { endH12 = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(":", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TimePartDropdown(
                            value = String.format("%02d", endMin),
                            options = minutesList,
                            label = "Minute",
                            onValueChange = { endMin = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        TimePartDropdown(
                            value = endAmPm,
                            options = amPmList,
                            label = "AM/PM",
                            onValueChange = { endAmPm = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalStartHour = if (startAmPm == "PM") {
                    if (startH12 == 12) 12 else startH12 + 12
                } else {
                    if (startH12 == 12) 0 else startH12
                }

                val finalEndHour = if (endAmPm == "PM") {
                    if (endH12 == 12) 12 else endH12 + 12
                } else {
                    if (endH12 == 12) 0 else endH12
                }

                onSave(slot.copy(
                    startHour = finalStartHour,
                    startMinute = startMin,
                    endHour = finalEndHour,
                    endMinute = endMin
                ))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePartDropdown(
    value: String,
    options: List<String>,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun NumberDropdownField(
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }
            if (digits.length <= 2) {
                textValue = digits
                if (digits.isNotEmpty()) {
                    val parsed = digits.toInt()
                    if (parsed in range) {
                        onValueChange(parsed)
                    }
                }
            }
        },
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        modifier = modifier
    )
}

// Helper method to detect if accessibility service permission is active
fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
    val expectedId = context.packageName + "/" + service.canonicalName
    val expectedShortId = context.packageName + "/." + service.simpleName
    val string = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    if (string.isNullOrEmpty()) return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(string)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedId, ignoreCase = true) || 
            componentName.equals(expectedShortId, ignoreCase = true) ||
            componentName.contains(service.simpleName, ignoreCase = true)
        ) {
            return true
        }
    }
    return false
}

fun getInstalledStudyAppsCount(context: Context, prefs: StudyBlockPreferences): Int {
    val pm = context.packageManager
    var count = 0
    for (pkg in prefs.studyApps) {
        try {
            pm.getPackageInfo(pkg, 0)
            count++
        } catch (e: Exception) {
            // Not installed
        }
    }
    return count
}

