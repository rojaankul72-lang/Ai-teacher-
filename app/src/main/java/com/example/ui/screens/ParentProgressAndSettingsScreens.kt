package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.AudioPlaybackState
import com.example.data.model.AppLanguage
import com.example.data.model.ConceptMastery
import com.example.data.model.LearningHistoryEntry
import com.example.data.model.StoryStyle
import com.example.data.model.StudentProfile
import com.example.data.model.TeachingMethod
import com.example.data.model.VoiceLanguageSettings
import com.example.data.model.WeeklyTestRecord
import com.example.ui.theme.MasterySage
import com.example.ui.theme.MasterySageContainer
import com.example.ui.theme.fontFamilyForLanguage
import kotlin.math.roundToInt

@Composable
fun ParentProgressScreen(
    allProfiles: List<StudentProfile>,
    activeProfile: StudentProfile?,
    masteryList: List<ConceptMastery>,
    sessions: List<LearningHistoryEntry>,
    weeklyTests: List<WeeklyTestRecord>,
    settings: VoiceLanguageSettings,
    onSelectProfile: (StudentProfile) -> Unit,
    onToggleEpicMode: (Boolean) -> Unit,
    onToggleFrontCameraPermission: (StudentProfile, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = activeProfile
    val childMastery = masteryList.filter { it.profileId == profile?.id }
    val childSessions = sessions.filter { it.profileId == profile?.id }
    val childTests = weeklyTests.filter { it.profileId == profile?.id }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Parent Progress & Concept Mastery",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shows real learning evidence (independent vs hint-assisted answers, misconceptions, and next-day recall)—never camera/silence guesses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allProfiles.forEach { p ->
                            FilterChip(
                                selected = p.id == profile?.id,
                                onClick = { onSelectProfile(p) },
                                label = { Text("${p.preferredName} (${p.studentClass})") }
                            )
                        }
                    }
                }
            }

            // Parent Controls Card
            if (profile != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Parent Safety & Story Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Indian Epic Storytelling Mode", style = MaterialTheme.typography.bodyLarge)
                                    Text("Allow stories with young Gopal, Hanuman, Rama, Lakshmana & Sita", style = MaterialTheme.typography.labelMedium)
                                }
                                Switch(
                                    checked = settings.indianEpicModeEnabled,
                                    onCheckedChange = onToggleEpicMode
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Front Camera for ${profile.preferredName}", style = MaterialTheme.typography.bodyLarge)
                                    Text("Allow child to show drawings or objects", style = MaterialTheme.typography.labelMedium)
                                }
                                Switch(
                                    checked = profile.cameraPermissionForChild,
                                    onCheckedChange = { onToggleFrontCameraPermission(profile, it) }
                                )
                            }
                        }
                    }
                }
            }

            // Concept Mastery Cards
            item {
                Text("Concept Understanding Evidence", style = MaterialTheme.typography.titleLarge)
            }
            if (childMastery.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No concept checks recorded yet for ${profile?.preferredName ?: "this child"}. Open Lesson A or B to begin!",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(childMastery, key = { it.id }) { m ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (m.needsPractice) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f) else MasterySageContainer
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.conceptTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Icon(
                                    imageVector = if (m.needsPractice) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = if (m.needsPractice) MaterialTheme.colorScheme.tertiary else MasterySage
                                )
                            }
                            Text(
                                text = "${m.subject} • ${m.demonstratedEvidence}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Independent: ${m.independentCorrect} • With Hint: ${m.hintAssistedCorrect} • Method: ${TeachingMethod.fromRaw(m.lastMethodUsed).label}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            if (m.misconceptions.isNotEmpty()) {
                                Text(
                                    text = "Misconceptions spotted: ${m.misconceptions.joinToString("; ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = when {
                                    m.nextDayRecallPassed -> "Next-day recall check: Passed ✓"
                                    m.nextDayRecallDue -> "Next-day recall check: Ready for tomorrow"
                                    else -> "Next-day recall check: Pending story completion"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Weekly Test Revision Report
            if (childTests.isNotEmpty()) {
                item {
                    Text("Sunday Weekly Test Revision Notes", style = MaterialTheme.typography.titleLarge)
                }
                items(childTests, key = { it.id }) { wt ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${wt.subject} (${wt.testDate}) • ${wt.correctCount}/${wt.totalQuestions} • ${if (wt.completed) "Completed" else "Saved In Progress"}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (wt.conceptsNeedingRevision.isNotEmpty()) {
                                Text(
                                    text = "Concepts needing revision: ${wt.conceptsNeedingRevision.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Per-child Learning History
            item {
                Text("Learning History (${profile?.preferredName ?: ""})", style = MaterialTheme.typography.titleLarge)
            }
            items(childSessions, key = { it.id }) { sess ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(sess.lessonTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${sess.subject} • ${sess.language} • ${if (sess.completed) "Completed" else "Saved at step ${sess.checkpointIndex + 1}"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (sess.summary.isNotBlank()) {
                            Text(sess.summary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceLanguageSettingsScreen(
    settings: VoiceLanguageSettings,
    audioState: AudioPlaybackState,
    onUpdateSettings: ((VoiceLanguageSettings) -> VoiceLanguageSettings) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onPlayVoiceSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = settings.languageEnum()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Voice, Language & Video Clarity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Language Selector (English, Hindi, Odia)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Teaching Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                FilterChip(
                                    selected = language == lang,
                                    onClick = { onSelectLanguage(lang) },
                                    label = {
                                        Text(
                                            text = "${lang.displayName} (${lang.nativeLabel})",
                                            fontFamily = fontFamilyForLanguage(lang)
                                        )
                                    },
                                    modifier = Modifier.testTag("settings_lang_${lang.name.lowercase()}")
                                )
                            }
                        }
                        Button(
                            onClick = onPlayVoiceSample,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("hear_voice_sample_button")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Hear Meera Didi's Voice Sample (${language.nativeLabel})")
                        }
                        Text(
                            text = "Voice Engine Status: ${audioState.voiceStatusNote}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // Audio Sliders
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Audio, Captions & Music Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Speech Speed: ${(settings.speechSpeed * 100).roundToInt() / 100.0}x")
                        Slider(
                            value = settings.speechSpeed.toFloat(),
                            onValueChange = { v -> onUpdateSettings { it.copy(speechSpeed = v.toDouble()) } },
                            valueRange = 0.75f..1.4f
                        )
                        Text("Narration Volume: ${(settings.narrationVolume * 100).roundToInt()}%")
                        Slider(
                            value = settings.narrationVolume.toFloat(),
                            onValueChange = { v -> onUpdateSettings { it.copy(narrationVolume = v.toDouble()) } },
                            valueRange = 0f..1f
                        )
                        Text("Background Music Volume (auto-ducks during speech): ${(settings.musicVolume * 100).roundToInt()}%")
                        Slider(
                            value = settings.musicVolume.toFloat(),
                            onValueChange = { v -> onUpdateSettings { it.copy(musicVolume = v.toDouble()) } },
                            valueRange = 0f..1f
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Synchronized Captions")
                            Switch(
                                checked = settings.captionsEnabled,
                                onCheckedChange = { c -> onUpdateSettings { it.copy(captionsEnabled = c) } }
                            )
                        }
                    }
                }
            }

            // Story Style Preferences
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Default Story Classroom Style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StoryStyle.entries.forEach { style ->
                            val enabled = !style.requiresParentToggle || settings.indianEpicModeEnabled
                            Surface(
                                onClick = {
                                    if (enabled) onUpdateSettings { it.copy(selectedStoryStyle = style.name) }
                                },
                                color = if (settings.storyStyleEnum() == style) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${style.title} • ${style.badge}", style = MaterialTheme.typography.titleMedium)
                                    Text(style.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Resolution & Offline Mode
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Phone Performance & Offline Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lightweight Mode (for slower phones)")
                                Text("Reduces scene motion while keeping clear diagrams", style = MaterialTheme.typography.labelMedium)
                            }
                            Switch(
                                checked = settings.lightweightMode,
                                onCheckedChange = { l -> onUpdateSettings { it.copy(lightweightMode = l, highResolutionMode = !l) } }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (settings.offlinePackDownloaded) Icons.Rounded.DownloadDone else Icons.Rounded.CloudDone,
                                    contentDescription = null
                                )
                                Column {
                                    Text("Offline Story Pack (Lessons A & B)")
                                    Text("Bundled stories & cached audio work offline; Live AI chat needs internet", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Switch(
                                checked = settings.offlinePackDownloaded,
                                onCheckedChange = { o -> onUpdateSettings { it.copy(offlinePackDownloaded = o) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
