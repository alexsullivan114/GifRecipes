package alexsullivan.gifrecipes.favoriting

import alexsullivan.gifrecipes.database.GifRecipeDao
import alexsullivan.gifrecipes.database.toGifRecipe
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.Observable

class FavoriteGifRecipeRepository(val gifRecipeDao: GifRecipeDao): GifRecipeRepository {
    override fun consumeGifRecipes(totalDesiredGifs: Int, searchTerm: String, pageKey: String): Observable<GifRecipe> {
        return Observable.fromCallable { gifRecipeDao.findFavorites() }
                .flatMap { Observable.fromIterable(it) }
                .map(FavoriteRecipe::toGifRecipe)
    }
}