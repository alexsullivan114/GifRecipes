package com.alexsullivan.reddit

import com.alexsullivan.GifRecipeProvider

interface RedditGifRecipeProvider: GifRecipeProvider {
    companion object {
        fun create(deviceId: String): RedditGifRecipeProvider {
            return RedditGifRecipeProviderImpl.create(deviceId)
        }
    }
}