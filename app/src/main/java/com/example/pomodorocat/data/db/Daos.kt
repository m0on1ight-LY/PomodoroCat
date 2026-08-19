package com.example.pomodorocat.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity): Long

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :start AND startTime <= :end ORDER BY startTime DESC")
    fun getSessionsBetween(start: Long, end: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE isCompleted = 1")
    fun getTotalCompletedCount(): Flow<Int>

    @Query("SELECT SUM(earnedFish) FROM focus_sessions")
    fun getTotalEarnedFish(): Flow<Int?>

    @Query("UPDATE focus_sessions SET rating = :rating, diaryNote = :note WHERE id = :id")
    suspend fun updateRatingAndNote(id: Long, rating: Int, note: String)
}

@Dao
interface TaskTagDao {
    @Query("SELECT * FROM task_tags ORDER BY orderIndex ASC")
    fun getAllTags(): Flow<List<TaskTagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TaskTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TaskTagEntity>)

    @Delete
    suspend fun deleteTag(tag: TaskTagEntity)

    @Update
    suspend fun updateTag(tag: TaskTagEntity)
}

@Dao
interface CatProfileDao {
    @Query("SELECT * FROM cat_profiles")
    fun getAllCats(): Flow<List<CatProfileEntity>>

    @Query("SELECT * FROM cat_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveCat(): Flow<CatProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cats: List<CatProfileEntity>)

    @Update
    suspend fun updateCat(cat: CatProfileEntity)

    @Query("UPDATE cat_profiles SET isActive = CASE WHEN id = :catId THEN 1 ELSE 0 END")
    suspend fun setActiveCat(catId: String)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(badges: List<BadgeEntity>)

    @Update
    suspend fun updateBadge(badge: BadgeEntity)
}
