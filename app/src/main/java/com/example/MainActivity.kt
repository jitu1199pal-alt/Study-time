package com.example

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
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var prefs: StudyBlockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = StudyBlockPreferences(applicationContext)

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
    val isChecked: Boolean = false
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
    
    // Live states
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)) }
    var isBlockActive by remember { mutableStateOf(prefs.isBlockerActiveRightNow()) }
    var breakRemainingMinutes by remember { mutableStateOf(prefs.getBreakRemainingMinutes()) }
    var studyAppsCount by remember { mutableStateOf(prefs.studyApps.size) }

    var currentTimeString by remember { mutableStateOf("") }
    var currentDayOfWeekString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    // Live clock ticking and state polling
    LaunchedEffect(Unit) {
        while (true) {
            isServiceEnabled = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
            isBlockActive = prefs.isBlockerActiveRightNow()
            breakRemainingMinutes = prefs.getBreakRemainingMinutes()
            studyAppsCount = prefs.studyApps.size

            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR)
            val minute = cal.get(Calendar.MINUTE)
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.PM) "PM" else "AM"
            val h12 = if (hour == 0) 12 else hour
            currentTimeString = String.format("%02d:%02d %s", h12, minute, amPm)

            val day = cal.get(Calendar.DAY_OF_MONTH)
            val year = cal.get(Calendar.YEAR)
            val months = arrayOf("जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर")
            val monthName = months[cal.get(Calendar.MONTH)]
            currentDateString = String.format("%02d %s %d", day, monthName, year)

            val daysOfWeekH = arrayOf("रविवार (Sunday)", "सोमवार (Monday)", "मंगलवार (Tuesday)", "बुधवार (Wednesday)", "गुरुवार (Thursday)", "शुक्रवार (Friday)", "शनिवार (Saturday)")
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
                            text = "वार: $currentDayOfWeekString",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981) // Matching preview emerald green color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "तारीख: $currentDateString",
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
                                text = "Break लें",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("डैशबोर्ड", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Apps") },
                    label = { Text("ऐप्स जोड़ें", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Help") },
                    label = { Text("मदद", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPermissionsDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Permissions Settings"
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
                    onOpenSettings = onOpenSettings,
                    onStatusRefresh = {
                        isServiceEnabled = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
                        isBlockActive = prefs.isBlockerActiveRightNow()
                        breakRemainingMinutes = prefs.getBreakRemainingMinutes()
                    }
                )
                1 -> AppSelectorTab(
                    prefs = prefs,
                    onAppsUpdated = { studyAppsCount = prefs.studyApps.size }
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
                    text = "इमरजेंसी ब्रेक समय चुनें (Choose Break Time)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "चयनित समय के दौरान सभी मोबाइल ऐप्स और गेम्स चालू हो जाएंगे। 1 से 60 मिनट की अवधि चुनें:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Text(
                        text = "${tempBreakMinutes.toInt()} मिनट (Minutes)",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Slider(
                        value = tempBreakMinutes,
                        valueRange = 1f..60f,
                        steps = 58,
                        onValueChange = { tempBreakMinutes = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    prefs.startEmergencyBreak(tempBreakMinutes.toInt())
                    showBreakPopup = false
                    isBlockActive = prefs.isBlockerActiveRightNow()
                    breakRemainingMinutes = prefs.getBreakRemainingMinutes()
                }) {
                    Text("ओके (OK)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakPopup = false }) {
                    Text("रद्द करें (Cancel)")
                }
            }
        )
    }

    if (showPermissionsDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionsDialog = false },
            title = {
                Text(
                    text = "ऐप अनुमतियाँ (App Permissions)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ऐप की सुचारू कार्यप्रणाली के लिए कृपया निम्नलिखित अनुमतियाँ प्रदान करें। ये अनुमतियाँ केवल ऐप्स को ब्लॉक करने के लिए आवश्यक हैं:",
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
                                    text = "एक्सेसिबिलिटी सर्विस (Accessibility)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isAccessibilityOn) "चालू (ON)" else "बंद (OFF)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAccessibilityOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "मॉनिटर करने के लिए कि आपने कौन सा स्टडी या ब्लॉक ऐप खोला है, ताकि ब्लॉक किया जा सके।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAccessibilityOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isAccessibilityOn) "सेटिंग्स देखें (View Settings)" else "अनुमति चालू करें (Enable)",
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
                                    text = "डिस्प्ले ओवर ऐप्स (Overlay)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isOverlayOn) "मंजूर (ON)" else "अस्वीकृत (OFF)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOverlayOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ब्लॉक किए गए ऐप्स को तुरंत कवर करने के लिए ब्लॉक स्क्रीन दिखाने के लिए।",
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
                                    text = if (isOverlayOn) "सेटिंग्स देखें (View Settings)" else "अनुमति चालू करें (Enable)",
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
                                    text = "बैटरी ऑप्टिमाइजेशन (Battery Saver)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isBatteryOn) "अनुकूलित (Ignored)" else "सक्रिय (Optimizing)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isBatteryOn) Color(0xFF10B981) else Color(0xFFEAB308)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "सिस्टम द्वारा पृष्ठभूमि में आपकी ब्लॉक सेवा को बंद होने से बचाने के लिए। (प्ले स्टोर सुरक्षा नियम कंपैटिबल)",
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
                                    text = if (isBatteryOn) "सेटिंग्स देखें (View Settings)" else "अनुकूलन बंद करें (Ignore)",
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
                                        text = "नोटिफिकेशन अनुमति (Notification)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isNotificationOn) "मंजूर (ON)" else "अस्वीकृत (OFF)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isNotificationOn) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "बैकग्राउंड में टाइमर और शेड्यूल के सही संचालन की जानकारी स्टेटस बार में दिखाने के लिए।",
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
                                        text = if (isNotificationOn) "सेटिंग्स देखें (View Settings)" else "अनुमति चालू करें (Enable)",
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
                    Text("ओके (OK)")
                }
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
    onStatusRefresh: () -> Unit
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
                                text = "सर्विस बंद है! (Service Disabled)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "सच्चे लॉक फीचर के लिए Accessibility सर्विस चालू करना बेहद ज़रूरी है। नीचे बटन दबाकर इसे ओन करें।",
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
                            Text("सर्विस चालू करें (Enable Service)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        text = if (isBlockActive) "सुरक्षित पढ़ाई लोक चालू है" else "फ्री मोड (Unlocked)",
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
                            Text(text = "स्टडी ऐप्स", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val activeSlotsCount = slots.filter { it.isEnabled }.size
                            val scheduleText = if (isScheduleEnabled) {
                                "$activeSlotsCount एक्टिव"
                            } else {
                                "बंद"
                            }
                            Text(text = scheduleText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "शेड्यूल समय", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                "इमरजेंसी ब्रेक: $breakRemainingMinutes मिनट बाकी",
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
                            Text("खत्म करें (Lock Now)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        Text(text = "शेड्यूल लॉक सक्रिय करें", fontSize = 16.sp, fontWeight = FontWeight.Black)
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
                text = "7 पढ़ाई शेड्यूल की तालिका (7 Time Slots Table)",
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
                                text = "शेड्यूल #${slot.id}",
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
                                        text = "एक्टिव",
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
                            text = "समय: $startStr से $endStr",
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
                        text = "आपातकालीन ब्रेक (Emergency Break)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "इमरजेंसी होने पर तुरंत 1 से 60 मिनट तक का ब्रेक लें, जिसमें सभी ऐप्स चालू रहेंगे।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intervals = listOf(5, 15, 30, 60)
                        intervals.forEach { min ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        prefs.startEmergencyBreak(min)
                                        onStatusRefresh()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$min Min",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(text = "ब्रेक", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
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
                        Text("1 से 60 मिनट में चुनें (Custom Break)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showBreakPopupAtDashboard) {
        AlertDialog(
            onDismissRequest = { showBreakPopupAtDashboard = false },
            title = { Text("कस्टम ब्रेक समय (Custom Break Duration)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("सभी मोबाइल ऐप्स अनलॉक करने के लिए समय (1 से 60 मिनट) चुनें:")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${dashboardBreakMinutes.toInt()} मिनट (Minutes)",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Slider(
                        value = dashboardBreakMinutes,
                        valueRange = 1f..60f,
                        steps = 58,
                        onValueChange = { dashboardBreakMinutes = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    prefs.startEmergencyBreak(dashboardBreakMinutes.toInt())
                    showBreakPopupAtDashboard = false
                    onStatusRefresh()
                }) {
                    Text("ब्रेक लें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakPopupAtDashboard = false }) {
                    Text("रद्द करें")
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
                            text = "यह कैसे काम करता है? (Instructions)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bulletPoints = listOf(
                        "1. **ऐप्स चयन**: 'ऐप्स जोड़ें' टैब में जाकर अपनी पढ़ाई की जरूरत वाली ऐप्स को सिलेक्ट करें (जैसे NCERT, YouTube)।",
                        "2. **7 टाइम शेड्यूल**: स्टूडेंट अपनी सुविधा के अनुसार 7 टाइम पीरियड सेट कर सकता है।",
                        "3. **लॉक एक्टिवेशन**: शेड्यूल के समय के दौरान मोबाइल में सिर्फ स्टडी ऐप्स ही खुलेंगी और बाकी सब ब्लॉक रहेंगी।",
                        "4. **इमरजेंसी ब्रेक**: पढ़ाई के बीच अगर ज़रूरत पड़े, तो ऊपर दाहिने कोने (Top Right) से 1 से 60 मिनट का ब्रेक लेकर सभी सामान्य ऐप्स को इस्तेमाल कर सकते हैं।"
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👤 योग्यता मानदंड (Age Criteria)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "यह सुरक्षा फोकस यूटिलिटी 18 वर्ष से अधिक आयु के छात्रों (18+ Higher Education Students) की एकाग्रता बढ़ाने के लिए है।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
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
                            text = "🔒 सुरक्षा एवं गोपनीयता नीति",
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
                        text = "यह ऐप पूरी तरह से ऑफलाइन और सुरक्षित है। हम आपका कोई भी पर्सनल या ब्राउज़िंग डेटा किसी बाहरी सर्वर पर नहीं भेजते हैं।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1. अभिगम्यता सेवा (Accessibility Service):\nयह अनुमति केवल स्क्रीन पर चल रहे ऐप के पैकेज नाम की जाँच करने के लिए ली जाती है। इसका उद्देश्य सिर्फ आपके चुने हुए समय अंतराल पर विचलित करने वाले ऐप्स को ब्लॉक करना है।",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2. 100% ऑफलाइन गोपनीयता (Data Privacy):\nआपके डिवाइस से कोई पर्सनल डेटा एकत्रित, शेयर या क्लाउड पर स्टोर नहीं किया जाता है। सभी शेड्यूल्स और सेटिंग्स आपके फ़ोन में लोकल तौर पर सुरक्षित रहती हैं।",
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
                            text = "विस्तृत प्रकटीकरण पढ़ने के लिए टैप करें...",
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
            
            AppInfoModel(
                label = label,
                packageName = packageName,
                isSystem = isSystem,
                isChecked = prefs.isStudyApp(packageName)
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
            placeholder = { Text("ऐप्स खोजें (Search installed apps...)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
                Text("सभी सिलेक्ट", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                Text("सभी हटाएँ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                    text = "स्टडी ऐप्स",
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
                            "कोई स्टडी ऐप नहीं चुना गया है!\nऊपर से ऐप सिलेक्ट करें।"
                        } else {
                            "कोई ऐप नहीं मिला।"
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
        title = { Text("टाइम शेड्यूल #${slot.id} संपादित करें", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("स्टार्ट समय (Start Time):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePartDropdown(
                            value = String.format("%02d", startH12),
                            options = hoursList,
                            label = "घंटा",
                            onValueChange = { startH12 = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(":", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TimePartDropdown(
                            value = String.format("%02d", startMin),
                            options = minutesList,
                            label = "मिनट",
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
                    Text("एंड समय (End Time):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePartDropdown(
                            value = String.format("%02d", endH12),
                            options = hoursList,
                            label = "घंटा",
                            onValueChange = { endH12 = it.toInt() },
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(":", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TimePartDropdown(
                            value = String.format("%02d", endMin),
                            options = minutesList,
                            label = "मिनट",
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
                Text("सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
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
