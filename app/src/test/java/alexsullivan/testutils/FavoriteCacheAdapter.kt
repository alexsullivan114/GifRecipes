package alexsullivan.testutils

import alexsullivan.gifrecipes.favoriting.FavoriteCache
import com.alexsullivan.GifRecipe
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

abstract class FavoriteCacheAdapter : FavoriteCache {
    override fun isRecipeFavorited(id: String) = Single.never<Boolean>()
    override fun insertFavoriteRecipe(gifRecipe: GifRecipe) = Completable.complete()
    override fun deleteFavoriteRecipe(gifRecipe: GifRecipe) = Completable.complete()
    override fun favoriteStateChangedFlowable(): Flowable<Pair<GifRecipe, Boolean>> = Flowable.empty()
}