package com.example.harmankardoncontrol

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SceneButton(
    text: String,
    sceneId: String,
    service: TuyaApiService,
    onResult: (String) -> Unit,
    homeId:String
) {
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch(Dispatchers.IO) {
            try {
                // Just grab homeId once since you only use the first home anyway
                val response = service.triggerScene(homeId, sceneId).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onResult("$text: Success ✅")
                    } else {
                        onResult("$text: Failed ❌ (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("$text: Error 💥 ${e.message}")
                }
            }
        }
    }) {
        Text(text)
    }
}