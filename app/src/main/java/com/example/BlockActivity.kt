package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class BlockActivity : ComponentActivity() {
    
    private lateinit var prefs: StudyBlockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = StudyBlockPreferences(applicationContext)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: "Application"

        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->
                    BlockScreen(
                        modifier = Modifier.padding(innerPadding),
                        blockedPackage = blockedPackage,
                        onBreakSelected = { minutes ->
                            prefs.startEmergencyBreak(minutes)
                            // Go back to main activity to show the break countdown
                            val mainIntent = Intent(this@BlockActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(mainIntent)
                            finish()
                        },
                        onOpenStudyLaunchpad = {
                            val mainIntent = Intent(this@BlockActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("navigate_to_study", true)
                            }
                            startActivity(mainIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BlockScreen(
    modifier: Modifier = Modifier,
    blockedPackage: String,
    onBreakSelected: (Int) -> Unit,
    onOpenStudyLaunchpad: () -> Unit
) {
    // Elegant deep blue/crimson slate blocking gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1B4B), // Very dark indigo
            Color(0xFF0F172A)  // Very dark slate
        )
    )

    val context = LocalContext.current
    var showBreakDialog by remember { mutableStateOf(false) }
    var activeAdState by remember { mutableStateOf<ActiveAdData?>(null) }

    LaunchedEffect(activeAdState) {
        val ad = activeAdState
        if (ad != null && ad.adSecsRemaining > 0) {
            while (activeAdState != null && activeAdState!!.adSecsRemaining > 0) {
                delay(1000)
                activeAdState = activeAdState?.copy(adSecsRemaining = activeAdState!!.adSecsRemaining - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Red Lock Icon representing prohibition during study
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x20EF4444)),
            shape = RoundedCornerShape(100.dp),
            modifier = Modifier.size(100.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Icon",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Hindi/English Warning Text
        Text(
            text = "पढ़ाई का समय सक्रिय है!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Focus Mode is Active!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Informative note explaining which app is restricted
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x10FFFFFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "यह ऐप अभी बंद है क्योंकि यह स्टडी ऐप नहीं है:",
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = blockedPackage.substringAfterLast("."),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8), // Light blue app indicator
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Launch helper with direct buttons
        Button(
            onClick = onOpenStudyLaunchpad,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "स्टडी ऐप्स खोलें (Open Study Tab)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Take Break Button - Meets critical "Emergency 5/10/60 mins break" requirement
        OutlinedButton(
            onClick = { showBreakDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Break")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "इमरजेंसी ब्रेक लें (Emergency Break)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Motivational Quote
        Text(
            text = "“शिक्षा सबसे शक्तिशाली हथियार है जिसका उपयोग आप दुनिया को बदलने के लिए कर सकते हैं।”",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    if (showBreakDialog) {
        AlertDialog(
            onDismissRequest = { showBreakDialog = false },
            title = {
                Text(
                    text = "इमरजेंसी ब्रेक चुनें (Choose Break)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "इमरजेंसी के दौरान सभी मोबाइल ऐप्स चालू हो जाएंगे। कृपया उपयुक्त समय चुनें:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val breakIntervals = listOf(
                        5 to "5 मिनट",
                        10 to "10 मिनट",
                        30 to "30 मिनट",
                        60 to "60 मिनट (1 घंटा)"
                    )
                    
                    breakIntervals.forEach { (minutes, label) ->
                        Button(
                            onClick = {
                                val adSecs = if (minutes <= 5) 5 else if (minutes <= 15) 8 else 10
                                activeAdState = ActiveAdData(
                                    adDuration = adSecs,
                                    adSecsRemaining = adSecs,
                                    pendingBreakMinutes = minutes,
                                    adKey = (0..2).random()
                                )
                                showBreakDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "$label ($minutes Mins)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBreakDialog = false }) {
                    Text(text = "रद्द करें (Cancel)")
                }
            }
        )
    }

    activeAdState?.let { ad ->
        SimulatedAdOverlay(
            ad = ad,
            onFinishAd = {
                activeAdState = null
                onBreakSelected(ad.pendingBreakMinutes)
            },
            onAdRedirectMessage = { msg ->
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

data class ActiveAdData(
    val adDuration: Int,
    val adSecsRemaining: Int,
    val pendingBreakMinutes: Int,
    val adKey: Int
)

@Composable
fun SimulatedAdOverlay(
    ad: ActiveAdData,
    onFinishAd: () -> Unit,
    onAdRedirectMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val adTitle = when (ad.adKey) {
        0 -> "StudySphere Premium"
        1 -> "BrainBoost Focus"
        else -> "Coding Juniors Pro"
    }
    val adTagline = when (ad.adKey) {
        0 -> "Distraction-Free Cloud Learning"
        1 -> "Natural Memory & Alertness"
        else -> "Fullstack Engineering in 30 Days"
    }
    val adDesc = when (ad.adKey) {
        0 -> "Sync NCERT solutions plus full interactive physics labs directly across all study devices. Over 10M+ Indian students score higher with premium guides!"
        1 -> "Stay energized and ultra-focused through 3-hour long exam revision sessions! Pure vegetarian extracts certified of highest purity."
        else -> "Build Real Android apps, games, & deploy to AWS. Premium daily mentoring and guaranteed live placements with top tech partners."
    }
    val adStats = when (ad.adKey) {
        0 -> "4.9 ★ (1.2M Reviews) • 50M+ Installs"
        1 -> "4.7 ★ (250K Reviews) • 2M+ Sold"
        else -> "4.8 ★ (85K Reviews) • 505K+ Students"
    }
    val adActionText = when (ad.adKey) {
        0 -> "Install Free Trial"
        1 -> "Order 25% Off Today"
        else -> "Join Live Workshop"
    }
    val adColor = when (ad.adKey) {
        0 -> Color(0xFFD97706)
        1 -> Color(0xFF2563EB)
        else -> Color(0xFF9333EA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B13))
            .padding(24.dp)
            .clickable(enabled = false) {}, // consume clicks to avoid click-through
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ad",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google AdSense",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ad.adSecsRemaining > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0x33EF4444), RoundedCornerShape(100.dp))
                                .border(BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ad remains: ${ad.adSecsRemaining}s",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0x3310B981), RoundedCornerShape(100.dp))
                                .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✔ Completed!",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    IconButton(
                        onClick = { if (ad.adSecsRemaining <= 0) onFinishAd() },
                        enabled = ad.adSecsRemaining <= 0,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (ad.adSecsRemaining <= 0) Color(0xFFEF4444) else Color(0xFF1E293B),
                                RoundedCornerShape(100.dp)
                            )
                    ) {
                        Text(
                            text = "✕",
                            color = if (ad.adSecsRemaining <= 0) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Main Body Ad Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FEATURED ACADEMIC OFFER",
                    color = Color(0xFF818CF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "For Break of ${ad.pendingBreakMinutes} Min",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ad Illustration Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, adColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(adColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (ad.adKey == 0) "AI POWERED" else if (ad.adKey == 1) "100% ORGANIC" else "JOB GUARANTEE",
                                    color = adColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = adTitle,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Distraction Blocker Core Active",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0x1FFFFFFF), RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = adStats,
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = adTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = adTagline,
                    color = Color(0xFF818CF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = adDesc,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Bar Timeline Ticking
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(100.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = if (ad.adDuration > 0) ad.adSecsRemaining.toFloat() / ad.adDuration else 0f)
                            .background(Color(0xFF6366F1), RoundedCornerShape(100.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onAdRedirectMessage("Simulating Ad Redirect...") },
                    colors = ButtonDefaults.buttonColors(containerColor = adColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = adActionText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "*Ad completion is required to begin break duration.",
                    color = Color(0xFF475569),
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (ad.adSecsRemaining <= 0) {
                    Button(
                        onClick = onFinishAd,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "ब्रेक शुरू करें (Start Break)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ad details loading... (${ad.adSecsRemaining}s remaining)",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
