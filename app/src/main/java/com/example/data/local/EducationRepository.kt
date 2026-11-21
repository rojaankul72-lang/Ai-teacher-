package com.example.data.local

import android.content.Context
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.ConceptMastery
import com.example.data.model.LearningHistoryEntry
import com.example.data.model.StudentProfile
import com.example.data.model.VoiceLanguageSettings
import com.example.data.model.WeeklyTestRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EducationRepository(private val dao: EducationDao) {

    constructor(context: Context) : this(AppDatabase.getInstance(context).educationDao())

    fun observeProfiles(): Flow<List<StudentProfile>> =
        dao.observeProfiles().map { list -> list.map { it.toDomain() } }

    fun observeConceptMastery(): Flow<List<ConceptMastery>> =
        dao.observeConceptMastery().map { list -> list.map { it.toDomain() } }

    fun observeSessions(): Flow<List<LearningHistoryEntry>> =
        dao.observeSessions().map { list -> list.map { it.toDomain() } }

    fun observeWeeklyTests(): Flow<List<WeeklyTestRecord>> =
        dao.observeWeeklyTests().map { list -> list.map { it.toDomain() } }

    fun observeSettings(): Flow<VoiceLanguageSettings> =
        dao.observeSettings().map { entity -> entity?.toDomain() ?: VoiceLanguageSettings() }

    suspend fun ensureStarterDataSeeded(): Result<Unit> {
        return try {
            if (dao.getProfileCount() == 0) {
                val barsha = LessonCatalog.starterBarshaProfile("local_family")
                val raghav = LessonCatalog.starterRaghavProfile("local_family")
                dao.upsertProfile(StudentProfileEntity.fromDomain(barsha, barsha.id))
                dao.upsertProfile(StudentProfileEntity.fromDomain(raghav, raghav.id))

                val m1 = LessonCatalog.starterBarshaMastery("local_family")
                val m2 = LessonCatalog.starterRaghavMastery("local_family")
                dao.upsertConceptMastery(ConceptMasteryEntity.fromDomain(m1, m1.id))
                dao.upsertConceptMastery(ConceptMasteryEntity.fromDomain(m2, m2.id))
            }
            if (dao.getSettingsOnce() == null) {
                dao.upsertSettings(VoiceLanguageSettingsEntity.fromDomain(VoiceLanguageSettings()))
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProfile(profile: StudentProfile): Result<String> {
        return try {
            val cleanId = profile.id.takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]+$")) }
                ?: "child_${System.currentTimeMillis()}"
            val existing = dao.getProfileById(cleanId)
            val entity = StudentProfileEntity.fromDomain(
                profile = profile.copy(
                    createdAtMillis = existing?.createdAtMillis ?: profile.createdAtMillis
                ),
                idOverride = cleanId
            )
            dao.upsertProfile(entity)
            Result.success(cleanId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(profileId: String): Result<Unit> {
        return try {
            dao.deleteProfileById(profileId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLessonCheckpoint(
        profileId: String,
        lessonId: String,
        lessonTitle: String,
        conceptId: String,
        stepIndex: Int,
        teachingApproach: String
    ): Result<Unit> {
        return try {
            dao.updateLessonCheckpoint(
                profileId = profileId,
                lessonId = lessonId.take(64),
                lessonTitle = lessonTitle.take(120),
                conceptId = conceptId.take(64),
                stepIndex = stepIndex.coerceIn(0, 100),
                teachingApproach = teachingApproach.take(64),
                updatedAtMillis = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertConceptMastery(mastery: ConceptMastery): Result<Unit> {
        return try {
            val cleanId = mastery.id.ifBlank { "${mastery.profileId}__${mastery.conceptId}" }
                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(128)
            dao.upsertConceptMastery(ConceptMasteryEntity.fromDomain(mastery, cleanId))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logLearningSession(entry: LearningHistoryEntry): Result<Unit> {
        return try {
            val cleanId = entry.id.ifBlank { "sess_${entry.profileId}_${entry.lessonId}" }
                .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                .take(128)
            dao.upsertSession(LearningSessionEntity.fromDomain(entry, cleanId))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveWeeklyTestRecord(record: WeeklyTestRecord): Result<Unit> {
        return try {
            val cleanId = record.id.ifBlank {
                "test_${record.profileId}_${record.testDate}_${record.subject}"
            }.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(128)
            val existing = dao.getWeeklyTestById(cleanId)
            if (existing?.completed == true) {
                return Result.success(Unit)
            }
            dao.upsertWeeklyTest(WeeklyTestEntity.fromDomain(record, cleanId))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSettings(settings: VoiceLanguageSettings): Result<Unit> {
        return try {
            dao.upsertSettings(VoiceLanguageSettingsEntity.fromDomain(settings))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
