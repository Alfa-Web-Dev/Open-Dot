package app.opendot.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

data class DotAgent(
    val id: Long,
    val name: String,
    val description: String,
    val instructions: String
)

data class ChatMessage(
    val id: Long,
    val agentId: Long,
    val role: String,
    val content: String,
    val createdAt: Long
)

class AgentStore(context: Context) : SQLiteOpenHelper(context, "opendot.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE agents(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL, instructions TEXT NOT NULL)")
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, agent_id INTEGER NOT NULL, role TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("INSERT INTO agents(name,description,instructions) VALUES('Assistant','Your general-purpose AI assistant.','You are a helpful, clear and honest assistant.')")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun agents(): List<DotAgent> {
        val result = mutableListOf<DotAgent>()
        readableDatabase.rawQuery("SELECT id,name,description,instructions FROM agents ORDER BY id DESC", null).use { c ->
            while (c.moveToNext()) result += DotAgent(c.getLong(0), c.getString(1), c.getString(2), c.getString(3))
        }
        return result
    }

    fun addAgent(name: String, description: String, instructions: String): Long {
        val values = android.content.ContentValues().apply {
            put("name", name); put("description", description); put("instructions", instructions)
        }
        return writableDatabase.insertOrThrow("agents", null, values)
    }

    fun messages(agentId: Long): List<ChatMessage> {
        val result = mutableListOf<ChatMessage>()
        readableDatabase.rawQuery("SELECT id,agent_id,role,content,created_at FROM messages WHERE agent_id=? ORDER BY id", arrayOf(agentId.toString())).use { c ->
            while (c.moveToNext()) result += ChatMessage(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getLong(4))
        }
        return result
    }

    fun addMessage(agentId: Long, role: String, content: String) {
        val values = android.content.ContentValues().apply {
            put("agent_id", agentId); put("role", role); put("content", content); put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertOrThrow("messages", null, values)
    }
}

class SecretVault(context: Context) {
    private val prefs = context.getSharedPreferences("secure_provider_settings", Context.MODE_PRIVATE)
    private val alias = "opendot_android_master_key"

    private fun secretKey(): java.security.Key {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        store.getKey(alias, null)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }

    fun save(key: String, value: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString("${key}_iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(key, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun read(key: String): String {
        val encrypted = prefs.getString(key, null) ?: return ""
        val iv = prefs.getString("${key}_iv", null) ?: return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), Charsets.UTF_8)
    }

    fun saveText(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    fun readText(key: String, fallback: String = ""): String = prefs.getString(key, fallback) ?: fallback
}

object CompatibleAiClient {
    fun reply(endpoint: String, model: String, apiKey: String, systemPrompt: String, history: List<ChatMessage>): String {
        val base = endpoint.trim().trimEnd('/')
        require(base.startsWith("https://") || base.startsWith("http://localhost") || base.startsWith("http://127.0.0.1")) {
            "Use HTTPS, or localhost for a local model endpoint."
        }
        require(model.isNotBlank()) { "Enter a model name in Settings." }
        require(history.isNotEmpty()) { "Write a message first." }
        val url = URL("$base/chat/completions")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 90000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            if (apiKey.isNotBlank()) setRequestProperty("Authorization", "Bearer $apiKey")
        }
        try {
            val messages = JSONArray().put(JSONObject().put("role", "system").put("content", systemPrompt))
            history.takeLast(30).forEach { messages.put(JSONObject().put("role", it.role).put("content", it.content)) }
            val payload = JSONObject().put("model", model).put("messages", messages).put("stream", false)
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (status !in 200..299) throw IllegalStateException("AI provider returned HTTP $status: ${body.take(500)}")
            val json = JSONObject(body)
            return json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
        } finally {
            connection.disconnect()
        }
    }
}
