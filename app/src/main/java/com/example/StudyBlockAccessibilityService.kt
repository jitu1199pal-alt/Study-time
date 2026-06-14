package com.example

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat

class StudyBlockAccessibilityService : AccessibilityService() {

    private lateinit var prefs: StudyBlockPreferences
    private val handler = Handler(Looper.getMainLooper())
    private val NOTIFICATION_ID = 8820
    private val CHANNEL_ID = "study_block_channel"

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            // Periodic update every 10 seconds to keep live state synced in notification drawer
            handler.postDelayed(this, 10000)
        }
    }

    // Essential system apps that should never be blocked to prevent bricking/lockouts
    private val exemptPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.providers.telecom",
        "com.android.settings", // allow settings to change accessibility if needed
        "com.example", // our app
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.google.android.gms" // Google Play Services
    )

    override fun onCreate() {
        super.onCreate()
        prefs = StudyBlockPreferences(applicationContext)
        startForegroundNotification()
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Study Mode App Lock & Timer Guard Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the blocker resilient and active in the background"
            }
            manager.createNotificationChannel(channel)
        }

        try {
            val notification = buildStatusNotification()
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("StudyBlockService", "Failed to start foreground service: ${e.message}", e)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            val notification = buildStatusNotification()
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e("StudyBlockService", "Failed to update notification: ${e.message}")
        }
    }

    private fun buildStatusNotification(): Notification {
        val active = prefs.isBlockerActiveRightNow()
        val isBreak = prefs.isBreakActive()
        val title: String
        val text: String

        if (active) {
            title = "Study Mode App Lock is Active 🔒"
            text = "Study mode is currently active. Essential apps are allowed, others are blocked."
        } else if (isBreak) {
            val minsLeft = prefs.getBreakRemainingMinutes()
            title = "Study Mode: On Emergency Break ☕"
            text = "Emergency Break: $minsLeft minutes remaining. All apps are temporarily unlocked."
        } else {
            title = "Study Mode App Lock is running in background 🛡️"
            text = "Your distracting apps will be blocked as soon as study hours begin."
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(this, MainActivity::class.java)
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()
    }

    private fun getLauncherPackageName(): String {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = packageManager.resolveActivity(intent, 0)
            resolveInfo?.activityInfo?.packageName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // We check window changes, window focus, and view focus to ensure thorough, instant blocking
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED || 
            eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {
            val packageName = event.packageName?.toString() ?: return
            
            // Check if blocker is currently active (schedule enabled, inside study hours, not on break)
            if (prefs.isBlockerActiveRightNow()) {
                
                // If the package is exempt, or is a registered study app, allow it
                if (exemptPackages.any { packageName.startsWith(it) } || prefs.isStudyApp(packageName)) {
                    // Allowed apps can run
                    return
                }

                // If it is another launcher or keyboard, we should generally allow them to run
                val lowerPkg = packageName.lowercase()
                val currentLauncher = getLauncherPackageName().lowercase()
                if (lowerPkg.contains("launcher") || 
                    lowerPkg.contains("home") || 
                    lowerPkg.contains("keyboard") || 
                    lowerPkg.contains("inputmethod") ||
                    lowerPkg == "com.miui.home" ||
                    (currentLauncher.isNotEmpty() && lowerPkg == currentLauncher)
                ) {
                    return
                }

                // Block the app!
                blockApp(packageName)
            }
        }
    }

    private fun blockApp(packageName: String) {
        // Go back to absolute home to minimize the blocked app immediately
        performGlobalAction(GLOBAL_ACTION_HOME)

        // Launch our BlockActivity on top to show a warning screen
        val intent = Intent(this, BlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("blocked_package", packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        Log.d("StudyBlockService", "Accessibility service interrupted.")
    }
}
