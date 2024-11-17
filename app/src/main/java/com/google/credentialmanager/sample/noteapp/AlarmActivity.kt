package com.google.credentialmanager.sample.noteapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.credentialmanager.sample.R
import java.util.Calendar


class AlarmActivity : AppCompatActivity() {
    var dp: DatePicker? = null
    var tp: TimePicker? = null
    var bt: Button? = null
    var title: String? = null
    var content: String? = null

    private val NOTIFICATION_PERMISSION_REQUEST_CODE: Int = 112

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        init_views()
        val extra = intent.extras!!
        title = extra.getString("title")
        content = extra.getString("content")

    }

    private fun getNotificationPermission() {
        if (Build.VERSION.SDK_INT > 32) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.i("abc", "success")
                } else {
                    Log.i("abc", "failed")
                }
                return
            }

        }
    }

    fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT > 32) {
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
        return false
    }


    private fun checkSchedulePermission(): Boolean {
        if (Build.VERSION.SDK_INT > 32) {
            val result = checkSelfPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                    0
                )
            }
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }


    @SuppressLint("MissingPermission")
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
            if (!shouldShowRequestPermissionRationale("112")){
                getNotificationPermission();
            }
            if (Build.VERSION.SDK_INT > 32) {
                val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
                if (alarmManager?.canScheduleExactAlarms() == false) {
                    Intent().also { intent ->
                        intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        this.startActivity(intent)
                    }
                }

                try {
                    alarmManager?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        alarmIntent
                    );
                    Toast.makeText(
                        applicationContext,
                        "Thông báo sẽ được gửi lúc " + calendar.time.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Cần được cấp quyền để sử dụng tính năng hẹn giờ thông báo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        });
    }
}

