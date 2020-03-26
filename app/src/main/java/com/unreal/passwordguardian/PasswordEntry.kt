package com.unreal.passwordguardian

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


data class PasswordEntry(val password: String, val cite: String) {
    var description: String? = null
}

object PasswordEntryLoader {
    @UseExperimental(ExperimentalStdlibApi::class)
    fun load(context: Context, masterPassword: String): MutableList<PasswordEntry>? {
        val file = File(context.filesDir, CommonConstants.StorageFileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val encryptedText = file.readBytes()
        if (encryptedText.isEmpty()) {
            return mutableListOf()
        }

        val rawText = EncryptionManager.decrypt(encryptedText, masterPassword, context)
        return parseRawText(rawText.decodeToString())
    }

    fun save(context: Context, data: List<PasswordEntry>, masterPassword: String) {
        File(context.filesDir, CommonConstants.StorageFileName)
            .writeBytes(EncryptionManager.encrypt(serialize(data).toByteArray(), masterPassword, context))
    }

    fun generatePassword(length: Int): String {
        return (1..length)
            .map { charPool.random()}
            .joinToString("")
    }

    private fun parseRawText(text: String): MutableList<PasswordEntry>? {
        val array = JSONArray(text)
        val result = mutableListOf<PasswordEntry>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            result.add(
                PasswordEntry(item.getString("password"),
                    item.getString("name")))
        }
        return result
    }

    private fun serialize(data: List<PasswordEntry>): String {
        val array = JSONArray()
        for (entry in data) {
            array.put(
                JSONObject()
                    .put("password", entry.password)
                    .put("name", entry.cite)
                    .put("description", entry.description)
            )
        }
        return array.toString()
    }


    private fun generateDomain(): String {
        return listOf(
            "one", "two", "one",
            "kremlin", "wikipedia", "wargaming",
            "mipt", "kotlinlang", "pornhub",
            "rain", "sun", "oracle",
            "android", "lttstore", "mint",
            "debian", "gradle", "termianl",
            "example", "keyboard", "amazon",
            "laptop", "build", "game").random()
    }

    private fun generateEndDomain(): String {
        return listOf(
            "com", "ru", "net",
            "org", "edu", "biz",
            "рф", "uk", "us",
            "eu", "onion", "tv").random()
    }

    private fun generateCite(): String {
        val length = (1..4).random()
        var result = ""
        for (i in (0..length)) {
            result += generateDomain() + "."
        }
        return result + generateEndDomain()
    }

    fun generatePasswords(count: Int): List<PasswordEntry> {
        return (1..count)
            .map {i: Int -> PasswordEntry(generatePassword(i % 17), generateCite())}
            .toList()
    }

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9') +
            arrayListOf(
                '@', '#', '$', '!', '?', '-', '=',
                '<', '>', '_', '\'', '%', '^', '*',
                '|', ';', ':', '&', '[', ']', '{',
                '}', '(', ')', ',', '/', '~', '№')
}