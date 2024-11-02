package com.google.credentialmanager.sample.noteapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.credentialmanager.sample.R
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(private val context: Context, private var notes: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>(), Filterable {
    private val notes1: List<Note> = notes

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var content: TextView
        var timeCreated: TextView

        init {
            title = itemView.findViewById(R.id.title)
            content = itemView.findViewById(R.id.content)
            timeCreated = itemView.findViewById(R.id.timeCreated)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val strSearch = charSequence.toString()
                notes = if (strSearch.isEmpty()) {
                    notes1
                } else {
                    val list: MutableList<Note> = ArrayList()
                    for (note in notes1) {
                        if (note.title!!.lowercase()
                                .contains(strSearch.lowercase(Locale.getDefault()))
                        ) {
                            list.add(note)
                        }
                    }
                    list
                }
                val filterResults = FilterResults()
                filterResults.values = notes
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                notes = results.values as List<Note>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.content.text = note.content
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault() )
        val formattedTime = sdf.format(note.timeCreated)
        holder.timeCreated.text = formattedTime
        holder.itemView.setOnClickListener {
            try {
                val intent = Intent(context, EditNoteActivity::class.java)

                //Vì khai báo intent không trong activity nên phải thêm hàng này
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val timeCreated = note.timeCreated

                //Lưu dữ liệu vào bundle
                val myBundle = Bundle()
                myBundle.putLong("timeCreated", timeCreated)
                intent.putExtra("myBundle", myBundle)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.d("MyTag", "Lỗi khi nhấn nút ADD NEW NOTE: $e")
            }
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
