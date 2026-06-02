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
