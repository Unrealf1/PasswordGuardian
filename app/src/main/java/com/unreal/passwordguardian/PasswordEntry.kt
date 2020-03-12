package com.unreal.passwordguardian

data class PasswordEntry(val password: String, val cite: String) {
    var description: String? = null
}

object PasswordEntryLoader {
    fun generatePassword(length: Int): String {
        return (1..length)
            .map { charPool.random()}
            .joinToString("")
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