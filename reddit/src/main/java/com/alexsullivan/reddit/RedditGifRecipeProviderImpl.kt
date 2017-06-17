package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.ImageType
import com.alexsullivan.isPlayingMedia
import com.alexsullivan.reddit.models.RedditGifRecipe
import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.models.RedditListingResponse
import com.alexsullivan.reddit.network.RedditOkHttpClient
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.serialization.RedditResponseItemDeserializer
import com.alexsullivan.reddit.urlmanipulation.GfycatUrlManipulator
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

    // Hold onto a map of after values so we can pageinate. Key will be the blank string for queries
    // without search values and the search term otherwise.
    val afterMap = mutableMapOf<String, String>()

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
            return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java), listOf(ImgurUrlManipulator(), GfycatUrlManipulator()), { isPlayingMedia(it) })
        }
    }

    override fun consumeRecipes(limit: Int, searchTerm: String): Observable<GifRecipe> {
        return if (!searchTerm.isEmpty()) fetchWithSearchTerm(limit, searchTerm) else fetchHot(limit)
    }

    private fun fetchWithSearchTerm(limit: Int, searchTerm: String): Observable<GifRecipe> {
        return service.searchRecipes(searchTerm, limit = limit)
                .flatMap { processListingResponse(it, searchTerm) }
    }

    private fun fetchHot(limit: Int): Observable<GifRecipe> {
        return service.hotRecipes(limit = limit, after = afterMap.get(""))
                .flatMap { processListingResponse(it, "") }
    }

    private fun processListingResponse(listing: RedditListingResponse, searchTerm: String = ""): Observable<GifRecipe> {
        return Observable.just(listing)
                .doOnNext { afterMap.put(searchTerm, it.data.after) }
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