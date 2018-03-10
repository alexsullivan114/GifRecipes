package com.alexsullivan.reddit.providers

import com.alexsullivan.isPlayingMedia
import com.alexsullivan.isStaticImgae
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.models.RedditListingItem
import com.alexsullivan.reddit.network.RedditOkHttpClient
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.serialization.RedditResponseItemDeserializer
import com.alexsullivan.reddit.urlmanipulation.GfycatUrlManipulator
import com.alexsullivan.reddit.urlmanipulation.ImgurUrlManipulator
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import com.gfycat.GfycatRepositoryImpl
import com.google.gson.GsonBuilder
import com.imgur.ImgurRepositoryImpl
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

private typealias DynamicMediaChecker = (String)  -> Boolean
private typealias Factory = (RedditService, List<UrlManipulator>, DynamicMediaChecker, Logger, Scheduler) -> SubredditGifRecipeProvider

private fun create(deviceId: String, logger: Logger, factory: Factory): SubredditGifRecipeProvider {

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

  return factory(retrofit.create(RedditService::class.java),
      listOf(
          ImgurUrlManipulator(imgurRepo, staticMediaChecker),
          GfycatUrlManipulator(gfycatRepo, staticMediaChecker)),
      dynamicMediaChecker,
      logger,
      Schedulers.io())
}

fun createRGifRecipesProvider(deviceId: String, logger: Logger): RedditGifRecipesProvider {
  val factory = fun (service: RedditService,
                     manipulators: List<UrlManipulator>,
                     checker: DynamicMediaChecker,
                     logger: Logger,
                     scheduler: Scheduler): SubredditGifRecipeProvider =
      RGifRecipesProvider(service, manipulators, checker, logger, scheduler)

  return create(deviceId, logger, factory)
}

fun createRVeganGifRecipesProvider(deviceId: String, logger: Logger): RedditGifRecipesProvider {
  val factory = fun (service: RedditService,
                     manipulators: List<UrlManipulator>,
                     checker: DynamicMediaChecker,
                     logger: Logger,
                     scheduler: Scheduler): SubredditGifRecipeProvider =
      RVeganGifRecipesProvider(service, manipulators, checker, logger, scheduler)

  return create(deviceId, logger, factory)
}

fun createRAlcoholGifRecipesProvider(deviceId: String, logger: Logger): RedditGifRecipesProvider {
  val factory = fun (service: RedditService,
                     manipulators: List<UrlManipulator>,
                     checker: DynamicMediaChecker,
                     logger: Logger,
                     scheduler: Scheduler): SubredditGifRecipeProvider =
      RAlcoholGifRecipesProvider(service, manipulators, checker, logger, scheduler)

  return create(deviceId, logger, factory)
}