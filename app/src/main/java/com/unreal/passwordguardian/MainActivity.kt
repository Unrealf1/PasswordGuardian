package com.unreal.passwordguardian

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unreal.passwordguardian.CommonConstants.KEY_PASSWORD
import com.unreal.passwordguardian.CommonConstants.KEY_REGISTER_INTENTION
import com.unreal.passwordguardian.PasswordEntry.*
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareMembers(intent.extras, savedInstanceState)

        enableVisualLoading()

        passwordLoadTask = PasswordLoadTask(WeakReference(onPasswordsLoaded))
        passwordLoadTask?.execute(BackgroundLoadArguments(this, masterPassword))

        val filterTextEdit = findViewById<EditText>(R.id.filterText)
        val createNewButton = findViewById<Button>(R.id.buttonCreateNew)

        filterTextEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val searchInPasswords = preferences.getBoolean(getString(
                    R.string.if_search_in_passwords_preference_key), false)
                val newData = filterPasswords(s.toString(), passwords, searchInPasswords)
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
                    addPassword(
                        PasswordEntry(
                            newPassword.text.toString(),
                            newName.text.toString()
                        ))
                }
                .setNegativeButton(R.string.button_cancel) { _, _ ->

                }

            val dialog = builder.create()

            dialog.show()
        }

        supportActionBar?.setDisplayShowHomeEnabled(true)
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
            R.id.actionChangePassword -> {
                enableVisualLoading()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra(KEY_REGISTER_INTENTION, true)
                this.startActivityForResult(intent, changePasswordRequest)
            }
            else -> {

            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == changePasswordRequest) {
            if (resultCode == Activity.RESULT_OK) {
                val returnedResult = data?.data.toString()
                if (returnedResult != null) {
                    masterPassword = returnedResult
                    savePasswords(passwords)
                    return
                }
            }
        }

        showError(R.string.error_could_not_change_password)
        disableVisualLoading()
    }

    override fun onDestroy() {
        passwordLoadTask?.cancel(false)
        passwordSaveTask?.cancel(false)
        super.onDestroy()
    }

    fun removePassword(passwordEntry: PasswordEntry) {
        passwords.remove(passwordEntry)
        savePasswords(passwords)
    }

    private fun addPassword(passwordEntry: PasswordEntry) {
        if (!checkLoaded()) {
            return
        }
        passwords.add(passwordEntry)
        savePasswords(passwords)
    }

    private fun checkLoaded(): Boolean {
        val value = loaded.get()
        if (!value) {
            showError(R.string.error_still_loading)
        }
        return value
    }

    private fun savePasswords(data: MutableList<PasswordEntry>) {
        enableVisualLoading()
        passwordSaveTask = PasswordSaveTask(WeakReference(onPasswordsSaved))
        passwordSaveTask?.execute(BackgroundSaveArguments(this, data, masterPassword))
    }

    private fun filterPasswords(filter: String,
                                data: List<PasswordEntry>,
                                searchInPasswords: Boolean): List<PasswordEntry> {
        return data.filter { entry ->
            entry.cite.contains(filter) ||
            (if (searchInPasswords) entry.password.contains(filter) else false) ||
            entry.description?.contains(filter) ?: false}
    }

    fun updateVisiblePasswords(data: List<PasswordEntry>) {
        passwordsView?.adapter =
            PasswordsAdapter(
                data,
                this
            )

    }

    fun updateVisiblePasswords() {
        updateVisiblePasswords(passwords)
    }

    fun getData() : MutableList<PasswordEntry> {
        return passwords
    }

    private fun enableVisualLoading() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        //loadingView?.visibility = View.VISIBLE
        //darkeningView?.visibility = View.VISIBLE
    }

    private fun disableVisualLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        //loadingView?.visibility = View.GONE
        //darkeningView?.visibility = View.GONE
    }

    private val onPasswordsLoaded = object : PasswordLoadTask.UIListener {
        override fun onPasswordsLoaded(passwords: MutableList<PasswordEntry>) {
            this@MainActivity.passwords = passwords
            updateVisiblePasswords()
            loaded.set(true)
            disableVisualLoading()
        }
    }

    private val onPasswordsSaved = object : PasswordSaveTask.UIListener {
        override fun onPasswordsSaved() {
            updateVisiblePasswords()
            disableVisualLoading()
        }
    }

    private fun prepareMembers(extras: Bundle?, savedInstanceState: Bundle?) {
        masterPassword = if (savedInstanceState == null) {
            extras?.getString(KEY_PASSWORD)
        } else {
            savedInstanceState.getSerializable(KEY_PASSWORD) as String?
        } ?: ""

        passwordsView = findViewById(R.id.passwords)
        passwordsView?.layoutManager = LinearLayoutManager(this)
        loadingView = findViewById(R.id.loadingView)
        darkeningView = findViewById(R.id.darkeningView)
    }

    private fun showError(errorText: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            .setMessage(errorText)
            .setTitle(R.string.error_dialog_title)
            .setPositiveButton(R.string.button_accept, null)
        builder.create().show()
    }

    private var masterPassword = ""
    private var loadingView: View? = null
    private var darkeningView: View? = null
    private var passwordsView: RecyclerView? = null
    private var passwords = mutableListOf<PasswordEntry>()
    private var passwordLoadTask: PasswordLoadTask? = null
    private var passwordSaveTask: PasswordSaveTask? = null
    private var loaded = AtomicBoolean(false)
    private val thisActivity = this

    companion object {
        private const val bigLoadingThreshold = 50
        private const val changePasswordRequest = 1
    }
}
