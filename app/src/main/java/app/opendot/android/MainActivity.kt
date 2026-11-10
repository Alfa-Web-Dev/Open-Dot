package app.opendot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Canvas = Color(0xFF0D0E13)
private val Panel = Color(0xFF171923)
private val Panel2 = Color(0xFF202230)
private val Muted = Color(0xFF9699AA)
private val Accent = Color(0xFF9B8CFF)
private val Mint = Color(0xFF83D8BC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenDotApp() }
    }
}

private enum class Destination(val label: String, val glyph: String) {
    Home("Home", "⌂"), Dots("Dots", "✳"), Chats("Chats", "▤"),
    Tasks("Tasks", "✓"), Settings("Settings", "⚙")
}

@Composable
private fun OpenDotApp() {
    var selected by remember { mutableStateOf(Destination.Home) }
    val scheme = darkColorScheme(
        primary = Accent,
        onPrimary = Color.White,
        background = Canvas,
        surface = Panel,
        onSurface = Color(0xFFF2F1F8),
        secondary = Mint
    )
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = Canvas,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF14151D),
                    contentColor = Muted
                ) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { selected = destination },
                            icon = { Text(destination.glyph, fontSize = 19.sp) },
                            label = { Text(destination.label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                AppHeader()
                Spacer(Modifier.height(24.dp))
                when (selected) {
                    Destination.Home -> HomeScreen(
                        onDots = { selected = Destination.Dots },
                        onChats = { selected = Destination.Chats },
                        onTasks = { selected = Destination.Tasks }
                    )
                    Destination.Dots -> DotsScreen()
                    Destination.Chats -> ChatsScreen()
                    Destination.Tasks -> TasksScreen()
                    Destination.Settings -> SettingsScreen()
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppHeader() {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).background(
                    Brush.linearGradient(listOf(Accent, Color(0xFF6554C8))),
                    RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("O", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("OpenDot", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("PERSONAL AI WORKSPACE", fontSize = 8.sp, color = Muted, letterSpacing = 1.sp)
            }
        }
        Surface(color = Panel2, shape = CircleShape) {
            Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(Mint, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text("LOCAL MODE", color = Mint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeScreen(onDots: () -> Unit, onChats: () -> Unit, onTasks: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column {
            Text("Good to see you", color = Muted, fontSize = 14.sp)
            Text("Your workspace.", fontSize = 29.sp, fontWeight = FontWeight.Bold)
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(23.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF292344), Color(0xFF191C2C), Color(0xFF172B2B)))
                ).padding(21.dp)
            ) {
                Text("YOUR AI, YOUR WAY", color = Color(0xFFC4B9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                Spacer(Modifier.height(12.dp))
                Text("One space for
all your ideas.", fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Create agents, start conversations and keep your work organized.", color = Color(0xFFC1C2D0), fontSize = 13.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(17.dp))
                Button(
                    onClick = onChats,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Start a conversation", color = Color.White, fontWeight = FontWeight.SemiBold) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            MetricCard("YOUR DOTS", "0", "AI agents", Modifier.weight(1f))
            MetricCard("TASKS", "0", "In your workspace", Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Workspace", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("OVERVIEW", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        ActionCard("✳", "Your Dots", "Build AI agents for different roles", Accent, onDots)
        ActionCard("▤", "Conversations", "Continue or start a new chat", Color(0xFF79B8F2), onChats)
        ActionCard("✓", "Tasks & plans", "Keep track of what needs doing", Mint, onTasks)
        Surface(color = Panel, shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("i", color = Accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Getting started", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Connect an AI provider in Settings to enable AI responses.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
        Text("OpenDot Android  •  Early access", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun MetricCard(title: String, value: String, caption: String, modifier: Modifier) {
    Surface(modifier, color = Panel, shape = RoundedCornerShape(17.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(caption, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ActionCard(symbol: String, title: String, description: String, tint: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Panel,
        shape = RoundedCornerShape(17.dp)
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(tint.copy(alpha = 0.13f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Text(symbol, color = tint, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(description, fontSize = 11.sp, color = Muted)
            }
            Text("›", color = Muted, fontSize = 25.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(5.dp))
    Text(subtitle, color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun DotsScreen() {
    Column {
        SectionTitle("Your Dots", "AI agents for the way you work.")
        EmptyState("✳", "No Dots yet", "Your AI agents will appear here. Dot creation and configuration are coming next.")
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Create your first Dot")
        }
        Text("Dot creation is not implemented in this preview build.", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun ChatsScreen() {
    var draft by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }
    Column {
        SectionTitle("Chats", "Your conversations, all in one place.")
        if (messages.isEmpty()) {
            EmptyState("▤", "Start with a blank page", "Connect an AI provider in Settings before sending messages to an AI.")
        } else {
            messages.forEach { message ->
                Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text(message, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
                }
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Write a message…", color = Muted) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        Spacer(Modifier.height(9.dp))
        Button(
            onClick = { if (draft.isNotBlank()) { messages.add(draft.trim()); draft = "" } },
            enabled = draft.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Add to preview conversation") }
        Text("Preview only: messages stay on this screen and are not sent to an AI yet.", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 9.dp))
    }
}

@Composable
private fun TasksScreen() {
    val tasks = remember { mutableStateListOf("Connect an AI provider", "Create your first Dot", "Explore workspace settings") }
    val completed = remember { mutableStateListOf<String>() }
    Column {
        SectionTitle("Tasks", "A simple space to organize your next steps.")
        tasks.forEach { task ->
            val done = completed.contains(task)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp).clickable {
                    if (done) completed.remove(task) else completed.add(task)
                },
                color = Panel,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (done) "☑" else "□", color = if (done) Mint else Accent, fontSize = 21.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(task, color = if (done) Muted else Color.White, fontSize = 13.sp)
                }
            }
        }
        Text("Sample tasks only. Task creation and persistent storage are not implemented yet.", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun SettingsScreen() {
    Column {
        SectionTitle("Settings", "Configure your OpenDot workspace.")
        ActionCard("◈", "AI providers", "Provider setup will be added next", Accent) {}
        Spacer(Modifier.height(10.dp))
        ActionCard("文", "Language", "English • Hindi • Urdu planned", Mint) {}
        Spacer(Modifier.height(10.dp))
        ActionCard("⌑", "Privacy & storage", "Local-first settings planned", Color(0xFF79B8F2)) {}
        Spacer(Modifier.height(16.dp))
        Text("These settings are visual placeholders. Provider configuration and persistence are not active yet.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyState(symbol: String, title: String, description: String) {
    Surface(color = Panel, shape = RoundedCornerShape(19.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(54.dp).background(Accent.copy(alpha = 0.13f), CircleShape), contentAlignment = Alignment.Center) {
                Text(symbol, color = Accent, fontSize = 24.sp)
            }
            Spacer(Modifier.height(15.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            Text(description, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
