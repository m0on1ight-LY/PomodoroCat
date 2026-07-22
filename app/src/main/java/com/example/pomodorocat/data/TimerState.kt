package com.example.pomodorocat.data

/**
 * 计时器会话类型
 */
enum class SessionType(val label: String) {
    WORK("专注工作"),
    SHORT_BREAK("短时休息"),
    LONG_BREAK("长时休息")
}

/**
 * 计时器的生命周期阶段
 */
enum class TimerPhase {
    IDLE,       // 空闲/初始状态
    RUNNING,    // 计时运行中
    PAUSED,     // 已暂停
    FINISHED    // 本次倒计时正常结束
}

/**
 * 计时器的全局数据状态
 */
data class TimerData(
    val phase: TimerPhase = TimerPhase.IDLE,
    val sessionType: SessionType = SessionType.WORK,
    val remainingMillis: Long = 25 * 60 * 1000L, // 默认 25 分钟
    val totalMillis: Long = 25 * 60 * 1000L,
    val completedPomodoros: Int = 0,             // 今日已完成番茄个数
    val targetPomodoros: Int = 4                 // 目标完成个数 (大循环周期)
)

/**
 * 白噪音混音器的音量状态 (0.0f 到 1.0f)
 */
data class MixerSettings(
    val rainVolume: Float = 0f,
    val campfireVolume: Float = 0f,
    val oceanVolume: Float = 0f,
    val forestVolume: Float = 0f
)
