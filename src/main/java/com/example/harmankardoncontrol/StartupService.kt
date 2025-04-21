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

class StartupService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "startup_channel")
            .setContentTitle("Harman Automation Running")
            .setContentText("Waiting for projector trigger…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = fetchAccessToken()
                val api = createRetrofitWithToken(token)

                val response = api.triggerScene("47772040", "iPqTDzhI536D3oFh").execute()
                if (response.isSuccessful) {
                    Log.d("AUTOBOOT", "Scene triggered successfully on boot.")
                } else {
                    Log.e("AUTOBOOT", "Scene trigger failed. Code: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("AUTOBOOT", "Exception during boot scene trigger: ${e.message}", e)
            } finally {
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "startup_channel",
                "Startup Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}