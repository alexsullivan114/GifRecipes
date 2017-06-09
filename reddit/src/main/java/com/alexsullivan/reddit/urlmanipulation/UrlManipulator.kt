package com.alexsullivan.reddit.urlmanipulation

import com.alexsullivan.reddit.models.RedditGifRecipe
import io.reactivex.Observable

internal interface UrlManipulator {
    fun matchesDomain(domain: String): Boolean
    fun modifyRedditItem(item: RedditGifRecipe): Observable<RedditGifRecipe>
}