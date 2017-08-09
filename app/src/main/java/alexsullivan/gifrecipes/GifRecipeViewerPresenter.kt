package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.database.FavoriteCache
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
                                   private val favoriteCache: FavoriteCache,
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
                favoriteCache.insertFavoriteRecipe(gifRecipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
            } else {
                favoriteCache.deleteFavoriteRecipe(gifRecipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
            }
        }

        favoriteStream
                .observeOn(Schedulers.io())
                .subscribe {
                    saveFavorite(it)
                    propagateFavoriteInfo(false, it)
                }
    }

    private fun loadInitialFavoriteState() {
        // Load our initial favorited state.
        disposables.add(favoriteCache.isRecipeFavorited(gifRecipe.id)
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
            returnState = state.copyFavorite(lastState.recipe.favorite, lastState.favoriteLocked)
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
        fun create(gifRecipe: GifRecipeUI, cacheServer: CacheServer, favoriteCache: FavoriteCache, logger: Logger): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(gifRecipe, cacheServer, favoriteCache, logger)
        }
    }

    fun favoriteClicked(isFavorited: Boolean)
}

sealed class GifRecipeViewerViewState(val recipe: GifRecipeUI, val favoriteLocked: Boolean = false) : ViewState {
    class Preloading(recipe: GifRecipeUI, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(recipe, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return Preloading(recipe.copy(favorite = favorite), favoriteLocked)
        }
    }
    class Loading(val progress: Int, recipe: GifRecipeUI, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(recipe, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return Loading(progress, recipe.copy(favorite = favorite), favoriteLocked)
        }
    }
    class PlayingVideo(val url: String, recipe: GifRecipeUI, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(recipe, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return PlayingVideo(url, recipe.copy(favorite = favorite), favoriteLocked)
        }
    }
    class PlayingGif(val url: String, recipe: GifRecipeUI, favoriteLocked: Boolean = true) : GifRecipeViewerViewState(recipe, favoriteLocked) {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState {
            return PlayingGif(url, recipe.copy(favorite = favorite), favoriteLocked)
        }
    }

    abstract fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState
}