package com.alexsullivan.reddit

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Alexs on 5/10/2017.
 */
fun RedditGifRecipeProvider.Companion.with(deviceId: String): RedditGifRecipeProvider {
    val okClient = RedditOkHttpClient(deviceId).client
    val retrofit = Retrofit.Builder()
            .client(okClient)
            .baseUrl(RedditService.statics.baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java))
}