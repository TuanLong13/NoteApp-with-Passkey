package com.google.credentialmanager.sample.noteapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.credentialmanager.sample.EncryptionHelper
import com.google.credentialmanager.sample.R


class NoteAppActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var noteAdapter: NoteAdapter? = null
    private var searchView: SearchView? = null
    private var notes = ArrayList<Note>()
    private var addNoteBtn: FloatingActionButton? = null
    private var cb_sx: CheckBox? = null
    private var emptyNoteListTxt: TextView? = null

    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noteapp)
        setSupportActionBar((findViewById(R.id.toolbar)));

        val extra = intent.extras
        if (extra != null) {
            id = extra.getString("id")!!
        }
        if (Build.VERSION.SDK_INT > 32) {
            if (checkPermission()) {
                cb_sx = findViewById(R.id.cb_sx)

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
            } else {
                Toast.makeText(
                    this,
                    "Cần được cấp quyền để sử dụng tính năng thêm hình ảnh",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else
        {
            Toast.makeText(
                this,
                "Ứng dụng này yêu cầu Android 33 trở lên để sử dụng",
                Toast.LENGTH_LONG
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
                ).putExtra("id", id)
            )
        })
        recyclerView = findViewById(R.id.notesRecyclerView)

        //Lấy danh sách dữ liệu các ghi chú
        notes = noteFiles

        //Thông báo danh sách ghi chú rỗng
        if (notes.size == 0) {
            emptyNoteListTxt?.visibility = View.VISIBLE
        } else {
            emptyNoteListTxt?.visibility = View.GONE
        }

        //Khởi tạo adapter để hiển thị dữ liệu ở RecyclerView
        noteAdapter = NoteAdapter(applicationContext, notes)
        recyclerView?.adapter = noteAdapter
        recyclerView?.layoutManager = LinearLayoutManager(this)
    }

    val noteFiles: ArrayList<Note>
        @SuppressLint("Range")
        get() {
            val encryptionHelper = EncryptionHelper()
            val noteFiles = ArrayList<Note>()
            try {
                //Dùng ContentResolver để thao tác với dữ liệu
                val contentResolver = contentResolver
                val uri: Uri = NoteProvider.CONTENT_URI
                val projection = arrayOf(
                    DBHelper.COLUMN_TITLE,
                    DBHelper.COLUMN_CONTENT,
                    DBHelper.COLUMN_TIME_CREATED,
                    DBHelper.COLUMN_USER_ID
                ) //Các dữ liệu cột cần lấy
                val selection: String = DBHelper.COLUMN_USER_ID + " = '" + id + "'"
                val selectionArgs: Array<String>? = null
                val sortOrder = DBHelper.COLUMN_TIME_CREATED + " DESC"
                //Kiểu sắp xếp (nên để theo thời gian giảm dần)

                //Cho cursor chạy để tìm hàng dữ liệu thỏa với điều kiện trong database
                val cursor =
                    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        val title =
                            encryptionHelper.decrypt(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TITLE)))
                        val content =
                            encryptionHelper.decrypt(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_CONTENT)))
                        val timeCreated =
                            cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_TIME_CREATED))
                        val userID =
                            cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_USER_ID))

                        noteFiles.add(Note(title, content, timeCreated, null, userID))
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

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT > 32) {
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
        else
        {
            Toast.makeText(
                this,
                "Cần được cấp quyền để sử dụng tính năng thêm hình ảnh",
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
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

    override fun onBackPressed() {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                // Perform logout action
                showLogoutConfirmationDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        finish()
    }


}