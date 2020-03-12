package com.unreal.passwordguardian

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unreal.passwordguardian.CommonConstants.KEY_PASSWORD

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        masterHash = if (savedInstanceState == null) {
            val extras = intent.extras
            extras?.getString(KEY_PASSWORD)
        } else {
            savedInstanceState.getSerializable(KEY_PASSWORD) as String?
        }

        val data = PasswordEntryLoader.generatePasswords(15)

        passwordsView = findViewById(R.id.passwords)
        passwordsView?.layoutManager = LinearLayoutManager(this)
        passwordsView?.adapter = PasswordsAdapter(data) { password ->
            onPasswordClick?.invoke(password)
        }

    }

    var masterHash: String? = null

    private var passwordsView: RecyclerView? = null
    private var onPasswordClick : ((PasswordEntry) -> Unit)? = null
}
