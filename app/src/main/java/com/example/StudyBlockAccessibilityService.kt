package com.example

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class StudyBlockAccessibilityService : AccessibilityService() {

    private lateinit var prefs: StudyBlockPreferences

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
        "com.android.packageinstaller"
    )

    override fun onCreate() {
        super.onCreate()
        prefs = StudyBlockPreferences(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // We only care about window change events which indicate a new app has come to foreground
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Check if blocker is currently active (schedule enabled, inside study hours, not on break)
            if (prefs.isBlockerActiveRightNow()) {
                
                // If the package is exempt, or is a registered study app, allow it
                if (exemptPackages.any { packageName.startsWith(it) } || prefs.isStudyApp(packageName)) {
                    // Allowed apps can run
                    return
                }

                // If it is another launcher or keyboard, we should generally allow them to run
                if (packageName.contains("launcher") || packageName.contains("keyboard") || packageName.contains("inputmethod")) {
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
