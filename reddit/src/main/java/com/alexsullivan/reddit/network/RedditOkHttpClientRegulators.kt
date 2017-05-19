package com.alexsullivan.reddit.network

import okhttp3.*

internal class RedditAuthInterceptor(val accessTokenProvider: () -> String): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.header("authorization") == null) {
            val token = accessTokenProvider()
            request = request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        }
        return chain.proceed(request)
    }
}

internal class RedditAuthenticator(val accessTokenConsumer: (String) -> Unit, val clientProvider: () -> OkHttpClient,
                          val deviceId: String): Authenticator {
    override fun authenticate(route: Route, response: Response): Request? {
        if (response.count() > 5) {
            return null
        }

        val mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded")
        val body = okhttp3.RequestBody.create(mediaType, "grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=$deviceId")
        val request = okhttp3.Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
                .post(body)
                .addHeader("user-agent", "GifRecipes Android App User Agent by Alex Sullivan")
                .addHeader("authorization", "Basic a3E3RE5EMWtONnJYZ2c6")
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

        val authResponse = clientProvider().newCall(request).execute().body().string()
        val gson = com.google.gson.Gson()
        val auth = gson.fromJson(authResponse, com.alexsullivan.reddit.models.RedditAuth::class.java)
        val accessToken = auth.access_token
        accessTokenConsumer(accessToken)
        return response.request().newBuilder()
                .header("authorization", "Bearer " + accessToken)
                .build()
    }
}
