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
import android.os.Handler
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class SoundService : Service() {
    companion object {
        const val STOP_ACTION = "net.osomahe.whitenoise247.STOP"
    }

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var startTime: LocalDateTime
    override fun onCreate() {
        super.onCreate()
        startTime = LocalDateTime.now()
        mediaPlayer = MediaPlayer.create(this, R.raw.noise_full)
        mediaPlayer.isLooping = true
        mediaPlayer.setVolume(1.0f, 1.0f)

        val intent = Intent(this, SoundService::class.java)
        intent.action = STOP_ACTION

        val pendingIntent =
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("wn_service", "White Noise 247")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }
        val builder = NotificationCompat.Builder(this, channelId).setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle("White Noise 247")
            .setContentText("Running: ${runningTimeToString()}")
            .addAction(R.raw.stop, "Stop", pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                builder.setContentText("Running: ${runningTimeToString()}")
                notificationManager.notify(101, builder.build())
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)
        startForeground(101, builder.build())
    }

    private fun runningTimeToString(): String {
        val differenceSecs = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now())
        val hours = differenceSecs / 3_600
        val minutes = (differenceSecs - hours * 3_600) / 60
        val minutesString = if (minutes < 10) "0$minutes" else minutes
        val seconds = differenceSecs - hours * 3_600 - minutes * 60
        val secondsString = if (seconds < 10) "0$seconds" else seconds
        return "$hours:$minutesString:$secondsString"
    }

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