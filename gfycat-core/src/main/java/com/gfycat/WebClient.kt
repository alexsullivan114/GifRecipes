package com.gfycat

import com.count
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*

object WebClient {
    var okHttpClient: OkHttpClient
    var accessToken = "empty"

    init {
        okHttpClient = okhttp3.OkHttpClient.Builder()
                .addInterceptor(buildAuthInterceptor())
                .authenticator(buildAuthenticator())
                .build()
    }

    fun buildAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            if (request.header("authorization") == null) {
                request = request.newBuilder().addHeader("Authorization", "Bearer $accessToken").build()
            }
            return@Interceptor chain.proceed(request)
        }
    }

    fun buildAuthenticator(): Authenticator {
        return Authenticator { _, response ->
            if (response.count() > 5) {
                return@Authenticator null
            }

            val mediaType = MediaType.parse("application/octet-stream")
            val body = RequestBody.create(mediaType, "{\"grant_type\":\"client_credentials\", " +
                    "\"client_secret\":\"Rss2UbdEAMEDHMxRpcOqOosQikHd_pShA1vnu9JrmWR4XPKpRDi9V_fqUnf2Bl0V\", " +
                    "\"client_id\":\"2_ntJYHc\"}");
            val request = Request.Builder().url("https://api.gfycat.com/v1/oauth/token").post(body).build()

            val authResponse = okHttpClient.newCall(request).execute().body().string()
            accessToken = Gson().fromJson(authResponse, JsonObject::class.java).get("access_token").asString
            return@Authenticator response.request().newBuilder()
                    .header("authorization", "Bearer " + accessToken)
                    .build()
        }
    }
}