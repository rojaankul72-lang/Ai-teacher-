package com.example.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.TeacherAudioEngine
import com.example.data.ai.AiConnectionStatus
import com.example.data.ai.TeacherAiService
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.ChatTurn
import com.example.data.model.ConceptMastery
import com.example.data.model.HomeworkAnalysis
import com.example.data.model.LearningHistoryEntry
import com.example.data.model.StoryChoice
import com.example.data.model.StoryLesson
import com.example.data.model.StoryScene
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.TeachingMethod
import com.example.data.model.VoiceLanguageSettings
import com.example.data.model.WeeklyTestQuestion
import com.example.data.local.EducationRepository
import com.example.data.model.WeeklyTestRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

private const val TAG = "MainViewModel"

sealed interface DataState<out T> {
    data object Loading : DataState<Nothing>
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val message: String) : DataState<Nothing>
}

data class StoryClassroomState(
    val lesson: StoryLesson = LessonCatalog.lessonThreeFestivalLamps,
    val sceneIndex: Int = 0,
    val nextDayRecallMode: Boolean = false,
    val tappedLampIndices: Set<Int> = emptySet(),
    val childInputText: String = "",
    val feedbackMessage: String = "",
    val isFeedbackPositive: Boolean? = null,
    val showAdaptiveRetryHint: Boolean = false,
    val currentMethod: TeachingMethod = TeachingMethod.STORY_SCENE,
    val independentCorrectCount: Int = 0,
    val hintAssistedCount: Int = 0,
    val attemptsCount: Int = 0,
    val misconceptions: List<String> = emptyList(),
    val lessonCompleted: Boolean = false,
    val isEvaluatingWithAi: Boolean = false
) {
    val activeScene: StoryScene?
        get() = lesson.scenes.getOrNull(sceneIndex)
}

data class LiveClassroomState(
    val subject: String = "English",
    val topic: String = "First Meeting & Concept Check",
    val currentMethod: TeachingMethod = TeachingMethod.STORY_SCENE,
    val turns: List<ChatTurn> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val aiStatus: AiConnectionStatus = AiConnectionStatus.Ready
)

data class HomeworkState(
    val selectedBitmap: Bitmap? = null,
    val isCameraActive: Boolean = false,
    val useFrontCamera: Boolean = false,
    val isAnalyzing: Boolean = false,
    val analysis: HomeworkAnalysis? = null,
    val editableQuestion: String = "",
    val childAttemptText: String = "",
    val coachFeedback: String = "",
    val errorMessage: String? = null
)

data class ActiveWeeklyTestState(
    val subject: String = "",
    val questions: List<WeeklyTestQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val conceptsNeedingRevision: List<String> = emptyList(),
    val answersSummary: List<String> = emptyList(),
    val lastFeedback: String = "",
    val completed: Boolean = false
)

class MainViewModel(
    private val repository: EducationRepository,
    private val aiService: TeacherAiService,
    val audioEngine: TeacherAudioEngine,
    val currentUserId: String = "local_family"
) : ViewModel() {

    val profilesState: StateFlow<DataState<List<StudentProfile>>> =
        repository.observeProfiles()
            .map<List<StudentProfile>, DataState<List<StudentProfile>>> { DataState.Success(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing profiles", error)
                emit(DataState.Error(error.message ?: "Failed to load profiles"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataState.Loading)

    val masteryList: StateFlow<List<ConceptMastery>> =
        repository.observeConceptMastery()
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing concept mastery", error)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val sessionHistory: StateFlow<List<LearningHistoryEntry>> =
        repository.observeSessions()
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing sessions", error)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val weeklyTests: StateFlow<List<WeeklyTestRecord>> =
        repository.observeWeeklyTests()
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing weekly tests", error)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val settings: StateFlow<VoiceLanguageSettings> =
        repository.observeSettings()
            .catch { error ->
                if (error is CancellationException) throw error
                Log.w(TAG, "Error observing settings", error)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                VoiceLanguageSettings(userId = currentUserId)
            )

    private val _selectedProfileId = MutableStateFlow<String?>(null)
    val activeProfile: StateFlow<StudentProfile?> = combine(
        profilesState,
        _selectedProfileId
    ) { state, selectedId ->
        val list = (state as? DataState.Success)?.data.orEmpty()
        list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _storyState = MutableStateFlow(StoryClassroomState())
    val storyState: StateFlow<StoryClassroomState> = _storyState.asStateFlow()

    private val _liveState = MutableStateFlow(LiveClassroomState())
    val liveState: StateFlow<LiveClassroomState> = _liveState.asStateFlow()

    private val _homeworkState = MutableStateFlow(HomeworkState())
    val homeworkState: StateFlow<HomeworkState> = _homeworkState.asStateFlow()

    private val _activeWeeklyTest = MutableStateFlow<ActiveWeeklyTestState?>(null)
    val activeWeeklyTest: StateFlow<ActiveWeeklyTestState?> = _activeWeeklyTest.asStateFlow()

    private val _sundayPreviewOverride = MutableStateFlow<Boolean?>(null)
    val isSundayTestDay: StateFlow<Boolean> = _sundayPreviewOverride
        .map { override -> override ?: (LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY
        )

    private val _homeworkCheckAnswer = MutableStateFlow<Boolean?>(null)
    val hasHomeworkToday: StateFlow<Boolean?> = _homeworkCheckAnswer.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val audioState: StateFlow<AudioPlaybackState> = audioEngine.state

    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            val seedRes = repository.ensureStarterDataSeeded()
            if (seedRes.isFailure) {
                _snackbarMessage.value =
                    seedRes.exceptionOrNull()?.localizedMessage ?: "Could not sync starter profiles"
            }
        }
        viewModelScope.launch {
            settings.collect { s ->
                audioEngine.updateAudioControls(
                    speed = s.speechSpeed.toFloat(),
                    narrationVolume = s.narrationVolume.toFloat(),
                    musicVolume = s.musicVolume.toFloat()
                )
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setHasHomeworkToday(hasHomework: Boolean) {
        _homeworkCheckAnswer.value = hasHomework
    }

    fun selectProfile(profile: StudentProfile) {
        _selectedProfileId.value = profile.id
        // Sync preferred teaching language to profile preference when switching children
        viewModelScope.launch {
            val currentSettings = settings.value
            if (currentSettings.selectedLanguage != profile.preferredLanguage) {
                repository.saveSettings(
                    currentSettings.copy(selectedLanguage = profile.preferredLanguage)
                )
            }
        }
        // Reset live classroom greeting for returning child
        resetLiveConversation(profile)
    }

    fun saveStudentProfile(profile: StudentProfile, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveProfile(profile)
            if (res.isSuccess) {
                _selectedProfileId.value = res.getOrNull()
                _snackbarMessage.value = "Saved ${profile.preferredName}'s profile"
                onSaved()
            } else {
                _snackbarMessage.value =
                    res.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
            }
        }
    }

    fun deleteStudentProfile(profileId: String) {
        viewModelScope.launch {
            val res = repository.deleteProfile(profileId)
            if (res.isSuccess) {
                if (_selectedProfileId.value == profileId) {
                    _selectedProfileId.value = null
                }
                _snackbarMessage.value = "Profile removed"
            } else {
                _snackbarMessage.value =
                    res.exceptionOrNull()?.localizedMessage ?: "Failed to remove profile"
            }
        }
    }

    fun updateSubjectDuration(profile: StudentProfile, subject: String, minutes: Long) {
        val updatedMap = profile.subjectDurations.toMutableMap().apply {
            this[subject] = minutes.coerceIn(5L, 90L)
        }
        val updatedSubjects = if (profile.subjects.contains(subject)) {
            profile.subjects
        } else {
            profile.subjects + subject
        }
        saveStudentProfile(
            profile.copy(subjects = updatedSubjects, subjectDurations = updatedMap)
        )
    }

    fun addSubjectToProfile(profile: StudentProfile, newSubject: String, durationMinutes: Long = 20L) {
        val trimmed = newSubject.trim().take(40)
        if (trimmed.isBlank()) return
        val updatedSubjects = (profile.subjects + trimmed).distinct().take(15)
        val updatedMap = profile.subjectDurations.toMutableMap().apply {
            this[trimmed] = durationMinutes.coerceIn(5L, 90L)
        }
        saveStudentProfile(profile.copy(subjects = updatedSubjects, subjectDurations = updatedMap))
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val updated = settings.value.copy(selectedLanguage = language.name)
            val res = repository.saveSettings(updated)
            if (res.isFailure) {
                _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage
            }
            activeProfile.value?.let { p ->
                repository.saveProfile(p.copy(preferredLanguage = language.name))
            }
        }
    }

    fun updateSettings(transform: (VoiceLanguageSettings) -> VoiceLanguageSettings) {
        viewModelScope.launch {
            val updated = transform(settings.value)
            val res = repository.saveSettings(updated)
            if (res.isFailure) {
                _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun toggleMusicPlayback() {
        if (audioState.value.isMusicPlaying) {
            audioEngine.stopAmbientMusic()
        } else {
            audioEngine.startAmbientMusic()
        }
    }

    fun playVoiceSample() {
        val lang = settings.value.languageEnum()
        val name = activeProfile.value?.preferredName ?: "Barsha"
        val sample = when (lang) {
            AppLanguage.ENGLISH -> "Namaste $name! I am Meera Didi, your AI teacher. Let us turn today's lesson into a wonderful story!"
            AppLanguage.HINDI -> "नमस्ते $name! मैं मीरा दीदी हूँ, आपकी एआई शिक्षिका। चलो आज के पाठ को एक प्यारी कहानी बनाते हैं!"
            AppLanguage.ODIA -> "ନମସ୍କାର $name! ମୁଁ ମୀରା ଦିଦି, ତୁମର ଏଆଇ ଶିକ୍ଷୟିତ୍ରୀ। ଚାଲ ଆଜିର ପାଠକୁ ଗୋଟିଏ ସୁନ୍ଦର କାହାଣୀ କରିବା!"
        }
        audioEngine.speak(sample, lang, preferGeminiTts = true, isTenderMoment = true)
    }

    // --- STORY CLASSROOM ENGINE ---

    fun openLesson(
        lessonId: String,
        resumeFromSavedCheckpoint: Boolean = false,
        nextDayRecallOnly: Boolean = false
    ) {
        val lesson = LessonCatalog.lessonById(lessonId)
        val profile = activeProfile.value
        val startIndex = when {
            nextDayRecallOnly && lesson.scenes.isNotEmpty() ->
                lesson.scenes.indexOfFirst { it.isNextDayRecallScene }.takeIf { it >= 0 } ?: 0
            resumeFromSavedCheckpoint && profile != null && profile.lastLessonId == lesson.id ->
                profile.lastStepIndex.coerceIn(0, (lesson.scenes.size - 1).coerceAtLeast(0))
            else -> 0
        }

        _storyState.value = StoryClassroomState(
            lesson = lesson,
            sceneIndex = startIndex,
            nextDayRecallMode = nextDayRecallOnly,
            currentMethod = TeachingMethod.STORY_SCENE
        )

        speakCurrentStoryScene()
        preloadNextStoryScene()
    }

    fun speakCurrentStoryScene() {
        val st = _storyState.value
        val scene = st.activeScene ?: return
        val lang = settings.value.languageEnum()
        val childName = activeProfile.value?.preferredName ?: "Friend"
        val narration = scene.teacherNarration.get(lang).replace("{name}", childName)
        val question = scene.pauseQuestion?.get(lang)?.replace("{name}", childName).orEmpty()
        val combined = if (question.isNotBlank()) "$narration $question" else narration
        audioEngine.speak(combined, lang, preferGeminiTts = true)
    }

    private fun preloadNextStoryScene() {
        val st = _storyState.value
        val nextScene = st.lesson.scenes.getOrNull(st.sceneIndex + 1) ?: return
        val lang = settings.value.languageEnum()
        val childName = activeProfile.value?.preferredName ?: "Friend"
        val line = nextScene.teacherNarration.get(lang).replace("{name}", childName)
        audioEngine.preloadLine(line, lang)
    }

    fun onLampTappedInStory(lampIndex: Int) {
        val st = _storyState.value
        val scene = st.activeScene ?: return
        if (lampIndex in st.tappedLampIndices) {
            val lang = settings.value.languageEnum()
            val reminder = when (lang) {
                AppLanguage.ENGLISH -> "You already counted that lamp! Try a glowing lamp you haven't tapped yet."
                AppLanguage.HINDI -> "यह दीया तुम गिन चुकी हो! अब कोई दूसरा दीया छुओ।"
                AppLanguage.ODIA -> "ଏହି ଦୀପଟି ଗଣା ସରିଛି! ଏବେ ଅନ୍ୟ ଗୋଟିଏ ଦୀପକୁ ଛୁଅଁ।"
            }
            _storyState.value = st.copy(feedbackMessage = reminder, isFeedbackPositive = false)
            audioEngine.speak(reminder, lang, preferGeminiTts = false)
            return
        }

        audioEngine.playLampNote(lampIndex)
        val updatedSet = st.tappedLampIndices + lampIndex
        val count = updatedSet.size
        val lang = settings.value.languageEnum()
        val msg = when {
            count < (scene.expectedNumber ?: 3) -> "$count..."
            else -> {
                audioEngine.playCelebrationChime()
                val childName = activeProfile.value?.preferredName ?: "Barsha"
                scene.successPraiseTemplate?.get(lang)?.replace("{name}", childName)
                    ?: "1, 2, 3! Every lamp counted once!"
            }
        }
        val finishedGuided = count >= (scene.expectedNumber ?: 3)
        _storyState.value = st.copy(
            tappedLampIndices = updatedSet,
            feedbackMessage = msg,
            isFeedbackPositive = if (finishedGuided) true else null,
            hintAssistedCount = if (finishedGuided) st.hintAssistedCount + 1 else st.hintAssistedCount,
            attemptsCount = if (finishedGuided) st.attemptsCount + 1 else st.attemptsCount
        )
        if (finishedGuided) {
            audioEngine.speak(msg, lang, preferGeminiTts = true, isTenderMoment = true)
            persistLessonCheckpoint(completedScene = true)
        }
    }

    fun submitStoryChoice(choice: StoryChoice) {
        val st = _storyState.value
        val scene = st.activeScene ?: return
        val lang = settings.value.languageEnum()
        val childName = activeProfile.value?.preferredName ?: "Friend"

        if (choice.isCorrect) {
            audioEngine.playCelebrationChime()
            val praise = scene.successPraiseTemplate?.get(lang)?.replace("{name}", childName)
                ?: "Wonderful, $childName!"
            val usedHint = st.showAdaptiveRetryHint
            _storyState.value = st.copy(
                feedbackMessage = praise,
                isFeedbackPositive = true,
                independentCorrectCount = if (!usedHint) st.independentCorrectCount + 1 else st.independentCorrectCount,
                hintAssistedCount = if (usedHint) st.hintAssistedCount + 1 else st.hintAssistedCount,
                attemptsCount = st.attemptsCount + 1
            )
            audioEngine.speak(praise, lang, preferGeminiTts = true, isTenderMoment = true)
            persistLessonCheckpoint(completedScene = true)
        } else {
            // Wrong answer -> switch explanation method, never repeat louder!
            val newMisconceptions = if (!choice.misconceptionTag.isNullOrBlank()) {
                (st.misconceptions + choice.misconceptionTag).distinct()
            } else st.misconceptions

            val retryMsg = scene.adaptiveRetryPrompt?.get(lang)
                ?: when (lang) {
                    AppLanguage.ENGLISH -> "Let us try a different way together using ${scene.alternativeMethod.label}!"
                    AppLanguage.HINDI -> "कोई बात नहीं! चलो इसे दूसरे तरीके से समझते हैं: ${scene.alternativeMethod.label}।"
                    AppLanguage.ODIA -> "କିଛି କଥା ନାହିଁ! ଚାଲ ଏହାକୁ ଅନ୍ୟ ଉପାୟରେ ବୁଝିବା: ${scene.alternativeMethod.label}।"
                }
            _storyState.value = st.copy(
                feedbackMessage = retryMsg,
                isFeedbackPositive = false,
                showAdaptiveRetryHint = true,
                currentMethod = scene.alternativeMethod,
                attemptsCount = st.attemptsCount + 1,
                misconceptions = newMisconceptions
            )
            audioEngine.speak(retryMsg, lang, preferGeminiTts = true)
            persistLessonCheckpoint(completedScene = false)
        }
    }

    fun updateStoryInputText(text: String) {
        _storyState.value = _storyState.value.copy(childInputText = text)
    }

    fun submitStoryFreeAnswer(answerText: String) {
        val st = _storyState.value
        val scene = st.activeScene ?: return
        val trimmed = answerText.trim()
        if (trimmed.isBlank()) return

        val lang = settings.value.languageEnum()
        val childName = activeProfile.value?.preferredName ?: "Friend"
        val profile = activeProfile.value

        // Fast offline keyword/number match when offline or matching exact story answer
        val lower = trimmed.lowercase()
        val matchedKeyword = scene.expectedKeywords.any { lower.contains(it.lowercase()) }
        val matchedNum = scene.expectedNumber?.let { num ->
            lower.contains(num.toString())
        } ?: false

        if (matchedKeyword || matchedNum) {
            audioEngine.playCelebrationChime()
            val praise = scene.successPraiseTemplate?.get(lang)?.replace("{name}", childName)
                ?: "Great job, $childName!"
            val usedHint = st.showAdaptiveRetryHint
            _storyState.value = st.copy(
                childInputText = "",
                feedbackMessage = praise,
                isFeedbackPositive = true,
                independentCorrectCount = if (!usedHint) st.independentCorrectCount + 1 else st.independentCorrectCount,
                hintAssistedCount = if (usedHint) st.hintAssistedCount + 1 else st.hintAssistedCount,
                attemptsCount = st.attemptsCount + 1
            )
            audioEngine.speak(praise, lang, preferGeminiTts = true, isTenderMoment = true)
            persistLessonCheckpoint(completedScene = true)
            return
        }

        // Otherwise ask Gemini 3.8 Flash to evaluate mixed-language meaning & pick a new teaching method!
        if (profile == null) return
        _storyState.value = st.copy(isEvaluatingWithAi = true)
        viewModelScope.launch {
            val res = aiService.evaluateAdaptiveResponse(
                profile = profile,
                language = lang,
                subject = st.lesson.subject,
                conceptTitle = st.lesson.conceptTitle,
                questionAsked = scene.pauseQuestion?.get(lang).orEmpty(),
                childAnswer = trimmed,
                previousMethod = st.currentMethod
            )
            res.onSuccess { reply ->
                val msg = buildString {
                    append(reply.teacherReply)
                    if (reply.nextQuestion.isNotBlank()) {
                        append("\n\n")
                        append(reply.nextQuestion)
                    }
                }
                if (reply.understood) audioEngine.playCelebrationChime()
                val updatedMisconceptions = if (reply.misconceptionDetected.isNotBlank()) {
                    (st.misconceptions + reply.misconceptionDetected).distinct()
                } else st.misconceptions

                _storyState.value = _storyState.value.copy(
                    isEvaluatingWithAi = false,
                    childInputText = "",
                    feedbackMessage = msg,
                    isFeedbackPositive = reply.understood,
                    showAdaptiveRetryHint = !reply.understood,
                    currentMethod = reply.methodUsed,
                    independentCorrectCount = if (reply.understood && !reply.usedHint && !st.showAdaptiveRetryHint) st.independentCorrectCount + 1 else st.independentCorrectCount,
                    hintAssistedCount = if (reply.understood && (reply.usedHint || st.showAdaptiveRetryHint)) st.hintAssistedCount + 1 else st.hintAssistedCount,
                    attemptsCount = st.attemptsCount + 1,
                    misconceptions = updatedMisconceptions
                )
                audioEngine.speak(msg, lang, preferGeminiTts = true)
                persistLessonCheckpoint(completedScene = reply.understood)
            }.onFailure { err ->
                val fallbackMsg = scene.adaptiveRetryPrompt?.get(lang)
                    ?: "Let us try another way: ${scene.alternativeMethod.label}. (${err.localizedMessage ?: "Offline mode"})"
                _storyState.value = _storyState.value.copy(
                    isEvaluatingWithAi = false,
                    feedbackMessage = fallbackMsg,
                    isFeedbackPositive = false,
                    showAdaptiveRetryHint = true,
                    currentMethod = scene.alternativeMethod,
                    attemptsCount = st.attemptsCount + 1
                )
                audioEngine.speak(fallbackMsg, lang, preferGeminiTts = false)
            }
        }
    }

    fun advanceToNextScene() {
        val st = _storyState.value
        val nextIdx = st.sceneIndex + 1
        if (nextIdx >= st.lesson.scenes.size) {
            _storyState.value = st.copy(lessonCompleted = true)
            audioEngine.playCelebrationChime()
            persistLessonCheckpoint(completedScene = true, finishedLesson = true)
            return
        }
        _storyState.value = st.copy(
            sceneIndex = nextIdx,
            tappedLampIndices = emptySet(),
            childInputText = "",
            feedbackMessage = "",
            isFeedbackPositive = null,
            showAdaptiveRetryHint = false,
            currentMethod = TeachingMethod.STORY_SCENE
        )
        persistLessonCheckpoint(completedScene = false)
        speakCurrentStoryScene()
        preloadNextStoryScene()
    }

    private fun persistLessonCheckpoint(completedScene: Boolean, finishedLesson: Boolean = false) {
        val profile = activeProfile.value ?: return
        val st = _storyState.value
        val lang = settings.value.languageEnum()
        viewModelScope.launch {
            repository.updateLessonCheckpoint(
                profileId = profile.id,
                lessonId = st.lesson.id,
                lessonTitle = st.lesson.title.get(lang),
                conceptId = st.lesson.conceptId,
                stepIndex = st.sceneIndex,
                teachingApproach = st.currentMethod.name
            )
            val total = st.attemptsCount.coerceAtLeast(1)
            val evidence = "${st.independentCorrectCount} of $total answered independently"
            val currentMastery = ConceptMastery(
                id = "${profile.id}__${st.lesson.conceptId}",
                userId = currentUserId,
                profileId = profile.id,
                conceptId = st.lesson.conceptId,
                conceptTitle = st.lesson.conceptTitle,
                subject = st.lesson.subject,
                lessonId = st.lesson.id,
                independentCorrect = st.independentCorrectCount,
                hintAssistedCorrect = st.hintAssistedCount,
                attempts = total,
                misconceptions = st.misconceptions,
                needsPractice = st.misconceptions.isNotEmpty() || st.independentCorrectCount == 0,
                nextDayRecallDue = finishedLesson && !st.nextDayRecallMode,
                nextDayRecallPassed = st.nextDayRecallMode && completedScene,
                lastMethodUsed = st.currentMethod.name,
                demonstratedEvidence = evidence
            )
            repository.upsertConceptMastery(currentMastery)
            repository.logLearningSession(
                LearningHistoryEntry(
                    id = "sess_${profile.id}_${st.lesson.id}",
                    userId = currentUserId,
                    profileId = profile.id,
                    lessonId = st.lesson.id,
                    lessonTitle = st.lesson.title.get(lang),
                    subject = st.lesson.subject,
                    storyStyle = settings.value.selectedStoryStyle,
                    language = lang.name,
                    summary = "$evidence • Method: ${st.currentMethod.label}",
                    checkpointIndex = st.sceneIndex,
                    completed = finishedLesson,
                    durationMinutes = (profile.subjectDurations[st.lesson.subject] ?: 20L).toInt()
                )
            )
        }
    }

    // --- LIVE CLASSROOM ENGINE ---

    fun resetLiveConversation(profileOverride: StudentProfile? = null) {
        val profile = profileOverride ?: activeProfile.value ?: return
        val lang = settings.value.languageEnum()
        val returningHint = if (profile.lastLessonTitle.isNotBlank()) {
            when (lang) {
                AppLanguage.ENGLISH -> "Welcome back, ${profile.preferredName}! Last time we explored ${profile.lastLessonTitle}. "
                AppLanguage.HINDI -> "वापसी पर स्वागत है, ${profile.preferredName}! पिछली बार हमने '${profile.lastLessonTitle}' पढ़ा था। "
                AppLanguage.ODIA -> "ସ୍ୱାଗତ ${profile.preferredName}! ଗତଥର ଆମେ '${profile.lastLessonTitle}' ପଢ଼ିଥିଲୁ। "
            }
        } else ""
        val firstQ = LessonCatalog.firstAssessmentQuestion(profile, lang)
        val greeting = returningHint + firstQ
        _liveState.value = LiveClassroomState(
            subject = profile.subjects.firstOrNull() ?: "English",
            topic = profile.lastLessonTitle.ifBlank { "First Meeting Assessment" },
            turns = listOf(
                ChatTurn(
                    id = "turn_0",
                    isTeacher = true,
                    text = greeting,
                    methodUsed = TeachingMethod.STORY_SCENE,
                    audioSourceLabel = audioState.value.activeVoiceEngine.label
                )
            )
        )
    }

    fun selectLiveSubject(subject: String) {
        _liveState.value = _liveState.value.copy(subject = subject)
    }

    fun updateLiveInput(text: String) {
        _liveState.value = _liveState.value.copy(inputText = text)
    }

    fun interruptAndListen() {
        audioEngine.stopNarration()
        streamJob?.cancel()
        audioEngine.startChildVoiceInput(settings.value.languageEnum())
    }

    fun confirmVoiceTranscriptForLiveClassroom() {
        val transcript = audioState.value.recognizedTranscript.trim()
        audioEngine.clearRecognizedTranscript()
        if (transcript.isNotBlank()) {
            sendLiveMessage(transcript)
        }
    }

    fun sendLiveMessage(message: String) {
        val trimmed = message.trim()
        val profile = activeProfile.value ?: return
        if (trimmed.isBlank()) return

        // Child interrupted or replied -> stop current TTS immediately!
        audioEngine.stopNarration()
        streamJob?.cancel()

        val lang = settings.value.languageEnum()
        val style = settings.value.storyStyleEnum()
        val st = _liveState.value

        // Switch teaching method explicitly if child asks "Why?", "I don't know", or "Another example"
        val nextMethod = when {
            trimmed.contains("why", true) || trimmed.contains("क्यों", true) || trimmed.contains("କାହିଁକି", true) ->
                TeachingMethod.ACT_IT_OUT
            trimmed.contains("don't know", true) || trimmed.contains("नहीं पता", true) || trimmed.contains("ଜାଣିନି", true) ->
                TeachingMethod.SHORTER_STORY
            trimmed.contains("another example", true) || trimmed.contains("उदाहरण", true) ->
                TeachingMethod.COMPARE_EXAMPLES
            else -> st.currentMethod
        }

        val childTurn = ChatTurn(
            id = "turn_child_${System.currentTimeMillis()}",
            isTeacher = false,
            text = trimmed
        )
        val teacherPlaceholderId = "turn_teacher_${System.currentTimeMillis()}"
        val updatedTurns = st.turns + childTurn + ChatTurn(
            id = teacherPlaceholderId,
            isTeacher = true,
            text = "...",
            methodUsed = nextMethod,
            isStreaming = true
        )
        _liveState.value = st.copy(
            turns = updatedTurns,
            inputText = "",
            isStreaming = true,
            currentMethod = nextMethod,
            aiStatus = AiConnectionStatus.Busy
        )

        val historyPairs = st.turns.map { it.isTeacher to it.text }
        streamJob = viewModelScope.launch {
            var latestReply = ""
            try {
                aiService.streamTeacherReply(
                    profile = profile,
                    language = lang,
                    storyStyle = style,
                    subject = st.subject,
                    topic = st.topic,
                    previousMethod = nextMethod,
                    childMessage = trimmed,
                    conversationHistory = historyPairs
                ).collect { chunkedText ->
                    latestReply = chunkedText
                    _liveState.value = _liveState.value.copy(
                        turns = _liveState.value.turns.map { turn ->
                            if (turn.id == teacherPlaceholderId) {
                                turn.copy(text = chunkedText, isStreaming = true)
                            } else turn
                        }
                    )
                }
                _liveState.value = _liveState.value.copy(
                    isStreaming = false,
                    aiStatus = AiConnectionStatus.Online("gemini-3.8-flash"),
                    turns = _liveState.value.turns.map { turn ->
                        if (turn.id == teacherPlaceholderId) {
                            turn.copy(
                                isStreaming = false,
                                audioSourceLabel = audioState.value.activeVoiceEngine.label
                            )
                        } else turn
                    }
                )
                audioEngine.speak(latestReply, lang, preferGeminiTts = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: "Live AI needs internet & App Check."
                _liveState.value = _liveState.value.copy(
                    isStreaming = false,
                    aiStatus = AiConnectionStatus.Error(errMsg),
                    turns = _liveState.value.turns.map { turn ->
                        if (turn.id == teacherPlaceholderId) {
                            turn.copy(
                                text = "Live AI connection could not finish ($errMsg). You can switch to our offline Story Classroom anytime!",
                                isStreaming = false
                            )
                        } else turn
                    }
                )
            }
        }
    }

    // --- HOMEWORK CAMERA & ANALYSIS ---

    fun setCameraActive(active: Boolean) {
        _homeworkState.value = _homeworkState.value.copy(isCameraActive = active)
    }

    fun switchCameraLens() {
        _homeworkState.value = _homeworkState.value.copy(useFrontCamera = !_homeworkState.value.useFrontCamera)
    }

    fun onHomeworkPhotoCaptured(bitmap: Bitmap) {
        _homeworkState.value = _homeworkState.value.copy(
            selectedBitmap = bitmap,
            isCameraActive = false,
            errorMessage = null,
            coachFeedback = ""
        )
        analyzeHomework(bitmap, correctedQuestion = null)
    }

    fun retakeHomeworkPhoto() {
        _homeworkState.value = _homeworkState.value.copy(
            selectedBitmap = null,
            analysis = null,
            editableQuestion = "",
            coachFeedback = "",
            errorMessage = null
        )
    }

    fun updateEditableHomeworkQuestion(text: String) {
        _homeworkState.value = _homeworkState.value.copy(editableQuestion = text)
    }

    fun updateHomeworkAttempt(text: String) {
        _homeworkState.value = _homeworkState.value.copy(childAttemptText = text)
    }

    fun reAnalyzeCorrectedHomework() {
        val bmp = _homeworkState.value.selectedBitmap ?: return
        val corrected = _homeworkState.value.editableQuestion.trim()
        if (corrected.isBlank()) return
        analyzeHomework(bmp, correctedQuestion = corrected)
    }

    private fun analyzeHomework(bitmap: Bitmap, correctedQuestion: String?) {
        val profile = activeProfile.value ?: return
        val lang = settings.value.languageEnum()
        _homeworkState.value = _homeworkState.value.copy(isAnalyzing = true, errorMessage = null)
        viewModelScope.launch {
            val res = aiService.analyzeHomeworkPhoto(bitmap, profile, lang, correctedQuestion)
            res.onSuccess { analysis ->
                _homeworkState.value = _homeworkState.value.copy(
                    isAnalyzing = false,
                    analysis = analysis,
                    editableQuestion = analysis.recognizedQuestion
                )
                val speakLine = if (!analysis.imageClarityOk) {
                    analysis.clarityFeedback
                } else {
                    "${analysis.firstStepExplanation} ${analysis.guidingQuestion}"
                }
                audioEngine.speak(speakLine, lang, preferGeminiTts = true)
            }.onFailure { err ->
                _homeworkState.value = _homeworkState.value.copy(
                    isAnalyzing = false,
                    errorMessage = err.localizedMessage ?: "Homework photo analysis needs internet."
                )
            }
        }
    }

    fun submitHomeworkStepAttempt() {
        val attempt = _homeworkState.value.childAttemptText.trim()
        val analysis = _homeworkState.value.analysis ?: return
        val profile = activeProfile.value ?: return
        val lang = settings.value.languageEnum()
        if (attempt.isBlank()) return

        _homeworkState.value = _homeworkState.value.copy(isAnalyzing = true)
        viewModelScope.launch {
            val res = aiService.evaluateAdaptiveResponse(
                profile = profile,
                language = lang,
                subject = analysis.subjectDetected,
                conceptTitle = analysis.recognizedQuestion.take(60),
                questionAsked = analysis.guidingQuestion,
                childAnswer = attempt,
                previousMethod = TeachingMethod.SOLVE_TOGETHER
            )
            res.onSuccess { reply ->
                val feedback = "${reply.teacherReply}\n${reply.nextQuestion}".trim()
                _homeworkState.value = _homeworkState.value.copy(
                    isAnalyzing = false,
                    childAttemptText = "",
                    coachFeedback = feedback
                )
                audioEngine.speak(feedback, lang, preferGeminiTts = true)
            }.onFailure { err ->
                _homeworkState.value = _homeworkState.value.copy(
                    isAnalyzing = false,
                    errorMessage = err.localizedMessage ?: "Could not evaluate attempt."
                )
            }
        }
    }

    // --- SUNDAY WEEKLY TEST DAY ---

    fun setSundayPreviewOverride(enabled: Boolean?) {
        _sundayPreviewOverride.value = enabled
    }

    fun startOrResumeWeeklyTest(subject: String) {
        val profile = activeProfile.value ?: return
        val todayStr = LocalDate.now().toString()
        val saved = weeklyTests.value.firstOrNull {
            it.profileId == profile.id && it.testDate == todayStr && it.subject == subject
        }
        val questions = LessonCatalog.weeklyTestForSubject(subject, profile.studentClass)
        if (saved != null && !saved.completed) {
            _activeWeeklyTest.value = ActiveWeeklyTestState(
                subject = subject,
                questions = questions,
                currentIndex = saved.currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0)),
                correctCount = saved.correctCount,
                conceptsNeedingRevision = saved.conceptsNeedingRevision,
                answersSummary = saved.answersSummary,
                completed = false
            )
        } else {
            _activeWeeklyTest.value = ActiveWeeklyTestState(
                subject = subject,
                questions = questions,
                currentIndex = 0,
                correctCount = 0
            )
        }
    }

    fun answerWeeklyTestQuestion(selectedOptionIndex: Int) {
        val test = _activeWeeklyTest.value ?: return
        val q = test.questions.getOrNull(test.currentIndex) ?: return
        val profile = activeProfile.value ?: return
        val lang = settings.value.languageEnum()
        val isCorrect = selectedOptionIndex == q.correctIndex

        val newCorrect = if (isCorrect) test.correctCount + 1 else test.correctCount
        val newRevision = if (!isCorrect) (test.conceptsNeedingRevision + q.conceptTitle).distinct() else test.conceptsNeedingRevision
        val summaryLine = "${q.conceptTitle}: ${if (isCorrect) "Independent ✓" else "Needs revision"}"
        val newSummary = test.answersSummary + summaryLine
        val nextIndex = test.currentIndex + 1
        val finished = nextIndex >= test.questions.size

        val feedback = if (isCorrect) {
            audioEngine.playCelebrationChime()
            "Well done, ${profile.preferredName}! ${q.conceptTitle} is strong."
        } else {
            "Good try! Hint: ${q.hint.get(lang)}"
        }

        _activeWeeklyTest.value = test.copy(
            currentIndex = if (finished) test.currentIndex else nextIndex,
            correctCount = newCorrect,
            conceptsNeedingRevision = newRevision,
            answersSummary = newSummary,
            lastFeedback = feedback,
            completed = finished
        )

        saveWeeklyTestToFirestore(
            profile = profile,
            subject = test.subject,
            total = test.questions.size,
            correct = newCorrect,
            currentIdx = if (finished) test.questions.size else nextIndex,
            completed = finished,
            revision = newRevision,
            summary = newSummary
        )
    }

    fun takeBreakAndSaveWeeklyTest() {
        val test = _activeWeeklyTest.value ?: return
        val profile = activeProfile.value ?: return
        saveWeeklyTestToFirestore(
            profile = profile,
            subject = test.subject,
            total = test.questions.size,
            correct = test.correctCount,
            currentIdx = test.currentIndex,
            completed = false,
            revision = test.conceptsNeedingRevision,
            summary = test.answersSummary
        )
        _activeWeeklyTest.value = null
        _snackbarMessage.value = "Saved unfinished ${test.subject} test—take a gentle break!"
    }

    fun dismissWeeklyTest() {
        _activeWeeklyTest.value = null
    }

    private fun saveWeeklyTestToFirestore(
        profile: StudentProfile,
        subject: String,
        total: Int,
        correct: Int,
        currentIdx: Int,
        completed: Boolean,
        revision: List<String>,
        summary: List<String>
    ) {
        val todayStr = LocalDate.now().toString()
        viewModelScope.launch {
            repository.saveWeeklyTestRecord(
                WeeklyTestRecord(
                    id = "test_${profile.id}_${todayStr}_$subject",
                    userId = currentUserId,
                    profileId = profile.id,
                    testDate = todayStr,
                    subject = subject,
                    totalQuestions = total,
                    correctCount = correct,
                    currentQuestionIndex = currentIdx,
                    completed = completed,
                    conceptsNeedingRevision = revision,
                    answersSummary = summary
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
