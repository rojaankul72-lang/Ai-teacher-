package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppLanguage
import com.example.data.model.ConceptMastery
import com.example.data.model.LearningHistoryEntry
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.TeachingMethod
import com.example.data.model.VoiceLanguageSettings
import com.example.data.model.WeeklyTestRecord
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "student_profiles")
data class StudentProfileEntity(
    @PrimaryKey val id: String,
    val preferredName: String,
    val age: Int,
    val school: String,
    val studentClass: String,
    val rollNumber: String,
    val board: String,
    val schoolMedium: String,
    val preferredLanguage: String,
    val subjectsJson: String,
    val subjectDurationsJson: String,
    val interestsJson: String,
    val avatarColorHex: String,
    val sampleProfile: Boolean,
    val cameraPermissionForChild: Boolean,
    val lastLessonId: String,
    val lastLessonTitle: String,
    val lastConceptId: String,
    val lastStepIndex: Int,
    val lastTeachingApproach: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): StudentProfile = StudentProfile(
        id = id,
        userId = "local_family",
        preferredName = preferredName,
        age = age,
        school = school,
        studentClass = studentClass,
        rollNumber = rollNumber,
        board = board,
        schoolMedium = schoolMedium,
        preferredLanguage = preferredLanguage,
        subjects = jsonToStringList(subjectsJson),
        subjectDurations = jsonToLongMap(subjectDurationsJson),
        interests = jsonToStringList(interestsJson),
        avatarColorHex = avatarColorHex,
        sampleProfile = sampleProfile,
        cameraPermissionForChild = cameraPermissionForChild,
        lastLessonId = lastLessonId,
        lastLessonTitle = lastLessonTitle,
        lastConceptId = lastConceptId,
        lastStepIndex = lastStepIndex,
        lastTeachingApproach = lastTeachingApproach,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(profile: StudentProfile, idOverride: String = profile.id): StudentProfileEntity {
            val now = System.currentTimeMillis()
            return StudentProfileEntity(
                id = idOverride,
                preferredName = profile.preferredName.trim().take(40).ifBlank { "Student" },
                age = profile.age.coerceIn(3, 18),
                school = profile.school.trim().take(80),
                studentClass = profile.studentClass.trim().take(20).ifBlank { "UKG" },
                rollNumber = profile.rollNumber.trim().take(20),
                board = profile.board.trim().take(30).ifBlank { "CBSE" },
                schoolMedium = profile.schoolMedium.trim().take(20).ifBlank { "English" },
                preferredLanguage = AppLanguage.fromRaw(profile.preferredLanguage).name,
                subjectsJson = stringListToJson(profile.subjects.take(15)),
                subjectDurationsJson = longMapToJson(profile.subjectDurations),
                interestsJson = stringListToJson(profile.interests.take(15)),
                avatarColorHex = profile.avatarColorHex.take(12).ifBlank { "#D9531E" },
                sampleProfile = profile.sampleProfile,
                cameraPermissionForChild = profile.cameraPermissionForChild,
                lastLessonId = profile.lastLessonId.take(64),
                lastLessonTitle = profile.lastLessonTitle.take(120),
                lastConceptId = profile.lastConceptId.take(64),
                lastStepIndex = profile.lastStepIndex.coerceIn(0, 100),
                lastTeachingApproach = profile.lastTeachingApproach.take(64).ifBlank { TeachingMethod.STORY_SCENE.name },
                createdAtMillis = profile.createdAtMillis.takeIf { it > 0 } ?: now,
                updatedAtMillis = now
            )
        }
    }
}

@Entity(tableName = "concept_mastery")
data class ConceptMasteryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val conceptId: String,
    val conceptTitle: String,
    val subject: String,
    val lessonId: String,
    val independentCorrect: Int,
    val hintAssistedCorrect: Int,
    val attempts: Int,
    val misconceptionsJson: String,
    val needsPractice: Boolean,
    val nextDayRecallDue: Boolean,
    val nextDayRecallPassed: Boolean,
    val lastMethodUsed: String,
    val demonstratedEvidence: String,
    val updatedAtMillis: Long
) {
    fun toDomain(): ConceptMastery = ConceptMastery(
        id = id,
        userId = "local_family",
        profileId = profileId,
        conceptId = conceptId,
        conceptTitle = conceptTitle,
        subject = subject,
        lessonId = lessonId,
        independentCorrect = independentCorrect,
        hintAssistedCorrect = hintAssistedCorrect,
        attempts = attempts,
        misconceptions = jsonToStringList(misconceptionsJson),
        needsPractice = needsPractice,
        nextDayRecallDue = nextDayRecallDue,
        nextDayRecallPassed = nextDayRecallPassed,
        lastMethodUsed = lastMethodUsed,
        demonstratedEvidence = demonstratedEvidence,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(m: ConceptMastery, idOverride: String = m.id): ConceptMasteryEntity = ConceptMasteryEntity(
            id = idOverride,
            profileId = m.profileId.take(128),
            conceptId = m.conceptId.take(64),
            conceptTitle = m.conceptTitle.take(120),
            subject = m.subject.take(40),
            lessonId = m.lessonId.take(64),
            independentCorrect = m.independentCorrect.coerceIn(0, 1000),
            hintAssistedCorrect = m.hintAssistedCorrect.coerceIn(0, 1000),
            attempts = m.attempts.coerceIn(0, 2000),
            misconceptionsJson = stringListToJson(m.misconceptions.take(20)),
            needsPractice = m.needsPractice,
            nextDayRecallDue = m.nextDayRecallDue,
            nextDayRecallPassed = m.nextDayRecallPassed,
            lastMethodUsed = m.lastMethodUsed.take(64),
            demonstratedEvidence = m.demonstratedEvidence.take(200),
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}

@Entity(tableName = "learning_sessions")
data class LearningSessionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val lessonId: String,
    val lessonTitle: String,
    val subject: String,
    val storyStyle: String,
    val language: String,
    val summary: String,
    val checkpointIndex: Int,
    val completed: Boolean,
    val durationMinutes: Int,
    val updatedAtMillis: Long
) {
    fun toDomain(): LearningHistoryEntry = LearningHistoryEntry(
        id = id,
        userId = "local_family",
        profileId = profileId,
        lessonId = lessonId,
        lessonTitle = lessonTitle,
        subject = subject,
        storyStyle = storyStyle,
        language = language,
        summary = summary,
        checkpointIndex = checkpointIndex,
        completed = completed,
        durationMinutes = durationMinutes,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(entry: LearningHistoryEntry, idOverride: String = entry.id): LearningSessionEntity =
            LearningSessionEntity(
                id = idOverride,
                profileId = entry.profileId.take(128),
                lessonId = entry.lessonId.take(64),
                lessonTitle = entry.lessonTitle.take(120),
                subject = entry.subject.take(40),
                storyStyle = entry.storyStyle.take(40),
                language = AppLanguage.fromRaw(entry.language).name,
                summary = entry.summary.take(400),
                checkpointIndex = entry.checkpointIndex.coerceIn(0, 100),
                completed = entry.completed,
                durationMinutes = entry.durationMinutes.coerceIn(0, 240),
                updatedAtMillis = System.currentTimeMillis()
            )
    }
}

@Entity(tableName = "weekly_tests")
data class WeeklyTestEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val testDate: String,
    val subject: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val currentQuestionIndex: Int,
    val completed: Boolean,
    val conceptsNeedingRevisionJson: String,
    val answersSummaryJson: String,
    val updatedAtMillis: Long
) {
    fun toDomain(): WeeklyTestRecord = WeeklyTestRecord(
        id = id,
        userId = "local_family",
        profileId = profileId,
        testDate = testDate,
        subject = subject,
        totalQuestions = totalQuestions,
        correctCount = correctCount,
        currentQuestionIndex = currentQuestionIndex,
        completed = completed,
        conceptsNeedingRevision = jsonToStringList(conceptsNeedingRevisionJson),
        answersSummary = jsonToStringList(answersSummaryJson),
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(record: WeeklyTestRecord, idOverride: String = record.id): WeeklyTestEntity =
            WeeklyTestEntity(
                id = idOverride,
                profileId = record.profileId.take(128),
                testDate = record.testDate.take(20),
                subject = record.subject.take(40),
                totalQuestions = record.totalQuestions.coerceIn(1, 50),
                correctCount = record.correctCount.coerceIn(0, 50),
                currentQuestionIndex = record.currentQuestionIndex.coerceIn(0, 50),
                completed = record.completed,
                conceptsNeedingRevisionJson = stringListToJson(record.conceptsNeedingRevision.take(20)),
                answersSummaryJson = stringListToJson(record.answersSummary.take(20)),
                updatedAtMillis = System.currentTimeMillis()
            )
    }
}

@Entity(tableName = "app_settings")
data class VoiceLanguageSettingsEntity(
    @PrimaryKey val id: String = "voice_and_app",
    val selectedLanguage: String,
    val selectedStoryStyle: String,
    val speechSpeed: Double,
    val narrationVolume: Double,
    val musicVolume: Double,
    val captionsEnabled: Boolean,
    val lightweightMode: Boolean,
    val highResolutionMode: Boolean,
    val indianEpicModeEnabled: Boolean,
    val offlinePackDownloaded: Boolean,
    val updatedAtMillis: Long
) {
    fun toDomain(): VoiceLanguageSettings = VoiceLanguageSettings(
        id = id,
        userId = "local_family",
        selectedLanguage = selectedLanguage,
        selectedStoryStyle = selectedStoryStyle,
        speechSpeed = speechSpeed,
        narrationVolume = narrationVolume,
        musicVolume = musicVolume,
        captionsEnabled = captionsEnabled,
        lightweightMode = lightweightMode,
        highResolutionMode = highResolutionMode,
        indianEpicModeEnabled = indianEpicModeEnabled,
        offlinePackDownloaded = offlinePackDownloaded,
        updatedAtMillis = updatedAtMillis
    )

    companion object {
        fun fromDomain(settings: VoiceLanguageSettings): VoiceLanguageSettingsEntity =
            VoiceLanguageSettingsEntity(
                id = "voice_and_app",
                selectedLanguage = AppLanguage.fromRaw(settings.selectedLanguage).name,
                selectedStoryStyle = StoryStyle.fromRaw(settings.selectedStoryStyle).name,
                speechSpeed = settings.speechSpeed.coerceIn(0.5, 2.0),
                narrationVolume = settings.narrationVolume.coerceIn(0.0, 1.0),
                musicVolume = settings.musicVolume.coerceIn(0.0, 1.0),
                captionsEnabled = settings.captionsEnabled,
                lightweightMode = settings.lightweightMode,
                highResolutionMode = settings.highResolutionMode,
                indianEpicModeEnabled = settings.indianEpicModeEnabled,
                offlinePackDownloaded = settings.offlinePackDownloaded,
                updatedAtMillis = System.currentTimeMillis()
            )
    }
}

private fun stringListToJson(items: List<String>): String {
    val arr = JSONArray()
    items.forEach { arr.put(it) }
    return arr.toString()
}

private fun jsonToStringList(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { idx -> arr.optString(idx).takeIf { it.isNotBlank() } }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun longMapToJson(map: Map<String, Long>): String {
    val obj = JSONObject()
    map.entries.take(15).forEach { (k, v) -> obj.put(k, v) }
    return obj.toString()
}

private fun jsonToLongMap(raw: String): Map<String, Long> {
    if (raw.isBlank()) return emptyMap()
    return try {
        val obj = JSONObject(raw)
        val result = linkedMapOf<String, Long>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            result[k] = obj.optLong(k, 15L)
        }
        result
    } catch (_: Exception) {
        emptyMap()
    }
}
