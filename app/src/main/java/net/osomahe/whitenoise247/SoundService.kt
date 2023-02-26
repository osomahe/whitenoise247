package net.osomahe.whitenoise247

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class SoundService : Service() {
    companion object {
        const val STOP_ACTION = "net.osomahe.whitenoise247.STOP"
    }

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.whitenoise)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(1.0f, 1.0f)

        val intent = Intent(this, SoundService::class.java)
        intent.action = STOP_ACTION

        val pendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "wn_service").setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle("White Noise 247")
            .addAction(R.raw.stop, "Stop", pendingIntent)
            .build()
        startForeground(101, notification)
    }

    /*
    val notificationId = 1 // unique notification ID
    val channelId = "my_channel_id" // unique channel ID for the notification
    val intent = Intent(this, MyActivity::class.java) // intent to launch when the button is clicked
    val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0) // pending intent for the button
    val action = NotificationCompat.Action.Builder(R.drawable.ic_action_name, "Button Text", pendingIntent).build() // create the button action
    val notification = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_notification_icon)
        .setContentTitle("Notification Title")
        .setContentText("Notification Text")
        .addAction(action) // add the button action to the notification
        .build()
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager // get the notification manager
    notificationManager.notify(notificationId, notification) // show the notification

     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            STOP_ACTION -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                mediaPlayer.start()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}