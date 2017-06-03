package com.alexsullivan.reddit.network

import okhttp3.OkHttpClient


internal class RedditOkHttpClient(deviceId: String) {
    lateinit var client: OkHttpClient
    var accessToken = "empty"

    init {
        client = okhttp3.OkHttpClient.Builder()
                .addInterceptor(RedditAuthInterceptor({accessToken}))
                .authenticator(RedditAuthenticator({
                    accessToken = it
                }, {client}, deviceId))
                .build()
    }
}
