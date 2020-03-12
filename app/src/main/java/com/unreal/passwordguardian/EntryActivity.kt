package com.unreal.passwordguardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.unreal.passwordguardian.CommonConstants.KEY_PASSWORD
import com.unreal.passwordguardian.CommonConstants.PREF_NAME
import java.security.MessageDigest


class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val passwordEdit = findViewById<EditText>(R.id.passwordEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        val preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val realPassword = preferences.getString(KEY_PASSWORD, null)

        confirmButton.setOnClickListener {
            val password = passwordEdit.text.toString()
            if (password == realPassword) {
                enter(this, password)
            } else {
                Toast.makeText(this, R.string.wrong_password_text, Toast.LENGTH_LONG).show()
            }
        }

        if (realPassword == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        fun enter(context: Context, password: String) {
            val md: MessageDigest = MessageDigest.getInstance("SHA256")

            val intent = Intent(context, MainActivity::class.java)

            md.update(password.toByteArray())
            val hash = md.digest().toString()
            intent.putExtra(KEY_PASSWORD, hash)
            context.startActivity(intent)
        }
    }
}
