package com.alexsullivan.reddit.network

import okhttp3.OkHttpClient


internal class RedditOkHttpClient(deviceId: String) {
    val client = fetchClient(deviceId)

    companion object {
        var accessToken = "empty"
        var client: OkHttpClient? = null

        fun fetchClient(deviceId: String): OkHttpClient {
            if (client == null) {
                client = okhttp3.OkHttpClient.Builder()
                        .addInterceptor(RedditAuthInterceptor({accessToken}))
                        .authenticator(RedditAuthenticator({ accessToken = it }, {client!!}, deviceId))
                        .build()
            }

            return client!!
        }
    }
}
