package app.opendot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OpenDotApp() }
    }
}

@Composable
private fun OpenDotApp() {
    val colors = darkColorScheme(
        primary = Color(0xFF9B8CFF),
        background = Color(0xFF0B0B10),
        surface = Color(0xFF171720)
    )
    MaterialTheme(colorScheme = colors) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OpenDot", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("ANDROID", color = Color(0xFF9B8CFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("Your AI workspace", color = Color(0xFFB7B7C5), fontSize = 15.sp)
                Surface(
                    color = Color(0xFF191923),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Welcome to OpenDot", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your personal AI workspace is being prepared. Create Dots, organize tasks, and connect an AI provider as features arrive.",
                            color = Color(0xFFB7B7C5), lineHeight = 22.sp
                        )
                    }
                }
                Text("WORKSPACE", color = Color(0xFF858595), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                listOf("Dots" to "Create and configure your AI agents", "Chats" to "Your conversations in one place", "Tasks" to "Plan and track your work").forEach { item ->
                    Surface(color = Color(0xFF171720), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text(item.first, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(item.second, color = Color(0xFF9999A8), fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("EARLY DEVELOPMENT BUILD • v0.1.0", color = Color(0xFF777786), fontSize = 10.sp)
            }
        }
    }
}
