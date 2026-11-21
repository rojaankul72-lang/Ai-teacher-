package com.example.data.model

enum class AppLanguage(
    val displayName: String,
    val nativeLabel: String,
    val localeTag: String,
    val speechLocale: String
) {
    ENGLISH("English", "English", "en-IN", "en-IN"),
    HINDI("Hindi", "हिन्दी", "hi-IN", "hi-IN"),
    ODIA("Odia", "ଓଡ଼ିଆ", "or-IN", "or-IN");

    companion object {
        fun fromRaw(raw: String?): AppLanguage =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ENGLISH
    }
}

enum class StoryStyle(
    val title: String,
    val subtitle: String,
    val badge: String,
    val requiresParentToggle: Boolean = false
) {
    HUMAN_ADVENTURE(
        "Human Adventure",
        "Grandparents, children, village friends & inventors solve real puzzles",
        "Warm & Realistic"
    ),
    COMIC_FRIENDSHIP(
        "Comic Friendship",
        "Original duo Bhiku & Chintu bring Motu-Patlu style warm comedy",
        "Original Duo"
    ),
    PLAYFUL_CHASE(
        "Playful Chase & Puzzle",
        "Mithu the clever squirrel & Sheru the pup show cause & effect",
        "Original Chase"
    ),
    INDIAN_EPIC(
        "Indian Epic Storytelling",
        "Respectful stories with young Gopal, Hanuman, Rama, Lakshmana & Sita",
        "Parent Choice",
        requiresParentToggle = true
    ),
    EMOTIONAL_FAMILY(
        "Emotional Family & Nature",
        "Care, courage, patience and teamwork with gentle resolutions",
        "Heartfelt"
    ),
    FUTURE_CLASSROOM(
        "Future Classroom",
        "Human teacher, holographic board, floating models & guide drone",
        "Holographic"
    );

    companion object {
        fun fromRaw(raw: String?): StoryStyle =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: HUMAN_ADVENTURE
    }
}

enum class TeachingMethod(val label: String) {
    STORY_SCENE("Story Scene"),
    ACT_IT_OUT("Act It Out Together"),
    DRAW_DIAGRAM("Visual Diagram / Hologram"),
    REAL_OBJECT("Real Courtyard Objects"),
    SHORTER_STORY("Shorter Step-by-Step Story"),
    HOME_LANGUAGE("Home Language Bridge"),
    COMPARE_EXAMPLES("Compare Two Examples"),
    SOLVE_TOGETHER("Solve One Together");

    companion object {
        fun fromRaw(raw: String?): TeachingMethod =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: STORY_SCENE
    }
}

data class MultilingualText(
    val english: String,
    val hindi: String,
    val odia: String
) {
    fun get(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> english
        AppLanguage.HINDI -> hindi
        AppLanguage.ODIA -> odia
    }
}

data class StudentProfile(
    val id: String = "",
    val userId: String = "local_family",
    val preferredName: String = "",
    val age: Int = 6,
    val school: String = "",
    val studentClass: String = "UKG",
    val rollNumber: String = "",
    val board: String = "CBSE",
    val schoolMedium: String = "English",
    val preferredLanguage: String = AppLanguage.ENGLISH.name,
    val subjects: List<String> = listOf(
        "English", "Hindi", "Odia", "Mathematics", "EVS",
        "Science", "Social Science", "GK", "Painting", "Yoga"
    ),
    val subjectDurations: Map<String, Long> = mapOf(
        "English" to 20L,
        "Hindi" to 15L,
        "Odia" to 15L,
        "Mathematics" to 20L,
        "EVS" to 15L,
        "Science" to 20L,
        "Social Science" to 15L,
        "GK" to 10L,
        "Painting" to 15L,
        "Yoga" to 10L
    ),
    val interests: List<String> = emptyList(),
    val avatarColorHex: String = "#D9531E",
    val sampleProfile: Boolean = false,
    val cameraPermissionForChild: Boolean = false,
    val lastLessonId: String = "",
    val lastLessonTitle: String = "",
    val lastConceptId: String = "",
    val lastStepIndex: Int = 0,
    val lastTeachingApproach: String = TeachingMethod.STORY_SCENE.name,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun languageEnum(): AppLanguage = AppLanguage.fromRaw(preferredLanguage)
}

data class ConceptMastery(
    val id: String = "",
    val userId: String = "local_family",
    val profileId: String = "",
    val conceptId: String = "",
    val conceptTitle: String = "",
    val subject: String = "",
    val lessonId: String = "",
    val independentCorrect: Int = 0,
    val hintAssistedCorrect: Int = 0,
    val attempts: Int = 0,
    val misconceptions: List<String> = emptyList(),
    val needsPractice: Boolean = false,
    val nextDayRecallDue: Boolean = false,
    val nextDayRecallPassed: Boolean = false,
    val lastMethodUsed: String = TeachingMethod.STORY_SCENE.name,
    val demonstratedEvidence: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class LearningHistoryEntry(
    val id: String = "",
    val userId: String = "local_family",
    val profileId: String = "",
    val lessonId: String = "",
    val lessonTitle: String = "",
    val subject: String = "",
    val storyStyle: String = StoryStyle.HUMAN_ADVENTURE.name,
    val language: String = AppLanguage.ENGLISH.name,
    val summary: String = "",
    val checkpointIndex: Int = 0,
    val completed: Boolean = false,
    val durationMinutes: Int = 5,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class WeeklyTestRecord(
    val id: String = "",
    val userId: String = "local_family",
    val profileId: String = "",
    val testDate: String = "",
    val subject: String = "",
    val totalQuestions: Int = 1,
    val correctCount: Int = 0,
    val currentQuestionIndex: Int = 0,
    val completed: Boolean = false,
    val conceptsNeedingRevision: List<String> = emptyList(),
    val answersSummary: List<String> = emptyList(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class VoiceLanguageSettings(
    val id: String = "voice_and_app",
    val userId: String = "local_family",
    val selectedLanguage: String = AppLanguage.ENGLISH.name,
    val selectedStoryStyle: String = StoryStyle.HUMAN_ADVENTURE.name,
    val speechSpeed: Double = 1.0,
    val narrationVolume: Double = 0.9,
    val musicVolume: Double = 0.35,
    val captionsEnabled: Boolean = true,
    val lightweightMode: Boolean = false,
    val highResolutionMode: Boolean = true,
    val indianEpicModeEnabled: Boolean = true,
    val offlinePackDownloaded: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun languageEnum(): AppLanguage = AppLanguage.fromRaw(selectedLanguage)
    fun storyStyleEnum(): StoryStyle = StoryStyle.fromRaw(selectedStoryStyle)
}

// Lesson & Classroom Domain Models
enum class LessonReadiness(val badgeLabel: String) {
    FULL_STORY_READY("Ready to Play • Full Story"),
    AI_GUIDED_READY("AI Teacher Guided")
}

enum class SceneVisualType {
    LAMPS_COURTYARD_TWO_PLUS_ONE,
    LAMPS_TAP_COUNTING_GUIDED,
    LAMPS_REARRANGED_CHECK,
    LAMPS_NEXT_DAY_MARIGOLDS,
    JEYPORE_PUDDLE_HOOK,
    METAL_PLATE_EXPERIMENT,
    HOLOGRAPHIC_WATER_CYCLE,
    SUNNY_TERRACE_CLOTHES_CHECK,
    NEXT_DAY_BOWL_RECALL,
    GENERAL_STORY_STAGE
}

enum class SceneInteractionType {
    CONTINUE_ONLY,
    NUMERIC_OR_SPEECH_ANSWER,
    TAP_EACH_LAMP_GUIDED,
    CHOICE_OR_EXPLAIN,
    FREE_SPEECH_OR_TYPE
}

data class StoryChoice(
    val id: String,
    val label: MultilingualText,
    val isCorrect: Boolean,
    val misconceptionTag: String? = null
)

data class StoryScene(
    val id: String,
    val beatTag: String,
    val visualType: SceneVisualType,
    val interactionType: SceneInteractionType,
    val drawableResId: Int? = null,
    val characterLine: MultilingualText? = null,
    val teacherNarration: MultilingualText,
    val pauseQuestion: MultilingualText? = null,
    val expectedKeywords: List<String> = emptyList(),
    val expectedNumber: Int? = null,
    val choices: List<StoryChoice> = emptyList(),
    val adaptiveRetryPrompt: MultilingualText? = null,
    val memoryLine: MultilingualText? = null,
    val successPraiseTemplate: MultilingualText? = null,
    val alternativeMethod: TeachingMethod = TeachingMethod.STORY_SCENE,
    val isNextDayRecallScene: Boolean = false
)

data class StoryLesson(
    val id: String,
    val title: MultilingualText,
    val subtitle: MultilingualText,
    val targetClass: String,
    val subject: String,
    val conceptId: String,
    val conceptTitle: String,
    val recommendedStyle: StoryStyle,
    val readiness: LessonReadiness,
    val heroDrawableResId: Int,
    val offlineAvailable: Boolean,
    val scenes: List<StoryScene>
)

data class ChatTurn(
    val id: String,
    val isTeacher: Boolean,
    val text: String,
    val methodUsed: TeachingMethod? = null,
    val audioSourceLabel: String? = null,
    val isStreaming: Boolean = false
)

data class HomeworkAnalysis(
    val recognizedQuestion: String,
    val subjectDetected: String,
    val imageClarityOk: Boolean,
    val clarityFeedback: String,
    val firstStepExplanation: String,
    val guidingQuestion: String
)

data class WeeklyTestQuestion(
    val id: String,
    val subject: String,
    val conceptId: String,
    val conceptTitle: String,
    val prompt: MultilingualText,
    val options: List<MultilingualText>,
    val correctIndex: Int,
    val hint: MultilingualText
)
