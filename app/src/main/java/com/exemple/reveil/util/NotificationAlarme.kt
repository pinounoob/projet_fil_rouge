package com.exemple.reveil.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.exemple.reveil.MainActivity

object NotificationAlarme {
    private const val CANAL_ID = "alarme_reveil"

    private fun creerCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val son = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val attributs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            val canal = NotificationChannel(
                CANAL_ID, "Alarme du matin", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(son, attributs)
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
        }
    }

    fun afficher(context: Context, groupeId: String) {
        creerCanal(context)
        val ouvrir = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("groupeId", groupeId)
        }
        val pendingOuvrir = PendingIntent.getActivity(
            context, groupeId.hashCode(), ouvrir,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Debout!")
            .setContentText("Ne sois pas le dernier touche pour te lever")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingOuvrir)
            .setFullScreenIntent(pendingOuvrir, true)
            .setOngoing(true)
            .build()

        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        val autorise = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (autorise) {
            NotificationManagerCompat.from(context).notify(groupeId.hashCode(), notification)
        }
    }

    fun arreterSonnerie(context: Context, groupeId: String) {
        NotificationManagerCompat.from(context).cancel(groupeId.hashCode())
    }
}