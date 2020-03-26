package com.unreal.passwordguardian

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unreal.passwordguardian.CommonConstants.KEY_PASSWORD
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val extras = intent.extras

        masterHash = if (savedInstanceState == null) {
            extras?.getString(KEY_PASSWORD)
        } else {
            savedInstanceState.getSerializable(KEY_PASSWORD) as String?
        } ?: ""
        data = PasswordEntryLoader.load(this, masterHash) ?: exitProcess(1)

        val filterTextEdit = findViewById<EditText>(R.id.filterText)
        val createNewButton = findViewById<Button>(R.id.buttonCreateNew)


        filterTextEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val newData = filterPasswords(s.toString(), data)
                updateVisiblePasswords(newData)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        createNewButton.setOnClickListener {

            val inflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.new_entry_popup, null)

            val generatePasswordButton = view.findViewById<Button>(R.id.generatePasswordButton)
            val newPassword = view.findViewById<EditText>(R.id.newEntryPasswordField)
            val newName = view.findViewById<EditText>(R.id.newEntryNameField)

            generatePasswordButton.setOnClickListener {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)

                val length = preferences.getInt(getString(
                    R.string.random_password_length_preference_key), 12)

                newPassword.setText(PasswordEntryLoader.generatePassword(length))
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                .setTitle(R.string.new_entry_popup_title)
                .setView(view)
                .setPositiveButton(R.string.button_accept) { _, _ ->
                    Toast.makeText(this, "Password added", Toast.LENGTH_SHORT).show()
                    addPassword(PasswordEntry(newPassword.text.toString(), newName.text.toString()),
                                data)
                    updateVisiblePasswords(data)
                }
                .setNegativeButton(R.string.button_cancel) { _, _ ->

                }

            val dialog = builder.create()

            dialog.show()
        }

        supportActionBar?.setDisplayShowHomeEnabled(true)

        passwordsView = findViewById(R.id.passwords)
        passwordsView?.layoutManager = LinearLayoutManager(this)
        updateVisiblePasswords(data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSettings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.actionAbout -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    .setPositiveButton(R.string.button_accept, null)
                    .setTitle(getString(R.string.about_title))
                    .setMessage(getString(R.string.about_message))
                builder.create().show()
            }
            else -> {

            }
        }
        return true
    }

    fun removePassword(passwordEntry: PasswordEntry,
                               data: MutableList<PasswordEntry>) {
        data.remove(passwordEntry)
        PasswordEntryLoader.save(this, data, masterHash)
    }

    private fun addPassword(passwordEntry: PasswordEntry,
                            data: MutableList<PasswordEntry>) {
        data.add(passwordEntry)
        PasswordEntryLoader.save(this, data, masterHash)
    }

    private fun filterPasswords(filter: String, data: List<PasswordEntry>): List<PasswordEntry> {
        return data.filter { entry ->
            entry.cite.contains(filter) || entry.description?.contains(filter) ?: false }
    }

    fun updateVisiblePasswords(data: List<PasswordEntry>) {
        passwordsView?.adapter = PasswordsAdapter(data, this)
    }

    fun updateVisiblePasswords() {
        updateVisiblePasswords(data)
    }

    fun getData() : MutableList<PasswordEntry> {
        return data
    }

    private var masterHash = ""
    private var passwordsView: RecyclerView? = null
    private var data = mutableListOf<PasswordEntry>()
}
