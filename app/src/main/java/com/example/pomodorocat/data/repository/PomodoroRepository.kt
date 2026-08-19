package com.example.pomodorocat.data.repository

import android.content.Context
import com.example.pomodorocat.data.PreferenceManager
import com.example.pomodorocat.data.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PomodoroRepository(
    private val database: AppDatabase,
    private val prefManager: PreferenceManager
) {
    companion object {
        @Volatile
        private var INSTANCE: PomodoroRepository? = null

        fun getInstance(context: Context): PomodoroRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val prefs = PreferenceManager(context)
                val instance = PomodoroRepository(db, prefs)
                INSTANCE = instance
                instance
            }
        }
    }

    private val sessionDao = database.focusSessionDao()
    private val tagDao = database.taskTagDao()
    private val catDao = database.catProfileDao()
    private val badgeDao = database.badgeDao()

    // --- 1. 会话记录相关 ---
    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    val totalFocusMinutes: Flow<Int?> = sessionDao.getTotalFocusMinutes()
    val totalCompletedCount: Flow<Int> = sessionDao.getTotalCompletedCount()

    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<FocusSessionEntity>> {
        return sessionDao.getSessionsBetween(startTime, endTime)
    }

    suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        tagId: String,
        tagName: String,
        sessionType: String,
        isCompleted: Boolean,
        rating: Int = 5,
        diaryNote: String = "",
        earnedFish: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        val session = FocusSessionEntity(
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            tagId = tagId,
            tagName = tagName,
            sessionType = sessionType,
            isCompleted = isCompleted,
            rating = rating,
            diaryNote = diaryNote,
            earnedFish = earnedFish
        )
        val id = sessionDao.insert(session)
        if (earnedFish > 0) {
            addDriedFish(earnedFish)
        }
        checkBadgesOnSessionCompleted(durationMinutes, isCompleted, startTime)
        id
    }

    suspend fun updateSession(session: FocusSessionEntity) = withContext(Dispatchers.IO) {
        sessionDao.update(session)
    }

    suspend fun updateSessionRatingAndNote(id: Long, rating: Int, note: String) = withContext(Dispatchers.IO) {
        sessionDao.updateRatingAndNote(id, rating, note)
    }

    // --- 2. 任务标签相关 ---
    val allTags: Flow<List<TaskTagEntity>> = tagDao.getAllTags()

    suspend fun createCustomTag(name: String, iconKey: String, colorHex: Long) = withContext(Dispatchers.IO) {
        val tag = TaskTagEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            iconKey = iconKey,
            colorHex = colorHex,
            isCustom = true,
            orderIndex = 100
        )
        tagDao.insertTag(tag)
    }

    suspend fun deleteTag(tag: TaskTagEntity) = withContext(Dispatchers.IO) {
        tagDao.deleteTag(tag)
    }

    suspend fun updateTag(tag: TaskTagEntity) = withContext(Dispatchers.IO) {
        tagDao.updateTag(tag)
    }

    // --- 3. 猫咪伙伴与养成相关 ---
    val allCats: Flow<List<CatProfileEntity>> = catDao.getAllCats()
    val activeCat: Flow<CatProfileEntity?> = catDao.getActiveCat()

    suspend fun unlockCat(catId: String, cost: Int): Boolean = withContext(Dispatchers.IO) {
        val currentFish = prefManager.totalDriedFish
        if (currentFish >= cost) {
            prefManager.totalDriedFish = currentFish - cost
            val cats = catDao.getAllCats().firstOrNull() ?: return@withContext false
            val target = cats.find { it.id == catId } ?: return@withContext false
            catDao.updateCat(target.copy(isUnlocked = true))
            checkBadgesOnCatUnlocked()
            true
        } else {
            false
        }
    }

    suspend fun feedActiveCat(): Boolean = withContext(Dispatchers.IO) {
        val feedCost = 10
        val expGain = 10
        val currentFish = prefManager.totalDriedFish
        if (currentFish >= feedCost) {
            prefManager.totalDriedFish = currentFish - feedCost
            val cat = catDao.getActiveCat().firstOrNull() ?: return@withContext false
            val newExp = cat.affectionExp + expGain
            val newLevel = calculateBondLevel(newExp)
            catDao.updateCat(cat.copy(affectionExp = newExp, bondLevel = newLevel))
            true
        } else {
            false
        }
    }

    private fun calculateBondLevel(exp: Int): Int {
        return when {
            exp >= 300 -> 5 // 灵魂伴侣
            exp >= 180 -> 4 // 撒娇怪
            exp >= 90  -> 3 // 粘人精
            exp >= 30  -> 2 // 渐熟络
            else       -> 1 // 怯生生
        }
    }

    suspend fun setActiveCat(catId: String) = withContext(Dispatchers.IO) {
        catDao.setActiveCat(catId)
    }

    // --- 4. 货币管理 ---
    fun getDriedFishBalance(): Int = prefManager.totalDriedFish

    fun addDriedFish(count: Int) {
        prefManager.totalDriedFish += count
    }

    // --- 5. 勋章徽章相关 ---
    val allBadges: Flow<List<BadgeEntity>> = badgeDao.getAllBadges()

    private suspend fun checkBadgesOnSessionCompleted(durationMinutes: Int, isCompleted: Boolean, timestamp: Long) {
        if (!isCompleted) return
        val badges = badgeDao.getAllBadges().firstOrNull() ?: return

        // 1. 初出茅庐 (first_pomodoro)
        val firstBadge = badges.find { it.id == "first_pomodoro" }
        if (firstBadge != null && !firstBadge.isUnlocked) {
            badgeDao.updateBadge(firstBadge.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()))
        }

        // 2. 夜行学者 (night_owl): 22:00 ~ 05:00
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 5) {
            val nightBadge = badges.find { it.id == "night_owl" }
            if (nightBadge != null && !nightBadge.isUnlocked) {
                badgeDao.updateBadge(nightBadge.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()))
            }
        }

        // 3. 累计鱼干徽章 (fish_100, fish_500)
        val totalFish = prefManager.totalDriedFish
        badges.find { it.id == "fish_100" }?.let { b ->
            if (!b.isUnlocked && totalFish >= 100) {
                badgeDao.updateBadge(b.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), currentCount = totalFish))
            }
        }
        badges.find { it.id == "fish_500" }?.let { b ->
            if (!b.isUnlocked && totalFish >= 500) {
                badgeDao.updateBadge(b.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), currentCount = totalFish))
            }
        }
    }

    private suspend fun checkBadgesOnCatUnlocked() {
        val cats = catDao.getAllCats().firstOrNull() ?: return
        val unlockedCount = cats.count { it.isUnlocked }
        if (unlockedCount >= 5) {
            val badges = badgeDao.getAllBadges().firstOrNull() ?: return
            badges.find { it.id == "cat_lover" }?.let { b ->
                if (!b.isUnlocked) {
                    badgeDao.updateBadge(b.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis(), currentCount = 5))
                }
            }
        }
    }
}
