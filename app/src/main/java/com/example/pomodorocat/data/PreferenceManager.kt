package com.example.pomodorocat.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 偏好设置管理器，保存个性化番茄钟设置
 */
class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pomodoro_cat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WORK_MIN = "work_duration_min"
        private const val KEY_SHORT_BREAK_MIN = "short_break_duration_min"
        private const val KEY_LONG_BREAK_MIN = "long_break_duration_min"
        private const val KEY_TARGET_POMODOROS = "target_pomodoros"
        private const val KEY_AUTO_START_BREAK = "auto_start_break"
        private const val KEY_AUTO_START_WORK = "auto_start_work"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_DRIED_FISH = "total_dried_fish"
        private const val KEY_SELECTED_TAG = "selected_tag_id"
        private const val KEY_DARK_MODE = "dark_mode_setting"
    }

    var darkMode: Int
        get() = prefs.getInt(KEY_DARK_MODE, 0) // 0: 跟随系统, 1: 浅色, 2: 深色
        set(value) = prefs.edit().putInt(KEY_DARK_MODE, value).apply()

    var totalDriedFish: Int
        get() = prefs.getInt(KEY_DRIED_FISH, 0)
        set(value) = prefs.edit().putInt(KEY_DRIED_FISH, value).apply()

    var selectedTagId: String
        get() = prefs.getString(KEY_SELECTED_TAG, "work") ?: "work"
        set(value) = prefs.edit().putString(KEY_SELECTED_TAG, value).apply()

    var workDurationMin: Int
        get() = prefs.getInt(KEY_WORK_MIN, 25)
        set(value) = prefs.edit().putInt(KEY_WORK_MIN, value).apply()

    var shortBreakMin: Int
        get() = prefs.getInt(KEY_SHORT_BREAK_MIN, 5)
        set(value) = prefs.edit().putInt(KEY_SHORT_BREAK_MIN, value).apply()

    var longBreakMin: Int
        get() = prefs.getInt(KEY_LONG_BREAK_MIN, 15)
        set(value) = prefs.edit().putInt(KEY_LONG_BREAK_MIN, value).apply()

    var targetPomodoros: Int
        get() = prefs.getInt(KEY_TARGET_POMODOROS, 4)
        set(value) = prefs.edit().putInt(KEY_TARGET_POMODOROS, value).apply()

    var autoStartBreak: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_BREAK, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START_BREAK, value).apply()

    var autoStartWork: Boolean
        get() = prefs.getBoolean(KEY_AUTO_START_WORK, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_START_WORK, value).apply()

    var selectedTheme: Int
        get() = prefs.getInt(KEY_SELECTED_THEME, 0) // 0: 粉萌, 1: 森绿, 2: 向日黄
        set(value) = prefs.edit().putInt(KEY_SELECTED_THEME, value).apply()

    // 默认恢复出厂设置
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
