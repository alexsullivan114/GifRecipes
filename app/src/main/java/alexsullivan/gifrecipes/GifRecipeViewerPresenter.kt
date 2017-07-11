package alexsullivan.gifrecipes;

import alexsullivan.gifrecipes.GifRecipeViewerViewState.*
import alexsullivan.gifrecipes.cache.CacheServer
import android.graphics.Bitmap
import com.alexsullivan.ImageType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class GifRecipeViewerPresenterImpl(val url: String,
                                   val title: String,
                                   val imageType: ImageType,
                                   val cacheServer: CacheServer) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    override fun start() {
        val buildPlayingState = fun(): GifRecipeViewerViewState {
            if (imageType == ImageType.GIF) {
                return PlayingGif(cacheServer.get(url), BitmapHolder.get(url), title)
            } else {
                return PlayingVideo(cacheServer.get(url), BitmapHolder.get(url), title)
            }
        }

        if (cacheServer.isCached(url)) {
            stateStream.onNext(buildPlayingState())
        } else {
            val buildLoadingState = { progress: Int -> Loading(progress) }
            stateStream.onNext(Preloading(BitmapHolder.get(url), title))
            disposables.add(cacheServer.prefetch(url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { stateStream.onNext(buildPlayingState()) }
                    .subscribe { stateStream.onNext(buildLoadingState(it)) })
        }
    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }

    override fun destroy() {
        super.destroy()
        BitmapHolder.remove(url)
    }
}

interface GifRecipeViewerPresenter : Presenter<GifRecipeViewerViewState> {
    companion object {
        fun create(url: String, title: String, imageType: ImageType, cacheServer: CacheServer): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(url, title, imageType, cacheServer)
        }
    }
}

sealed class GifRecipeViewerViewState : ViewState {
    class Preloading(val image: Bitmap?, val title: String): GifRecipeViewerViewState()
    class Loading(val progress: Int): GifRecipeViewerViewState()
    class PlayingVideo(val url: String, val image: Bitmap?, val title: String): GifRecipeViewerViewState()
    class PlayingGif(val url: String, val image: Bitmap?, val title: String): GifRecipeViewerViewState()
}