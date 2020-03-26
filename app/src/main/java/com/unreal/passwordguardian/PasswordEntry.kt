package com.unreal.passwordguardian

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


data class PasswordEntry(val password: String, val cite: String) {
    var description: String? = null
}

object EncryptionManager{
    fun verify(key: String): Boolean {
        return true
    }

    fun encrypt(bytes: ByteArray, key: String, context: Context): ByteArray {
        val saltFile = File(context.filesDir, CommonConstants.FILENAME_SALT)
        val ivFile = File(context.filesDir, CommonConstants.FILENAME_IV)

        val gotSalt = getSalt(saltFile)
        val salt = if (gotSalt == null) {
            setSalt(generateSalt(), saltFile)
            getSalt(saltFile)!!
        } else {
            gotSalt
        }

        val keySpec = getKey(key, salt)
        val gotIV = getIV(ivFile)

        val iv = if (gotIV == null) {
            setIV(generateIV(), ivFile)
            getIV(ivFile)!!
        } else {
            gotIV
        }
        val ivSpec = IvParameterSpec(iv)
        keykey = keySpec
        old_IV = iv
        old_salt = salt

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(bytes)
    }
    private var keykey: SecretKeySpec? = null
    var old_salt: ByteArray? = null
    var old_IV:ByteArray? = null

    fun decrypt(bytes: ByteArray, key: String, context: Context): ByteArray {
        val saltFile = File(context.filesDir, CommonConstants.FILENAME_SALT)
        val ivFile = File(context.filesDir, CommonConstants.FILENAME_IV)

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec = getKey(key, getSalt(saltFile)!!)
        val ivSpec = IvParameterSpec(getIV(ivFile)!!)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(bytes)
    }

    private fun generateIV(): ByteArray {
        val ivRandom = SecureRandom()
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        return iv
    }

    private fun getIV(file: File): ByteArray? {
        return if (!file.exists()) {
            file.createNewFile()
            null
        } else {
            file.readBytes()
        }
    }

    private fun setIV(iv: ByteArray, file: File) {
        file.writeBytes(iv)
    }

    private fun getKey(password: String, salt:ByteArray): SecretKeySpec {
        val pbKeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            1324,
            256)

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun getSalt(file: File): ByteArray? {
        return if (!file.exists()) {
            file.createNewFile()
            null
        } else {
            file.readBytes()
        }
    }

    private fun setSalt(salt: ByteArray, file: File) {
        file.writeBytes(salt)
    }

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(256)
        random.nextBytes(salt)
        return salt
    }

}

object PasswordEntryLoader {
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

        return parseRawText(rawText.toString())
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