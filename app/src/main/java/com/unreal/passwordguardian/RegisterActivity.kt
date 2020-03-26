package com.unreal.passwordguardian

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.unreal.passwordguardian.CommonConstants.KEY_REGISTERED
import com.unreal.passwordguardian.CommonConstants.PREF_NAME
import com.unreal.passwordguardian.EntryActivity.Companion.enter
import java.io.File

class RegisterActivity : AppCompatActivity() {

    enum class PasswordError {
        OK {
            override fun getText(context: Context): String {
                return ""
            }
        },

        EMPTY {
            override fun getText(context: Context): String {
                return context.getString(R.string.password_empty)
            }
        },

        SHORT {
            override fun getText(context: Context): String {
                return context.getString(R.string.password_too_short)
            }
        },

        NOT_EQUAL {
            override fun getText(context: Context): String {
                return context.getString(R.string.passwords_not_equal)
            }
        };

        abstract fun getText(context: Context): String
    }

    private fun checkPasswords(password1: String, password2: String): PasswordError {
        return when {
            (password1 == "") -> PasswordError.EMPTY
            (password1.length < minimal_password_length) -> PasswordError.SHORT
            (password1 != password2) -> PasswordError.NOT_EQUAL
            else -> PasswordError.OK
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val passwordRegister = findViewById<EditText>(R.id.passwordRegisterText)
        val passwordRepeat = findViewById<EditText>(R.id.passwordRepeatText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val errorText = findViewById<TextView>(R.id.error_text)

        registerButton.setOnClickListener {
            val password1 = passwordRegister.text.toString()
            val password2 = passwordRepeat.text.toString()

            val error = checkPasswords(password1, password2)

            if (error == PasswordError.OK) {
                val preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putBoolean(KEY_REGISTERED, true)
                editor.apply()
                createVerification(password1)
                Toast.makeText(this, R.string.succesiful_registration_toast, Toast.LENGTH_SHORT).show()

                enter(this, password1)
            } else {
                Toast.makeText(this, error.getText(this), Toast.LENGTH_SHORT).show()
            }

        }
        val activity = this
        val errorSignWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val password1 = passwordRegister.text.toString()
                val password2 = passwordRepeat.text.toString()

                val error = checkPasswords(password1, password2)

                if (error == PasswordError.OK) {
                    errorText.visibility = View.INVISIBLE
                } else {
                    errorText.visibility = View.VISIBLE
                    errorText.text = error.getText(activity)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }

        passwordRegister.addTextChangedListener(errorSignWatcher)
        passwordRepeat.addTextChangedListener(errorSignWatcher)
    }

    private fun createVerification(masterPassword: String) {
        val file = File(this.filesDir, CommonConstants.FILENAME_VERIFICATION)
        file.createNewFile()
        file.writeBytes(EncryptionManager.encrypt(
            CommonConstants.VerificationConstant.toByteArray(),
            masterPassword,
            this))
    }

    companion object {
        const val minimal_password_length = 4
    }
}
