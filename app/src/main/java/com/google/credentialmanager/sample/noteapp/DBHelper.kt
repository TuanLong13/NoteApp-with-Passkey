package com.google.credentialmanager.sample.noteapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper



class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
        db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN userID;")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "note.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "notes"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_TIME_CREATED = "timeCreated"
        const val COLUMN_IMAGE_URI = "imageUri"
        const val COLUMN_USER_ID = "userID"

        //Câu SQL để tạo bảng
        private const val TABLE_CREATE = "create table " + TABLE_NAME + " (" +
                COLUMN_TITLE + " text not null, " +
                COLUMN_CONTENT + " text not null, " +
                COLUMN_TIME_CREATED + " interger primary key not null, " +  //Dùng timeCreated để làm khóa chính
                COLUMN_IMAGE_URI + " text" +
                COLUMN_USER_ID + " text);"
    }
}
