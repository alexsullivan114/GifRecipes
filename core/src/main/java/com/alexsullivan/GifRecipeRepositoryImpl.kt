package com.alexsullivan

import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import io.reactivex.Observable

internal class GifRecipeRepositoryImpl(private val providers: List<GifRecipeProvider>) : GifRecipeRepository {

  override fun consumeGifRecipes(totalPageSize: Int, searchTerm: String): Observable<GifRecipeRepository.Response> {
    if (providers.isEmpty()) {
      return Observable.empty()
    }
    // Each provider will be in charge of fetching 1/providers.size portion of the total desired
    // gifs. The last provider will be tasked with picking up the remainder so we achieve the
    // expected page size.
    val sizes = pageSizeList(totalPageSize, providers.size)
    // Call consume recipes on each of our providers. This will net us a list of observables
    // that hold a GifRecipeRepositoryResponse
    val observables = providers.mapIndexed { index, gifRecipeProvider ->
      gifRecipeProvider.consumeRecipes(sizes[index], searchTerm)
    }
    // And now we need to merge that list of responses into a single observable.
    return mergeResponses(totalPageSize, observables)
  }

  private fun mergeResponses(requestedSize: Int, responses: List<Observable<GifRecipeProviderResponse>>): Observable<GifRecipeRepository.Response> {
    // Merge without propagating our error. This should produce an Observable<GifRecipeProviderResponse>
    // that will omit multiple responses.
    return Observable.mergeDelayError(responses)
        // If we get a response that has no recipes then that provider is shot and we shouldn't
        // use it anymore, so filter it our of our list.
        .filter { it.recipes.isNotEmpty() }
        // Bundle everything up into a List<GifRecipeProviderResponse>
        .toList()
        // If we've got nothing left, we're donezo!
        .filter { it.isNotEmpty() }
        // Mapping on our singular List<GifRecipeProviderResponse>
        .map { responseList ->
          // Take our multiple lists of recipes (since each provider response has a list of recipes)
          // and combine it into one list of recipes.
          val recipes = responseList.flatMap { it.recipes }.toMutableList()
          // Sort them by our creation date.
          recipes.sortByDescending { it.creationDate }
          // Now this is where shit gets funky. Each of our responses includes an Observable that
          // emits another response. We want to package up our responseList, which, again, is itself
          // a list of responses into one singular Response. Luckily, we have a function that does
          // just that! THE ONE WE'RE CURRENTLY IN. So our requested size stays the same - that's
          // the total number of recipes we want (though this is kind of confusing - is it just
          // for each "page" or is that the total period? I'm pretty sure its really total for each
          // page.) and for our list of responses, we just call the continuation provided in each
          // response with the total count divided by the number of responses we got! To make sure
          // we hit the our total requested size, the last provider will _also_ take on the
          // remainder difference.
          val sizes = pageSizeList(requestedSize, responseList.size)
          val continuations = responseList.mapIndexed { index, gifRecipeProviderResponse ->
            gifRecipeProviderResponse.continuation(sizes[index])
          }
          val observable = mergeResponses(requestedSize, continuations)
          GifRecipeRepository.Response(recipes, observable)
        }
        .toObservable()
  }

  private fun pageSizeList(totalPageSize: Int, numProviders: Int): List<Int> {
    val sizes = (1..numProviders)
        .map { totalPageSize / numProviders }
        .toMutableList()
    sizes[sizes.lastIndex] = totalPageSize / numProviders + totalPageSize % numProviders
    return sizes.toList()
  }
}