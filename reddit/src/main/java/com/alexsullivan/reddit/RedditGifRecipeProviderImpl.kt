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
import com.alexsullivan.utils.TAG
import com.gfycat.GfycatRepositoryImpl
import com.gfycat.ImgurRepositoryImpl
import com.google.gson.GsonBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException

/**
 * Created by Alexs on 5/10/2017.
 */
internal class RedditGifRecipeProviderImpl(private val service: RedditService, private val urlManipulators: List<UrlManipulator>,
                                           private val medidaChecker: (String) -> Boolean, private val logger: Logger): RedditGifRecipeProvider {

    override val id: String
        get() = "RedditProvider"

    companion object Factory {
        fun create(deviceId: String, logger: Logger): RedditGifRecipeProviderImpl {
            val okClient = RedditOkHttpClient(deviceId, logger).client
            val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
            val retrofit = Retrofit.Builder()
                    .client(okClient)
                    .baseUrl(RedditService.statics.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            val imgurRepo = ImgurRepositoryImpl.create(logger)
            val gfycatRepo = GfycatRepositoryImpl.create(logger)
            return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java),
                    listOf(ImgurUrlManipulator(imgurRepo), GfycatUrlManipulator(gfycatRepo)), { isPlayingMedia(it, okClient) }, logger)
        }
    }

    override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> {
        logger.d(TAG, "Consume recipes called with limit: $limit and search term: $searchTerm and page key: $pageKey")
        return if (!searchTerm.isEmpty()) fetchWithSearchTerm(limit, searchTerm, pageKey) else fetchHot(limit, searchTerm, pageKey)
    }

    private fun fetchWithSearchTerm(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> {
        logger.d(TAG, "Making search request with limit $limit and search term $searchTerm and page key $pageKey")
        return service.searchRecipes(searchTerm, limit = limit, after = pageKey)
                .flatMap { processListingResponse(it) }
    }

    private fun fetchHot(limit: Int, lastItem: String, pageKey: String): Observable<GifRecipe> {
        logger.d(TAG, "Making hot request with limit $limit and last item $lastItem")
        var startTime = 0L
        return service.hotRecipes(limit = limit, after = pageKey)
                .doOnSubscribe { startTime = System.currentTimeMillis() }
                .doOnNext {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    logger.d(TAG, "Fetching hot took $elapsedTime milliseconds")
                    startTime = 0
                }
                .flatMap { processListingResponse(it) }
    }

    private fun processListingResponse(listing: RedditListingResponse): Observable<GifRecipe> {
        return Observable.just(listing)
            .flatMap { response -> Observable.fromIterable(response.data.children)
                .map { it.copy(pageKey = response.data.after) }}
            .toFlowable(BackpressureStrategy.BUFFER)
            .parallel()
            .runOn(Schedulers.io())
            .filter { !it.removed }
            .map { RedditGifRecipe(it.url, it.id, ImageType.GIF, it.thumbnail, it.previewUrl, it.domain, it.title, it.pageKey) }
            .flatMap {
                var returnObservable = Observable.just(it)
                urlManipulators.filter { manipulator -> manipulator.matchesDomain(it.url) }
                    .forEach { manipulator -> returnObservable = manipulator.modifyRedditItem(it) }
                // Retry up to 2 times if we time out.
                returnObservable.retry(2, {t: Throwable -> t is SocketTimeoutException })
                    .toFlowable(BackpressureStrategy.BUFFER)
            }
            .filter { medidaChecker(it.url) }
            .map { GifRecipe(it.url, it.id, it.thumbnail, it.imageType, it.title, it.pageKey) }
            .sequential()
            .toObservable()
    }
}