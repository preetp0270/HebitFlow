package com.habitflow.app.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.habitflow.app.data.remote.StatisticsData
import com.habitflow.app.data.remote.TodayData
import com.habitflow.app.ui.components.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(onAddHabit: () -> Unit) {
    var data by remember { mutableStateOf<TodayData?>(null) }
    var stats by remember { mutableStateOf<StatisticsData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var offline by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            val repo = HabitFlowApp.instance.habitRepository
            repo.pushPending()
            runCatching { repo.syncFromServer() }
            repo.getToday().onSuccess { data = it; offline = false }.onFailure { offline = true }
            runCatching { stats = ApiClient.api.statistics().data }
            loading = false
        }
    }
    LaunchedEffect(Unit) { reload() }

    val greeting = when {
        LocalTime.now().hour < 12 -> "Good morning"
        LocalTime.now().hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null)
                    Text(" New Habit", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HabitFlowTopBar(synced = !offline)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(dateStr.uppercase(Locale.getDefault()), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.6.sp)
                    Text("$greeting 👋", fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    Text("Build better. Every day.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF131B2E)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (offline) "⚠" else "✓", color = if (offline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text(if (offline) "  Offline — will sync when online" else "  All habits synced across Android & Web", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    val p = data?.progress
                    MomentumCard(p?.percentage ?: 0, p?.completed ?: 0, p?.total ?: 0, stats?.currentStreak ?: 0)
                }
                item {
                    SectionLabel("This Week")
                    WeekStrip()
                }
                item { SectionLabel("Today's Habits", "${data?.items?.size ?: 0} total") }
                if (loading) item { Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                else if (data?.items.isNullOrEmpty()) item {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No habits yet", fontWeight = FontWeight.SemiBold)
                        Text("Start with one small habit and build from there.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                } else items(data!!.items, key = { it.habit._id }) { item ->
                    HabitRowCard(
                        icon = item.habit.icon ?: "🎯",
                        name = item.habit.name,
                        subtitle = item.habit.description?.ifBlank { item.habit.frequencyType.replace('_', ' ') } ?: item.habit.frequencyType.replace('_', ' '),
                        streak = item.currentStreak,
                        completed = item.completed,
                        onToggle = {
                            scope.launch {
                                if (item.completed) HabitFlowApp.instance.habitRepository.uncompleteHabit(item.habit._id)
                                else HabitFlowApp.instance.habitRepository.completeHabit(item.habit._id)
                                reload()
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun WeekStrip() {
    var series by remember { mutableStateOf(List(7) { -1 }) }
    LaunchedEffect(Unit) {
        runCatching { series = ApiClient.api.weeklyStats().data?.series.orEmpty().map { it.percentage } }
    }
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { i, label ->
            val pct = series.getOrNull(i) ?: -1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(
                        when {
                            pct == 100 -> MaterialTheme.colorScheme.secondary
                            pct > 0 -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color(0xFF2D3449)
                        }
                    ), contentAlignment = Alignment.Center
                ) {
                    when {
                        pct == 100 -> Text("✓", color = Color(0xFF003824), fontWeight = FontWeight.Bold)
                        pct > 0 -> Text("$pct", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        else -> Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF464554)))
                    }
                }
            }
        }
    }
}
