package com.alexsullivan

import io.reactivex.Observable

internal class GifRecipeRepositoryImpl(private val providers: List<GifRecipeProvider>) : GifRecipeRepository {

  override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String): Observable<GifRecipeRepository.Response> {
    if (providers.isEmpty()) {
      return Observable.empty()
    }
    // Each provider will be in charge of fetching 1/providers.size portion of the total desired
    // gifs. At some point we may want to introduce weighting, so that we could, for example,
    // weigh reddit gif recipes more heavily then, say, tasty gif recipes.
    val fetchCount = totalDesiredGifs / providers.size
    val observables = providers.map { it.consumeRecipes(fetchCount, searchTerm) }
    return mergeResponses(observables)
  }

  private fun mergeResponses(responses: List<Observable<GifRecipeRepository.Response>>): Observable<GifRecipeRepository.Response> {
    return Observable.mergeDelayError(responses)
        .toList()
        .map { responseList ->
          val recipes = responseList.flatMap { it.recipes }.toMutableList()
          recipes.shuffle()
          val observable = mergeResponses(responseList.map { it.continuation })
          GifRecipeRepository.Response(recipes, observable)
        }
        .toObservable()
  }
}