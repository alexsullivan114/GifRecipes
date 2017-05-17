package com.alexsullivan.reddit.network


class RedditOkHttpClient(deviceId: String) {
    lateinit var client: okhttp3.OkHttpClient
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
