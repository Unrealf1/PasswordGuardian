package com.unreal.passwordguardian.PasswordEntry

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.unreal.passwordguardian.CommonConstants
import com.unreal.passwordguardian.EncryptionManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.ref.WeakReference


class BackgroundLoadArguments(val context: Context,
                              val masterPassword: String)

class BackgroundSaveArguments(val context: Context,
                              val passwords: MutableList<PasswordEntry>,
                              val masterPassword: String)

class PasswordSaveTask(private val listener: WeakReference<UIListener>)
    : AsyncTask<BackgroundSaveArguments, Void, Unit>() {
    interface UIListener {
        fun onPasswordsSaved()
    }

    override fun doInBackground(vararg params: BackgroundSaveArguments) {
        if (params.isNullOrEmpty()) {
            return
        }
        val args = params.first()
        val context = args.context
        val masterPassword = args.masterPassword
        val data = args.passwords

        return PasswordEntryLoader.save(context, data, masterPassword)
    }

    override fun onPostExecute(result: Unit) {
        listener.get()?.onPasswordsSaved()
        super.onPostExecute(result)
    }
}

class PasswordLoadTask(private val listener: WeakReference<UIListener>)
    : AsyncTask<BackgroundLoadArguments, Void, MutableList<PasswordEntry>>() {
    interface UIListener {
        fun onPasswordsLoaded(passwords: MutableList<PasswordEntry>)
    }

    override fun doInBackground(vararg params: BackgroundLoadArguments): MutableList<PasswordEntry>? {
        if (params.isNullOrEmpty()) {
            return mutableListOf()
        }
        val args = params.first()
        val context = args.context
        val masterPassword = args.masterPassword

        return PasswordEntryLoader.load(context, masterPassword)
    }

    override fun onPostExecute(result: MutableList<PasswordEntry>?) {
        listener.get()?.onPasswordsLoaded(result!!)
        super.onPostExecute(result)
    }
}



object PasswordEntryLoader {
    @UseExperimental(ExperimentalStdlibApi::class)
    fun load(context: Context, masterPassword: String): MutableList<PasswordEntry>? {
        val file = File(
            context.filesDir,
            CommonConstants.StorageFileName
        )
        if (!file.exists()) {
            file.createNewFile()
        }
        val encryptedText = file.readBytes()
        if (encryptedText.isEmpty()) {
            return mutableListOf()
        }

        val rawText = EncryptionManager.decrypt(
            encryptedText,
            masterPassword,
            context
        )
        return parseRawText(
            rawText.decodeToString()
        )
    }

    fun save(context: Context, data: List<PasswordEntry>, masterPassword: String) {
        File(
                context.filesDir,
                CommonConstants.StorageFileName
            )
            .writeBytes(
                EncryptionManager.encrypt(
                    serialize(
                        data
                    ).toByteArray(),
                    masterPassword,
                    context
                )
            )
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
                PasswordEntry(
                    item.getString("password"),
                    item.getString("name")
                )
            )
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
            .map {i: Int ->
                PasswordEntry(
                    generatePassword(
                        i % 17
                    ),
                    generateCite()
                )
            }
            .toList()
    }

    private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9') +
            arrayListOf(
                '@', '#', '$', '!', '?', '-', '=',
                '<', '>', '_', '\'', '%', '^', '*',
                '|', ';', ':', '&', '[', ']', '{',
                '}', '(', ')', ',', '/', '~', '№')
}