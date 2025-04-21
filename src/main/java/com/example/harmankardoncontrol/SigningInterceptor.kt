
package com.example.harmankardoncontrol

import okhttp3.Interceptor
import okhttp3.Response
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class SigningInterceptor(
    private val clientId: String,
    private val secret: String,
    private val token: String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.method.uppercase()
        val path = request.url.encodedPath + if (request.url.encodedQuery != null) "?${request.url.encodedQuery}" else ""
        val t = (Utils.getNetworkTime() ?: System.currentTimeMillis()).toString()
        val nonce = UUID.randomUUID().toString()

        val body = request.body
        val bodyBytes = if (body != null) {
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readByteArray()
        } else {
            ByteArray(0)
        }

        val bodyHash = sha256(bodyBytes)

        val stringToSign = "$method\n$bodyHash\n\n$path"
        val fullString = clientId + (token ?: "") + t + nonce + stringToSign
        val signature = hmacSha256(fullString, secret).uppercase(Locale.ROOT)

        val newRequest = request.newBuilder()
            .addHeader("client_id", clientId)
            .addHeader("sign", signature)
            .addHeader("t", t)
            .addHeader("nonce", nonce)
            .addHeader("sign_method", "HMAC-SHA256")
            .apply {
                if (!token.isNullOrEmpty()) {
                    addHeader("access_token", token)
                }
            }
            .build()

        return chain.proceed(newRequest)
    }

    private fun sha256(input: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun hmacSha256(data: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val result = mac.doFinal(data.toByteArray())
        return result.joinToString("") { "%02x".format(it) }
    }
}