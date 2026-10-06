package com.habitflow.app.ui.screens.habits

import androidx.compose.foundation.background
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
import com.habitflow.app.util.ReminderScheduler
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val ICONS = listOf("📚", "💧", "🏃", "🧘", "💻", "🥗", "🎵", "😴", "✍️", "🎯")
private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")
private val DAY_API = listOf(1, 2, 3, 4, 5, 6, 0)

/** Convert 12h + AM/PM → "HH:mm" 24h for API / AlarmManager */
fun to24h(hour12: Int, minute: Int, isPm: Boolean): String {
    val h = when {
        isPm && hour12 == 12 -> 12
        isPm -> hour12 + 12
        hour12 == 12 -> 0 // 12 AM
        else -> hour12
    }
    return "%02d:%02d".format(h, minute.coerceIn(0, 59))
}

/** Parse "HH:mm" into hour12, minute, isPm */
fun from24h(time: String): Triple<Int, Int, Boolean> {
    val parts = time.split(":")
    val h24 = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val isPm = h24 >= 12
    val h12 = when {
        h24 == 0 -> 12
        h24 > 12 -> h24 - 12
        else -> h24
    }
    return Triple(h12, m, isPm)
}

fun friendlyApiError(t: Throwable): String {
    val http = t as? HttpException
    val code = http?.code()
    return when (code) {
        429 -> "Too many requests. Wait 1–2 minutes and try again."
        401, 403 -> "Session expired. Please log in again."
        402 -> "Server rejected the request (402). Check your account / plan, then retry."
        400 -> "Invalid habit data. Check name and time."
        409 -> "This conflicts with an existing habit."
        in 500..599 -> "Server error. Try again shortly."
        else -> t.message?.takeIf { it.isNotBlank() } ?: "Could not save habit"
    }
}

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
    // 12-hour UI state
    var hour12 by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }
    var isPm by remember { mutableStateOf(false) }
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
                val (hh, mm, pm) = from24h(h.reminderTime ?: "09:00")
                hour12 = hh
                minute = mm
                isPm = pm
            }.onFailure { error = friendlyApiError(it) }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(if (isEdit) "Edit Habit" else "New Habit", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onDone) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(if (isEdit) "Update habit" else "New habit", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Build a routine that sticks.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Why (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            Spacer(Modifier.height(16.dp))
            Text("Icon", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ICONS.take(5).forEach { ic ->
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (icon == ic) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D))
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) { Text(ic, fontSize = 22.sp) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ICONS.drop(5).forEach { ic ->
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (icon == ic) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D))
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) { Text(ic, fontSize = 22.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Schedule", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DAILY" to "Every day", "SELECTED_DAYS" to "Specific days", "WEEKLY_TARGET" to "Per week").forEach { (k, label) ->
                    val on = frequency == k
                    Text(
                        label,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (on) MaterialTheme.colorScheme.primaryContainer else Color(0xFF222A3D))
                            .clickable { frequency = k }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (frequency == "SELECTED_DAYS") {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DAY_LABELS.forEachIndexed { i, label ->
                        val apiDay = DAY_API[i]
                        val on = apiDay in selectedDays
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (on) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2D3449))
                                .clickable {
                                    selectedDays = if (on) selectedDays - apiDay else selectedDays + apiDay
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontWeight = FontWeight.SemiBold,
                                color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Daily reminder", fontWeight = FontWeight.SemiBold)
                    Text("Local notification at the time you set", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = reminder,
                    onCheckedChange = { reminder = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.secondary)
                )
            }

            if (reminder) {
                Spacer(Modifier.height(12.dp))
                Text("Reminder time", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hour 1–12
                    TimeStepper(
                        label = "Hour",
                        value = hour12,
                        range = 1..12,
                        onChange = { hour12 = it }
                    )
                    Text(":", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    // Minute 0–59 step 5 for easier picking, allow all
                    TimeStepper(
                        label = "Min",
                        value = minute,
                        range = 0..59,
                        onChange = { minute = it },
                        step = 5
                    )
                    Spacer(Modifier.width(4.dp))
                    // AM / PM
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AmPmChip("AM", selected = !isPm) { isPm = false }
                        AmPmChip("PM", selected = isPm) { isPm = true }
                    }
                }
                Text(
                    "Will notify at ${"%d:%02d %s".format(hour12, minute, if (isPm) "PM" else "AM")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Habit title is required"
                        return@Button
                    }
                    if (frequency == "SELECTED_DAYS" && selectedDays.isEmpty()) {
                        error = "Pick at least one day"
                        return@Button
                    }
                    loading = true
                    error = null
                    scope.launch {
                        val time24 = if (reminder) to24h(hour12, minute, isPm) else null
                        val req = CreateHabitRequest(
                            name = name.trim(),
                            description = description.trim(),
                            icon = icon,
                            color = color,
                            frequencyType = frequency,
                            selectedDays = selectedDays.toList().sorted(),
                            reminderEnabled = reminder,
                            reminderTime = time24
                        )
                        val result = runCatching {
                            if (isEdit) {
                                val res = ApiClient.api.updateHabit(habitId!!, req)
                                HabitFlowApp.instance.habitRepository.syncFromServer()
                                val h = res.data?.habit
                                if (h != null) {
                                    if (req.reminderEnabled && !req.reminderTime.isNullOrBlank()) {
                                        ReminderScheduler.schedule(
                                            HabitFlowApp.instance, h._id, h.name, req.reminderTime!!
                                        )
                                    } else {
                                        ReminderScheduler.cancel(HabitFlowApp.instance, h._id)
                                    }
                                }
                            } else {
                                HabitFlowApp.instance.habitRepository.createHabit(req).getOrThrow()
                            }
                        }
                        loading = false
                        result.onSuccess { onDone() }.onFailure { error = friendlyApiError(it) }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    if (loading) "Saving…" else if (isEdit) "Save changes" else "Create habit",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AmPmChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2D3449))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TimeStepper(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    step: Int = 1
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "▲",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val next = value + step
                    onChange(if (next > range.last) range.first else next)
                }
                .padding(8.dp),
            fontSize = 14.sp
        )
        Text(
            if (label == "Min") "%02d".format(value) else "$value",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            "▼",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    val next = value - step
                    onChange(if (next < range.first) range.last else next)
                }
                .padding(8.dp),
            fontSize = 14.sp
        )
    }
}
