package com.unreal.passwordguardian

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class PasswordViewHolder(
    itemView: ConstraintLayout,
    private val mainActivity: MainActivity,
    val onClick : (PasswordEntry) -> Unit)
                : RecyclerView.ViewHolder(itemView) {

    private var citeTextView : TextView? = itemView.findViewById(R.id.password_entry_cite)
    private var removeButton = itemView.findViewById<Button>(R.id.remove_entry_button)


    fun bind(entry: PasswordEntry) {
        citeTextView?.text = "${entry.cite}\n${entry.password}"
        citeTextView?.setOnClickListener {
            onClick(entry)
        }

        citeTextView?.setOnLongClickListener {
            val myClipboard = mainActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("password", entry.password)
            myClipboard.setPrimaryClip(clip)

            Toast.makeText(mainActivity, R.string.copy_to_clipboard_text, Toast.LENGTH_SHORT).show()

            true
        }

        removeButton.setOnClickListener {
            mainActivity.removePassword(entry, mainActivity.getData())
            mainActivity.updatePasswords()
        }
    }
}

class PasswordsAdapter (
    private val passwords: List<PasswordEntry>,
    private val mainActivity: MainActivity,
    private val onClick : (PasswordEntry) -> Unit
) : RecyclerView.Adapter<PasswordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.password_entry, parent, false) as ConstraintLayout

        return PasswordViewHolder(itemView, mainActivity, onClick)
    }

    override fun getItemCount(): Int {
        return passwords.size
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(passwords[position])
    }

}