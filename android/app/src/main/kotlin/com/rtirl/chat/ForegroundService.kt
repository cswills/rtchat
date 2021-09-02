package com.rtirl.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

class ForegroundService : Service(), EventChannel.StreamHandler {
    private val binder = ForegroundServiceBinder()
    private val backgroundEngine = FlutterEngine(this)

    companion object {
        const val ONGOING_NOTIFICATION_ID = 68448
        const val NOTIFICATION_CHANNEL_ID = "com.rtirl.chat.audio"
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun start(callbackInfo: FlutterCallbackInformation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Audio sources",
                NotificationManager.IMPORTANCE_MIN
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
        val notificationIntent =
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(0xFF009FDF.toInt())
            .setContentTitle("RealtimeChat is running in the background")
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(ONGOING_NOTIFICATION_ID, builder.build())

        val args = DartExecutor.DartCallback(
            assets,
            FlutterLoader.getInstance().findAppBundlePath(),
            callbackInfo
        )
        val channel =
            EventChannel(
                backgroundEngine.dartExecutor.binaryMessenger,
                "com.rtirl.chat/audio_urls"
            )
        backgroundEngine.dartExecutor.executeDartCallback(args)
        channel.setStreamHandler(this)
    }

    fun stop() {
        stopForeground(true)
    }

    inner class ForegroundServiceBinder : Binder() {
        fun getService(): ForegroundService {
            return this@ForegroundService
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {

    }

    override fun onCancel(arguments: Any?) {

    }
}