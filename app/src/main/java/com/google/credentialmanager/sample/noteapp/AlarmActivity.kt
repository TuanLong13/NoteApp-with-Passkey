package com.google.credentialmanager.sample.noteapp

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.credentialmanager.sample.R
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {
    var dp: DatePicker? = null
    var tp: TimePicker? = null
    var bt: Button? = null
    var title: String? = null
    var content: String? = null
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        init_views()
        val extra = intent.extras!!
        title = extra.getString("title")
        content = extra.getString("content")
        if (!checkPermission()) {
            Toast.makeText(
                this,
                "Cần được cấp quyền để sử dụng tính năng thông báo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermission(): Boolean {
        val result = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        return result == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun init_views() {
        dp = findViewById(R.id.datePicker)
        tp = findViewById(R.id.timePicker)
        bt = findViewById(R.id.btAlarm)
        bt?.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            calendar[dp!!.year, dp!!.month] = dp!!.dayOfMonth
            calendar[Calendar.HOUR_OF_DAY] = tp!!.hour
            calendar[Calendar.MINUTE] = tp!!.minute
            calendar[Calendar.SECOND] = 0
            Log.i("time", calendar.time.toString())
            val intent = Intent(applicationContext, AlarmReceiver::class.java)
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            val alarmIntent = PendingIntent.getBroadcast(
                applicationContext,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = alarmIntent
            Toast.makeText(
                applicationContext,
                "Thông báo sẽ được gửi lúc " + calendar.time.toString(),
                Toast.LENGTH_LONG
            ).show()
        })
    }
}