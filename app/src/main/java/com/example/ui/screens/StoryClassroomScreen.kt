package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.AudioPlaybackState
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.SceneInteractionType
import com.example.data.model.StoryChoice
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.VoiceLanguageSettings
import com.example.ui.StoryClassroomState
import com.example.ui.components.AudioAndCaptionBar
import com.example.ui.components.MeeraTeacherPortrait
import com.example.ui.components.StoryStageCanvas
import com.example.ui.theme.MasterySage
import com.example.ui.theme.MasterySageContainer
import com.example.ui.theme.fontFamilyForLanguage

@Composable
fun StoryClassroomScreen(
    storyState: StoryClassroomState,
    activeProfile: StudentProfile?,
    settings: VoiceLanguageSettings,
    audioState: AudioPlaybackState,
    onSelectLesson: (String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectStyle: (StoryStyle) -> Unit,
    onLampTap: (Int) -> Unit,
    onChoiceSelected: (StoryChoice) -> Unit,
    onInputTextChange: (String) -> Unit,
    onSubmitFreeAnswer: (String) -> Unit,
    onAdvanceScene: () -> Unit,
    onReplayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onToggleMusic: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleCaptions: () -> Unit,
    onStartVoiceAnswer: () -> Unit,
    onConfirmVoiceTranscript: (String) -> Unit,
    onClearVoiceTranscript: () -> Unit,
    onOpenNextDayRecall: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = settings.languageEnum()
    val childName = activeProfile?.preferredName ?: "Barsha"
    val scene = storyState.activeScene

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartVoiceAnswer()
        }
    }

    LaunchedEffect(audioState.recognizedTranscript) {
        if (audioState.recognizedTranscript.isNotBlank() && !audioState.speechConfidenceUncertain) {
            onInputTextChange(audioState.recognizedTranscript)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lesson Switcher (Lesson A vs Lesson B) + Language Switcher
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = storyState.lesson.id == LessonCatalog.LESSON_LAMPS_ID,
                    onClick = { onSelectLesson(LessonCatalog.LESSON_LAMPS_ID) },
                    label = { Text("Lesson A: Festival Lamps (UKG)") },
                    modifier = Modifier.testTag("story_tab_lamps")
                )
                FilterChip(
                    selected = storyState.lesson.id == LessonCatalog.LESSON_DROP_ID,
                    onClick = { onSelectLesson(LessonCatalog.LESSON_DROP_ID) },
                    label = { Text("Lesson B: Drop Came Home (Class 4)") },
                    modifier = Modifier.testTag("story_tab_drop")
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { onSelectLanguage(lang) },
                        label = {
                            Text(lang.nativeLabel, fontFamily = fontFamilyForLanguage(lang))
                        },
                        modifier = Modifier.testTag("story_lang_${lang.name.lowercase()}")
                    )
                }
                Spacer(Modifier.width(4.dp))
                StoryStyle.entries
                    .filter { !it.requiresParentToggle || settings.indianEpicModeEnabled }
                    .forEach { style ->
                        FilterChip(
                            selected = settings.storyStyleEnum() == style,
                            onClick = { onSelectStyle(style) },
                            label = { Text(style.title) }
                        )
                    }
            }

            if (storyState.lessonCompleted) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MasterySageContainer),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = "Lesson Completed",
                            tint = MasterySage,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Shabash, $childName!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MasterySage
                        )
                        Text(
                            text = "You finished ${storyState.lesson.title.get(language)}! Independent answers: ${storyState.independentCorrectCount} • Saved to Parent Progress.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = fontFamilyForLanguage(language)
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onOpenNextDayRecall,
                                modifier = Modifier.testTag("story_next_day_recall_button")
                            ) {
                                Text("Try Next-Day Recall")
                            }
                            OutlinedButton(onClick = onBackHome) {
                                Text("Back to Home")
                            }
                        }
                    }
                }
                return@Column
            }

            if (scene != null) {
                val totalScenes = storyState.lesson.scenes.size.coerceAtLeast(1)
                LinearProgressIndicator(
                    progress = { (storyState.sceneIndex + 1).toFloat() / totalScenes },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${scene.beatTag} (${storyState.sceneIndex + 1}/$totalScenes)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = storyState.lesson.title.get(language),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = fontFamilyForLanguage(language)
                            )
                        )
                    }
                    MeeraTeacherPortrait(
                        isSpeaking = audioState.isSpeaking,
                        lightweightMode = settings.lightweightMode,
                        size = 56.dp,
                        showBadge = false
                    )
                }

                // Cinematic / Interactive Story Stage
                StoryStageCanvas(
                    scene = scene,
                    tappedLamps = storyState.tappedLampIndices,
                    onLampTap = onLampTap,
                    lightweightMode = settings.lightweightMode,
                    highResolutionMode = settings.highResolutionMode
                )

                // Audio, Captions, Speed & Music Controls
                AudioAndCaptionBar(
                    audioState = audioState,
                    language = language,
                    captionsEnabled = settings.captionsEnabled,
                    speechSpeed = settings.speechSpeed,
                    onReplay = onReplayAudio,
                    onStopSpeech = onStopAudio,
                    onToggleMusic = onToggleMusic,
                    onCycleSpeed = onCycleSpeed,
                    onToggleCaptions = onToggleCaptions
                )

                // Character Dialogue + Teacher Narration
                scene.characterLine?.let { charLine ->
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = charLine.get(language),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = fontFamilyForLanguage(language)
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Meera Didi (AI Teacher) • Method: ${storyState.currentMethod.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = scene.teacherNarration.get(language).replace("{name}", childName),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = fontFamilyForLanguage(language)
                            )
                        )
                        scene.pauseQuestion?.let { q ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = q.get(language).replace("{name}", childName),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // Memory Rhyme / Concept Cue Card
                scene.memoryLine?.let { rhyme ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = "Memory Line",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Column {
                                Text(
                                    text = "Memory Line (Cue)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = rhyme.get(language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Choice Options (for Pause Questions)
                if (scene.choices.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        scene.choices.forEach { choice ->
                            OutlinedButton(
                                onClick = { onChoiceSelected(choice) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("story_choice_${choice.id}"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Text(
                                    text = choice.label.get(language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Voice Uncertainty Confirmation Box
                AnimatedVisibility(visible = audioState.recognizedTranscript.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "I heard: \"${audioState.recognizedTranscript}\" — is that right?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val said = audioState.recognizedTranscript
                                        onClearVoiceTranscript()
                                        onConfirmVoiceTranscript(said)
                                    },
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("Yes, Send")
                                }
                                OutlinedButton(
                                    onClick = onClearVoiceTranscript,
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Text("Fix / Retype")
                                }
                            }
                        }
                    }
                }

                // Voice / Typing Input for Free Answers or Interruptions
                if (scene.interactionType != SceneInteractionType.CONTINUE_ONLY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("story_mic_button")
                        ) {
                            Icon(Icons.Rounded.Mic, contentDescription = "Speak answer")
                            Spacer(Modifier.width(4.dp))
                            Text(if (audioState.isListening) "Listening..." else "Speak")
                        }
                        OutlinedTextField(
                            value = storyState.childInputText,
                            onValueChange = onInputTextChange,
                            placeholder = { Text("Type or speak answer...") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("story_answer_input")
                        )
                        IconButton(
                            onClick = { onSubmitFreeAnswer(storyState.childInputText) },
                            enabled = storyState.childInputText.isNotBlank() && !storyState.isEvaluatingWithAi,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("story_send_answer_button")
                        ) {
                            if (storyState.isEvaluatingWithAi) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp))
                            } else {
                                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Submit answer")
                            }
                        }
                    }
                }

                // Teacher Feedback / Adaptive Retry Box
                AnimatedVisibility(visible = storyState.feedbackMessage.isNotBlank()) {
                    val isPositive = storyState.isFeedbackPositive == true
                    Surface(
                        color = if (isPositive) MasterySageContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("story_feedback_box")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Rounded.AutoAwesome else Icons.Rounded.Psychology,
                                    contentDescription = null,
                                    tint = if (isPositive) MasterySage else MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = if (isPositive) "Checkpoint Saved!" else "Adaptive Retry • ${storyState.currentMethod.label}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                text = storyState.feedbackMessage,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = fontFamilyForLanguage(language)
                                )
                            )
                        }
                    }
                }

                // Next Scene Button
                val canAdvance = scene.interactionType == SceneInteractionType.CONTINUE_ONLY ||
                    storyState.isFeedbackPositive == true ||
                    (scene.interactionType == SceneInteractionType.TAP_EACH_LAMP_GUIDED &&
                        storyState.tappedLampIndices.size >= (scene.expectedNumber ?: 3))

                Button(
                    onClick = onAdvanceScene,
                    enabled = canAdvance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("story_next_scene_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (storyState.sceneIndex + 1 >= totalScenes) {
                            "Finish Story & Save Mastery"
                        } else {
                            "Next Story Scene"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
