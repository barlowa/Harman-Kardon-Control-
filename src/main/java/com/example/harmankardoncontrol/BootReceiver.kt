package com.example.harmankardoncontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BOOT", "Boot completed. Starting services.")

        val startup = Intent(context, StartupService::class.java)
        val shutdownSniffer = Intent(context, ShutdownPollingService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startup)
            context.startForegroundService(shutdownSniffer)
        } else {
            context.startService(startup)
            context.startService(shutdownSniffer)
        }
    }
}