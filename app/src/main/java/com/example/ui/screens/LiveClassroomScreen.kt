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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.ai.AiConnectionStatus
import com.example.data.model.AppLanguage
import com.example.data.model.ChatTurn
import com.example.data.model.StudentProfile
import com.example.data.model.VoiceLanguageSettings
import com.example.ui.LiveClassroomState
import com.example.ui.components.AudioAndCaptionBar
import com.example.ui.components.MeeraTeacherPortrait
import com.example.ui.theme.fontFamilyForLanguage

@Composable
fun LiveClassroomScreen(
    liveState: LiveClassroomState,
    activeProfile: StudentProfile?,
    settings: VoiceLanguageSettings,
    audioState: AudioPlaybackState,
    onSelectSubject: (String) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onInterruptAndListen: () -> Unit,
    onConfirmTranscript: () -> Unit,
    onClearTranscript: () -> Unit,
    onResetAssessment: () -> Unit,
    onReplayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onToggleMusic: () -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleCaptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = settings.languageEnum()
    val subjects = activeProfile?.subjects ?: listOf("English", "Hindi", "Odia", "Mathematics", "Science")

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onInterruptAndListen()
        }
    }

    LaunchedEffect(activeProfile?.id) {
        if (liveState.turns.isEmpty() && activeProfile != null) {
            onResetAssessment()
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Expressive Teacher Header + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MeeraTeacherPortrait(
                    isSpeaking = audioState.isSpeaking,
                    lightweightMode = settings.lightweightMode,
                    size = 80.dp,
                    showBadge = true
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Live Classroom • ${activeProfile?.preferredName ?: "Student"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (val st = liveState.aiStatus) {
                            AiConnectionStatus.Ready -> "Connected: Firebase AI Logic (gemini-3.8-flash)"
                            AiConnectionStatus.Busy -> "Meera Didi is listening & thinking..."
                            is AiConnectionStatus.Online -> "Live AI: ${st.modelName} • Method: ${liveState.currentMethod.label}"
                            is AiConnectionStatus.Error -> "Offline / Error: ${st.message}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onResetAssessment,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("First Check")
                        }
                    }
                }
            }

            // Language + Subject Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { onSelectLanguage(lang) },
                        label = { Text(lang.nativeLabel, fontFamily = fontFamilyForLanguage(lang)) },
                        modifier = Modifier.testTag("live_lang_${lang.name.lowercase()}")
                    )
                }
                subjects.forEach { subj ->
                    FilterChip(
                        selected = liveState.subject == subj,
                        onClick = { onSelectSubject(subj) },
                        label = { Text(subj) }
                    )
                }
            }

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

            // Conversation Turns
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(liveState.turns, key = { it.id }) { turn ->
                    ChatTurnBubble(turn = turn, language = language)
                }
            }

            // Speech Recognition Uncertainty Confirmation
            AnimatedVisibility(visible = audioState.recognizedTranscript.isNotBlank() || !audioState.speechError.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (audioState.recognizedTranscript.isNotBlank()) {
                            Text(
                                text = if (audioState.speechConfidenceUncertain) {
                                    "I heard (uncertain): \"${audioState.recognizedTranscript}\" — correct it or send?"
                                } else {
                                    "Heard: \"${audioState.recognizedTranscript}\""
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onConfirmTranscript,
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text("Send to Teacher")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onInputChange(audioState.recognizedTranscript)
                                        onClearTranscript()
                                    },
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Text("Edit Text")
                                }
                            }
                        } else if (!audioState.speechError.isNullOrBlank()) {
                            Text(
                                text = audioState.speechError,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Quick Child Interruption / Action Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Why?",
                    "I don't know",
                    "Give another example",
                    "Explain in simple words"
                ).forEach { chipText ->
                    AssistChip(
                        onClick = { onSendMessage(chipText) },
                        label = { Text(chipText) },
                        modifier = Modifier.testTag("live_quick_${chipText.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Bottom Interrupt & Speak + Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (audioState.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("live_interrupt_speak_button")
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = "Interrupt and speak")
                    Spacer(Modifier.width(4.dp))
                    Text(if (audioState.isListening) "Listening" else "Speak")
                }

                OutlinedTextField(
                    value = liveState.inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("Ask or answer Meera Didi...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("live_chat_input")
                )

                IconButton(
                    onClick = { onSendMessage(liveState.inputText) },
                    enabled = liveState.inputText.isNotBlank() && !liveState.isStreaming,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("live_chat_send_button")
                ) {
                    if (liveState.isStreaming) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTurnBubble(
    turn: ChatTurn,
    language: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (turn.isTeacher) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            color = if (turn.isTeacher) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (turn.isTeacher) 4.dp else 18.dp,
                bottomEnd = if (turn.isTeacher) 18.dp else 4.dp
            ),
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (turn.isTeacher) {
                    Text(
                        text = buildString {
                            append("Meera Didi (AI Teacher)")
                            turn.methodUsed?.let { append(" • ${it.label}") }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = turn.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = fontFamilyForLanguage(language)
                    ),
                    color = if (turn.isTeacher) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
