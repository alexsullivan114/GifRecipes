package com.alexsullivan

import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import io.reactivex.Observable
import kotlin.math.min

fun createRecipe(
    url: String = "",
    id: String = "",
    thumbnail: String = "",
    imageType: ImageType = ImageType.VIDEO,
    title: String = "",
    sourceThumbnail: Int = 0,
    recipeSourceLink: String = "",
    creationDate: Long = 0): GifRecipe = GifRecipe(url, id, thumbnail, imageType, title, sourceThumbnail, recipeSourceLink, creationDate)

fun createProvider(
    id: String = "",
    totalRecipesInProvider: Int,
    creationDates: List<Long> = emptyList()
): GifRecipeProvider = object : GifRecipeProvider {
  override val id: String
    get() = id

  override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String) =
      consumeRecipes(limit, totalRecipesInProvider, creationDates)
}

fun consumeRecipes(pageSize: Int, totalRecipeCount: Int, creationDates: List<Long>): Observable<GifRecipeProviderResponse> {

  fun buildChain(remainingCount: Int): (Int) -> Observable<GifRecipeProviderResponse> {
    return { pageSize ->
      val items = (1..min(remainingCount, pageSize))
          .map {
            val creationDateIndex = (remainingCount - it)
            val creationDate = if (creationDates.size <= creationDateIndex) 0 else creationDates[creationDateIndex]
            createRecipe(id = "$remainingCount$it", creationDate = creationDate)
          }
      val updatedRemainingCount = remainingCount - pageSize
      val emptyContinuation = { _: Int -> Observable.empty<GifRecipeProviderResponse>() }
      val nextChainContinuation = buildChain(updatedRemainingCount)
      Observable.just(
          GifRecipeProviderResponse(
              items,
              if (updatedRemainingCount <= 0) emptyContinuation else nextChainContinuation
          )
      )
    }
  }

  return buildChain(totalRecipeCount)(pageSize)
}