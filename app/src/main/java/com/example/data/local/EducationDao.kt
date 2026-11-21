package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EducationDao {
    @Query("SELECT * FROM student_profiles ORDER BY preferredName ASC")
    fun observeProfiles(): Flow<List<StudentProfileEntity>>

    @Query("SELECT COUNT(*) FROM student_profiles")
    suspend fun getProfileCount(): Int

    @Query("SELECT * FROM student_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: StudentProfileEntity)

    @Query("DELETE FROM student_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)

    @Query(
        """
        UPDATE student_profiles
        SET lastLessonId = :lessonId,
            lastLessonTitle = :lessonTitle,
            lastConceptId = :conceptId,
            lastStepIndex = :stepIndex,
            lastTeachingApproach = :teachingApproach,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :profileId
        """
    )
    suspend fun updateLessonCheckpoint(
        profileId: String,
        lessonId: String,
        lessonTitle: String,
        conceptId: String,
        stepIndex: Int,
        teachingApproach: String,
        updatedAtMillis: Long
    )

    @Query("SELECT * FROM concept_mastery")
    fun observeConceptMastery(): Flow<List<ConceptMasteryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConceptMastery(mastery: ConceptMasteryEntity)

    @Query("SELECT * FROM learning_sessions ORDER BY updatedAtMillis DESC")
    fun observeSessions(): Flow<List<LearningSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: LearningSessionEntity)

    @Query("SELECT * FROM weekly_tests ORDER BY updatedAtMillis DESC")
    fun observeWeeklyTests(): Flow<List<WeeklyTestEntity>>

    @Query("SELECT * FROM weekly_tests WHERE id = :id LIMIT 1")
    suspend fun getWeeklyTestById(id: String): WeeklyTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeeklyTest(record: WeeklyTestEntity)

    @Query("SELECT * FROM app_settings WHERE id = 'voice_and_app' LIMIT 1")
    fun observeSettings(): Flow<VoiceLanguageSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'voice_and_app' LIMIT 1")
    suspend fun getSettingsOnce(): VoiceLanguageSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: VoiceLanguageSettingsEntity)
}
