package com.unreal.passwordguardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unreal.passwordguardian.CommonConstants.KEY_PASSWORD
import com.unreal.passwordguardian.CommonConstants.KEY_REGISTERED
import com.unreal.passwordguardian.CommonConstants.PREF_NAME


class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val passwordEdit = findViewById<EditText>(R.id.passwordEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        confirmButton.setOnClickListener {
            val password = passwordEdit.text.toString()
            if (EncryptionManager.verify(password, this)) {
                enter(this, password)
            } else {
                Toast.makeText(this, R.string.wrong_password_text, Toast.LENGTH_LONG).show()
            }
        }

        if (!sharedPreferences.getBoolean(KEY_REGISTERED, false)) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    companion object {
        fun enter(context: Context, password: String) {
            val intent = Intent(context, MainActivity::class.java)

            intent.putExtra(KEY_PASSWORD, password)
            intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
}
