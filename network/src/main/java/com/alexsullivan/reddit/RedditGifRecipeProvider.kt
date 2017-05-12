package com.alexsullivan.reddit

import com.alexsullivan.reddit.models.RedditGifRecipe
import io.reactivex.Observable

/**
 * Created by Alexs on 5/10/2017.
 *
 * TODO: This should extend a paramaterized GifRecipeProvider interface that will ultimately be
 * the interface through which the gif recipes are accumulated.
 */
interface RedditGifRecipeProvider {

    companion object {}

    fun getRecipes(): Observable<RedditGifRecipe>
}