package com.habitflow.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.remote.CreateHabitRequest
import kotlinx.coroutines.launch

private val ICONS = listOf("📚", "💧", "🏃", "🧘", "💻", "🥗", "🎵", "😴", "✍️", "🎯")
private val COLORS = listOf("#8083FF", "#4EDEA3", "#FFB95F", "#C0C1FF", "#FFB4AB", "#6FFBBE")
private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private val DAY_API = listOf(1, 2, 3, 4, 5, 6, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(habitId: String? = null, onDone: () -> Unit) {
    val isEdit = !habitId.isNullOrBlank()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("🎯") }
    var color by remember { mutableStateOf("#8083FF") }
    var frequency by remember { mutableStateOf("DAILY") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var reminder by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("09:00") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = Color(0xFF464554),
        focusedContainerColor = Color(0xFF131B2E),
        unfocusedContainerColor = Color(0xFF131B2E),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    LaunchedEffect(habitId) {
        if (isEdit) {
            runCatching {
                val h = ApiClient.api.getHabit(habitId!!).data?.habit ?: return@runCatching
                name = h.name
                description = h.description.orEmpty()
                icon = h.icon ?: "🎯"
                color = h.color ?: "#8083FF"
                frequency = h.frequencyType
                selectedDays = h.selectedDays?.toSet().orEmpty()
                reminder = h.reminderEnabled == true
                reminderTime = h.reminderTime ?: "09:00"
            }.onFailure { error = it.message }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(if (isEdit) "Edit Habit" else "New Habit", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onDone) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text(if (isEdit) "Update Discipline" else "New Discipline", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Forge habits that stick.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Habit Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), colors = fieldColors)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("The Why (optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = fieldColors)
            Spacer(Modifier.height(16.dp))
            Text("Choose Icon", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ICONS.take(5).forEach { ic ->
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(if (icon == ic) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D)).clickable { icon = ic }, contentAlignment = Alignment.Center) {
                        Text(ic, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ICONS.drop(5).forEach { ic ->
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(if (icon == ic) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D)).clickable { icon = ic }, contentAlignment = Alignment.Center) {
                        Text(ic, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Cadence", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DAILY" to "Every day", "SELECTED_DAYS" to "Specific days", "WEEKLY_TARGET" to "Per week").forEach { (k, label) ->
                    val on = frequency == k
                    Text(label, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (on) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D)).clickable { frequency = k }.padding(horizontal = 12.dp, vertical = 8.dp), color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (frequency == "SELECTED_DAYS") {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DAY_LABELS.forEachIndexed { i, label ->
                        val apiDay = DAY_API[i]
                        val on = apiDay in selectedDays
                        Box(Modifier.size(40.dp).clip(CircleShape).background(if (on) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2D3449)).clickable {
                            selectedDays = if (on) selectedDays - apiDay else selectedDays + apiDay
                        }, contentAlignment = Alignment.Center) {
                            Text(label, fontWeight = FontWeight.SemiBold, color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Daily Nudge", fontWeight = FontWeight.SemiBold)
                    Text("Local reminder notification", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = reminder, onCheckedChange = { reminder = it }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.secondary))
            }
            if (reminder) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, label = { Text("Time HH:mm") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = fieldColors)
            }
            error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isBlank()) { error = "Habit title is required"; return@Button }
                    loading = true
                    scope.launch {
                        val req = CreateHabitRequest(
                            name = name.trim(), description = description.trim(), icon = icon, color = color,
                            frequencyType = frequency, selectedDays = selectedDays.toList().sorted(),
                            reminderEnabled = reminder, reminderTime = if (reminder) reminderTime else null
                        )
                        val result = runCatching {
                            if (isEdit) {
                                ApiClient.api.updateHabit(habitId!!, req)
                                HabitFlowApp.instance.habitRepository.syncFromServer()
                            } else HabitFlowApp.instance.habitRepository.createHabit(req).getOrThrow()
                        }
                        loading = false
                        result.onSuccess { onDone() }.onFailure { error = it.message }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text(if (loading) "Saving…" else if (isEdit) "Save Changes" else "Forge This Habit", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
