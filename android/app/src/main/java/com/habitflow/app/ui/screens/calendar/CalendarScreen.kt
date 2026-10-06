package com.habitflow.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.data.remote.ApiClient
import com.habitflow.app.data.remote.DaySeriesDto
import com.habitflow.app.data.remote.TodayItemDto
import com.habitflow.app.ui.components.HabitFlowTopBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen() {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var series by remember { mutableStateOf<List<DaySeriesDto>>(emptyList()) }
    var dayItems by remember { mutableStateOf<List<TodayItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val byDate = remember(series) { series.associateBy { it.date } }

    fun loadMonth() {
        scope.launch {
            loading = true
            runCatching {
                series = ApiClient.api.monthlyStats(month.year, month.monthValue).data?.series.orEmpty()
            }
            loading = false
        }
    }
    fun loadDay(d: LocalDate) {
        scope.launch {
            runCatching { dayItems = ApiClient.api.today(d.toString()).data?.items.orEmpty() }
                .onFailure { dayItems = emptyList() }
        }
    }
    LaunchedEffect(month) { loadMonth() }
    LaunchedEffect(selected) { loadDay(selected) }

    val first = month.atDay(1)
    val monFirstPad = (first.dayOfWeek.value + 6) % 7
    val daysInMonth = month.lengthOfMonth()

    Column(Modifier.fillMaxSize()) {
        HabitFlowTopBar()
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev") }
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next") }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("M","T","W","T","F","S","S").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            if (loading) Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else {
                val cells = buildList {
                    repeat(monFirstPad) { add(null as LocalDate?) }
                    for (d in 1..daysInMonth) add(month.atDay(d))
                }
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            Box(Modifier.weight(1f).aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
                                if (day != null) {
                                    val info = byDate[day.toString()]
                                    val pct = info?.percentage ?: -1
                                    val isSelected = day == selected
                                    val bg = when {
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        pct == 100 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                        pct > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        pct == 0 && (info?.scheduled ?: 0) > 0 -> Color(0xFFFFB4AB).copy(alpha = 0.2f)
                                        else -> Color.Transparent
                                    }
                                    Column(
                                        Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(bg)
                                            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier)
                                            .clickable { selected = day },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("${day.dayOfMonth}", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                        if (pct >= 0 && (info?.scheduled ?: 0) > 0) {
                                            Box(Modifier.padding(top = 2.dp).height(3.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(2.dp)).background(
                                                when { pct == 100 -> MaterialTheme.colorScheme.secondary; pct > 0 -> MaterialTheme.colorScheme.primary; else -> Color(0xFFFFB4AB) }
                                            ))
                                        }
                                    }
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(selected.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text("${dayItems.count { it.completed }} of ${dayItems.size} done", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dayItems.isEmpty()) item {
                    Text("No habits scheduled this day", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                }
                items(dayItems, key = { it.habit._id }) { item ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.habit.icon ?: "🎯", fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.habit.name, fontWeight = FontWeight.SemiBold)
                            Text(if (item.completed) "Completed" else "Not completed", color = if (item.completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        if (item.completed) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
