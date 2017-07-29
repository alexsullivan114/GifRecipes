package com.alexsullivan.reddit.network

import com.alexsullivan.logging.Logger
import okhttp3.OkHttpClient


internal class RedditOkHttpClient(deviceId: String, logger: Logger) {
    val client = fetchClient(deviceId, logger)

    companion object {
        var accessToken = "empty"
        var client: OkHttpClient? = null

        fun fetchClient(deviceId: String, logger: Logger): OkHttpClient {
            if (client == null) {
                client = okhttp3.OkHttpClient.Builder()
                        .addInterceptor(RedditAuthInterceptor({accessToken}))
//                        .addInterceptor(HttpLoggingInterceptor({
//                            logger.d("RedditOkHttpClient", it)
//                        }).setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .authenticator(RedditAuthenticator({ accessToken = it }, {client!!}, deviceId))
                        .build()
            }

            return client!!
        }
    }
}
