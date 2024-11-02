package com.google.credentialmanager.sample.noteapp

import android.content.ContentValues
import android.content.Intent
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

class AddNoteActivity : AppCompatActivity() {
    private var titleInput: EditText? = null
    private var contentInput: EditText? = null
    private var saveNoteBtn: Button? = null
    private var addImageBtn: Button? = null
    private var setAlarm: Button? = null
    private var iv: ImageView? = null
    private var noteImage: String? = null
    private var title: String? = null
    private var content: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        initView()
    }

    private fun initView() {
        titleInput = findViewById(R.id.titleInput)
        contentInput = findViewById(R.id.contentInput)
        iv = findViewById(R.id.imageView)
        saveNoteBtn = findViewById(R.id.saveNoteBtn)
        saveNoteBtn?.setOnClickListener(View.OnClickListener {
            val noteTitle = titleInput?.text.toString()
            val noteContent = contentInput?.text.toString()

            //Kiểm tra thông tin nhập bị thiếu
            if (noteTitle == "") Toast.makeText(
                applicationContext,
                "Please enter your title!",
                Toast.LENGTH_SHORT
            ).show() else if (noteContent == "") Toast.makeText(
                applicationContext, "Please enter your content!", Toast.LENGTH_SHORT
            ).show() else {
                val timeCreated = System.currentTimeMillis()
                try {
                    val uri = NoteProvider.CONTENT_URI

                    //Thêm các giá trị cần lưu vào ContentValues
                    val values = ContentValues()
                    values.put("title", noteTitle)
                    values.put("content", noteContent)
                    values.put("timeCreated", timeCreated)
                    values.put("imageUri", noteImage)
                    //Dùng ContentResolver gọi hàm insert để thêm
                    contentResolver.insert(uri, values)
                } catch (e: Exception) {
                    Log.d("MyTag", "Lỗi khi thêm ghi chú: $e")
                }
                Toast.makeText(applicationContext, "Note saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
        iv = findViewById(R.id.imageView)
        addImageBtn = findViewById(R.id.addImage)
        addImageBtn?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                imagePicker.launch(intent)
            }

            private val imagePicker = registerForActivityResult<Intent, ActivityResult>(
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
}