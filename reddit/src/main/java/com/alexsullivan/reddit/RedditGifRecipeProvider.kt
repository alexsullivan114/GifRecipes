package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.isImage
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.network.RedditOkHttpClient
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.serialization.RedditResponseItemDeserializer
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Alexs on 5/10/2017.
 */
class RedditGifRecipeProvider(val service: RedditService, val imageChecker: (String) -> Boolean,
                              val modelMapper: (RedditGifRecipe) -> GifRecipe): GifRecipeProvider {

    // Used for extension methods.
    companion object Factory {
        fun create(deviceId: String): RedditGifRecipeProvider {
            val okClient = RedditOkHttpClient(deviceId).client
            val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
            val retrofit = Retrofit.Builder()
                    .client(okClient)
                    .baseUrl(RedditService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return RedditGifRecipeProvider(retrofit.create(RedditService::class.java), { isImage(it) }, {
                GifRecipe(it.url, it.id, it.thumbnail, it.previewUrl)
            })
        }
    }
    // Gets updated with our last after value as we consume more recipes.
    var lastAfter: String? = ""

    override fun consumeRecipes(limit: Int): Observable<GifRecipe> {
        return service.hotRecipes(limit = limit, after = lastAfter)
                .doOnNext { lastAfter = it.data.after }
                .flatMap { Observable.fromIterable(it.data.children) }
                .filter { imageChecker(it.url) }
                .map { RedditGifRecipe(it.url, it.id, it.thumbnail, it.previewUrl) }
                .map { modelMapper(it) }
    }
}