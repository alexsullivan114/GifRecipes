package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.database.FavoriteCache
import alexsullivan.gifrecipes.viewarchitecture.BasePresenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.ImageType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class GifRecipeViewerPresenterImpl(private val gifRecipe: GifRecipeUI,
                                   private val cacheServer: CacheServer,
                                   private val favoriteCache: FavoriteCache): GifRecipeViewerPresenter() {

    val disposables = CompositeDisposable()

    val favoriteStream: PublishSubject<Boolean> = PublishSubject.create()

    init {

        loadInitialPlayingState()
        loadInitialFavoriteState()
        subscribeToFavoriteStream()
    }

    override fun reduce(old: GifRecipeViewerViewState, new: GifRecipeViewerViewState): GifRecipeViewerViewState {
        when (new) {
            // If a favorited came through our stream, favoriting is always unlocked.
            is Favorited -> return old.copyFavorite(new.favorite, false)
            // If its not a new favoriting status we just carryover the old favorite info.
            else -> return new.copyFavorite(old.favoriteStatus(), old.favoriteLocked())
        }
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun favoriteClicked(isFavorited: Boolean) {
        favoriteStream.onNext(isFavorited)
    }

    private fun subscribeToFavoriteStream() {
        favoriteStream.observeOn(Schedulers.io()).subscribe {
            if (it) {
                favoriteCache.insertFavoriteRecipe(gifRecipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
            } else {
                favoriteCache.deleteFavoriteRecipe(gifRecipe.toGifRecipe()).subscribeOn(Schedulers.io()).subscribe()
            }
            pushValue(Favorited(it))
        }
    }

    private fun loadInitialFavoriteState() {
        // Load our initial favorited state.
        disposables.add(favoriteCache.isRecipeFavorited(gifRecipe.id)
                .subscribeOn(Schedulers.io())
                .subscribe({ favorited ->
                    pushValue(Favorited(favorited))
                }, {
                    throw it
                }))
    }

    private fun loadInitialPlayingState() {

        val buildPlayingState = fun(): GifRecipeViewerViewState {
            if (gifRecipe.imageType == ImageType.GIF) {
                return PlayingGif(cacheServer.get(gifRecipe.url), gifRecipe)
            } else {
                return PlayingVideo(cacheServer.get(gifRecipe.url), gifRecipe)
            }
        }

        val buildLoadingState = fun(progress: Int): GifRecipeViewerViewState {
            return Loading(progress, gifRecipe)
        }

        if (cacheServer.isCached(gifRecipe.url)) {
            pushValue(buildPlayingState())
        } else {
            pushValue(Preloading(gifRecipe))
            disposables.add(cacheServer.prefetch(gifRecipe.url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { pushValue(buildPlayingState()) }
                    .subscribe { pushValue(buildLoadingState(it)) })
        }
    }
}

abstract class GifRecipeViewerPresenter : BasePresenter<GifRecipeViewerViewState>() {
    companion object {
        fun create(gifRecipe: GifRecipeUI, cacheServer: CacheServer, favoriteCache: FavoriteCache): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(gifRecipe, cacheServer, favoriteCache)
        }
    }

    abstract fun favoriteClicked(isFavorited: Boolean)
}

sealed class GifRecipeViewerViewState : ViewState {
    class Preloading(val recipe: GifRecipeUI, val favoriteLocked: Boolean = true) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = Preloading(recipe.copy(favorite = favorite), favoriteLocked)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }
    class Loading(val progress: Int, val recipe: GifRecipeUI, val favoriteLocked: Boolean = true) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = Loading(progress, recipe.copy(favorite = favorite), favoriteLocked)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }
    class PlayingVideo(val url: String, val recipe: GifRecipeUI, val favoriteLocked: Boolean = true) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = PlayingVideo(url, recipe.copy(favorite = favorite), favoriteLocked)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }
    class PlayingGif(val url: String, val recipe: GifRecipeUI, val favoriteLocked: Boolean = true) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = PlayingGif(url, recipe.copy(favorite = favorite), favoriteLocked)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }
    class Favorited(val favorite: Boolean): GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = Favorited(favorite)
        override fun favoriteStatus() = favorite
        override fun favoriteLocked() = false
    }

    abstract fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean): GifRecipeViewerViewState
    abstract fun favoriteStatus(): Boolean
    abstract fun favoriteLocked(): Boolean
}