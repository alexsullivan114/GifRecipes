package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.toGifRecipeUI
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

class GifRecipeUiProviderImpl(private val repository: GifRecipeRepository,
                              private val favoriteCache: FavoriteCache,
                              private val searchTerm: String) : GifRecipeUiProvider {
  override fun fetchRecipes(count: Int, key: String): Observable<Pair<String?, List<GifRecipeUI>>> {
    return repository.consumeGifRecipes(count, searchTerm, key)
        .flatMap { recipe ->
          favoriteCache.isRecipeFavorited(recipe.id).toObservable()
              .map { recipe.pageKey to recipe.toGifRecipeUI(it) }
        }
        .toList()
        .flatMapObservable {
          if (it.isEmpty()) {
            Observable.just(null to emptyList<GifRecipeUI>())
          } else {
            val newKey = it.first().first
            val recipes = it.map { it.second }
            Observable.just(newKey to recipes)
          }
        }
  }
}