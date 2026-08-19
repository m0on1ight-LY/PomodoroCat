package com.example.pomodorocat.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 专注会话记录实体
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,            // 会话开始时间戳
    val endTime: Long,              // 会话结束时间戳
    val durationMinutes: Int,       // 专注时长 (分钟)
    val tagId: String,              // 绑定的任务标签 ID
    val tagName: String,            // 标签名称
    val sessionType: String,        // 会话类型: WORK, SHORT_BREAK, LONG_BREAK
    val isCompleted: Boolean,       // 是否正常坚持完成
    val rating: Int = 5,            // 用户自评专注度 (1..5 星)
    val diaryNote: String = "",     // 专注心得与反思
    val earnedFish: Int = 0         // 本次获得的鱼干数
)

/**
 * 任务标签实体
 */
@Entity(tableName = "task_tags")
data class TaskTagEntity(
    @PrimaryKey
    val id: String,                 // 标签唯一标识 (如 "work", "study" 或 UUID)
    val name: String,               // 显示名称
    val iconKey: String,            // 图标代号
    val colorHex: Long,             // 主题色 ARGB 值
    val isCustom: Boolean = false,  // 是否为用户自定义创建
    val orderIndex: Int = 0
)

/**
 * 猫咪图鉴伙伴实体
 */
@Entity(tableName = "cat_profiles")
data class CatProfileEntity(
    @PrimaryKey
    val id: String,                 // 猫咪 ID (如 "orange_tabby", "calico", "tuxedo", "siamese", "british_shorthair")
    val name: String,               // 名字 (如 "元气橘橘")
    val breed: String,              // 品种 (如 "中华田园橘猫")
    val personality: String,        // 性格描述 (如 "干饭王、元气活泼")
    val unlockCostFish: Int,        // 解锁所需小鱼干
    val isUnlocked: Boolean,        // 是否已解锁
    val isActive: Boolean,          // 是否为当前出战/陪伴中的猫咪
    val affectionExp: Int = 0,      // 亲密度当前经验
    val bondLevel: Int = 1,         // 亲密度等级 (1..5)
    val quote: String               // 专属问候台词
)

/**
 * 成就勋章实体
 */
@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey
    val id: String,                 // 勋章 ID
    val title: String,              // 勋章称号
    val description: String,        // 解锁条件描述
    val iconEmoji: String,          // 显示 Emoji / 图标
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val targetCount: Int = 1,       // 目标达成数值
    val currentCount: Int = 0       // 当前累计数值
)
