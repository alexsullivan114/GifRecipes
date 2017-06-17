package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeProvider {

    val id: String

    fun consumeRecipes(limit: Int, searchTerm: String = ""): Observable<GifRecipe>
}