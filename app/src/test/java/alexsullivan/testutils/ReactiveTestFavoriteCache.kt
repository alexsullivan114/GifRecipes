package alexsullivan.testutils

import com.alexsullivan.GifRecipe
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject

class ReactiveTestFavoriteCache : FavoriteCacheAdapter() {

    val subject = PublishSubject.create<Pair<GifRecipe, Boolean>>()

    override fun insertFavoriteRecipe(gifRecipe: GifRecipe): Completable {
        subject.onNext(Pair(gifRecipe, true))
        return super.insertFavoriteRecipe(gifRecipe)
    }

    override fun deleteFavoriteRecipe(gifRecipe: GifRecipe): Completable {
        subject.onNext(Pair(gifRecipe, false))
        return super.insertFavoriteRecipe(gifRecipe)
    }

    override fun favoriteStateChangedFlowable() = subject.toFlowable(BackpressureStrategy.BUFFER)
}