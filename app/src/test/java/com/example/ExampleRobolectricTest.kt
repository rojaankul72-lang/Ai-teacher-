package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioPlaybackState
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.LessonReadiness
import com.example.data.model.TeachingMethod
import com.example.data.model.VoiceLanguageSettings
import com.example.ui.ActiveWeeklyTestState
import com.example.ui.HomeworkState
import com.example.ui.StoryClassroomState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeworkCameraScreen
import com.example.ui.screens.StoryClassroomScreen
import com.example.ui.screens.WeeklyTestsScreen
import com.example.ui.theme.BharatShikshaTheme
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `app name matches AI Bharat Shiksha`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Bharat Shiksha", appName)
    }

    @Test
    fun `multilingual fonts including Odia and Devanagari are bundled and loadable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(ResourcesCompat.getFont(context, R.font.outfit))
        assertNotNull(ResourcesCompat.getFont(context, R.font.plus_jakarta_sans))
        assertNotNull(ResourcesCompat.getFont(context, R.font.noto_sans_oriya))
        assertNotNull(ResourcesCompat.getFont(context, R.font.noto_sans_devanagari))
    }

    @Test
    fun `curriculum catalog includes full Lesson A and Lesson B plus LKG to Class 10`() {
        val barsha = LessonCatalog.starterBarshaProfile("u1")
        val raghav = LessonCatalog.starterRaghavProfile("u1")
        assertEquals("UKG", barsha.studentClass)
        assertEquals(20L, barsha.subjectDurations["English"])
        assertEquals("Class 4", raghav.studentClass)

        val lessonA = LessonCatalog.lessonThreeFestivalLamps
        assertEquals(LessonReadiness.FULL_STORY_READY, lessonA.readiness)
        assertEquals(4, lessonA.scenes.size)
        assertTrue(lessonA.scenes.any { it.isNextDayRecallScene })
        assertEquals(
            "एक-एक दीप गिनें, हर दीप को एक बार गिनें।",
            lessonA.scenes[2].memoryLine?.get(AppLanguage.HINDI)
        )
        assertTrue(lessonA.scenes[2].memoryLine?.get(AppLanguage.ODIA)?.contains("ଦୀପ") == true)

        val lessonB = LessonCatalog.lessonDayDropCameHome
        assertEquals(LessonReadiness.FULL_STORY_READY, lessonB.readiness)
        assertEquals(5, lessonB.scenes.size)
        assertTrue(lessonB.scenes.last().isNextDayRecallScene)

        val guidedCatalog = LessonCatalog.additionalLessons
        assertTrue(guidedCatalog.any { it.targetClass == "LKG" && it.readiness == LessonReadiness.AI_GUIDED_READY })
        assertTrue(guidedCatalog.any { it.targetClass == "Class 10" && it.readiness == LessonReadiness.AI_GUIDED_READY })
    }

    @Test
    fun `home screen supports Barsha and Raghav switching Odia selection and one tap continue`() {
        val barsha = LessonCatalog.starterBarshaProfile("uid_test")
        val raghav = LessonCatalog.starterRaghavProfile("uid_test")
        var selectedProfile by mutableStateOf(barsha)
        var selectedLang by mutableStateOf(AppLanguage.ENGLISH)
        var continuedLessonId by mutableStateOf<String?>(null)

        composeTestRule.setContent {
            BharatShikshaTheme {
                HomeScreen(
                    activeProfile = selectedProfile,
                    allProfiles = listOf(barsha, raghav),
                    language = selectedLang,
                    isSundayTestDay = false,
                    hasHomeworkToday = null,
                    onSelectProfile = { selectedProfile = it },
                    onSelectLanguage = { selectedLang = it },
                    onContinueLesson = { continuedLessonId = it },
                    onOpenLessonDirect = { id, _ -> continuedLessonId = id },
                    onHomeworkAnswer = {},
                    onOpenHomeworkCamera = {},
                    onOpenLiveClassroom = {},
                    onOpenProfiles = {},
                    onOpenTimetable = {},
                    onOpenWeeklyTests = {},
                    onOpenParentProgress = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Namaste, Barsha!").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_profile_chip_raghav").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Raghav", selectedProfile.preferredName)
        composeTestRule.onNodeWithText("Namaste, Raghav!").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_lang_odia").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppLanguage.ODIA, selectedLang)

        composeTestRule.onNodeWithTag("continue_my_lesson_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(LessonCatalog.LESSON_DROP_ID, continuedLessonId)
    }

    @Test
    fun `story classroom switches teaching method on wrong answer instead of repeating`() {
        val barsha = LessonCatalog.starterBarshaProfile("uid_test")
        val lessonA = LessonCatalog.lessonThreeFestivalLamps
        var storyState by mutableStateOf(
            StoryClassroomState(
                lesson = lessonA,
                sceneIndex = 0,
                currentMethod = TeachingMethod.STORY_SCENE
            )
        )

        composeTestRule.setContent {
            BharatShikshaTheme {
                StoryClassroomScreen(
                    storyState = storyState,
                    activeProfile = barsha,
                    settings = VoiceLanguageSettings(
                        userId = "uid_test",
                        selectedLanguage = AppLanguage.ENGLISH.name
                    ),
                    audioState = AudioPlaybackState(),
                    onSelectLesson = {},
                    onSelectLanguage = {},
                    onSelectStyle = {},
                    onLampTap = {},
                    onChoiceSelected = { choice ->
                        val scene = storyState.activeScene!!
                        storyState = if (choice.isCorrect) {
                            storyState.copy(
                                feedbackMessage = "Lovely, Barsha!",
                                isFeedbackPositive = true
                            )
                        } else {
                            storyState.copy(
                                feedbackMessage = scene.adaptiveRetryPrompt?.get(AppLanguage.ENGLISH).orEmpty(),
                                isFeedbackPositive = false,
                                showAdaptiveRetryHint = true,
                                currentMethod = scene.alternativeMethod
                            )
                        }
                    },
                    onInputTextChange = {},
                    onSubmitFreeAnswer = {},
                    onAdvanceScene = {},
                    onReplayAudio = {},
                    onStopAudio = {},
                    onToggleMusic = {},
                    onCycleSpeed = {},
                    onToggleCaptions = {},
                    onStartVoiceAnswer = {},
                    onConfirmVoiceTranscript = {},
                    onClearVoiceTranscript = {},
                    onOpenNextDayRecall = {},
                    onBackHome = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("story_choice_c2")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(TeachingMethod.REAL_OBJECT, storyState.currentMethod)
        assertTrue(storyState.showAdaptiveRetryHint)
        composeTestRule.onNodeWithTag("story_feedback_box")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `weekly tests screen allows starting test answering and taking a break`() {
        val raghav = LessonCatalog.starterRaghavProfile("uid_test")
        val mathQuestions = LessonCatalog.weeklyTestForSubject("Mathematics", raghav.studentClass)
        var activeTest by mutableStateOf<ActiveWeeklyTestState?>(null)
        var breakSaved by mutableStateOf(false)

        composeTestRule.setContent {
            BharatShikshaTheme {
                WeeklyTestsScreen(
                    activeProfile = raghav,
                    language = AppLanguage.ENGLISH,
                    isSundayTestDay = true,
                    savedRecords = emptyList(),
                    activeTest = activeTest,
                    onStartOrResumeTest = { subj ->
                        activeTest = ActiveWeeklyTestState(
                            subject = subj,
                            questions = mathQuestions,
                            currentIndex = 0
                        )
                    },
                    onAnswerQuestion = { idx ->
                        val test = activeTest ?: return@WeeklyTestsScreen
                        val q = test.questions[test.currentIndex]
                        activeTest = test.copy(
                            currentIndex = test.currentIndex + 1,
                            correctCount = if (idx == q.correctIndex) test.correctCount + 1 else test.correctCount
                        )
                    },
                    onTakeBreak = {
                        breakSaved = true
                        activeTest = null
                    },
                    onDismissCompleted = { activeTest = null },
                    onToggleSundayPreview = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("start_weekly_test_Mathematics")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("weekly_test_option_1")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, activeTest?.correctCount)

        composeTestRule.onNodeWithTag("weekly_test_break_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(breakSaved)
    }

    @Test
    fun `homework camera screen defaults camera off with photo picker available`() {
        val raghav = LessonCatalog.starterRaghavProfile("uid_test")
        composeTestRule.setContent {
            BharatShikshaTheme {
                HomeworkCameraScreen(
                    homeworkState = HomeworkState(isCameraActive = false),
                    activeProfile = raghav,
                    language = AppLanguage.ENGLISH,
                    onSetCameraActive = {},
                    onSwitchCameraLens = {},
                    onPhotoCaptured = {},
                    onRetake = {},
                    onEditableQuestionChange = {},
                    onReAnalyzeCorrected = {},
                    onChildAttemptChange = {},
                    onSubmitAttempt = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("start_camera_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("upload_homework_photo_button").assertIsDisplayed()
    }

    @Test
    fun `room repository seeds Barsha and Raghav and saves checkpoints without login`() = kotlinx.coroutines.test.runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            val repo = com.example.data.local.EducationRepository(db.educationDao())
            assertTrue(repo.ensureStarterDataSeeded().isSuccess)

            val profiles = repo.observeProfiles().first()
            assertEquals(2, profiles.size)
            assertTrue(profiles.any { it.preferredName == "Barsha" && it.studentClass == "UKG" })
            assertTrue(profiles.any { it.preferredName == "Raghav" && it.studentClass == "Class 4" })

            assertTrue(
                repo.updateLessonCheckpoint(
                    profileId = "sample_barsha_ukg",
                    lessonId = LessonCatalog.LESSON_LAMPS_ID,
                    lessonTitle = "The Three Festival Lamps",
                    conceptId = "counting_one_to_one",
                    stepIndex = 2,
                    teachingApproach = TeachingMethod.REAL_OBJECT.name
                ).isSuccess
            )
            val updated = repo.observeProfiles().first()
                .first { it.id == "sample_barsha_ukg" }
            assertEquals(2, updated.lastStepIndex)
        } finally {
            db.close()
        }
    }
}
