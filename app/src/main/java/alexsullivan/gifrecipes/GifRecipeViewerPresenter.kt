package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.database.RecipeDatabase
import alexsullivan.gifrecipes.database.toFavorite
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.ImageType
import com.alexsullivan.logging.Logger
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


class GifRecipeViewerPresenterImpl(private val gifRecipe: GifRecipeUI,
                                   private val cacheServer: CacheServer,
                                   private val recipeDatabase: RecipeDatabase,
                                   private val logger: Logger) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    val favoriteStream: PublishSubject<Boolean> = PublishSubject.create()

    init {

        loadInitialPlayingState()
        loadInitialFavoriteState()
        subscribeToFavoriteStream()
    }

    private fun subscribeToFavoriteStream() {

        val saveFavorite = fun(favorited: Boolean) {
            if (favorited) {
                recipeDatabase.gifRecipeDao().insertFavoriteRecipe(gifRecipe.toGifRecipe().toFavorite())
            } else {
                recipeDatabase.gifRecipeDao().deleteFavoriteRecipe(gifRecipe.toGifRecipe().toFavorite())
            }
        }

        favoriteStream
                .observeOn(Schedulers.io())
                .subscribe {
                    logger.d(TAG, "Presenter favorite stream received value: $it")
                    saveFavorite(it)
                    propagateFavoriteInfo(false, it)
                }
    }

    private fun loadInitialFavoriteState() {
        // Load our initial favorited state.
        disposables.add(recipeDatabase.gifRecipeDao().recipeIsFavorited(gifRecipe.id)
                .first(false)
                .subscribeOn(Schedulers.io())
                .subscribe({ favorited ->
                    propagateFavoriteInfo(false, favorited)
                }, {
                    throw it
                }))
    }

    private fun loadInitialPlayingState() {

        val buildPlayingState = fun(): GifRecipeViewerViewState {
            if (gifRecipe.imageType == ImageType.GIF) {
                return carryOverFavoriteInfo(PlayingGif(cacheServer.get(gifRecipe.url), gifRecipe))
            } else {
                return carryOverFavoriteInfo(PlayingVideo(cacheServer.get(gifRecipe.url), gifRecipe))
            }
        }

        val buildLoadingState = fun(progress: Int): GifRecipeViewerViewState {
            return carryOverFavoriteInfo(Loading(progress, gifRecipe))
        }

        if (cacheServer.isCached(gifRecipe.url)) {
            stateStream.onNext(buildPlayingState())
        } else {
            stateStream.onNext(carryOverFavoriteInfo(Preloading(gifRecipe)))
            disposables.add(cacheServer.prefetch(gifRecipe.url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { stateStream.onNext(buildPlayingState()) }
                    .subscribe { stateStream.onNext(buildLoadingState(it)) })
        }
    }

    private fun carryOverFavoriteInfo(state: GifRecipeViewerViewState): GifRecipeViewerViewState {
        var returnState = state
        if (stateStream.hasValue()) {
            val lastState = stateStream.value
            returnState = state.copyFavorite(lastState.favorited, lastState.favoriteLocked)
        }

        return returnState
    }

    private fun propagateFavoriteInfo(favoriteLocked: Boolean, favorited: Boolean) {
        // If our state stream has emitted something, fetch that value and
        // re-emit it with our updated favorite information
        if (stateStream.hasValue()) {
            stateStream.onNext(stateStream.value.copyFavorite(favorited, favoriteLocked))
        } else {
            // Otherwise we should listen for the first element the stream emits
            // and re-emit it with the favorite information.
            disposables.add(stateStream.firstElement().subscribe {
                stateStream.onNext(it.copyFavorite(favorited, favoriteLocked))
            })
        }
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun favoriteClicked(isFavorited: Boolean) {
        favoriteStream.onNext(isFavorited)
    }
}

interface GifRecipeViewerPresenter : Presenter<GifRecipeViewerViewState> {
    companion object {
        fun create(gifRecipe: GifRecipeUI, cacheServer: CacheServer, recipeDatabase: RecipeDatabase, logger: Logger): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(gifRecipe, cacheServer, recipeDatabase, logger)
        }
    }

    fun favoriteClicked(isFavorited: Boolean)
}

sealed class GifRecipeViewerViewState(val favorited: Boolean = false, val favoriteLocked: Boolean = false) : ViewState {
    class Preloading(val recipe: GifRecipeUI, favorited: Boolean = false, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(favorited, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return Preloading(recipe, favorite, favoriteLocked)
        }
    }
    class Loading(val progress: Int, val recipe: GifRecipeUI, favorited: Boolean = false, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(favorited, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return Loading(progress, recipe, favorite, favoriteLocked)
        }
    }
    class PlayingVideo(val url: String, val recipe: GifRecipeUI, favorited: Boolean = false, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(favorited, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return PlayingVideo(url, recipe, favorite, favoriteLocked)
        }
    }
    class PlayingGif(val url: String, val recipe: GifRecipeUI, favorited: Boolean = false, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(favorited, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return PlayingGif(url, recipe, favorite, favoriteLocked)
        }
    }

    abstract fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState
}