package alexsullivan.gifrecipes

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.ImageType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class GifRecipeViewerPresenterImpl(private val gifRecipe: GifRecipeUI,
                                   private val cacheServer: CacheServer) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    init {
        val buildPlayingState = fun(): GifRecipeViewerViewState {
            if (gifRecipe.imageType == ImageType.GIF) {
                return PlayingGif(cacheServer.get(gifRecipe.url), gifRecipe)
            } else {
                return PlayingVideo(cacheServer.get(gifRecipe.url), gifRecipe)
            }
        }

        if (cacheServer.isCached(gifRecipe.url)) {
            stateStream.onNext(buildPlayingState())
        } else {
            val buildLoadingState = { progress: Int -> Loading(progress, gifRecipe) }
            stateStream.onNext(Preloading(gifRecipe))
            disposables.add(cacheServer.prefetch(gifRecipe.url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { stateStream.onNext(buildPlayingState()) }
                    .subscribe { stateStream.onNext(buildLoadingState(it)) })
        }
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }
}

interface GifRecipeViewerPresenter : Presenter<GifRecipeViewerViewState> {
    companion object {
        fun create(gifRecipe: GifRecipeUI, cacheServer: CacheServer): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(gifRecipe, cacheServer)
        }
    }
}

sealed class GifRecipeViewerViewState : ViewState {
    class Preloading(val recipe: GifRecipeUI): GifRecipeViewerViewState()
    class Loading(val progress: Int, val recipe: GifRecipeUI): GifRecipeViewerViewState()
    class PlayingVideo(val url: String, val recipe: GifRecipeUI): GifRecipeViewerViewState()
    class PlayingGif(val url: String, val recipe: GifRecipeUI): GifRecipeViewerViewState()
}