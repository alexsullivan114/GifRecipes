package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.addTo
import alexsullivan.gifrecipes.utils.toGifRecipe
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
            // If we're a loading video we need to carryover whether we've transitioned to the new loading position
            is LoadingVideo -> {
                var hasTransitioned = false
                if (old is LoadingVideo) {
                    hasTransitioned = old.hasTransitioned
                } else if (old is TransitioningVideo) {
                    hasTransitioned = true
                }
                val updatedFavoriteInfo = new.copyFavorite(old.favoriteStatus(), old.favoriteLocked())
                return LoadingVideo(updatedFavoriteInfo.progress, updatedFavoriteInfo.recipe,
                    updatedFavoriteInfo.favoriteLocked(), updatedFavoriteInfo.url, hasTransitioned)
            }
            // If we're transitioning the video but we've already transitioned, ditch it.
            is TransitioningVideo -> {
                if (old is LoadingVideo && old.hasTransitioned) return old
                else return new.copyFavorite(old.favoriteStatus(), old.favoriteLocked())
            }
            // Otherwise just copy our favorite info and move on
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

    override fun videoStarted() {
        cacheServer.cacheProgress(gifRecipe.url)
            .subscribeOn(Schedulers.io())
            .take(1)
            .subscribe { pushValue(TransitioningVideo(gifRecipe, progress = it, url = cacheServer.get(gifRecipe.url))) }
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
            if (gifRecipe.imageType == ImageType.GIF) {
                return LoadingGif(progress, gifRecipe, url = cacheServer.get(gifRecipe.url))
            } else {
                return LoadingVideo(progress, gifRecipe, url = cacheServer.get(gifRecipe.url), hasTransitioned = false)
            }
        }

        if (cacheServer.isCached(gifRecipe.url)) {
            pushValue(buildPlayingState())
        } else {
            pushValue(Preloading(gifRecipe))
            cacheServer.cacheProgress(gifRecipe.url)
                .doOnComplete { pushValue(buildPlayingState()) }
                .doOnSubscribe { pushValue(buildLoadingState(0)) }
                .subscribeOn(Schedulers.io())
                .subscribe { pushValue(buildLoadingState(it)) }
                .addTo(disposables)
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
    abstract fun videoStarted()
}

sealed class GifRecipeViewerViewState : ViewState {
    class Preloading(val recipe: GifRecipeUI, val favoriteLocked: Boolean = true) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = Preloading(recipe.copy(favorite = favorite), favoriteLocked)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }
    class LoadingGif(val progress: Int, val recipe: GifRecipeUI, val favoriteLocked: Boolean = true, val url: String) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = LoadingGif(progress, recipe.copy(favorite = favorite), favoriteLocked, url)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }

    class LoadingVideo(val progress: Int, val recipe: GifRecipeUI, val favoriteLocked: Boolean = true, val url: String, val hasTransitioned: Boolean) : GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = LoadingVideo(progress, recipe.copy(favorite = favorite), favoriteLocked, url, hasTransitioned)
        override fun favoriteStatus() = recipe.favorite
        override fun favoriteLocked() = favoriteLocked
    }

    class TransitioningVideo(val recipe: GifRecipeUI, val favoriteLocked: Boolean = true, val progress: Int, val url: String): GifRecipeViewerViewState() {
        override fun copyFavorite(favorite: Boolean, favoriteLocked: Boolean) = TransitioningVideo(recipe.copy(favorite = favorite), favoriteLocked, progress, url)
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