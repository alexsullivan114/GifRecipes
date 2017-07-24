package com.alexsullivan

import io.reactivex.Observable

internal class GifRecipeRepositoryImpl(val providers: List<GifRecipeProvider>): GifRecipeRepository {

    override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String, lastItem: String): Observable<GifRecipe> {
        if (providers.isEmpty()) {
            return Observable.empty()
        }
        // Each provider will be in charge of fetching 1/providers.size portion of the total desired
        // gifs. At some point we may want to introduce weighting, so that we could, for example,
        // weigh reddit gif recipes more heavily then, say, tasty gif recipes.
        val fetchCount = totalDesiredGifs / providers.size
        val observables = providers.map { it.consumeRecipes(fetchCount, searchTerm, lastItem) }
        return Observable.merge(observables)
    }
}