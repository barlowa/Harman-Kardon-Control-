package com.example.harmankardoncontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SHUTDOWN", "Shutdown detected. Launching farewell automation.")
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            val serviceIntent = Intent(context, ShutdownService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}