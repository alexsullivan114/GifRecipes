package com.alexsullivan.reddit

import com.alexsullivan.count
import com.squareup.moshi.Moshi
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody


class RedditOkHttpClient(val deviceId: String) {
    val client: okhttp3.OkHttpClient
    var accessToken = "empty"
    val baseUrl = "https://oauth.reddit.com/"

    init {
        client = okhttp3.OkHttpClient.Builder()
                .addInterceptor(buildAuthenticationInterceptor())
                .authenticator(buildAuthenticator())
                .build()
    }

    /**
     * Build the authentication interceptor that will make sure all calls have the relevant
     * authorizatoin headers. Note that this authoriation may not be a valid, that will be checked
     * in the actual authenticator
     */
    private fun buildAuthenticationInterceptor(): okhttp3.Interceptor {
        return okhttp3.Interceptor { chain ->
            var request = chain.request()
            if (chain.request().header("authorization") == null) {
                request = request.newBuilder().addHeader("Authorization", "Bearer $accessToken").build()
            }
            chain.proceed(request)
        }
    }

    /**
     * Build the authenticator that will fetch a new access token if the old one returned a 401.
     * Only gets called if a 401 is returned.
     */
    private fun buildAuthenticator(): okhttp3.Authenticator {
        return okhttp3.Authenticator { _, response ->
            // If we've already made a few requests, something in our scheme isn't working
            // and we shouldn't nail the network.
            if (response.count() > 5) {
                return@Authenticator null
            }

            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body = RequestBody.create(mediaType, "grant_type=https%3A%2F%2Foauth.reddit.com%2Fgrants%2Finstalled_client&device_id=$deviceId")
            val request = Request.Builder()
                    .url("https://www.reddit.com/api/v1/access_token")
                    .post(body)
                    .addHeader("user-agent", "GifRecipes Android App User Agent by Alex Sullivan")
                    .addHeader("authorization", "Basic a3E3RE5EMWtONnJYZ2c6")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .build()

            val authResponse = client.newCall(request).execute().body().string()
            val jsonAdapter = Moshi.Builder().build().adapter(com.alexsullivan.reddit.models.RedditAuth::class.java)
            val auth = jsonAdapter.fromJson(authResponse)
            accessToken = auth.access_token
            response.request().newBuilder()
                    .header("authorization", "Bearer " + accessToken)
                    .build()
        }
    }
}
