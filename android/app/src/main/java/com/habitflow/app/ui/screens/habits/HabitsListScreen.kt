package com.habitflow.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.data.local.HabitEntity
import com.habitflow.app.ui.components.HabitForgeTopBar
import kotlinx.coroutines.launch

@Composable
fun HabitsListScreen(onAdd: () -> Unit, onEdit: (String) -> Unit) {
    var habits by remember { mutableStateOf<List<HabitEntity>>(emptyList()) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val repo = HabitFlowApp.instance.habitRepository

    fun reload() {
        scope.launch {
            repo.syncFromServer()
            habits = HabitFlowApp.instance.database.habitDao().getActive()
        }
    }
    LaunchedEffect(Unit) { reload() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HabitForgeTopBar()
            Column(Modifier.padding(16.dp)) {
                Text("Manage Habits", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("Edit or delete your habits", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                if (habits.isEmpty()) {
                    Text("No habits yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(habits, key = { it.id }) { h ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(h.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(h.name, fontWeight = FontWeight.SemiBold)
                                    Text(h.frequencyType.replace('_', ' '), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onEdit(h.id) }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { deleteId = h.id }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete habit?") },
            text = { Text("This removes the habit from your active list.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repo.deleteHabit(id); deleteId = null; reload() }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } }
        )
    }
}
