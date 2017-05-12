package com.alexsullivan.reddit

import com.alexsullivan.reddit.models.RedditGifRecipe
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * Created by Alexs on 5/10/2017.
 */
interface RedditService {
    object statics {
        val baseUrl = "https://oauth.reddit.com/"
    }

    @GET("r/gifrecipes/hot")
    fun hotRecipes(): Observable<RedditGifRecipe>
}