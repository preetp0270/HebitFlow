package com.habitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HabitForgeTopBar(title: String = "HabitForge", synced: Boolean = true, userInitial: String = "U") {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("HF", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.width(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        if (synced) {
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFF131B2E)).padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                Spacer(Modifier.width(6.dp))
                Text("Synced", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(10.dp))
        }
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(userInitial.take(1).uppercase(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
fun SectionLabel(text: String, trailing: String? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        if (trailing != null) Text(trailing, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MomentumCard(percentage: Int, completed: Int, total: Int, streak: Int) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TODAY'S MOMENTUM", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.8.sp)
                Text("Daily Progress", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Row(
                Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFF222A3D)).padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 12.sp)
                Spacer(Modifier.width(4.dp))
                Text("$streak day streak", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$percentage", fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 48.sp)
            Text("%", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF131B2E)), contentAlignment = Alignment.Center) {
                Text("⚡", fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF131B2E)).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
            Spacer(Modifier.width(6.dp))
            Text("$completed of $total habits completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val segs = total.coerceAtLeast(1)
            repeat(segs) { i ->
                Box(
                    Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (i < completed) MaterialTheme.colorScheme.secondary else Color(0xFF2D3449))
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        val left = (total - completed).coerceAtLeast(0)
        Text(
            if (left == 0) "Perfect day — all habits done!" else "$left habit${if (left == 1) "" else "s"} left to maintain your streak!",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HabitRowCard(
    icon: String, name: String, subtitle: String, streak: Int, completed: Boolean, onToggle: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF222A3D)), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Text("$streak day streak", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
            }
        }
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(if (completed) MaterialTheme.colorScheme.secondary else Color(0xFF2D3449)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Text(if (completed) "✓" else "", color = if (completed) Color(0xFF003824) else Color.Transparent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
