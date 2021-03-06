package com.alexsullivan.reddit.providers

import com.alexsullivan.logging.Logger
import com.alexsullivan.reddit.network.RedditService
import com.alexsullivan.reddit.urlmanipulation.UrlManipulator
import io.reactivex.Scheduler

internal class RGifRecipesProvider(
    service: RedditService,
    urlManipulators: List<UrlManipulator>,
    dynamicMediaChecker: (String) -> Boolean,
    logger: Logger,
    backgroundScheduler: Scheduler
) : SubredditGifRecipeProvider(service,
    urlManipulators, dynamicMediaChecker, logger, backgroundScheduler) {

  override val id: String
    get() = "RedditGifRecipesProvider"

  override val subreddit: String
    get() = "gifrecipes"
}