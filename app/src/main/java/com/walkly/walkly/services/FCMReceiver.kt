package com.walkly.walkly.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.walkly.walkly.MainActivity
import com.walkly.walkly.R

class FCMReceiver : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        createNotification()
    }

    private fun createNotification(){
        val targetIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("target-fragment", "friend-list")
        }
        val pendingInt = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, "02").apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle("New Friend Request")
            setContentText("You got a new friend request")
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingInt)
        }

        NotificationManagerCompat.from(this)
            .notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Friend Requests"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("02", name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}