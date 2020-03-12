package com.unreal.passwordguardian

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class PasswordViewHolder(
    itemView: ConstraintLayout,
    val onClick : (PasswordEntry) -> Unit)
    : RecyclerView.ViewHolder(itemView) {

    private var citeTextView : TextView? = itemView.findViewById(R.id.password_entry_cite)

    fun bind(entry: PasswordEntry) {
        citeTextView?.text = "${entry.cite}\n${entry.password}"
        citeTextView?.setOnClickListener {
            onClick(entry)
        }
    }
}

class PasswordsAdapter (
    private val passwords: List<PasswordEntry>,
    private val onClick : (PasswordEntry) -> Unit
) : RecyclerView.Adapter<PasswordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.password_entry, parent, false) as ConstraintLayout
        return PasswordViewHolder(itemView, onClick)
    }

    override fun getItemCount(): Int {
        return passwords.size
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(passwords[position])
    }

}