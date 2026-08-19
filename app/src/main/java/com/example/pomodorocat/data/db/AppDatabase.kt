package com.example.pomodorocat.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FocusSessionEntity::class,
        TaskTagEntity::class,
        CatProfileEntity::class,
        BadgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun taskTagDao(): TaskTagDao
    abstract fun catProfileDao(): CatProfileDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pomodoro_cat_database"
                )
                    .addCallback(DatabasePrepopulateCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulateCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // 1. 预置 6 个基础任务标签
            val defaultTags = listOf(
                TaskTagEntity("work", "工作", "Computer", 0xFF64B5F6, isCustom = false, orderIndex = 0),
                TaskTagEntity("study", "学习", "School", 0xFFFFB74D, isCustom = false, orderIndex = 1),
                TaskTagEntity("reading", "阅读", "MenuBook", 0xFF81C784, isCustom = false, orderIndex = 2),
                TaskTagEntity("code", "编程", "Terminal", 0xFFBA68C8, isCustom = false, orderIndex = 3),
                TaskTagEntity("exercise", "运动", "FitnessCenter", 0xFFFF8A65, isCustom = false, orderIndex = 4),
                TaskTagEntity("meditation", "冥想", "SelfImprovement", 0xFF4DB6AC, isCustom = false, orderIndex = 5)
            )
            db.taskTagDao().insertAll(defaultTags)

            // 2. 预置 5 只特色猫咪伙伴
            val defaultCats = listOf(
                CatProfileEntity(
                    id = "orange_tabby",
                    name = "元气橘橘",
                    breed = "中华田园橘猫",
                    personality = "干饭第一名、元气活泼",
                    unlockCostFish = 0,
                    isUnlocked = true,
                    isActive = true,
                    affectionExp = 0,
                    bondLevel = 1,
                    quote = "喵呜！今天也要元气满满地专注哦！"
                ),
                CatProfileEntity(
                    id = "calico",
                    name = "软萌三花",
                    breed = "温顺三花猫",
                    personality = "好奇宝宝、爱撒娇",
                    unlockCostFish = 100,
                    isUnlocked = false,
                    isActive = false,
                    affectionExp = 0,
                    bondLevel = 1,
                    quote = "呼噜呼噜... 有你陪着好安心喵~"
                ),
                CatProfileEntity(
                    id = "tuxedo",
                    name = "警长奶牛",
                    breed = "鬼马奶牛猫",
                    personality = "聪明好动、自带幽默感",
                    unlockCostFish = 250,
                    isUnlocked = false,
                    isActive = false,
                    affectionExp = 0,
                    bondLevel = 1,
                    quote = "本警长在此巡逻，不许走神喵！"
                ),
                CatProfileEntity(
                    id = "siamese",
                    name = "学霸暹罗",
                    breed = "暹罗重点色猫",
                    personality = "专注达人、高智商伙伴",
                    unlockCostFish = 500,
                    isUnlocked = false,
                    isActive = false,
                    affectionExp = 0,
                    bondLevel = 1,
                    quote = "优雅是专注的最高境界，我们继续努力吧。"
                ),
                CatProfileEntity(
                    id = "british_shorthair",
                    name = "贵族蓝宝",
                    breed = "英短蓝猫",
                    personality = "沉稳高贵、深情守候",
                    unlockCostFish = 1000,
                    isUnlocked = false,
                    isActive = false,
                    affectionExp = 0,
                    bondLevel = 1,
                    quote = "无论工作多难，本喵都会静静守候你。"
                )
            )
            db.catProfileDao().insertAll(defaultCats)

            // 3. 预置成就勋章
            val defaultBadges = listOf(
                BadgeEntity("first_pomodoro", "初出茅庐", "完成第 1 个专注番茄钟", "🌱", isUnlocked = false, targetCount = 1),
                BadgeEntity("night_owl", "夜行学者", "在深夜时分 (22:00 - 05:00) 完成专注", "🌙", isUnlocked = false, targetCount = 1),
                BadgeEntity("streak_3", "持之以恒", "连续 3 天完成专注打卡", "🔥", isUnlocked = false, targetCount = 3),
                BadgeEntity("fish_100", "小鱼干富翁", "累计获得 100 条小鱼干", "🐟", isUnlocked = false, targetCount = 100),
                BadgeEntity("fish_500", "大渔场主", "累计获得 500 条小鱼干", "🎣", isUnlocked = false, targetCount = 500),
                BadgeEntity("cat_lover", "猫舍大家庭", "解锁全部 5 只猫咪伙伴", "👑", isUnlocked = false, targetCount = 5)
            )
            db.badgeDao().insertAll(defaultBadges)
        }
    }
}
