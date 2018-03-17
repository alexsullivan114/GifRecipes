package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeProvider {

    val id: String

    fun consumeRecipes(limit: Int, searchTerm: String = "", pageKey: String = ""): Observable<GifRecipeProviderResponse>

    data class GifRecipeProviderResponse(val recipes: List<GifRecipe>, val continuation: (Int) -> Observable<GifRecipeProviderResponse>)
}