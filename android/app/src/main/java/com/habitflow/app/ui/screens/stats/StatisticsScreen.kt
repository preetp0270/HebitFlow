package com.habitflow.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.remote.DaySeriesDto
import com.habitflow.app.data.remote.StatisticsData
import com.habitflow.app.ui.components.HabitFlowTopBar

@Composable
fun StatisticsScreen() {
    var stats by remember { mutableStateOf<StatisticsData?>(null) }
    var weekly by remember { mutableStateOf<List<DaySeriesDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching {
            stats = ApiClient.api.statistics().data
            weekly = ApiClient.api.weeklyStats().data?.series.orEmpty()
        }.onFailure { error = it.message }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        HabitFlowTopBar()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("Your Progress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            when {
                loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                stats != null -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Modifier.weight(1f), "Current streak", "${stats!!.currentStreak} days")
                        StatTile(Modifier.weight(1f), "Best streak", "${stats!!.bestStreak} days")
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(Modifier.weight(1f), "Completion", "${stats!!.overallCompletionPercentage}%")
                        StatTile(Modifier.weight(1f), "Completions", "${stats!!.totalCompletions}")
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Weekly completion", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    val maxPct = weekly.maxOfOrNull { it.percentage }?.coerceAtLeast(1) ?: 1
                    Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        weekly.forEach { d ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${d.percentage}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.fillMaxWidth().height((100 * d.percentage / maxPct).coerceAtLeast(4).dp).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(
                                    if (d.percentage == 100) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
                                ))
                                Spacer(Modifier.height(4.dp))
                                Text(d.date.takeLast(2), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    stats!!.mostConsistent?.let {
                        Spacer(Modifier.height(20.dp))
                        Text("Most consistent", fontWeight = FontWeight.SemiBold)
                        Text("${it.name ?: "—"} · ${it.percentage}%", color = MaterialTheme.colorScheme.secondary)
                    }
                    stats!!.leastConsistent?.let {
                        Spacer(Modifier.height(12.dp))
                        Text("Needs attention", fontWeight = FontWeight.SemiBold)
                        Text("${it.name ?: "—"} · ${it.percentage}%", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, label: String, value: String) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
    }
}
