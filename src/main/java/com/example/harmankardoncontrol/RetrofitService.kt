package com.example.harmankardoncontrol

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

//Need to add this info from tuya once you have set up the project
public val clientId = ""
public val secret = ""
public val uid = ""

interface TuyaApiService {
    @GET("v1.0/token")
    fun getToken(@Query("grant_type") grantType: Int = 1): Call<DefaultResponse<TokenResult>>

    @GET("v1.0/users/{uid}/homes")
    fun getHomes(@Path("uid") uid: String): Call<DefaultResponse<List<Home>>>

    @GET("v1.1/homes/{homeId}/scenes")
    fun getScenes(@Path("homeId") homeId: String): Call<DefaultResponse<List<Scene>>>

    @POST("v1.0/homes/{homeId}/scenes/{sceneId}/trigger")
    fun triggerScene(
        @Path("homeId") homeId: String,
        @Path("sceneId") sceneId: String
    ):  Call<DefaultResponse<Boolean>>
}

data class DefaultResponse<Result>(
    val result: Result,
    val success: Boolean,
    val t: Long,
    val tid: String
)

data class TokenResult(
    val access_token: String,
    val expire_time: Long,
    val refresh_token: String,
    val uid: String,
)

data class Home(
    val home_id: Long,
    val lat: Float,
    val lon: Float,
    val name: String,
    val geo_name: String,
    val role: String
)

data class Scene(
    val actions: List<Action>,
    val scene_id: String,
    val name: String,
    val background: String,
    val enabled: Boolean,
    val status: Int
)

data class Action(
    val executor_property: Executor,
    val action_executor: String,
    val entity_id: String,
)

data class Executor(
    val keyId: Int,
)

fun createRetrofitWithToken(token: String): TuyaApiService {
    val client = OkHttpClient.Builder()
        .addInterceptor(SigningInterceptor(clientId, secret, token))
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://openapi.tuyaeu.com/")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(TuyaApiService::class.java)
}

fun fetchAccessToken(): String {
    val tempClient = OkHttpClient.Builder()
        .addInterceptor(SigningInterceptor(clientId, secret, null)) // null token on first fetch
        .build()

    val tempRetrofit = Retrofit.Builder()
        .baseUrl("https://openapi.tuyaeu.com/")
        .client(tempClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val tempService = tempRetrofit.create(TuyaApiService::class.java)
    val call = tempService.getToken()
    val response = call.execute()

    if (response.isSuccessful) {
        val token = response.body()?.result?.access_token
        if (token != null) {
            return token
        } else {
            throw Exception("Access token missing in body: ${response.body()}")
        }
    } else {
        throw Exception("Failed to get token. Code: ${response.code()} Body: ${response.errorBody()?.string()}")
    }
}





