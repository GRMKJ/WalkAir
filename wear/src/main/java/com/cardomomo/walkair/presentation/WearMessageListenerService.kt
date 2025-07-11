package com.cardomomo.walkair.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cardomomo.walkair.R
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/start_workout") {
            Log.d("WearMsgService", "Mensaje recibido: /start_workout")

            val channelId = "training_channel"
            val notificationManager = getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

            // Crear canal si es Android O+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Entrenamiento",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            // Intent para lanzar WaitingActivity
            val intent = Intent(this, WaitingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.outline_directions_walk_24) // Usa tu ícono real
                .setContentTitle("WalkAir")
                .setContentText("Toca para iniciar entrenamiento")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(1, notification)
        }
    }
}
