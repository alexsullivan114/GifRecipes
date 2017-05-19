package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeRepository {

    companion object {
        fun default(): GifRecipeRepository {
            return GifRecipeRepositoryImpl(GifRecipeRegistrar.providers)
        }
    }

    fun consumeGifRecipes(totalDesiredGifs: Int): Observable<GifRecipe>
}