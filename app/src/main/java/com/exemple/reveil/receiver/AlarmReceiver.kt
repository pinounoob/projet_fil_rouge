package com.exemple.reveil.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exemple.reveil.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val groupeId = intent.getStringExtra("groupeId") ?: ""

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("groupeId", groupeId)
        }
        context.startActivity(mainIntent)
    }
}