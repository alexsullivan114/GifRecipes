package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.ImageType
import com.alexsullivan.isPlayingMedia
import com.alexsullivan.logging.Logger
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
import kotlin.system.measureTimeMillis

/**
 * Created by Alexs on 5/10/2017.
 */
internal class RedditGifRecipeProviderImpl(val service: RedditService, val urlManipulators: List<UrlManipulator>,
                                           val medidaChecker: (String) -> Boolean, val logger: Logger): RedditGifRecipeProvider {

    private val TAG: String = javaClass.simpleName
    // Hold onto a map of after values so we can pageinate. Key will be the blank string for queries
    // without search values and the search term otherwise.
    val afterMap = mutableMapOf<String, String>()

    override val id: String
        get() = "RedditProvider"

    // Used for extension methods.
    companion object Factory {
        fun create(deviceId: String, logger: Logger): RedditGifRecipeProviderImpl {
            val okClient = RedditOkHttpClient(deviceId).client
            val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
            val retrofit = Retrofit.Builder()
                    .client(okClient)
                    .baseUrl(RedditService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java),
                    listOf(ImgurUrlManipulator(), GfycatUrlManipulator()), { isPlayingMedia(it) }, logger)
        }
    }

    override fun consumeRecipes(limit: Int, searchTerm: String): Observable<GifRecipe> {
        logger.d(TAG, "Consume recipes called with limit: $limit and search term: $searchTerm")
        return if (!searchTerm.isEmpty()) fetchWithSearchTerm(limit, searchTerm) else fetchHot(limit)
    }

    private fun fetchWithSearchTerm(limit: Int, searchTerm: String): Observable<GifRecipe> {
        logger.d(TAG, "Making search request with limit $limit and search term $searchTerm")
        return service.searchRecipes(searchTerm, limit = limit)
                .flatMap { processListingResponse(it, searchTerm) }
    }

    private fun fetchHot(limit: Int): Observable<GifRecipe> {
        logger.d(TAG, "Making hot request with limit $limit")
        var startTime = 0L
        return service.hotRecipes(limit = limit, after = afterMap[""])
                .doOnSubscribe { startTime = System.currentTimeMillis() }
                .doOnNext {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    logger.d(TAG, "Fetching hot took $elapsedTime milliseconds")
                    startTime = 0
                }
                .flatMap { processListingResponse(it, "") }
    }

    private fun processListingResponse(listing: RedditListingResponse, searchTerm: String = ""): Observable<GifRecipe> {
        return Observable.just(listing)
                .doOnNext { afterMap.put(searchTerm, it.data.after) }
                .flatMap { Observable.fromIterable(it.data.children) }
                .map { RedditGifRecipe(it.url, it.id, ImageType.GIF, it.thumbnail, it.previewUrl, it.domain, it.title) }
                .flatMap {
                    var returnObservable = Observable.just(it)
                    urlManipulators.filter { manipulator -> manipulator.matchesDomain(it.domain) }
                            .forEach { manipulator -> returnObservable = manipulator.modifyRedditItem(it) }
                    returnObservable
                }
                .filter {
                    var isMedia = false
                    val elapsedTime = measureTimeMillis { isMedia = medidaChecker(it.url) }
                    logger.d(TAG, "Checking media type for ${it.url} took ${elapsedTime} milliseconds")
                    isMedia
                }
                .map { GifRecipe(it.url, it.id, it.thumbnail, it.previewUrl, it.imageType, it.title) }
    }
}