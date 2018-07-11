package com.alexsullivan

import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import io.reactivex.Observable
import kotlin.math.max
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
    totalRecipesInProvider: Int
): GifRecipeProvider = object : GifRecipeProvider {
  override val id: String
    get() = id

  override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String) =
      consumeRecipes(limit, searchTerm, pageKey, totalRecipesInProvider)
}

// TODO: Limit might be a bit misleading here. Man I sure should comment stuff...
fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String, totalRecipeCount: Int = 0): Observable<GifRecipeProviderResponse> {

  fun buildChain(remainingCount: Int): (Int) -> Observable<GifRecipeProviderResponse> {
    if (remainingCount == 0) {
      return {
        Observable.empty()
      }
    }

    // TODO: We need to actually care about the integer value passed through here
    return {
      val items = (1..min(remainingCount, it))
          .map { createRecipe(id = it.toString()) }
      Observable.just(
          GifRecipeProviderResponse(
              items,
              buildChain(max(0, remainingCount - it))
          )
      )
    }
  }

  return buildChain(totalRecipeCount)(limit)
}