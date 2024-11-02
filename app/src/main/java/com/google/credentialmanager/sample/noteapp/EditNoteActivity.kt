package com.google.credentialmanager.sample.noteapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.credentialmanager.sample.R
import java.io.IOException

class EditNoteActivity : AppCompatActivity() {
    var alertDialog: AlertDialog? = null
    private var timeCreated: Long = 0
    private var titleInput: EditText? = null
    private var contentInput: EditText? = null
    private var saveNoteBtn: Button? = null
    private var deleteNoteBtn: Button? = null
    private var addImage: Button? = null
    private var setAlarm: Button? = null
    private var iv: ImageView? = null
    private var noteImage: String? = null
    private var title: String? = null
    private var content: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)
        initView()
    }

    private fun initView() {
        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        iv = findViewById(R.id.imageView)
        addImage = findViewById(R.id.addImage)
        saveNoteBtn = findViewById(R.id.saveNoteBtn)
        deleteNoteBtn = findViewById(R.id.deleteNoteBtn)
        selectedNote
        addImage?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                imagePicker.launch(intent)
            }

            private val imagePicker = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result?.resultCode == RESULT_OK) {
                    val i = result.data
                    if (i != null) {
                        val imgUri = i.data
                        noteImage = try {
                            val imgBitmap =
                                MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
                            iv?.setImageBitmap(imgBitmap)
                            imgUri.toString()
                        } catch (e: IOException) {
                            throw RuntimeException(e)
                        }
                    }
                }
            }
        })
        saveNoteBtn?.setOnClickListener(View.OnClickListener {
            val title = titleInput?.text.toString()
            val content = contentInput?.text.toString()
            try {
                val contentResolver = contentResolver
                val uri = NoteProvider.CONTENT_URI
                val selection = "timeCreated = ?"
                val selectionArgs = arrayOf(timeCreated.toString() + "")
                val values = ContentValues()
                values.put(DBHelper.COLUMN_TITLE, title)
                values.put(DBHelper.COLUMN_CONTENT, content)
                values.put(DBHelper.COLUMN_TIME_CREATED, System.currentTimeMillis())
                values.put(DBHelper.COLUMN_IMAGE_URI, noteImage)
                val rowsUpdated = contentResolver.update(uri, values, selection, selectionArgs)
            } catch (e: Exception) {
                Log.d("MyTag", e.toString())
            }
            Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
            finish()
            Log.d("MyTag", "Dong act tu nut SAVE")
        })
        deleteNoteBtn?.setOnClickListener(View.OnClickListener {
            try {
                val builder = AlertDialog.Builder(this@EditNoteActivity)
                builder.setMessage("Do you sure want to delete?")
                builder.setPositiveButton("Yes") { dialog, which -> // Xử lý khi người dùng chọn "Yes"
                    try {
                        val uri = NoteProvider.CONTENT_URI
                        val selection = "timeCreated = ?"
                        val selectionArgs = arrayOf(timeCreated.toString() + "")
                        val contentResolver = contentResolver
                        val rowsDeleted = contentResolver.delete(uri, selection, selectionArgs)
                    } catch (e: Exception) {
                        Log.d("MyTag", "Lỗi khi xóa: $e")
                    }
                    Toast.makeText(applicationContext, "Note deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }

                // Thêm nút No
                builder.setNegativeButton("No") { dialog, which -> }
                val dialog = builder.create()
                dialog.show()
            } catch (e: Exception) {
                Log.d("MyTag", "Lỗi khi nhấn nút DELETE: $e")
            }
        })
        setAlarm = findViewById(R.id.btAlarm)
        setAlarm?.setOnClickListener(View.OnClickListener {
            val i = Intent(applicationContext, AlarmActivity::class.java)
            title = titleInput?.text.toString()
            content = contentInput?.text.toString()
            i.putExtra("title", title)
            i.putExtra("content", content)
            startActivity(i)
        })
    }

    @get:SuppressLint("Range")
    private val selectedNote: Unit
        private get() {
            try {
                val intent = intent
                val myBundle = intent.getBundleExtra("myBundle")
                timeCreated = myBundle!!.getLong("timeCreated")
                val contentResolver = contentResolver
                val uri = NoteProvider.CONTENT_URI
                val selection = "timeCreated = ?"
                val selectionArgs = arrayOf(timeCreated.toString() + "")
                val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)
                var title: String? = ""
                var content: String? = ""
                var imgUri: String? = ""
                if (cursor != null && cursor.moveToFirst()) {
                    title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE))
                    content = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONTENT))
                    imgUri = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE_URI))
                } else Log.d("MyTag", "Cursor rỗng khi lấy ghi chú được chọn")
                cursor?.close()
                titleInput!!.setText(title)
                contentInput!!.setText(content)
                try {
                    val imgBitmap =
                        MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imgUri))
                    iv!!.setImageBitmap(imgBitmap)
                    noteImage = imgUri
                } catch (e: Exception) {
                    Log.i("Lỗi", e.toString())
                }
            } catch (e: Exception) {
                Log.d("MyTag", "Lỗi khi lấy ghi chú được chọn: $e")
            }
        }
}