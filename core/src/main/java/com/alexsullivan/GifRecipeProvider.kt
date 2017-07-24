package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeProvider {

    val id: String

    fun consumeRecipes(limit: Int, searchTerm: String = "", pageKey: String = ""): Observable<GifRecipe>
}