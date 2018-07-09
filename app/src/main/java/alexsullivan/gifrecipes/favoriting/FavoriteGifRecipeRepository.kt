package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.database.GifRecipeDao
import alexsullivan.gifrecipes.database.toGifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

class FavoriteGifRecipeRepository(val gifRecipeDao: GifRecipeDao) : GifRecipeRepository {
  override fun consumeGifRecipes(totalPageSize: Int, searchTerm: String): Observable<GifRecipeRepository.Response> {
    return Observable.fromCallable { gifRecipeDao.findFavorites() }
        .map { it.map(FavoriteRecipe::toGifRecipe) }
        .map { GifRecipeRepository.Response(it, Observable.empty()) }
  }
}