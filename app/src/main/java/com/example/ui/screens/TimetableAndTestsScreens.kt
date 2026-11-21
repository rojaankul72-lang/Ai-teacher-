package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.StudentProfile
import com.example.data.model.WeeklyTestRecord
import com.example.ui.ActiveWeeklyTestState
import com.example.ui.theme.MasterySage
import com.example.ui.theme.MasterySageContainer
import com.example.ui.theme.fontFamilyForLanguage
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun TimetableScreen(
    activeProfile: StudentProfile?,
    isSundayTestDay: Boolean,
    onUpdateDuration: (StudentProfile, String, Long) -> Unit,
    onAddSubject: (StudentProfile, String) -> Unit,
    onToggleSundayPreview: (Boolean?) -> Unit,
    onStartSubjectLesson: (String) -> Unit,
    onOpenWeeklyTests: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newSubjectName by remember { mutableStateOf("") }
    val profile = activeProfile
    val isRealSunday = LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY
    val isYounger = profile?.studentClass in listOf("LKG", "UKG", "Class 1")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Timetable & Daily Periods",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isYounger) {
                            "Starts with a 20-min English period, followed by shorter 5–10 min playful segments for ${profile?.studentClass}."
                        } else {
                            "Starts with a 20-min English period. Parents can adjust every subject's duration or add new subjects."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sunday Weekly Test Day Controls
            item {
                Surface(
                    color = if (isSundayTestDay) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                Icon(Icons.Rounded.CalendarToday, contentDescription = null)
                                Column {
                                    Text(
                                        text = if (isSundayTestDay) "Sunday Weekly Test Day Active" else "Regular Study Day (${LocalDate.now().dayOfWeek.name})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (isRealSunday) "Detected from device local date (Sunday)" else "Preview Sunday Test Day anytime for testing",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Switch(
                                checked = isSundayTestDay,
                                onCheckedChange = { checked -> onToggleSundayPreview(checked) },
                                modifier = Modifier.testTag("sunday_mode_toggle")
                            )
                        }
                        if (isSundayTestDay) {
                            Button(
                                onClick = onOpenWeeklyTests,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Go to Sunday Weekly Tests")
                            }
                        }
                    }
                }
            }

            if (profile != null) {
                items(profile.subjects, key = { it }) { subject ->
                    val mins = profile.subjectDurations[subject] ?: if (subject == "English") 20L else 15L
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (isYounger) {
                                        "$mins min • Short playful segments + period check"
                                    } else {
                                        "$mins min period • Ends with quick mastery check"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onUpdateDuration(profile, subject, mins - 5L) },
                                    modifier = Modifier.testTag("dec_duration_$subject")
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Decrease $subject minutes")
                                }
                                Text("${mins}m", style = MaterialTheme.typography.titleMedium)
                                IconButton(
                                    onClick = { onUpdateDuration(profile, subject, mins + 5L) },
                                    modifier = Modifier.testTag("inc_duration_$subject")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Increase $subject minutes")
                                }
                                Spacer(Modifier.width(4.dp))
                                Button(
                                    onClick = { onStartSubjectLesson(subject) },
                                    enabled = !isSundayTestDay,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 2.dp))
                                    Text("Start")
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubjectName,
                            onValueChange = { newSubjectName = it },
                            label = { Text("Add another subject (e.g. Computer, Sanskrit)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_subject_input")
                        )
                        Button(
                            onClick = {
                                if (newSubjectName.isNotBlank()) {
                                    onAddSubject(profile, newSubjectName)
                                    newSubjectName = ""
                                }
                            },
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("add_subject_button")
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTestsScreen(
    activeProfile: StudentProfile?,
    language: AppLanguage,
    isSundayTestDay: Boolean,
    savedRecords: List<WeeklyTestRecord>,
    activeTest: ActiveWeeklyTestState?,
    onStartOrResumeTest: (String) -> Unit,
    onAnswerQuestion: (Int) -> Unit,
    onTakeBreak: () -> Unit,
    onDismissCompleted: () -> Unit,
    onToggleSundayPreview: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = activeProfile
    val todayStr = LocalDate.now().toString()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sunday Weekly Test Day",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Separate short test per subject using taught concepts. Save unfinished tests and take breaks anytime.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSundayTestDay,
                        onCheckedChange = { onToggleSundayPreview(it) },
                        modifier = Modifier.testTag("weekly_test_sunday_switch")
                    )
                }
            }

            if (activeTest != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${activeTest.subject} Weekly Check",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(
                                    onClick = onTakeBreak,
                                    modifier = Modifier.testTag("weekly_test_break_button")
                                ) {
                                    Icon(Icons.Rounded.Pause, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Break & Save")
                                }
                            }

                            if (activeTest.completed) {
                                Surface(
                                    color = MasterySageContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Completed! Score: ${activeTest.correctCount} / ${activeTest.questions.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MasterySage
                                        )
                                        if (activeTest.conceptsNeedingRevision.isEmpty()) {
                                            Text("Every concept checked was answered cleanly!")
                                        } else {
                                            Text("Needs gentle revision: ${activeTest.conceptsNeedingRevision.joinToString(", ")}")
                                        }
                                        Button(onClick = onDismissCompleted) {
                                            Text("Back to Subject List")
                                        }
                                    }
                                }
                            } else {
                                val q = activeTest.questions.getOrNull(activeTest.currentIndex)
                                if (q != null) {
                                    Text(
                                        text = "Question ${activeTest.currentIndex + 1} of ${activeTest.questions.size} • ${q.conceptTitle}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = q.prompt.get(language),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = fontFamilyForLanguage(language)
                                        )
                                    )
                                    q.options.forEachIndexed { idx, opt ->
                                        OutlinedButton(
                                            onClick = { onAnswerQuestion(idx) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("weekly_test_option_$idx"),
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(14.dp)
                                        ) {
                                            Text(
                                                text = opt.get(language),
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontFamily = fontFamilyForLanguage(language)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            if (activeTest.lastFeedback.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activeTest.lastFeedback,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (profile != null) {
                items(profile.subjects, key = { it }) { subject ->
                    val saved = savedRecords.firstOrNull {
                        it.profileId == profile.id && it.testDate == todayStr && it.subject == subject
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = when {
                                            saved == null -> "Ready • Short concepts check"
                                            saved.completed -> "Completed (${saved.correctCount}/${saved.totalQuestions})"
                                            else -> "Paused at Q${saved.currentQuestionIndex + 1}/${saved.totalQuestions} • Saved"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = { onStartOrResumeTest(subject) },
                                enabled = saved?.completed != true,
                                modifier = Modifier.testTag("start_weekly_test_$subject")
                            ) {
                                Text(
                                    when {
                                        saved?.completed == true -> "Done"
                                        saved != null -> "Resume"
                                        else -> "Start Test"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
