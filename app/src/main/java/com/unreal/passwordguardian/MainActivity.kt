package com.unreal.passwordguardian

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        val filterTextEdit = findViewById<EditText>(R.id.filterText)
        val createNewButton = findViewById<Button>(R.id.buttonCreateNew)
        val settingsButton = findViewById<Button>(R.id.buttonSettings)

        val generatePasswordButton = findViewById<Button>(R.id.generatePasswordButton)
        val newPassword = findViewById<EditText>(R.id.newEntryPasswordField)
        val newName = findViewById<EditText>(R.id.newEntryNameField)

        filterTextEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filterPasswords(s.toString(), data)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        createNewButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                .setTitle(R.string.new_entry_popup_title)
                .setView(R.layout.new_entry_popup)
                .setPositiveButton(R.string.button_accept) { _, _ ->
                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show()
                    addPassword(PasswordEntry(
                        newPassword.text.toString(),
                        newName.text.toString() ))
                }
                .setNegativeButton(R.string.button_cancel) { _, _ ->

                }

            val dialog = builder.create()
            dialog.show()
        }

        generatePasswordButton.setOnClickListener {
            newPassword.setText(PasswordEntryLoader.generatePassword(12))
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        passwordsView = findViewById(R.id.passwords)
        passwordsView?.layoutManager = LinearLayoutManager(this)
        updatePasswords(data)
    }

    private fun addPassword(passwordEntry: PasswordEntry) {
        Toast.makeText(this, "Adding $passwordEntry", Toast.LENGTH_LONG).show()
    }

    private fun filterPasswords(filter: String, data: List<PasswordEntry>) {
        val filteredData = data.filter { entry ->
            entry.cite.contains(filter) || entry.description?.contains(filter) ?: false }
        updatePasswords(filteredData)
    }

    private fun updatePasswords(data: List<PasswordEntry>) {
        passwordsView?.adapter = PasswordsAdapter(data) { password ->
            onPasswordClick?.invoke(password)
        }
    }

    private var masterHash: String? = null
    private var passwordsView: RecyclerView? = null
    private var onPasswordClick : ((PasswordEntry) -> Unit)? = null
}
