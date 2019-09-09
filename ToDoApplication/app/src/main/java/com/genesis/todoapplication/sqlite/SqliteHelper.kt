package com.genesis.todoapplication.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.genesis.todoapplication.model.ToDoData


class SqliteHelper (context: Context) : SQLiteOpenHelper(context, DatabaseName, null, 1) {

    // Create Table
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "create table if not exists $TableName (id TEXT,name TEXT, email TEXT)"
        db.execSQL(createTableQuery)
    }

    // Drop Table for new table creation
    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TableName")
        onCreate(db)
    }

    // Insert into Table
    fun insertInto(id: String?,name: String?,email :String?): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID,id)
        contentValues.put(NAME, name)
        contentValues.put(EMAIL, email)
        return db.insert(TableName, null, contentValues).toInt()
    }

    // Select * from Table i.e get all data
    fun selectAllData(): Cursor {
        val db = this.writableDatabase
        val query = "select * from $TableName"
        return db.rawQuery(query, null)
    }

    // Update specific Task
    fun updateTask(id: String?,name: String?,email :String?): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(NAME, name)
        contentValues.put(EMAIL, email)
       return db.update(TableName, contentValues, "$ID= ?", arrayOf((id)))
    }

    // Delete specific Task
    fun deleteTask(id: String): Cursor {
        val db = this.writableDatabase
        val query = ("DELETE FROM $TableName WHERE $ID = '$id'")
        return db.rawQuery(query, null)
    }


    companion object {
        // Constants fields
        const val DatabaseName = "ToDo.db"
        const val TableName = "ToDoTask"
        const val NAME = "name"
        const val EMAIL = "email"
        const val ID = "id"
    }
}
