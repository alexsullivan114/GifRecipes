package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeRepository {

    companion object {
        val default: GifRecipeRepository by lazy {
            GifRecipeRepositoryImpl(GifRecipeRegistrar.providers)
        }
    }

    fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String = ""): Observable<Response>

    data class Response(val recipes: List<GifRecipe>, val continuation: Observable<Response>)
}