package com.genesis.todoapplication.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.genesis.todoapplication.sqlite.SqliteHelper


/**
 * Created by deepmetha on 8/28/16.
 */
class SplashActivity : AppCompatActivity() {
    internal lateinit var mySqliteHelper: SqliteHelper

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create Sqlite DB
        mySqliteHelper = SqliteHelper(this)
        // Go to Main Activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
