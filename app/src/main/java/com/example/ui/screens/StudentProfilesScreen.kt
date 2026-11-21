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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.LessonCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.StudentProfile
import com.example.ui.theme.fontFamilyForLanguage

@Composable
fun StudentProfilesScreen(
    profiles: List<StudentProfile>,
    activeProfile: StudentProfile?,
    currentUserId: String,
    onSelectProfile: (StudentProfile) -> Unit,
    onSaveProfile: (StudentProfile) -> Unit,
    onDeleteProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProfile by remember { mutableStateOf<StudentProfile?>(null) }

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
                            text = "Student Profiles & Parent Setup",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multiple children can study on the same phone with separate history, language, and subjects.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            editingProfile = StudentProfile(
                                id = "",
                                userId = currentUserId,
                                preferredName = "",
                                age = 7,
                                studentClass = "Class 2",
                                preferredLanguage = AppLanguage.ENGLISH.name
                            )
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_student_profile_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Child")
                    }
                }
            }

            // Optional Sample Profiles quick restore
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Starter Sample Profiles", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Barsha (UKG) & Raghav (Class 4) let you try both full lessons immediately.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (profiles.none { it.id == "sample_barsha_ukg" }) {
                                OutlinedButton(onClick = { onSaveProfile(LessonCatalog.starterBarshaProfile(currentUserId)) }) {
                                    Text("+ Barsha")
                                }
                            }
                            if (profiles.none { it.id == "sample_raghav_c4" }) {
                                OutlinedButton(onClick = { onSaveProfile(LessonCatalog.starterRaghavProfile(currentUserId)) }) {
                                    Text("+ Raghav")
                                }
                            }
                        }
                    }
                }
            }

            items(profiles, key = { it.id }) { profile ->
                val isSelected = profile.id == activeProfile?.id
                val lang = profile.languageEnum()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = profile.preferredName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (profile.sampleProfile) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = "Sample",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${profile.studentClass} (Age ${profile.age}) • ${profile.board} • Medium: ${profile.schoolMedium}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Language: ${lang.nativeLabel} • ${profile.school.ifBlank { "Home Study" }}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = fontFamilyForLanguage(lang)
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { editingProfile = profile },
                                    modifier = Modifier.testTag("edit_profile_${profile.preferredName.lowercase()}")
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit ${profile.preferredName}")
                                }
                                if (profiles.size > 1) {
                                    IconButton(onClick = { onDeleteProfile(profile.id) }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Remove ${profile.preferredName}")
                                    }
                                }
                            }
                        }

                        if (profile.interests.isNotEmpty()) {
                            Text(
                                text = "Interests: ${profile.interests.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last lesson: ${profile.lastLessonTitle.ifBlank { "Ready to begin" }}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { onSelectProfile(profile) },
                                enabled = !isSelected,
                                modifier = Modifier.testTag("switch_to_${profile.preferredName.lowercase()}")
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Active Child")
                                } else {
                                    Text("Switch to ${profile.preferredName}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingProfile?.let { draft ->
        EditProfileDialog(
            initial = draft,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                onSaveProfile(updated)
                editingProfile = null
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    initial: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (StudentProfile) -> Unit
) {
    var name by remember { mutableStateOf(initial.preferredName) }
    var age by remember { mutableIntStateOf(initial.age) }
    var school by remember { mutableStateOf(initial.school) }
    var studentClass by remember { mutableStateOf(initial.studentClass) }
    var rollNumber by remember { mutableStateOf(initial.rollNumber) }
    var board by remember { mutableStateOf(initial.board) }
    var medium by remember { mutableStateOf(initial.schoolMedium) }
    var language by remember { mutableStateOf(initial.languageEnum()) }
    var interestsText by remember { mutableStateOf(initial.interests.joinToString(", ")) }
    var cameraPerm by remember { mutableStateOf(initial.cameraPermissionForChild) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial.id.isBlank()) "Add Child Profile" else "Edit ${initial.preferredName}'s Profile")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preferred Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age.toString(),
                        onValueChange = { age = it.toIntOrNull()?.coerceIn(3, 18) ?: age },
                        label = { Text("Age") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll No (opt)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("School Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Class (LKG – Class 10)", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LessonCatalog.allClasses.forEach { cls ->
                        FilterChip(
                            selected = studentClass == cls,
                            onClick = { studentClass = cls },
                            label = { Text(cls) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = board,
                        onValueChange = { board = it },
                        label = { Text("Board (CBSE/BSE Odisha)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = medium,
                        onValueChange = { medium = it },
                        label = { Text("School Medium") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Preferred Teaching Language", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = {
                                Text(lang.nativeLabel, fontFamily = fontFamilyForLanguage(lang))
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = interestsText,
                    onValueChange = { interestsText = it },
                    label = { Text("Interests (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Front Camera Show & Tell", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Parent permission for child to show drawings/objects",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = cameraPerm, onCheckedChange = { cameraPerm = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedInterests = interestsText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .take(15)
                    onSave(
                        initial.copy(
                            preferredName = name.trim().ifBlank { "Student" },
                            age = age,
                            school = school,
                            studentClass = studentClass,
                            rollNumber = rollNumber,
                            board = board,
                            schoolMedium = medium,
                            preferredLanguage = language.name,
                            interests = parsedInterests,
                            sampleProfile = false,
                            cameraPermissionForChild = cameraPerm
                        )
                    )
                },
                modifier = Modifier.testTag("save_profile_confirm_button")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
