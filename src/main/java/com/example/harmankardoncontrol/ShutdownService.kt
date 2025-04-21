package com.example.harmankardoncontrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShutdownService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "shutdown_channel")
            .setContentTitle("Harman Automation Ending")
            .setContentText("Running shutdown scene…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(2, notification)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = fetchAccessToken()
                val api = createRetrofitWithToken(token)
                val response = api.triggerScene("47772040", "gcbVMYAq1rqxHdWz").execute()
                Log.d("SHUTDOWN", "Scene triggered on shutdown: ${response.isSuccessful}")
            } catch (e: Exception) {
                Log.e("SHUTDOWN", "Failed to trigger scene on shutdown: ${e.message}")
            } finally {
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shutdown_channel",
                "Shutdown Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}