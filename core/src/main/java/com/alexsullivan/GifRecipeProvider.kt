package com.alexsullivan

import io.reactivex.Observable

interface GifRecipeProvider {

    val id: String

    fun consumeRecipes(limit: Int, searchTerm: String = "", pageKey: String = ""): Observable<Response>

    data class Response(val recipes: List<GifRecipe>, val continuation: Observable<Response>)
}