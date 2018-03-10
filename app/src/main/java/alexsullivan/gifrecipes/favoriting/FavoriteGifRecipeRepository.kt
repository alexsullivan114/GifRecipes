package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.database.GifRecipeDao
import alexsullivan.gifrecipes.database.toGifRecipe
import com.alexsullivan.GifRecipeProvider
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

class FavoriteGifRecipeRepository(val gifRecipeDao: GifRecipeDao) : GifRecipeRepository {
  override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String): Observable<GifRecipeProvider.Response> {
    return Observable.fromCallable { gifRecipeDao.findFavorites() }
        .map { it.map(FavoriteRecipe::toGifRecipe) }
        .map { GifRecipeProvider.Response(it, Observable.empty()) }
  }
}