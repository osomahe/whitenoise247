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
import java.util.concurrent.CompletableFuture


class SoundService : Service() {
    companion object {
        const val STOP_ACTION = "net.osomahe.whitenoise247.STOP"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var mediaPlayerSecond: MediaPlayer? = null

    private lateinit var startTime: LocalDateTime
    override fun onCreate() {
        super.onCreate()
        startTime = LocalDateTime.now()
        mediaPlayer = MediaPlayer.create(this, R.raw.noise_5min)
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
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                builder.setContentText("Running: ${runningTimeToString()}")
                notificationManager.notify(101, builder.build())
                handler.postDelayed(this, 1000)
            }
        }
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
                handler.removeCallbacks(runnable)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                mediaPlayer.start()
                handler.postDelayed(runnable, 1000)
                startSecondPlayer()
            }
        }
        return START_STICKY
    }

    private fun startSecondPlayer() {
        CompletableFuture.runAsync {
            val timeToEnd = mediaPlayer.duration - mediaPlayer.currentPosition
            // 210s = 100s to volume mediaPlayerSecond up + 100s to volume mediaPlayer down + stop mediaPlayer 10s before end
            Thread.sleep(timeToEnd - 210_000L)
            mediaPlayerSecond = MediaPlayer.create(this, R.raw.noise_5min)
            var volume = 0f
            mediaPlayerSecond?.setVolume(volume, volume)
            mediaPlayerSecond?.start()
            while (volume < 1f) {
                volume += 0.01f
                mediaPlayerSecond?.setVolume(volume, volume)
                Thread.sleep(1_000)
            }
            while (volume > 0f) {
                volume -= 0.01f
                mediaPlayer.setVolume(volume, volume)
                Thread.sleep(1_000)
            }
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayer = mediaPlayerSecond as MediaPlayer
            mediaPlayerSecond = null
            startSecondPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        mediaPlayerSecond?.stop()
        mediaPlayerSecond?.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}