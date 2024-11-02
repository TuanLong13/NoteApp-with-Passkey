package com.google.credentialmanager.sample.noteapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.credentialmanager.sample.R

class NoteAppActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var noteAdapter: NoteAdapter? = null
    private var searchView: SearchView? = null
    private var notes = ArrayList<Note>()
    private var addNoteBtn: Button? = null
    private var cb_sx: CheckBox? = null
    private var logout: Button? = null
    private var emptyNoteListTxt: TextView? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noteapp)
        if (checkPermission()) {
            cb_sx = findViewById(R.id.cb_sx)
            logout = findViewById(R.id.logout)
            initView()
            cb_sx?.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    SortNoteByName()
                } else {
                    // Nếu checkbox không được chọn, trở về ban đầu
                    notes.clear()
                    notes.addAll(noteFiles)
                    noteAdapter!!.notifyDataSetChanged()
                }
            }
            logout?.setOnClickListener(View.OnClickListener {
                finish()
            })
        } else {
            Toast.makeText(
                this,
                "Cần được cấp quyền để sử dụng tính năng thêm hình ảnh",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Gọi onResume() để cập nhật lại dữ liệu hiển thị sau khi thêm ghi chú
    override fun onResume() {
        super.onResume()
        initView()
    }

    private fun initView() {
        addNoteBtn = findViewById(R.id.addNoteBtn)
        emptyNoteListTxt = findViewById(R.id.emptyNoteListTxt)
        addNoteBtn?.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@NoteAppActivity,
                    AddNoteActivity::class.java
                )
            )
        })
        recyclerView = findViewById(R.id.notesRecyclerView)

        //Lấy danh sách dữ liệu các ghi chú
        notes = noteFiles

        //Thông báo danh sách ghi chú rỗng
        if (notes.size == 0) {
            emptyNoteListTxt?.setVisibility(View.VISIBLE)
        } else {
            emptyNoteListTxt?.setVisibility(View.GONE)
        }

        //Khởi tạo adapter để hiển thị dữ liệu ở RecyclerView
        noteAdapter = NoteAdapter(applicationContext, notes)
        recyclerView?.adapter = noteAdapter
        recyclerView?.layoutManager = LinearLayoutManager(this)
    }

    val noteFiles: ArrayList<Note>
        @SuppressLint("Range")
        get() {
            val noteFiles = ArrayList<Note>()
            try {
                //Dùng ContentResolver để thao tác với dữ liệu
                val contentResolver = contentResolver
                val uri: Uri = NoteProvider.Companion.CONTENT_URI
                val projection = arrayOf(
                    DBHelper.COLUMN_TITLE,
                    DBHelper.COLUMN_CONTENT,
                    DBHelper.COLUMN_TIME_CREATED
                ) //Các dữ liệu cột cần lấy
                val selection: String? = null
                val selectionArgs: Array<String>? = null
                val sortOrder = DBHelper.COLUMN_TIME_CREATED + " DESC"
                //Kiểu sắp xếp (nên để theo thời gian giảm dần)

                //Cho cursor chạy để tìm hàng dữ liệu thỏa với điều kiện trong database
                val cursor =
                    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val title = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE))
                        val content =
                            cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONTENT))
                        val timeCreated =
                            cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_TIME_CREATED))
                        noteFiles.add(Note(title, content, timeCreated, null))
                    } while (cursor.moveToNext())
                    cursor.close()
                }
            } catch (e: Exception) {
                Log.d("MyTag", "Lỗi khi lấy danh sách ghi chú: $e")
            }
            return noteFiles
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search).actionView as SearchView?
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView!!.maxWidth = Int.MAX_VALUE
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                noteAdapter!!.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                noteAdapter!!.filter.filter(newText)
                return false
            }
        })
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkPermission(): Boolean {
        val result = checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                0
            )
        }
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun SortNoteByName() {
        notes.sortWith { note1, note2 ->
            note1.title!!.compareTo(note2.title!!, true)
        }
        noteAdapter!!.notifyDataSetChanged()
    }
}