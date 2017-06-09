package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.ImageType
import com.alexsullivan.isPlayingMedia
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.network.RedditOkHttpClient
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.serialization.RedditResponseItemDeserializer
import com.alexsullivan.reddit.urlmanipulation.ImgurUrlManipulator
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by Alexs on 5/10/2017.
 */
internal class RedditGifRecipeProviderImpl(val service: RedditService, val urlManipulators: List<UrlManipulator>,
                                           val medidaChecker: (String) -> Boolean): RedditGifRecipeProvider {

    // Gets updated with our last after value as we consume more recipes.
    var lastAfter: String? = ""

    override val id: String
        get() = "RedditProvider"

    // Used for extension methods.
    companion object Factory {
        fun create(deviceId: String): RedditGifRecipeProviderImpl {
            val okClient = RedditOkHttpClient(deviceId).client
            val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
            val retrofit = Retrofit.Builder()
                    .client(okClient)
                    .baseUrl(RedditService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java), listOf(ImgurUrlManipulator()), { isPlayingMedia(it) })
        }
    }

    override fun consumeRecipes(limit: Int): Observable<GifRecipe> {
        return service.hotRecipes(limit = limit, after = lastAfter)
                .doOnNext { lastAfter = it.data.after }
                .flatMap { Observable.fromIterable(it.data.children) }
                .map { RedditGifRecipe(it.url, it.id, ImageType.GIF, it.thumbnail, it.previewUrl, it.domain) }
                .flatMap {
                    var returnObservable = Observable.just(it)
                    urlManipulators.filter { manipulator -> manipulator.matchesDomain(it.domain) }
                            .forEach { manipulator -> returnObservable = manipulator.modifyRedditItem(it) }
                    returnObservable
                }
                .filter { medidaChecker(it.url) }
                .map { GifRecipe(it.url, it.id, it.thumbnail, it.previewUrl, it.imageType) }
    }
}