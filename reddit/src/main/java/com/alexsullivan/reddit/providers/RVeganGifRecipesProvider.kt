package com.alexsullivan.reddit.providers

import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import io.reactivex.Observable
import io.reactivex.Scheduler

internal class RVeganGifRecipesProvider(
    service: RedditService,
    urlManipulators: List<UrlManipulator>,
    dynamicMediaChecker: (String) -> Boolean,
    logger: Logger,
    backgroundScheduler: Scheduler
) : SubredditGifRecipeProvider(service,
    urlManipulators, dynamicMediaChecker, logger, backgroundScheduler) {

  override val id: String
    get() = "RedditVeganRecipesProvider"

  override val subreddit = "vegangifrecipes"

  override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> {
    return searchTerm.trim().toLowerCase().run {
      return if (this == "vegan" || this == "vegetarian") {
        super.consumeRecipes(limit, "", pageKey)
      } else {
        super.consumeRecipes(limit, searchTerm, pageKey)
      }
    }
  }
}
