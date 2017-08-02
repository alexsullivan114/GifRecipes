package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.database.RecipeDatabase
import alexsullivan.gifrecipes.database.toFavorite
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import android.database.sqlite.SQLiteConstraintException
import com.alexsullivan.ImageType
import com.alexsullivan.logging.Logger
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class GifRecipeViewerPresenterImpl(private val gifRecipe: GifRecipeUI,
                                   private val cacheServer: CacheServer,
                                   private val recipeDatabase: RecipeDatabase,
                                   private val logger: Logger) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()
    var favorited = false

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    init {
        val buildPlayingState = fun(favorite: Boolean): GifRecipeViewerViewState {
            if (gifRecipe.imageType == ImageType.GIF) {
                return PlayingGif(cacheServer.get(gifRecipe.url), gifRecipe, favorite)
            } else {
                return PlayingVideo(cacheServer.get(gifRecipe.url), gifRecipe, favorite)
            }
        }

        val buildLoadingState = fun(favorite: Boolean, progress: Int): GifRecipeViewerViewState {
            return Loading(progress, gifRecipe, favorite)
        }

        disposables.add(recipeDatabase.gifRecipeDao().recipeIsFavorited(gifRecipe.id)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    favorited = it
                    if (stateStream.hasValue()) {
                        stateStream.onNext(stateStream.value.copyFavorite(it))
                    }
                }, {
                    throw it
                }))

        if (cacheServer.isCached(gifRecipe.url)) {
            stateStream.onNext(buildPlayingState(favorited))
        } else {
            stateStream.onNext(Preloading(gifRecipe, favorited))
            disposables.add(cacheServer.prefetch(gifRecipe.url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { stateStream.onNext(buildPlayingState(favorited)) }
                    .subscribe { stateStream.onNext(buildLoadingState(favorited, it)) })
        }
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun favoriteClicked() {
        // Save this favorite off into the database
        if (!favorited) {
            disposables.add(Completable.fromAction {
                recipeDatabase.gifRecipeDao().insertFavoriteRecipe(gifRecipe.toGifRecipe().toFavorite())
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, {
                // If we get a constraint exception its probably a repeat insert because of a race condition around quickly liking/unliking stuff.
                // We'll just ignore it.
                if (it !is SQLiteConstraintException) {
                    throw it
                } else {
                    logger.e(TAG, "SQLite error while saving favorite! ", it)
                }
            }))
        } else {
            disposables.add(Completable.fromAction {
                recipeDatabase.gifRecipeDao().deleteFavoriteRecipe(gifRecipe.toGifRecipe().toFavorite())
            }.subscribeOn(Schedulers.io())
                    .subscribe({}, {
                        throw it
                    }))
        }
    }
}

interface GifRecipeViewerPresenter : Presenter<GifRecipeViewerViewState> {
    companion object {
        fun create(gifRecipe: GifRecipeUI, cacheServer: CacheServer, recipeDatabase: RecipeDatabase, logger: Logger): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(gifRecipe, cacheServer, recipeDatabase, logger)
        }
    }

    fun favoriteClicked()
}

sealed class GifRecipeViewerViewState : ViewState {
    class Preloading(val recipe: GifRecipeUI, val favorite: Boolean) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean): GifRecipeViewerViewState {
            return Preloading(recipe, favorite)
        }
    }
    class Loading(val progress: Int, val recipe: GifRecipeUI, val favorite: Boolean) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean): GifRecipeViewerViewState {
            return Loading(progress, recipe, favorite)
        }
    }
    class PlayingVideo(val url: String, val recipe: GifRecipeUI, val favorite: Boolean) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean): GifRecipeViewerViewState {
            return PlayingVideo(url, recipe, favorite)
        }
    }
    class PlayingGif(val url: String, val recipe: GifRecipeUI, val favorite: Boolean) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean): GifRecipeViewerViewState {
            return PlayingGif(url, recipe, favorite)
        }
    }

    abstract fun copyFavorite(favorite: Boolean): GifRecipeViewerViewState
}