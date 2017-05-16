package com.alexsullivan.reddit

import com.alexsullivan.isImage
import com.alexsullivan.reddit.models.RedditGifRecipe
import io.reactivex.Observable

/**
 * Created by Alexs on 5/10/2017.
 */
class RedditGifRecipeProviderImpl(val service: RedditService): RedditGifRecipeProvider {

    override fun getRecipes(): Observable<RedditGifRecipe> {
        return service.hotRecipes().flatMap {
            Observable.fromIterable(it.data.children)
        }.filter {
            isImage(it.url)
        }.cast(RedditGifRecipe::class.java)
    }
}