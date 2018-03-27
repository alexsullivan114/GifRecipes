package alexsullivan.com.gfycat

import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeProvider.GifRecipeProviderResponse
import com.alexsullivan.ImageType
import io.reactivex.Observable

internal class GfycatRecipeProvider(private val service: GfycatService) : GifRecipeProvider {
  override val id: String
    get() = "GfycatRecipesProvider"

  override fun consumeRecipes(limit: Int, searchTerm: String, pageKey: String): Observable<GifRecipeProviderResponse> {
    return service
        .fetchRecipes(limit = limit, pageKey = pageKey)
        .map { response ->
          val filteredRecipes = response.gfycats.filter { it.title.toLowerCase().contains(searchTerm.toLowerCase()) }
          GifRecipeProviderResponse(filteredRecipes.toGifRecipes(),
              buildContinuation(searchTerm, response.cursor)) }
  }

  private fun buildContinuation(searchTerm: String,
                                pageKey: String): (Int) -> Observable<GifRecipeProviderResponse> =
      { newLimit -> consumeRecipes(newLimit, searchTerm, pageKey) }

  private fun List<GfycatRecipe>.toGifRecipes(): List<GifRecipe> =
      map { GifRecipe(it.mp4Url, it.mp4Url, it.thumb100PosterUrl, ImageType.VIDEO, it.title) }
}