package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
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
    var selectedTab by remember { mutableStateOf(if (startInStudyTab) 3 else 0) }
    var showBreakPopup by remember { mutableStateOf(false) }
    var tempBreakMinutes by remember { mutableStateOf(15f) }
    
    // Live states
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)) }
    var isBlockActive by remember { mutableStateOf(prefs.isBlockerActiveRightNow()) }
    var breakRemainingMinutes by remember { mutableStateOf(prefs.getBreakRemainingMinutes()) }
    var studyAppsCount by remember { mutableStateOf(prefs.studyApps.size) }

    // Live countdown effect for countdown breaks
    LaunchedEffect(Unit) {
        while (true) {
            isServiceEnabled = isAccessibilityServiceEnabled(context, StudyBlockAccessibilityService::class.java)
            isBlockActive = prefs.isBlockerActiveRightNow()
            breakRemainingMinutes = prefs.getBreakRemainingMinutes()
            studyAppsCount = prefs.studyApps.size
            delay(5000) // Poll every 5 seconds for status updates
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "StudyShield Pro",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showBreakPopup = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Break",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ब्रेक लें (Break)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("डैशबोर्ड") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Apps") },
                    label = { Text("ऐप्स जोड़ें") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Schedule") },
                    label = { Text("शेड्यूल") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Study Launcher") },
                    label = { Text("स्टडी टैब") }
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
                2 -> ScheduleTab(prefs = prefs, onScheduleChanged = {
                    isBlockActive = prefs.isBlockerActiveRightNow()
                })
                3 -> StudyAppsLauncherTab(prefs = prefs)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Essential Time, Weekday, Date header display card
        item {
            var currentTimeString by remember { mutableStateOf("") }
            var currentDateString by remember { mutableStateOf("") }
            var currentDayOfWeekString by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                while (true) {
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
                    currentDateString = "$day $monthName $year"

                    val daysOfWeek = arrayOf("रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")
                    currentDayOfWeekString = daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1]

                    delay(1000)
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTimeString,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentDayOfWeekString • $currentDateString",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Essential accessibility warning card if permission not granted
        if (!isServiceEnabled) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "सर्विसेज बंद है! (Service Disabled)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "सच्चे लॉक फीचर के लिए Accessibility सर्विस चालू करना बेहद ज़रूरी है। नीचे बटन दबाकर इसे ओन करें।",
                            fontSize = 13.sp,
                            color = Color(0xFF7F1D1D),
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
                    containerColor = if (isBlockActive) Color(0xFFEEF2F6) else Color(0xFFF0FDF4)
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBlockActive) Color(0xFF1E3A8A) else Color(0xFF166534)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBlockActive) "Block mode is currently active" else "No restrictions active right now",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (breakRemainingMinutes > 0) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Break Time", tint = Color(0xFF065F46))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "इमरजेंसी ब्रेक: $breakRemainingMinutes मिनट बाकी",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F5C)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                prefs.cancelEmergencyBreak()
                                onStatusRefresh()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ब्रेक खत्म करें (Lock Now)")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$studyAppsCount", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text(text = "स्टडी ऐप्स", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val activeSlotsCount = prefs.getTimeSlots().filter { it.isEnabled }.size
                                val scheduleText = if (prefs.isScheduleEnabled) {
                                    "$activeSlotsCount एक्टिव स्लॉट्स"
                                } else {
                                    "शेड्यूल बंद"
                                }
                                Text(text = scheduleText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "शेड्यूल समय", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Core Requirement: EMERGENCY BREAK configuration options (1 to 60 Mins Slider selector)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "आपातकालीन ब्रेक (Emergency Break)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "इमरजेंसी होने पर तुरंत 1 से 60 मिनट तक का ब्रेक लें, जिसमें सभी ऐप्स चालू रहेंगे।",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intervals = listOf(5, 10, 30, 60)
                        intervals.forEach { min ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        prefs.startEmergencyBreak(min)
                                        onStatusRefresh()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$min Min",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(text = "ब्रेक", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showBreakPopupAtDashboard = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Break Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1 से 60 मिनट में चुनें (Custom Break)")
                    }
                }
            }
        }

        // Explanation of how the App works
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "यह कैसे काम करता है? (How it works)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BulletPoint("1. 'ऐप्स जोड़ें' टैब में जाकर अपनी पढ़ाई की ऐप्स (NCERT, NCERT Books, JCERT) को सेलेक्ट करें।")
                    BulletPoint("2. 'शेड्यूल' टैब में जाकर कुल 7 स्लॉट्स में से अपना पढ़ाई समय सेट करें (12-hour AM/PM Format)।")
                    BulletPoint("3. निश्चित शेड्यूल समय के दौरान सिलेक्टेड स्टडी ऐप्स के अलावा बाकी सभी मोबाइल ऐप्स ब्लॉक रहेंगी।")
                    BulletPoint("4. आपातकाल होने पर 1 से 60 मिनट का ब्रेक लेकर मोबाइल ऐप्स को फिर खोलें।")
                    BulletPoint("5. कोई भी शेड्यूल न होने पर स्टडी टैब (Study Launcher) से आसानी से सिलेक्टेड स्टडी ऐप्स रन करें।")
                }
            }
        }
    }

    if (showBreakPopupAtDashboard) {
        AlertDialog(
            onDismissRequest = { showBreakPopupAtDashboard = false },
            title = { Text("कस्टम ब्रेक समय (Custom Break Duration)") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectorTab(
    prefs: StudyBlockPreferences,
    onAppsUpdated: () -> Unit
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfoModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Query package manager inside a coroutine background thread
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        // Query applications that have a launch activity
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
        it.label.contains(searchQuery, ignoreCase = true) || 
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ऐप्स खोजें (Search installed apps...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newChecked = !app.isChecked
                                // Update states
                                installedApps = installedApps.map {
                                    if (it.packageName == app.packageName) it.copy(isChecked = newChecked) else it
                                }
                                if (newChecked) {
                                    prefs.addStudyApp(app.packageName)
                                } else {
                                    prefs.removeStudyApp(app.packageName)
                                }
                                onAppsUpdated()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(text = app.label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = app.packageName, fontSize = 12.sp, color = Color.Gray, overflow = TextOverflow.Ellipsis, maxLines = 1)
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
                            }
                        )
                    }
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun ScheduleTab(
    prefs: StudyBlockPreferences,
    onScheduleChanged: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(prefs.isScheduleEnabled) }
    var slots by remember { mutableStateOf(prefs.getTimeSlots()) }
    var editingSlotId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(isEnabled) {
        slots = prefs.getTimeSlots()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
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
                        Text(text = "शेड्यूल लॉक सक्रिय करें", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Enable scheduled blocking mode", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            prefs.isScheduleEnabled = it
                            onScheduleChanged()
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "7 पढ़ाई शेड्यूल की तालिका (7 Time Slots Table)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(slots) { slot ->
            Card(
                shape = RoundedCornerShape(12.dp),
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
                                fontSize = 15.sp,
                                color = if (slot.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            if (slot.isEnabled) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "एक्टिव",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
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
                                onScheduleChanged()
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (slot.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Time Slot",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "नोट: शेड्यूल समय के दौरान, StudyShield केवल आपके द्वारा सेलेक्ट की गयी 'स्टडी ऐप्स' को रन होने देगा, बाकी सभी ऐप्स ब्लॉक रहेंगी।",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
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
                    onScheduleChanged()
                    editingSlotId = null
                }
            )
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
    var startHour by remember { mutableStateOf(slot.startHour) }
    var startMinute by remember { mutableStateOf(slot.startMinute) }
    var endHour by remember { mutableStateOf(slot.endHour) }
    var endMinute by remember { mutableStateOf(slot.endMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("शेड्यूल #${slot.id} समय बदलें") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("स्टार्ट टाइम (Start Time):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberDropdownField(
                            value = startHour,
                            range = 0..23,
                            label = "घंटा (0-23)",
                            onValueChange = { startHour = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        NumberDropdownField(
                            value = startMinute,
                            range = 0..59,
                            label = "मिनट (0-59)",
                            onValueChange = { startMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "12H Format: " + formatSlotTime12H(startHour, startMinute),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text("एंड टाइम (End Time):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberDropdownField(
                            value = endHour,
                            range = 0..23,
                            label = "घंटा (0-23)",
                            onValueChange = { endHour = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        NumberDropdownField(
                            value = endMinute,
                            range = 0..59,
                            label = "मिनट (0-59)",
                            onValueChange = { endMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "12H Format: " + formatSlotTime12H(endHour, endMinute),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(slot.copy(
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute
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

@Composable
fun StudyAppsLauncherTab(prefs: StudyBlockPreferences) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    var studyAppList by remember { mutableStateOf<List<AppInfoModel>>(emptyList()) }
    var isBlockerActive by remember { mutableStateOf(prefs.isBlockerActiveRightNow()) }

    LaunchedEffect(Unit) {
        isBlockerActive = prefs.isBlockerActiveRightNow()
        val studyPackages = prefs.studyApps
        val list = mutableListOf<AppInfoModel>()
        
        for (pkg in studyPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val label = appInfo.loadLabel(pm).toString()
                list.add(AppInfoModel(label = label, packageName = pkg, isSystem = false, isChecked = true))
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore missing app packages
            }
        }
        studyAppList = list.sortedBy { it.label }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "स्टडी लॉन्चपैड (Study Tab)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isBlockerActive) "शेड्यूल ऑन है: केवल यही ऐप्स चल रही हैं!" else "शेड्यूल ऑफ है: यहाँ से सीधे स्टडी ऐप्स खोलें",
            fontSize = 13.sp,
            color = if (isBlockerActive) Color.Red else Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (studyAppList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, contentDescription = "No Study Apps", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "कोई स्टडी ऐप नहीं जोड़ी गयी है।",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "ऐप्स जोड़ने के लिए 'ऐप्स जोड़ें' टैब में जाएँ।",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(studyAppList) { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    // Fallback indicator
                                }
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run App",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Open",
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

// Helper method to detect if accessibility service permission is active
fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
    val expectedId = context.packageName + "/" + service.canonicalName
    val string = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
    if (string.isNullOrEmpty()) return false
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(string)
    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedId, ignoreCase = true)) {
            return true
        }
    }
    return false
}
