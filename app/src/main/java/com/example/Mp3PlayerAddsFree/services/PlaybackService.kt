package com.example.Mp3PlayerAddsFree.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import android.app.PendingIntent
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.Mp3PlayerAddsFree.MainActivity

class PlaybackService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager

    private val channelId = "PlaybackChannel"
    private val notificationId = 1

    private val mediaItems = listOf(
        MediaItem.fromUri("https://example.com/track1.mp3"),
        MediaItem.fromUri("https://example.com/track2.mp3"),
        MediaItem.fromUri("https://example.com/track3.mp3")
    )

    private var currentIndex = 0

    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(PlayerListener())

        // Load playlist into ExoPlayer
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()

        // Initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "PlaybackService").apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }

        // Initialize Notification Manager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel (Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Start foreground service with notification
        startForeground(notificationId, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true) // Fallback for older versions
        } 
        exoPlayer.release()
        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val controller = mediaSession.controller
        val metadata = controller.metadata
        val description = metadata?.description

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Now Playing")
            .setContentText("Track ${currentIndex + 1}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (exoPlayer.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (exoPlayer.isPlaying) "Pause" else "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        if (exoPlayer.isPlaying) PlaybackStateCompat.ACTION_PAUSE
                        else PlaybackStateCompat.ACTION_PLAY
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_next,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            exoPlayer.play()
            updateNotification()
            Log.d("PlaybackService", "onPlay triggered")
        }

        override fun onPause() {
            exoPlayer.pause()
            updateNotification()
            Log.d("PlaybackService", "onPause triggered")
        }

        override fun onSkipToNext() {
            if (currentIndex < mediaItems.size - 1) {
                currentIndex++
            } else {
                currentIndex = 0 // Loop back to the start
            }
            exoPlayer.seekTo(currentIndex, 0)
            exoPlayer.play()
            updateNotification()
            Log.d("PlaybackService", "Skipping to next track: $currentIndex")
        }

        override fun onSkipToPrevious() {
            if (currentIndex > 0) {
                currentIndex--
            } else {
                currentIndex = mediaItems.size - 1 // Loop back to the last track
            }
            exoPlayer.seekTo(currentIndex, 0)
            exoPlayer.play()
            updateNotification()
            Log.d("PlaybackService", "Skipping to previous track: $currentIndex")
        }
    }

    private inner class PlayerListener : androidx.media3.common.Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
            Log.d("PlaybackService", "ExoPlayer isPlaying: $isPlaying")
        }
    }

    private fun updateNotification() {
        // Update the existing notification without removing it
        val notification = createNotification()
        notificationManager.notify(notificationId, notification)
    }
}