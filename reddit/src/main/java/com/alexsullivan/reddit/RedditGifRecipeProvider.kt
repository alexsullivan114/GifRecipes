package com.alexsullivan.reddit

import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.logging.Logger

interface RedditGifRecipeProvider : GifRecipeProvider {
  companion object {
    fun createGifRecipesSubredditProvider(deviceId: String, logger: Logger): RedditGifRecipeProvider =
        RedditGifRecipeProviderImpl.create(deviceId, logger, "gifrecipes")

    fun createVeganGifRecipesSubredditProvider(deviceId: String, logger: Logger): RedditGifRecipeProvider =
        RedditGifRecipeProviderImpl.create(deviceId, logger, "vegangifrecipes")

  }
}