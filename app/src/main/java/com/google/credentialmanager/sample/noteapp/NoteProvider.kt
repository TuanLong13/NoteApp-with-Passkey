package com.google.credentialmanager.sample.noteapp

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class NoteProvider : ContentProvider() {
    private lateinit var database: SQLiteDatabase
    override fun onCreate(): Boolean {
        val dbHelper = DBHelper(context)
        database = dbHelper.writableDatabase
        return database != null
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = DBHelper.TABLE_NAME
        val uriType = sURIMatcher.match(uri)
        when (uriType) {
            NOTES -> {}
            NOTE -> queryBuilder.appendWhere(DBHelper.COLUMN_TIME_CREATED + "=" + uri.lastPathSegment)
            else -> throw IllegalArgumentException("Unknown URI: $uri")

        }
        val cursor = queryBuilder.query(
            database,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {

        val id = database!!.insert(DBHelper.TABLE_NAME, null, values)
        return if (id > 0) {
            val noteUri = ContentUris.withAppendedId(CONTENT_URI, id)
            context!!.contentResolver.notifyChange(noteUri, null)
            noteUri
        } else {
            throw SQLException("Failed to insert row into $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var rowsDeleted = 0
        val uriType = sURIMatcher.match(uri)
        when (uriType) {
            NOTES -> rowsDeleted = database!!.delete(DBHelper.TABLE_NAME, selection, selectionArgs)
            NOTE -> {
                val id = uri.lastPathSegment
                if (id != null) {
                    rowsDeleted = database!!.delete(
                        DBHelper.TABLE_NAME,
                        DBHelper.COLUMN_TIME_CREATED + " = ?",
                        arrayOf(id)
                    )
                }
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var rowsUpdated = 0
        val uriType = sURIMatcher.match(uri)
        when (uriType) {
            NOTES -> rowsUpdated =
                database!!.update(DBHelper.TABLE_NAME, values, selection, selectionArgs)

            NOTE -> {
                val id = uri.lastPathSegment
                if (id != null) {
                    rowsUpdated = database!!.update(
                        DBHelper.TABLE_NAME,
                        values,
                        DBHelper.COLUMN_TIME_CREATED + " = ?",
                        arrayOf(id)
                    )
                }
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    companion object {
        private const val AUTHORITY = "com.example.notes.provider"
        private const val PATH_NOTE_LIST = "notes"
        private const val PATH_NOTE_BY_TIME_CREATED = "note"
        val CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_NOTE_LIST)
        val CONTENT_URI_BY_TIME_CREATED =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_NOTE_BY_TIME_CREATED)
        private const val NOTES = 1
        private const val NOTE = 2
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sURIMatcher.addURI("com.example.notes.provider", "notes", NOTES)
            sURIMatcher.addURI("com.example.notes.provider", "note", NOTE)
        }
    }
}
