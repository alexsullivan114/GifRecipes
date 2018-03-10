package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.toGifRecipeUI
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

class GifRecipeUiProviderImpl(private val repository: GifRecipeRepository,
                              private val favoriteCache: FavoriteCache,
                              private val searchTerm: String) : GifRecipeUiProvider {
  override fun fetchRecipes(count: Int): Observable<Pair<Observable<GifRecipeProvider.Response>, List<GifRecipeUI>>> {

    return repository.consumeGifRecipes(count, searchTerm)
        .flatMap { response ->
          response.recipes.toGifRecipeUi().map { response.continuation to it}
        }
  }

  override fun fetchRecipes(continuation: Observable<GifRecipeProvider.Response>): Observable<Pair<Observable<GifRecipeProvider.Response>, List<GifRecipeUI>>> {
    return continuation
        .flatMap { response ->
          response.recipes.toGifRecipeUi().map { response.continuation to it }
        }
  }

  private fun List<GifRecipe>.toGifRecipeUi(): Observable<List<GifRecipeUI>> {
    return Observable.fromIterable(this)
        .flatMap { recipe ->
          favoriteCache.isRecipeFavorited(recipe.id).firstOrError().toObservable()
              .map { recipe.toGifRecipeUI(it) }
        }
        .toList()
        .toObservable()
  }
}