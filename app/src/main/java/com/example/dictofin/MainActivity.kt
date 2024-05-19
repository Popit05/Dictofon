package com.example.dictofin

import android.Manifest.permission.RECORD_AUDIO
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import java.io.File


class MainActivity : AppCompatActivity() {
    var statusValue: TextView? = null
    var mainButton: ImageView? = null
    var recordingAnimation: LottieAnimationView? = null
    var nameArea: View? = null
    var nameField: EditText? = null
    var confirmNameButton: ImageView? = null

    var recordsList: RecyclerView? = null
    var recordsArray: ArrayList<Record> = ArrayList()
    var adapter: RecordsAdapter? = null

    var mediaRecorder: MediaRecorder? = null
    var recordingEnabled: Boolean = false
    var imm: InputMethodManager? = null
    var currentFileName = ""
    private lateinit var dbHelper: DatabaseHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        dbHelper = DatabaseHelper(this)

        val searchField = findViewById<SearchView>(R.id.search_field)
        val initWidth = searchField.width

        searchField?.setOnSearchClickListener {
            val layoutParams = searchField.layoutParams
            layoutParams?.width = initWidth + 500
            searchField.layoutParams = layoutParams
        }
        searchField?.setOnCloseListener {
            val layoutParams = searchField.layoutParams
            layoutParams?.width = 140
            searchField.layoutParams = layoutParams
            return@setOnCloseListener false
        }
        searchField?.setOnQueryTextListener(object: SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    adapter?.filter(it)
                }

                return true
            }

            override fun onSuggestionSelect(position:  Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position:  Int): Boolean {
                return true
            }

        })
        statusValue = findViewById(R.id.status_value)
        mainButton = findViewById(R.id.main_button)
        recordingAnimation = findViewById(R.id.recording)
        nameArea = findViewById(R.id.name_area)
        nameField = findViewById(R.id.name_field)
        confirmNameButton = findViewById(R.id.ok)



        recordsList = findViewById(R.id.records_list)
        adapter = RecordsAdapter(recordsArray, this, dbHelper)
        recordsList?.adapter = adapter
        recordsList?.layoutManager = LinearLayoutManager(this)

        mainButton?.setOnClickListener {
            record()
        }
        confirmNameButton?.setOnClickListener {
            save()
        }
        getRecords()

    }
    private fun getRecords() {
        val cursor = dbHelper.readableDatabase.query(DatabaseHelper.TABLE_NAME, null, null, null, null,null, null )
        if(cursor.moveToFirst()){
            do{
                val id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME))
                val date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE))
                val fileName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FILE_NAME))

                val record = Record(id, name, fileName, date, false)
                recordsArray.add(record)
                adapter?.notifyDataSetChanged()
                adapter?.filter("")
            } while (cursor.moveToNext())
        }

        cursor.close()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun record() {
        if (recordingEnabled){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            mainButton?.setImageResource(R.drawable.free_icon_voice_recorder_7793413)
            recordingAnimation?.visibility = View.GONE
            statusValue?.setText(R.string.disabled)
            statusValue?.setTextColor(ContextCompat.getColor(this, R.color.light))
            mainButton?.visibility = View.GONE
            nameArea?.visibility = View.VISIBLE
        } else {
            currentFileName = Utils.getID().toString() + ".mp3"
            if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), 101)
            } else {
                startRecording()
            }
            mainButton?.setImageResource(R.drawable.icons8____64)
            recordingAnimation?.visibility = View.VISIBLE
            statusValue?.setText(R.string.enabled)
            statusValue?.setTextColor(ContextCompat.getColor(this, R.color.blue))
        }
        recordingEnabled = !recordingEnabled
    }

    private fun save(){
        var id = Utils.getID()
        imm?.hideSoftInputFromWindow( nameField?.windowToken, 0)

        val newRecord = Record(id, nameField?.text?.toString()!!, currentFileName, Utils.getCurrentDate())
        dbHelper.writableDatabase.insert(DatabaseHelper.TABLE_NAME, null, ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, newRecord.id)
            put(DatabaseHelper.COLUMN_NAME, newRecord.name)
            put(DatabaseHelper.COLUMN_DATE, newRecord.date)
            put(DatabaseHelper.COLUMN_FILE_NAME, newRecord.fileName)
        })

        nameArea?.visibility = View.GONE
        mainButton?.visibility = View.VISIBLE
        recordsArray.add(newRecord)
        adapter?.notifyDataSetChanged()
        adapter?.filter("")
        nameField?.setText("")
        currentFileName = ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startRecording(){
        val file = File(filesDir, currentFileName)
        if(file.exists()) {
            file.delete()
        }
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(file)
        mediaRecorder?.prepare()
        mediaRecorder?.start()

        Toast.makeText(this, "Recording Started!", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecording()
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}







