package com.alexsullivan.reddit

import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.serialization.RedditResponseItemDeserializer
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Alexs on 5/10/2017.
 */
fun RedditGifRecipeProvider.Companion.with(deviceId: String): RedditGifRecipeProvider {
    val okClient = RedditOkHttpClient(deviceId).client
    val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
    val retrofit = Retrofit.Builder()
            .client(okClient)
            .baseUrl(RedditService.statics.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java))
}