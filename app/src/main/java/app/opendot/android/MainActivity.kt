package app.opendot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Canvas = Color(0xFF0D0E13)
private val Panel = Color(0xFF171923)
private val Muted = Color(0xFF9699AA)
private val Accent = Color(0xFF9B8CFF)
private val Mint = Color(0xFF83D8BC)

private enum class Destination(val label: String, val glyph: String) {
    Home("Home", "⌂"), Dots("Dots", "✳"), Chats("Chats", "▤"),
    Tasks("Tasks", "✓"), Settings("Settings", "⚙")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenDotApp() }
    }
}

@Composable
private fun OpenDotApp() {
    val context = LocalContext.current
    val store = remember { AgentStore(context.applicationContext) }
    val vault = remember { SecretVault(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val agents = remember { mutableStateListOf<DotAgent>() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var selected by remember { mutableStateOf(Destination.Home) }
    var activeAgent by remember { mutableStateOf<DotAgent?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf(vault.readText("endpoint", "https://api.openai.com/v1")) }
    var model by remember { mutableStateOf(vault.readText("model", "")) }
    var apiKey by remember { mutableStateOf(vault.read("api_key")) }

    fun refreshAgents() {
        agents.clear()
        agents.addAll(store.agents())
    }
    fun refreshMessages(agent: DotAgent?) {
        messages.clear()
        if (agent != null) messages.addAll(store.messages(agent.id))
    }
    LaunchedEffect(Unit) { refreshAgents() }

    val scheme = darkColorScheme(primary = Accent, background = Canvas, surface = Panel, secondary = Mint)
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = Canvas,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF14151D)) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { selected = destination; status = "" },
                            icon = { Text(destination.glyph, fontSize = 19.sp) },
                            label = { Text(destination.label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 19.dp)) {
                Spacer(Modifier.height(15.dp))
                Header()
                Spacer(Modifier.height(22.dp))
                when (selected) {
                    Destination.Home -> HomeScreen(
                        agents.size,
                        onDots = { selected = Destination.Dots },
                        onChats = { selected = Destination.Chats },
                        onSettings = { selected = Destination.Settings }
                    )
                    Destination.Dots -> DotsScreen(
                        agents = agents,
                        onCreate = { showCreate = true },
                        onOpen = { agent -> activeAgent = agent; refreshMessages(agent); selected = Destination.Chats }
                    )
                    Destination.Chats -> ChatsScreen(
                        agents = agents,
                        activeAgent = activeAgent,
                        messages = messages,
                        draft = draft,
                        onDraft = { draft = it },
                        busy = busy,
                        status = status,
                        onSelect = { agent -> activeAgent = agent; refreshMessages(agent); status = "" },
                        onSend = {
                            val agent = activeAgent
                            val prompt = draft.trim()
                            if (agent == null) status = "Choose an agent first."
                            else if (prompt.isNotEmpty() && !busy) {
                                draft = ""
                                store.addMessage(agent.id, "user", prompt)
                                refreshMessages(agent)
                                busy = true
                                status = "Thinking…"
                                scope.launch {
                                    try {
                                        val answer = withContext(Dispatchers.IO) {
                                            CompatibleAiClient.reply(
                                                endpoint, model, vault.read("api_key"),
                                                agent.instructions, store.messages(agent.id)
                                            )
                                        }
                                        store.addMessage(agent.id, "assistant", answer)
                                        refreshMessages(agent)
                                        status = ""
                                    } catch (e: Exception) {
                                        status = e.message ?: "Could not reach the AI provider."
                                    } finally { busy = false }
                                }
                            }
                        }
                    )
                    Destination.Tasks -> TasksScreen()
                    Destination.Settings -> SettingsScreen(
                        endpoint, { endpoint = it }, model, { model = it },
                        apiKey, { apiKey = it }, status,
                        onSave = {
                            vault.saveText("endpoint", endpoint.trim())
                            vault.saveText("model", model.trim())
                            vault.save("api_key", apiKey)
                            status = "Provider settings saved securely on this device."
                        }
                    )
                }
            }
        }

        if (showCreate) {
            var name by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var instructions by remember { mutableStateOf("You are a helpful, honest assistant. Follow the user's instructions and explain uncertainty.") }
            AlertDialog(
                onDismissRequest = { showCreate = false },
                containerColor = Panel,
                title = { Text("Create a Dot") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        OutlinedTextField(name, { name = it }, label = { Text("Agent name") }, singleLine = true)
                        OutlinedTextField(description, { description = it }, label = { Text("What does it do?") })
                        OutlinedTextField(instructions, { instructions = it }, label = { Text("Custom instructions") }, minLines = 3)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotBlank() && instructions.isNotBlank()) {
                            store.addAgent(name.trim(), description.trim(), instructions.trim())
                            refreshAgents()
                            showCreate = false
                        }
                    }) { Text("Create Dot") }
                },
                dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Accent, shape = RoundedCornerShape(11.dp), modifier = Modifier.size(37.dp)) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Text("O", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text("OpenDot", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("PERSONAL AI WORKSPACE", fontSize = 8.sp, color = Muted, letterSpacing = 1.sp)
            }
        }
        Surface(color = Panel, shape = CircleShape) {
            Text("ANDROID", color = Accent, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HomeScreen(agentCount: Int, onDots: () -> Unit, onChats: () -> Unit, onSettings: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Your workspace.", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Your AI team, built around you.", color = Muted, fontSize = 13.sp)
        Surface(color = Color.Transparent, shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF30264E), Color(0xFF172D32)))).padding(20.dp)) {
                Text("MAKE AI YOUR OWN", color = Color(0xFFC8BEFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(10.dp))
                Text("Meet your next
AI teammate.", fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Create a Dot with its own role, personality and instructions.", color = Color(0xFFD0D0DA), fontSize = 12.sp)
                Spacer(Modifier.height(15.dp))
                Button(onClick = onDots, colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text("Explore your Dots") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("YOUR DOTS", agentCount.toString(), "Created agents", Modifier.weight(1f))
            StatCard("AI ENGINE", "Ready", "Connect a provider", Modifier.weight(1f))
        }
        Text("Workspace", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        NavCard("✳", "Your Dots", "Create and customize AI agents", onDots)
        NavCard("▤", "Conversations", "Chat with your own agents", onChats)
        NavCard("⚙", "AI settings", "Connect a compatible AI provider", onSettings)
        Text("Local-first preview • Agent data stays on this device", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun StatCard(label: String, value: String, hint: String, modifier: Modifier) {
    Surface(modifier, color = Panel, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text(hint, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun NavCard(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(color = Panel, shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symbol, color = Accent, fontSize = 22.sp)
            Spacer(Modifier.size(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = Muted, fontSize = 24.sp)
        }
    }
}

@Composable
private fun DotsScreen(agents: List<DotAgent>, onCreate: () -> Unit, onOpen: (DotAgent) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text("Your Dots", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Each Dot has its own role and instructions.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(15.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
            Text("+  Create a new Dot")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(agents, key = { it.id }) { agent ->
                Surface(color = Panel, shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().clickable { onOpen(agent) }) {
                    Column(Modifier.padding(15.dp)) {
                        Text(agent.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(agent.description.ifBlank { "Personal AI agent" }, color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Open conversation  ›", color = Accent, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatsScreen(
    agents: List<DotAgent>, activeAgent: DotAgent?, messages: List<ChatMessage>,
    draft: String, onDraft: (String) -> Unit, busy: Boolean, status: String,
    onSelect: (DotAgent) -> Unit, onSend: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("Conversations", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        if (agents.isNotEmpty()) {
            LazyRow(Modifier.height(58.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(agents, key = { it.id }) { agent ->
                    Surface(color = if (activeAgent?.id == agent.id) Accent.copy(alpha = 0.25f) else Panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { onSelect(agent) }) {
                        Text(agent.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), fontSize = 11.sp)
                    }
                }
            }
        }
        Text(activeAgent?.name ?: "Select an agent to start", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { message ->
                Surface(
                    color = if (message.role == "user") Color(0xFF292443) else Panel,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(13.dp)) {
                        Text(if (message.role == "user") "YOU" else activeAgent?.name ?: "DOT", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(message.content, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }
        if (status.isNotBlank()) Text(status, color = if (status.startsWith("AI provider") || status.contains("HTTP") || status.contains("Enter")) Color(0xFFFFB4AB) else Muted, fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft, onValueChange = onDraft, modifier = Modifier.weight(1f),
                placeholder = { Text("Message your Dot…") }, singleLine = true,
                enabled = activeAgent != null && !busy
            )
            Spacer(Modifier.size(7.dp))
            Button(onClick = onSend, enabled = activeAgent != null && draft.isNotBlank() && !busy) { Text(if (busy) "…" else "Send") }
        }
        Text("Messages are stored locally. AI replies require a configured provider.", color = Muted, fontSize = 9.sp)
    }
}

@Composable
private fun TasksScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tasks", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Task orchestration will connect to SuperDot in a later phase.", color = Muted, fontSize = 12.sp)
        Surface(color = Panel, shape = RoundedCornerShape(15.dp)) {
            Text("The original OpenDot project includes project planning, task dependencies, reviews and department Dots. Those capabilities are not ported yet.", modifier = Modifier.padding(16.dp), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun SettingsScreen(
    endpoint: String, onEndpoint: (String) -> Unit,
    model: String, onModel: (String) -> Unit,
    apiKey: String, onApiKey: (String) -> Unit,
    status: String, onSave: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("AI provider", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text("Use an OpenAI-compatible endpoint, including a local server that exposes the same API.", color = Muted, fontSize = 12.sp)
        OutlinedTextField(endpoint, onEndpoint, modifier = Modifier.fillMaxWidth(), label = { Text("Base URL") }, singleLine = true)
        OutlinedTextField(model, onModel, modifier = Modifier.fillMaxWidth(), label = { Text("Model name") }, singleLine = true)
        OutlinedTextField(apiKey, onApiKey, modifier = Modifier.fillMaxWidth(), label = { Text("API key (optional for local models)") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text("Save provider settings") }
        if (status.isNotBlank()) Text(status, color = Mint, fontSize = 11.sp)
        Surface(color = Panel, shape = RoundedCornerShape(14.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text("Free / local use", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("You can connect a local OpenAI-compatible model server without an API key. Cloud provider usage may have separate limits or charges.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(7.dp))
                Text("Security: API keys are encrypted with Android Keystore. Cleartext HTTP is restricted to localhost.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
            }
        }
    }
}
