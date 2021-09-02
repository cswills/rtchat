package com.rtirl.chat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.NonNull
import com.ryanheise.audioservice.AudioServicePlugin
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain

class MainActivity : FlutterActivity() {
    private var foregroundService: ForegroundService? = null

    override fun provideFlutterEngine(context: Context): FlutterEngine? {
        return AudioServicePlugin.getFlutterEngine(context)
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val channel =
            MethodChannel(
                flutterEngine.dartExecutor.binaryMessenger,
                "com.rtirl.chat/foreground_service"
            )

        var callbackHandle = 0L

        val connection = object : ServiceConnection {
            override fun onServiceConnected(
                className: ComponentName,
                service: IBinder
            ) {
                val binder = service as ForegroundService.ForegroundServiceBinder
                foregroundService = binder.getService()
                foregroundService?.start(
                    FlutterCallbackInformation.lookupCallbackInformation(callbackHandle))
            }

            override fun onServiceDisconnected(name: ComponentName) {
                foregroundService?.stop()
                foregroundService = null
            }
        }

        channel.setMethodCallHandler { call, result ->
            val intent = Intent(this, ForegroundService::class.java)
            when (call.method) {
                "start" -> {
                    callbackHandle = call.argument("callbackHandle")!!
                    bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    result.success(true)
                }
                "stop" -> {
                    stopService(intent)
                    result.success(false)
                }
                "set" -> {

                }
                else -> result.notImplemented()
            }
        }
    }
}
