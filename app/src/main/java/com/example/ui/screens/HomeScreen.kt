package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.StudentProfile
import com.example.ui.theme.fontFamilyForLanguage

@Composable
fun HomeScreen(
    activeProfile: StudentProfile?,
    allProfiles: List<StudentProfile>,
    language: AppLanguage,
    isSundayTestDay: Boolean,
    hasHomeworkToday: Boolean?,
    onSelectProfile: (StudentProfile) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onContinueLesson: (String) -> Unit,
    onOpenLessonDirect: (String, Boolean) -> Unit,
    onHomeworkAnswer: (Boolean) -> Unit,
    onOpenHomeworkCamera: () -> Unit,
    onOpenLiveClassroom: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenTimetable: () -> Unit,
    onOpenWeeklyTests: () -> Unit,
    onOpenParentProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val childName = activeProfile?.preferredName ?: "Student"
    val continueLessonId = activeProfile?.lastLessonId?.takeIf { it.isNotBlank() }
        ?: LessonCatalog.LESSON_LAMPS_ID
    val continueLesson = LessonCatalog.lessonById(continueLessonId)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top greeting + Child Profile Switcher + Language Quick Toggle
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_teacher_meera),
                                contentDescription = "Meera Didi AI Teacher",
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Text(
                                    text = when (language) {
                                        AppLanguage.ENGLISH -> "Namaste, $childName!"
                                        AppLanguage.HINDI -> "नमस्ते, $childName!"
                                        AppLanguage.ODIA -> "ନମସ୍କାର, $childName!"
                                    },
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeProfile?.studentClass ?: "UKG"} • ${activeProfile?.board ?: "CBSE"} • AI Teacher Meera Didi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButtonBadge(onClick = onOpenSettings)
                    }

                    // Profile Switcher Chips
                    if (allProfiles.isNotEmpty()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            allProfiles.forEach { profile ->
                                FilterChip(
                                    selected = profile.id == activeProfile?.id,
                                    onClick = { onSelectProfile(profile) },
                                    label = {
                                        Text("${profile.preferredName} (${profile.studentClass})")
                                    },
                                    modifier = Modifier.testTag("home_profile_chip_${profile.preferredName.lowercase()}")
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenProfiles,
                                modifier = Modifier.testTag("manage_profiles_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.FamilyRestroom, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Profiles")
                            }
                        }
                    }

                    // Instant English / Hindi / Odia Switcher
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
                                        text = lang.nativeLabel,
                                        fontFamily = fontFamilyForLanguage(lang)
                                    )
                                },
                                modifier = Modifier.testTag("home_lang_${lang.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Sunday Weekly Test Day Banner (pauses regular lessons on Sunday)
            if (isSundayTestDay) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Quiz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Sunday Weekly Test Day",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Text(
                                text = "Regular lessons pause on Sundays! Short subject tests help $childName revise what was taught this week, with gentle breaks anytime.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Button(
                                onClick = onOpenWeeklyTests,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("open_sunday_tests_button")
                            ) {
                                Text("Open Sunday Weekly Tests")
                            }
                        }
                    }
                }
            }

            // Prominent "Continue my lesson" Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(148.dp)
                        ) {
                            Image(
                                painter = painterResource(id = continueLesson.heroDrawableResId),
                                contentDescription = continueLesson.title.get(language),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xCC281004))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "WELCOME BACK, ${childName.uppercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFFDFD3)
                                )
                                Text(
                                    text = continueLesson.title.get(language),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = fontFamilyForLanguage(language)
                                    ),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Meera Didi remembers where you paused (Step ${(activeProfile?.lastStepIndex ?: 0) + 1} of ${continueLesson.scenes.size.coerceAtLeast(1)}). Ready to continue?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Button(
                                onClick = { onContinueLesson(continueLesson.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("continue_my_lesson_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Continue my lesson",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // "Did your school give you homework today?" Prompt
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Did your school give you homework today?",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onHomeworkAnswer(true)
                                    onOpenHomeworkCamera()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("homework_yes_button")
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Yes, Show Photo")
                            }
                            OutlinedButton(
                                onClick = {
                                    onHomeworkAnswer(false)
                                    onOpenLiveClassroom()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("homework_no_button")
                            ) {
                                Text("No, Start 20m English")
                            }
                        }
                        if (hasHomeworkToday == false) {
                            Text(
                                text = "Great! Let's begin with your 20-minute English period or open a Story Classroom lesson.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Featured Complete Stories (One-Tap Launch)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "One-Tap Interactive Story Lessons",
                        style = MaterialTheme.typography.titleLarge
                    )
                    LessonLaunchCard(
                        title = LessonCatalog.lessonThreeFestivalLamps.title.get(language),
                        subtitle = LessonCatalog.lessonThreeFestivalLamps.subtitle.get(language),
                        badge = "Lesson A • Ready to Play • Offline",
                        drawableRes = R.drawable.img_lesson_lamps_courtyard,
                        language = language,
                        testTag = "open_lesson_a_button",
                        onOpen = { onOpenLessonDirect(LessonCatalog.LESSON_LAMPS_ID, false) },
                        onNextDayRecall = { onOpenLessonDirect(LessonCatalog.LESSON_LAMPS_ID, true) }
                    )
                    LessonLaunchCard(
                        title = LessonCatalog.lessonDayDropCameHome.title.get(language),
                        subtitle = LessonCatalog.lessonDayDropCameHome.subtitle.get(language),
                        badge = "Lesson B • Ready to Play • Offline",
                        drawableRes = R.drawable.img_lesson_drop_jeypore,
                        language = language,
                        testTag = "open_lesson_b_button",
                        onOpen = { onOpenLessonDirect(LessonCatalog.LESSON_DROP_ID, false) },
                        onNextDayRecall = { onOpenLessonDirect(LessonCatalog.LESSON_DROP_ID, true) }
                    )
                }
            }

            // Quick Classroom & Parent Navigation Tiles
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Classroom & Parent Tools",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavTile(
                            title = "Live AI Teacher",
                            subtitle = "Voice & Chat",
                            icon = Icons.Rounded.RecordVoiceOver,
                            onClick = onOpenLiveClassroom,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_live_classroom")
                        )
                        QuickNavTile(
                            title = "Timetable",
                            subtitle = "20m Periods",
                            icon = Icons.Rounded.CalendarMonth,
                            onClick = onOpenTimetable,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_timetable")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavTile(
                            title = "Weekly Tests",
                            subtitle = "Sunday Revision",
                            icon = Icons.Rounded.Quiz,
                            onClick = onOpenWeeklyTests,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_weekly_tests")
                        )
                        QuickNavTile(
                            title = "Parent Progress",
                            subtitle = "Mastery & History",
                            icon = Icons.Rounded.Insights,
                            onClick = onOpenParentProgress,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tile_parent_progress")
                        )
                    }
                }
            }

            // Additional Reusable LKG-Class 10 Lessons (honestly labelled)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LKG – Class 10 Curriculum Library",
                        style = MaterialTheme.typography.titleLarge
                    )
                    LessonCatalog.additionalLessons.forEach { lesson ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenLiveClassroom() }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lesson.title.get(language),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = fontFamilyForLanguage(language)
                                        )
                                    )
                                    Text(
                                        text = "${lesson.subtitle.get(language)} • ${lesson.readiness.badgeLabel}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Open with AI teacher")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButtonBadge(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .size(48.dp)
            .testTag("home_settings_button")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.SettingsVoice,
                contentDescription = "Voice and Language Settings",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun LessonLaunchCard(
    title: String,
    subtitle: String,
    badge: String,
    drawableRes: Int,
    language: AppLanguage,
    testTag: String,
    onOpen: () -> Unit,
    onNextDayRecall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = title,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = fontFamilyForLanguage(language)
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = fontFamilyForLanguage(language)
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpen,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag(testTag),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Play Story")
                    }
                    OutlinedButton(
                        onClick = onNextDayRecall,
                        modifier = Modifier.height(44.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Next-Day Check")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickNavTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
