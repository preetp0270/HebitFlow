package com.habitflow.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.HabitFlowApp
import com.habitflow.app.ui.components.HabitFlowTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onLoggedOut: () -> Unit, onManageHabits: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        HabitFlowTopBar()
        Column(Modifier.padding(16.dp)) {
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Account and habits", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onManageHabits).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("📋  Manage habits", fontWeight = FontWeight.SemiBold)
                    Text("Edit or delete habits", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { scope.launch { HabitFlowApp.instance.authRepository.logout(); onLoggedOut() } },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) { Text("Log out", fontWeight = FontWeight.SemiBold) }
        }
    }
}
