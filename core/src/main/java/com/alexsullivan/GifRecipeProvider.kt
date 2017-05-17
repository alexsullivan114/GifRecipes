package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeProvider {
    fun consumeRecipes(limit: Int): Observable<GifRecipe>
}