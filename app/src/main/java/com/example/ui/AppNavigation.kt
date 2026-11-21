package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audio.AudioPlaybackState
import com.example.audio.TeacherAudioEngine
import com.example.data.ai.TeacherAiService
import com.example.data.curriculum.LessonCatalog
import com.example.data.local.EducationRepository
import com.example.data.model.ConceptMastery
import com.example.data.model.LearningHistoryEntry
import com.example.data.model.StudentProfile
import com.example.data.model.VoiceLanguageSettings
import com.example.data.model.WeeklyTestRecord
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeworkCameraScreen
import com.example.ui.screens.LiveClassroomScreen
import com.example.ui.screens.ParentProgressScreen
import com.example.ui.screens.StoryClassroomScreen
import com.example.ui.screens.StudentProfilesScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.screens.VoiceLanguageSettingsScreen
import com.example.ui.screens.WeeklyTestsScreen

object Routes {
    const val HOME = "home"
    const val STORY_CLASSROOM = "story_classroom"
    const val LIVE_CLASSROOM = "live_classroom"
    const val PROFILES = "profiles"
    const val HOMEWORK_CAMERA = "homework_camera"
    const val TIMETABLE = "timetable"
    const val WEEKLY_TESTS = "weekly_tests"
    const val PARENT_PROGRESS = "parent_progress"
    const val SETTINGS = "settings"
}

private data class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private val primaryTabs = listOf(
    NavTab(Routes.HOME, "Home", Icons.Rounded.Home, "nav_home"),
    NavTab(Routes.STORY_CLASSROOM, "Story", Icons.Rounded.AutoStories, "nav_story"),
    NavTab(Routes.LIVE_CLASSROOM, "Live", Icons.Rounded.RecordVoiceOver, "nav_live"),
    NavTab(Routes.HOMEWORK_CAMERA, "Homework", Icons.Rounded.CameraAlt, "nav_homework")
)

@Composable
fun AppContent() {
    BharatShikshaShell(currentUserId = "local_family")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BharatShikshaShell(currentUserId: String) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                MainViewModel(
                    repository = EducationRepository(appContext),
                    aiService = TeacherAiService(),
                    audioEngine = TeacherAudioEngine(appContext),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    val profilesState by viewModel.profilesState.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val masteryList by viewModel.masteryList.collectAsStateWithLifecycle()
    val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
    val weeklyTests by viewModel.weeklyTests.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val storyState by viewModel.storyState.collectAsStateWithLifecycle()
    val liveState by viewModel.liveState.collectAsStateWithLifecycle()
    val homeworkState by viewModel.homeworkState.collectAsStateWithLifecycle()
    val activeWeeklyTest by viewModel.activeWeeklyTest.collectAsStateWithLifecycle()
    val isSundayTestDay by viewModel.isSundayTestDay.collectAsStateWithLifecycle()
    val hasHomeworkToday by viewModel.hasHomeworkToday.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val allProfiles = (profilesState as? DataState.Success)?.data.orEmpty()
    val language = settings.languageEnum()
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val cycleSpeed: () -> Unit = {
        val speeds = listOf(0.85, 1.0, 1.15, 1.3)
        val idx = speeds.indexOfFirst { kotlin.math.abs(it - settings.speechSpeed) < 0.08 }
        val next = speeds[(idx + 1) % speeds.size]
        viewModel.updateSettings { it.copy(speechSpeed = next) }
    }

    val isSecondaryRoute = currentRoute in setOf(
        Routes.PROFILES,
        Routes.TIMETABLE,
        Routes.WEEKLY_TESTS,
        Routes.PARENT_PROGRESS,
        Routes.SETTINGS
    )

    val screenTitle = when (currentRoute) {
        Routes.HOME -> "AI Bharat Shiksha"
        Routes.STORY_CLASSROOM -> "Story Classroom"
        Routes.LIVE_CLASSROOM -> "Live Classroom • Meera Didi"
        Routes.PROFILES -> "Student Profiles"
        Routes.HOMEWORK_CAMERA -> "Homework Coach"
        Routes.TIMETABLE -> "Daily Timetable"
        Routes.WEEKLY_TESTS -> "Sunday Weekly Test Day"
        Routes.PARENT_PROGRESS -> "Parent Progress"
        Routes.SETTINGS -> "Voice & Language"
        else -> "AI Bharat Shiksha"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    primaryTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = currentRoute == tab.route,
                            onClick = { navigateToTab(navController, tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                    NavigationRailItem(
                        selected = currentRoute == Routes.TIMETABLE,
                        onClick = { navigateToTab(navController, Routes.TIMETABLE) },
                        icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Timetable") },
                        label = { Text("Timetable", maxLines = 1) },
                        modifier = Modifier.testTag("nav_timetable")
                    )
                    NavigationRailItem(
                        selected = currentRoute == Routes.WEEKLY_TESTS,
                        onClick = { navigateToTab(navController, Routes.WEEKLY_TESTS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (isSundayTestDay) Badge { Text("!") }
                                }
                            ) {
                                Icon(Icons.Rounded.Quiz, contentDescription = "Tests")
                            }
                        },
                        label = { Text("Tests", maxLines = 1) },
                        modifier = Modifier.testTag("nav_weekly_tests")
                    )
                    NavigationRailItem(
                        selected = currentRoute == Routes.PARENT_PROGRESS,
                        onClick = { navigateToTab(navController, Routes.PARENT_PROGRESS) },
                        icon = { Icon(Icons.Rounded.Insights, contentDescription = "Progress") },
                        label = { Text("Parent", maxLines = 1) },
                        modifier = Modifier.testTag("nav_parent_progress")
                    )
                    NavigationRailItem(
                        selected = currentRoute == Routes.PROFILES,
                        onClick = { navigateToTab(navController, Routes.PROFILES) },
                        icon = { Icon(Icons.Rounded.FamilyRestroom, contentDescription = "Profiles") },
                        label = { Text("Profiles", maxLines = 1) },
                        modifier = Modifier.testTag("nav_profiles")
                    )
                    NavigationRailItem(
                        selected = currentRoute == Routes.SETTINGS,
                        onClick = { navigateToTab(navController, Routes.SETTINGS) },
                        icon = { Icon(Icons.Rounded.SettingsVoice, contentDescription = "Settings") },
                        label = { Text("Voice", maxLines = 1) },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (isSecondaryRoute || !useRail) {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = screenTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    activeProfile?.let { p ->
                                        Text(
                                            text = "${p.preferredName} (${p.studentClass}) • ${language.nativeLabel}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                if (currentRoute != Routes.HOME) {
                                    IconButton(
                                        onClick = { navController.popBackStack(Routes.HOME, inclusive = false) },
                                        modifier = Modifier.testTag("top_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = "Back to Home"
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (!useRail) {
                                    Box {
                                        IconButton(
                                            onClick = { moreMenuExpanded = true },
                                            modifier = Modifier.testTag("top_more_menu_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreHoriz,
                                                contentDescription = "Parent & Class Tools"
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = moreMenuExpanded,
                                            onDismissRequest = { moreMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Student Profiles & Setup") },
                                                leadingIcon = { Icon(Icons.Rounded.FamilyRestroom, null) },
                                                onClick = {
                                                    moreMenuExpanded = false
                                                    navigateToTab(navController, Routes.PROFILES)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Timetable & Subject Durations") },
                                                leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) },
                                                onClick = {
                                                    moreMenuExpanded = false
                                                    navigateToTab(navController, Routes.TIMETABLE)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (isSundayTestDay) "Sunday Weekly Test Day (Active)"
                                                        else "Weekly Tests"
                                                    )
                                                },
                                                leadingIcon = { Icon(Icons.Rounded.Quiz, null) },
                                                onClick = {
                                                    moreMenuExpanded = false
                                                    navigateToTab(navController, Routes.WEEKLY_TESTS)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Parent Progress & Mastery") },
                                                leadingIcon = { Icon(Icons.Rounded.Insights, null) },
                                                onClick = {
                                                    moreMenuExpanded = false
                                                    navigateToTab(navController, Routes.PARENT_PROGRESS)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Voice, Language & Offline Mode") },
                                                leadingIcon = { Icon(Icons.Rounded.SettingsVoice, null) },
                                                onClick = {
                                                    moreMenuExpanded = false
                                                    navigateToTab(navController, Routes.SETTINGS)
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar {
                            primaryTabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentRoute == tab.route,
                                    onClick = { navigateToTab(navController, tab.route) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                            NavigationBarItem(
                                selected = isSecondaryRoute,
                                onClick = { navigateToTab(navController, Routes.PARENT_PROGRESS) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (isSundayTestDay) Badge { Text("S") }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Insights, contentDescription = "Parent")
                                    }
                                },
                                label = { Text("Parent", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.testTag("nav_parent")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        allProfiles = allProfiles,
                        activeProfile = activeProfile,
                        masteryList = masteryList,
                        sessionHistory = sessionHistory,
                        weeklyTests = weeklyTests,
                        settings = settings,
                        storyState = storyState,
                        liveState = liveState,
                        homeworkState = homeworkState,
                        activeWeeklyTest = activeWeeklyTest,
                        isSundayTestDay = isSundayTestDay,
                        hasHomeworkToday = hasHomeworkToday,
                        audioState = audioState,
                        currentUserId = currentUserId,
                        cycleSpeed = cycleSpeed
                    )
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    allProfiles: List<StudentProfile>,
    activeProfile: StudentProfile?,
    masteryList: List<ConceptMastery>,
    sessionHistory: List<LearningHistoryEntry>,
    weeklyTests: List<WeeklyTestRecord>,
    settings: VoiceLanguageSettings,
    storyState: StoryClassroomState,
    liveState: LiveClassroomState,
    homeworkState: HomeworkState,
    activeWeeklyTest: ActiveWeeklyTestState?,
    isSundayTestDay: Boolean,
    hasHomeworkToday: Boolean?,
    audioState: AudioPlaybackState,
    currentUserId: String,
    cycleSpeed: () -> Unit
) {
    val language = settings.languageEnum()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                activeProfile = activeProfile,
                allProfiles = allProfiles,
                language = language,
                isSundayTestDay = isSundayTestDay,
                hasHomeworkToday = hasHomeworkToday,
                onSelectProfile = { viewModel.selectProfile(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onContinueLesson = { lessonId ->
                    viewModel.openLesson(lessonId, resumeFromSavedCheckpoint = true)
                    navigateToTab(navController, Routes.STORY_CLASSROOM)
                },
                onOpenLessonDirect = { lessonId, nextDayRecall ->
                    viewModel.openLesson(
                        lessonId = lessonId,
                        resumeFromSavedCheckpoint = false,
                        nextDayRecallOnly = nextDayRecall
                    )
                    navigateToTab(navController, Routes.STORY_CLASSROOM)
                },
                onHomeworkAnswer = { hasHw ->
                    viewModel.setHasHomeworkToday(hasHw)
                },
                onOpenHomeworkCamera = { navigateToTab(navController, Routes.HOMEWORK_CAMERA) },
                onOpenLiveClassroom = {
                    viewModel.resetLiveConversation()
                    navigateToTab(navController, Routes.LIVE_CLASSROOM)
                },
                onOpenProfiles = { navigateToTab(navController, Routes.PROFILES) },
                onOpenTimetable = { navigateToTab(navController, Routes.TIMETABLE) },
                onOpenWeeklyTests = { navigateToTab(navController, Routes.WEEKLY_TESTS) },
                onOpenParentProgress = { navigateToTab(navController, Routes.PARENT_PROGRESS) },
                onOpenSettings = { navigateToTab(navController, Routes.SETTINGS) }
            )
        }

        composable(Routes.STORY_CLASSROOM) {
            BackHandler {
                viewModel.audioEngine.stopNarration()
                navController.popBackStack(Routes.HOME, inclusive = false)
            }
            StoryClassroomScreen(
                storyState = storyState,
                activeProfile = activeProfile,
                settings = settings,
                audioState = audioState,
                onSelectLesson = { lessonId -> viewModel.openLesson(lessonId) },
                onSelectLanguage = {
                    viewModel.setLanguage(it)
                    viewModel.speakCurrentStoryScene()
                },
                onSelectStyle = { style ->
                    viewModel.updateSettings { it.copy(selectedStoryStyle = style.name) }
                },
                onLampTap = { idx -> viewModel.onLampTappedInStory(idx) },
                onChoiceSelected = { choice -> viewModel.submitStoryChoice(choice) },
                onInputTextChange = { txt -> viewModel.updateStoryInputText(txt) },
                onSubmitFreeAnswer = { ans -> viewModel.submitStoryFreeAnswer(ans) },
                onAdvanceScene = { viewModel.advanceToNextScene() },
                onReplayAudio = { viewModel.speakCurrentStoryScene() },
                onStopAudio = { viewModel.audioEngine.stopNarration() },
                onToggleMusic = { viewModel.toggleMusicPlayback() },
                onCycleSpeed = cycleSpeed,
                onToggleCaptions = {
                    viewModel.updateSettings { it.copy(captionsEnabled = !it.captionsEnabled) }
                },
                onStartVoiceAnswer = {
                    viewModel.audioEngine.startChildVoiceInput(language)
                },
                onConfirmVoiceTranscript = { transcript ->
                    viewModel.audioEngine.clearRecognizedTranscript()
                    viewModel.submitStoryFreeAnswer(transcript)
                },
                onClearVoiceTranscript = { viewModel.audioEngine.clearRecognizedTranscript() },
                onOpenNextDayRecall = {
                    viewModel.openLesson(storyState.lesson.id, nextDayRecallOnly = true)
                },
                onBackHome = {
                    viewModel.audioEngine.stopNarration()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }

        composable(Routes.LIVE_CLASSROOM) {
            BackHandler {
                viewModel.audioEngine.stopNarration()
                navController.popBackStack(Routes.HOME, inclusive = false)
            }
            LiveClassroomScreen(
                liveState = liveState,
                activeProfile = activeProfile,
                settings = settings,
                audioState = audioState,
                onSelectSubject = { viewModel.selectLiveSubject(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onInputChange = { viewModel.updateLiveInput(it) },
                onSendMessage = { viewModel.sendLiveMessage(it) },
                onInterruptAndListen = { viewModel.interruptAndListen() },
                onConfirmTranscript = { viewModel.confirmVoiceTranscriptForLiveClassroom() },
                onClearTranscript = { viewModel.audioEngine.clearRecognizedTranscript() },
                onResetAssessment = { viewModel.resetLiveConversation() },
                onReplayAudio = {
                    val lastTeacher = liveState.turns.lastOrNull { it.isTeacher }?.text
                    if (!lastTeacher.isNullOrBlank()) {
                        viewModel.audioEngine.speak(lastTeacher, language, preferGeminiTts = true)
                    }
                },
                onStopAudio = { viewModel.audioEngine.stopNarration() },
                onToggleMusic = { viewModel.toggleMusicPlayback() },
                onCycleSpeed = cycleSpeed,
                onToggleCaptions = {
                    viewModel.updateSettings { it.copy(captionsEnabled = !it.captionsEnabled) }
                }
            )
        }

        composable(Routes.PROFILES) {
            BackHandler { navController.popBackStack(Routes.HOME, inclusive = false) }
            StudentProfilesScreen(
                profiles = allProfiles,
                activeProfile = activeProfile,
                currentUserId = currentUserId,
                onSelectProfile = { viewModel.selectProfile(it) },
                onSaveProfile = { viewModel.saveStudentProfile(it) },
                onDeleteProfile = { viewModel.deleteStudentProfile(it) }
            )
        }

        composable(Routes.HOMEWORK_CAMERA) {
            BackHandler {
                viewModel.setCameraActive(false)
                navController.popBackStack(Routes.HOME, inclusive = false)
            }
            HomeworkCameraScreen(
                homeworkState = homeworkState,
                activeProfile = activeProfile,
                language = language,
                onSetCameraActive = { viewModel.setCameraActive(it) },
                onSwitchCameraLens = { viewModel.switchCameraLens() },
                onPhotoCaptured = { viewModel.onHomeworkPhotoCaptured(it) },
                onRetake = { viewModel.retakeHomeworkPhoto() },
                onEditableQuestionChange = { viewModel.updateEditableHomeworkQuestion(it) },
                onReAnalyzeCorrected = { viewModel.reAnalyzeCorrectedHomework() },
                onChildAttemptChange = { viewModel.updateHomeworkAttempt(it) },
                onSubmitAttempt = { viewModel.submitHomeworkStepAttempt() }
            )
        }

        composable(Routes.TIMETABLE) {
            BackHandler { navController.popBackStack(Routes.HOME, inclusive = false) }
            TimetableScreen(
                activeProfile = activeProfile,
                isSundayTestDay = isSundayTestDay,
                onUpdateDuration = { profile, subject, mins ->
                    viewModel.updateSubjectDuration(profile, subject, mins)
                },
                onAddSubject = { profile, subj ->
                    viewModel.addSubjectToProfile(profile, subj)
                },
                onToggleSundayPreview = { viewModel.setSundayPreviewOverride(it) },
                onStartSubjectLesson = { subject ->
                    val lesson = LessonCatalog.allLessons
                        .firstOrNull { it.subject.equals(subject, ignoreCase = true) }
                    if (lesson != null) {
                        viewModel.openLesson(lesson.id)
                        navigateToTab(navController, Routes.STORY_CLASSROOM)
                    } else {
                        viewModel.selectLiveSubject(subject)
                        viewModel.resetLiveConversation()
                        navigateToTab(navController, Routes.LIVE_CLASSROOM)
                    }
                },
                onOpenWeeklyTests = { navigateToTab(navController, Routes.WEEKLY_TESTS) }
            )
        }

        composable(Routes.WEEKLY_TESTS) {
            BackHandler { navController.popBackStack(Routes.HOME, inclusive = false) }
            WeeklyTestsScreen(
                activeProfile = activeProfile,
                language = language,
                isSundayTestDay = isSundayTestDay,
                savedRecords = weeklyTests,
                activeTest = activeWeeklyTest,
                onStartOrResumeTest = { subject -> viewModel.startOrResumeWeeklyTest(subject) },
                onAnswerQuestion = { optIdx -> viewModel.answerWeeklyTestQuestion(optIdx) },
                onTakeBreak = { viewModel.takeBreakAndSaveWeeklyTest() },
                onDismissCompleted = { viewModel.dismissWeeklyTest() },
                onToggleSundayPreview = { viewModel.setSundayPreviewOverride(it) }
            )
        }

        composable(Routes.PARENT_PROGRESS) {
            BackHandler { navController.popBackStack(Routes.HOME, inclusive = false) }
            ParentProgressScreen(
                allProfiles = allProfiles,
                activeProfile = activeProfile,
                masteryList = masteryList,
                sessions = sessionHistory,
                weeklyTests = weeklyTests,
                settings = settings,
                onSelectProfile = { viewModel.selectProfile(it) },
                onToggleEpicMode = { enabled ->
                    viewModel.updateSettings { it.copy(indianEpicModeEnabled = enabled) }
                },
                onToggleFrontCameraPermission = { profile, enabled ->
                    viewModel.saveStudentProfile(profile.copy(cameraPermissionForChild = enabled))
                }
            )
        }

        composable(Routes.SETTINGS) {
            BackHandler { navController.popBackStack(Routes.HOME, inclusive = false) }
            VoiceLanguageSettingsScreen(
                settings = settings,
                audioState = audioState,
                onUpdateSettings = { viewModel.updateSettings(it) },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onPlayVoiceSample = { viewModel.playVoiceSample() }
            )
        }
    }
}

private fun navigateToTab(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
