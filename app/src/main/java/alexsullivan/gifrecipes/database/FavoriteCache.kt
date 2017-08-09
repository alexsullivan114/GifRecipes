package alexsullivan.gifrecipes.database

import com.alexsullivan.GifRecipe
import io.reactivex.Completable
import io.reactivex.Single

interface FavoriteCache {
    fun isRecipeFavorited(id: String): Single<Boolean>
    fun insertFavoriteRecipe(gifRecipe: GifRecipe): Completable
    fun deleteFavoriteRecipe(gifRecipe: GifRecipe): Completable
}