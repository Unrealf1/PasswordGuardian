package com.unreal.passwordguardian

import android.content.Context
import java.io.File
import java.lang.Exception
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager{
    @UseExperimental(ExperimentalStdlibApi::class)
    fun verify(key: String, context: Context): Boolean {
        val bytes = File(context.filesDir, CommonConstants.FILENAME_VERIFICATION).readBytes()
        var result = ""
        var caught = false
        try {
             result = decrypt(bytes, key, context).decodeToString()
        } catch (e: Exception) {
            caught = true
        }
        return (!caught) && (result == CommonConstants.VerificationConstant)
    }

    fun encrypt(bytes: ByteArray, key: String, context: Context): ByteArray {
        val saltFile = File(
            context.filesDir,
            CommonConstants.FILENAME_SALT
        )
        val ivFile = File(
            context.filesDir,
            CommonConstants.FILENAME_IV
        )

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
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(bytes)
    }

    fun decrypt(bytes: ByteArray, key: String, context: Context): ByteArray {
        val saltFile = File(
            context.filesDir,
            CommonConstants.FILENAME_SALT
        )
        val ivFile = File(
            context.filesDir,
            CommonConstants.FILENAME_IV
        )
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val salt = getSalt(saltFile)!!
        val keySpec = getKey(key, salt)
        val iv = getIV(ivFile)!!
        val ivSpec = IvParameterSpec(iv)
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
            256
        )

        val secretKeyFactory =
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
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