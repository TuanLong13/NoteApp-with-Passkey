package com.google.credentialmanager.sample.noteapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.credentialmanager.sample.R

class AlarmReceiver : BroadcastReceiver() {
    var title: String? = null
    var content: String? = null
    override fun onReceive(context: Context, intent: Intent) {
        val not = Notification(
            context,
            context.getString(R.string.channel_name),
            context.getString(R.string.channel_desc)
        )
        val extra = intent.extras
        title = extra!!.getString("title")
        content = extra.getString("content")
        not.setNotification(title, content)
    }
}