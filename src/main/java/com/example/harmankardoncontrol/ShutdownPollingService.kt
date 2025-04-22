package com.example.harmankardoncontrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.*

class ShutdownPollingService : LifecycleService() {

    private val pollIntervalMs = 5000L // 5 sec
    private val tag = "ShutdownPollingService" // Tag for logging

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service created, initializing polling...")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "polling_channel")
            .setContentTitle("Monitoring Shutdown")
            .setContentText("Watching for projector power off…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(3, notification)
        Log.d(tag, "Started foreground service with notification.")

        CoroutineScope(Dispatchers.IO).launch {
            monitorShutdown()
        }
    }

    private suspend fun monitorShutdown() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        Log.d(tag, "PowerManager obtained.")

        while (true) {
            try {
                val isInteractive = powerManager.isInteractive
                Log.d(tag, "Checking if device is interactive: $isInteractive")

                if (!isInteractive) {
                    Log.d(tag, "Shutdown detected — projector power off.")
                    // Trigger the scene once shutdown is detected
                    val token = fetchAccessToken()
                    Log.d(tag, "Access token fetched successfully.")
                    val api = createRetrofitWithToken(token)
                    try {
                        val response = api.triggerScene("47772040", "gcbVMYAq1rqxHdWz").execute()
                        if (response.isSuccessful) {
                            Log.d(tag, "Scene triggered successfully.")
                        } else {
                            Log.e(tag, "Failed to trigger scene. Response: ${response.errorBody()?.string()}")
                        }
                    } catch (apiException: Exception) {
                        Log.e(tag, "Error triggering scene: ${apiException.message}")
                    }
                    break // Stop the service after triggering the scene
                }

                // Wait for next poll
                Log.d(tag, "Projector is still active, waiting for next check...")
                delay(pollIntervalMs)
            } catch (e: Exception) {
                Log.e(tag, "Error during polling: ${e.message}")
                break // Exit loop on error
            }
        }

        Log.d(tag, "Polling finished, stopping service.")
        stopSelf() // Stop the service after the operation is done
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "polling_channel",
                "Shutdown Polling Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(tag, "Notification channel created.")
        }
    }
}