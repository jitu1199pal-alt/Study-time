package com.example

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

data class TimeSlot(
    val id: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean
)

class StudyBlockPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("study_block_prefs", Context.MODE_PRIVATE)

    init {
        if (!prefs.getBoolean("initialized_24h_v8", false)) {
            val editor = prefs.edit()
            val defaultStarts = arrayOf(8, 10, 13, 15, 18, 20, 23)
            val defaultStartMins = arrayOf(0, 30, 0, 30, 0, 30, 0)
            val defaultEnds = arrayOf(10, 13, 15, 18, 20, 23, 8)
            val defaultEndMins = arrayOf(30, 0, 30, 0, 30, 0, 0)
            
            for (i in 1..7) {
                editor.putInt("slot_${i}_start_hour", defaultStarts[i - 1])
                editor.putInt("slot_${i}_start_minute", defaultStartMins[i - 1])
                editor.putInt("slot_${i}_end_hour", defaultEnds[i - 1])
                editor.putInt("slot_${i}_end_minute", defaultEndMins[i - 1])
                editor.putBoolean("slot_${i}_enabled", true)
            }
            
            // Pre-seed some default educational app packages
            editor.putStringSet("study_apps", setOf(
                "com.study.ncert",
                "com.physicswallah",
                "org.khanacademy",
                "com.duolingo",
                "com.google.android.apps.classroom"
            ))
            
            editor.putBoolean("schedule_enabled", true)
            editor.putBoolean("initialized_24h_v8", true)
            editor.apply()
        }
    }

    companion object {
        private const val KEY_STUDY_APPS = "study_apps"
        private const val KEY_SCHEDULE_ENABLED = "schedule_enabled"
        private const val KEY_BREAK_END_TIME = "break_end_time"
    }

    // Study apps set
    var studyApps: Set<String>
        get() = prefs.getStringSet(KEY_STUDY_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_STUDY_APPS, value).apply()

    fun addStudyApp(packageName: String) {
        val current = studyApps.toMutableSet()
        current.add(packageName)
        studyApps = current
    }

    fun removeStudyApp(packageName: String) {
        val current = studyApps.toMutableSet()
        current.remove(packageName)
        studyApps = current
    }

    fun isStudyApp(packageName: String): Boolean {
        return studyApps.contains(packageName)
    }

    // Schedule master switch
    var isScheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply()

    // Get all 7 time slots
    fun getTimeSlots(): List<TimeSlot> {
        val list = mutableListOf<TimeSlot>()
        
        // Default times based on student requirements (AM/PM table representation) - 24H gapless coverage enabled by default
        val defaultStarts = arrayOf(8, 10, 13, 15, 18, 20, 23)
        val defaultStartMins = arrayOf(0, 30, 0, 30, 0, 30, 0)
        val defaultEnds = arrayOf(10, 13, 15, 18, 20, 23, 8)
        val defaultEndMins = arrayOf(30, 0, 30, 0, 30, 0, 0)
        
        for (i in 1..7) {
            val startHour = prefs.getInt("slot_${i}_start_hour", defaultStarts[i - 1])
            val startMinute = prefs.getInt("slot_${i}_start_minute", defaultStartMins[i - 1])
            val endHour = prefs.getInt("slot_${i}_end_hour", defaultEnds[i - 1])
            val endMinute = prefs.getInt("slot_${i}_end_minute", defaultEndMins[i - 1])
            // Enable all slots by default to ensure continuous block coverage when schedule is ON
            val isEnabled = prefs.getBoolean("slot_${i}_enabled", true)
            
            list.add(TimeSlot(i, startHour, startMinute, endHour, endMinute, isEnabled))
        }
        return list
    }

    // Save a time slot
    fun saveTimeSlot(slot: TimeSlot) {
        prefs.edit().apply {
            putInt("slot_${slot.id}_start_hour", slot.startHour)
            putInt("slot_${slot.id}_start_minute", slot.startMinute)
            putInt("slot_${slot.id}_end_hour", slot.endHour)
            putInt("slot_${slot.id}_end_minute", slot.endMinute)
            putBoolean("slot_${slot.id}_enabled", slot.isEnabled)
            apply()
        }
    }

    // Emergency break timestamp in ms
    var breakEndTimeMs: Long
        get() = prefs.getLong(KEY_BREAK_END_TIME, 0L)
        set(value) {
            prefs.edit().putLong(KEY_BREAK_END_TIME, value).commit()
        }

    // Start a temporary emergency break
    fun startEmergencyBreak(minutes: Int) {
        val now = System.currentTimeMillis()
        breakEndTimeMs = now + (minutes * 60 * 1000L)
    }

    // Cancel emergency break
    fun cancelEmergencyBreak() {
        breakEndTimeMs = 0L
    }

    // Check if emergency break is active
    fun isBreakActive(): Boolean {
        return System.currentTimeMillis() < breakEndTimeMs
    }

    // Get break remaining minutes
    fun getBreakRemainingMinutes(): Int {
        if (!isBreakActive()) return 0
        val diffMs = breakEndTimeMs - System.currentTimeMillis()
        return (diffMs / (60 * 1000L)).toInt() + 1
    }

    // Check if the current local time falls within study hours
    fun isWithinStudyHours(): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentVal = currentHour * 60 + currentMinute

        for (slot in getTimeSlots()) {
            if (!slot.isEnabled) continue
            val startVal = slot.startHour * 60 + slot.startMinute
            val endVal = slot.endHour * 60 + slot.endMinute

            val isActive = if (startVal < endVal) {
                currentVal in startVal until endVal
            } else {
                // Overnight schedule support (e.g., 22:00 to 02:00 next day)
                currentVal >= startVal || currentVal < endVal
            }
            if (isActive) return true
        }
        return false
    }

    // Final core decision check: Is blocker active right now?
    fun isBlockerActiveRightNow(): Boolean {
        return isScheduleEnabled && isWithinStudyHours() && !isBreakActive()
    }
}
