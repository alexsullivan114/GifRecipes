package alexsullivan.gifrecipes.favoriting

import com.alexsullivan.GifRecipe
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface FavoriteCache {
    fun isRecipeFavorited(id: String): Single<Boolean>
    fun insertFavoriteRecipe(gifRecipe: GifRecipe): Completable
    fun deleteFavoriteRecipe(gifRecipe: GifRecipe): Completable
    fun favoriteStateChangedFlowable(): Flowable<Pair<GifRecipe, Boolean>>
}