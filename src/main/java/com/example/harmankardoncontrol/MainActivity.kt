package com.example.harmankardoncontrol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var service by remember { mutableStateOf<TuyaApiService?>(null) }
            var scenes by remember { mutableStateOf<List<Scene>>(emptyList()) }
//            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        val token = fetchAccessToken()
                        val api = createRetrofitWithToken(token)
                        service = api

                        val homesResponse = api.getHomes(uid).execute()
                        Log.d("homes res", homesResponse.body()?.result.toString())
                        val homeId = homesResponse.body()?.result?.firstOrNull()?.home_id?.toString()
                        Log.d("home id", homeId.toString())

                        if (homeId != null) {
                            val scenesResponse = api.getScenes(homeId).execute()
                            Log.d("SCENES", "Fetched ${scenesResponse.body()}")
                            scenes = scenesResponse.body()?.result ?: emptyList()
                            Log.d("SCENES", "Fetched ${scenes.size} scenes")
                        } else {
                            Log.e("INIT", "No homes found")
                        }

                        Log.d("INIT", "API client initialized")
                    } catch (e: Exception) {
                        Log.e("INIT", "API init failed: ${e.message}")
                    }
                }
            }

            HarmanKardonUI(service, scenes)
        }
    }


}


