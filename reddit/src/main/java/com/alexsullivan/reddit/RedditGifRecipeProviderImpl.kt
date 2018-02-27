package com.alexsullivan.reddit

import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider.Response
import com.alexsullivan.ImageType
import com.alexsullivan.isPlayingMedia
import com.alexsullivan.isStaticImgae
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
import com.google.gson.GsonBuilder
import com.imgur.ImgurRepositoryImpl
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException

/**
 * Created by Alexs on 5/10/2017.
 */
internal class RedditGifRecipeProviderImpl(private val service: RedditService,
                                           private val urlManipulators: List<UrlManipulator>,
                                           private val dynamicMediaChecker: (String) -> Boolean,
                                           private val logger: Logger,
                                           private val backgroundScheduler: Scheduler,
                                           private val subreddit: String) : RedditGifRecipeProvider {

  override val id: String
    get() = "RedditProvider"

  companion object Factory {
    fun create(deviceId: String, logger: Logger, subreddit: String): RedditGifRecipeProviderImpl {
      val okClient = RedditOkHttpClient(deviceId, logger).client
      val gson = GsonBuilder().registerTypeAdapter(RedditListingItem::class.java, RedditResponseItemDeserializer()).create()
      val retrofit = Retrofit.Builder()
          .client(okClient)
          .baseUrl(RedditService.baseUrl)
          .addConverterFactory(GsonConverterFactory.create(gson))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build()
      val imgurRepo = ImgurRepositoryImpl.create(logger)
      val gfycatRepo = GfycatRepositoryImpl.create(logger)
      val dynamicMediaChecker = fun(url: String) = isPlayingMedia(url, okClient)
      val staticMediaChecker = fun(url: String) = isStaticImgae(url, okClient)
      return RedditGifRecipeProviderImpl(retrofit.create(RedditService::class.java),
          listOf(
              ImgurUrlManipulator(imgurRepo, staticMediaChecker),
              GfycatUrlManipulator(gfycatRepo, staticMediaChecker)),
          dynamicMediaChecker,
          logger,
          Schedulers.io(),
          subreddit)
    }
  }

  override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<Response> {
    logger.d(TAG, "Consume recipes called with limit: $limit and search term: $searchTerm and page key: $pageKey")
    val shouldSearch = !searchTerm.isEmpty()
    val observable = if (shouldSearch) fetchWithSearchTerm(limit, searchTerm, pageKey) else fetchHot(limit, searchTerm, pageKey)
    return observable
        .map { data -> Response(data.second, buildContinuation(limit, searchTerm, data.first)) }
  }

  private fun fetchWithSearchTerm(limit: Int, searchTerm: String, pageKey: String): Observable<Pair<String, List<GifRecipe>>> {
    logger.d(TAG, "Making search request with limit $limit and search term $searchTerm and page key $pageKey")
    return service.searchRecipes(subreddit, searchTerm, limit = limit, after = pageKey)
        .flatMap { processListingResponse(it) }
  }

  private fun fetchHot(limit: Int, lastItem: String, pageKey: String): Observable<Pair<String, List<GifRecipe>>> {
    logger.d(TAG, "Making hot request with limit $limit and last item $lastItem")
    var startTime = 0L
    return service.hotRecipes(subreddit, limit = limit, after = pageKey)
        .doOnSubscribe { startTime = System.currentTimeMillis() }
        .doOnNext {
          val elapsedTime = System.currentTimeMillis() - startTime
          logger.d(TAG, "Fetching hot took $elapsedTime milliseconds")
          startTime = 0
        }
        .flatMap { processListingResponse(it) }
  }

  /**
   * Takes the provided response from the Reddit API, maps it into appropriate model objects
   * and checks each item for its type. Checking each item requires making a HEAD request, as such
   * this method is parallelized.
   */
  private fun processListingResponse(listing: RedditListingResponse): Observable<Pair<String, List<GifRecipe>>> {
    return Observable.just(listing)
        .flatMap { response -> Observable.fromIterable(response.data.children).map { it.copy(pageKey = response.data.after) } }
        .toFlowable(BackpressureStrategy.BUFFER)
        .parallel()
        .runOn(backgroundScheduler)
        .filter { !it.removed }
        .map { RedditGifRecipe(it.url, it.id, ImageType.GIF, it.thumbnail, it.previewUrl, it.domain, it.title, it.pageKey) }
        .flatMap(this::applyFilters)
        .filter { item -> dynamicMediaChecker(item.url) }
        .map { item -> GifRecipe(item.url, item.id, item.thumbnail, item.imageType, item.title) }
        .sequential()
        .toObservable()
        .toList()
        .toObservable()
        .map { listing.data.after to it }
  }

  /**
   * Apply our list of url manipulators to this reddit gif recipe.
   */
  private fun applyFilters(recipe: RedditGifRecipe): Flowable<RedditGifRecipe> {
    val manipulator: UrlManipulator? = urlManipulators.firstOrNull { it.matchesDomain(recipe.url) }
    val observable = manipulator?.modifyRedditItem(recipe)
        ?.retry(2, { it is SocketTimeoutException })
        ?.toFlowable(BackpressureStrategy.BUFFER)

    return observable ?: Flowable.just(recipe)
  }

  private fun buildContinuation(limit: Int, searchTerm: String, pageKey: String?): Observable<Response> {
    return if (pageKey != null) consumeRecipes(limit, searchTerm, pageKey) else return Observable.empty<Response>()
  }
}