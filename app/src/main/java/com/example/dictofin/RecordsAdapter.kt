package com.example.dictofin

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class RecordsAdapter(private val records: ArrayList<Record>, private val context: Context, private val dbHelper: DatabaseHelper):
    RecyclerView.Adapter<RecordsAdapter.ViewHolder>(), IFilter {
    private val filteredList = ArrayList<Record>(records)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.record, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        holder.name?.text = item.name
        holder.date?.text = item.date
        var mediaPlayer: MediaPlayer? = null
        holder.toggleButton?.setOnClickListener{
            if (item.playing){
                item.playing = false
                holder.toggleButton?.setImageResource(R.drawable.icons8__64)
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    mediaPlayer?.release()
                }
            } else {
                item.playing = true
                holder.toggleButton?.setImageResource(R.drawable.icons8____64)
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(File(context.filesDir, item.fileName).path)
                mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    mediaPlayer?.start()
                }
                
            }
        }
        holder.deleteButton?.setOnClickListener {
            File(context.filesDir, item.fileName).delete()

            val db = dbHelper.writableDatabase
            val selection = "${DatabaseHelper.COLUMN_ID} = ?"
            val selectionArgs = arrayOf(item.id.toString())
            db.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs)

            records.removeAt(position)
            filteredList.removeAt(position)
            nofilyItemRemoved(position)
            notifyItemRangeChanged(position, records.size)
        }
    }

    override fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()){
            filteredList.addAll(records)
        } else{
           for (item in records){
               if (item.name.contains(query, ignoreCase = true)) {
                   filteredList.add(item)
               }
           }
        }
        notifyDataSetChanged()
    }

    override fun nofilyItemRemoved(position: Int) {
        TODO("Not yet implemented")
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var toggleButton: ImageView? = null
        var deleteButton: ImageView? = null
        var name: TextView? = null
        var date: TextView? = null

        init{
            toggleButton = itemView.findViewById(R.id.toggle)
            deleteButton = itemView.findViewById(R.id.delete)
            name = itemView.findViewById(R.id.name)
            date = itemView.findViewById(R.id.date)
        }
    }
}