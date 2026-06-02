package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border

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
                            .fillMaxWidth(fraction = if (ad.adDuration > 0) (ad.adDuration - ad.adSecsRemaining).toFloat() / ad.adDuration else 0f)
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
