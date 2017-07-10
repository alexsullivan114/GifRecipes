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
        if (cacheServer.isCached(url)) {
            stateStream.onNext(Playing(cacheServer.get(url), BitmapHolder.get(url), title, imageType))
        } else {
            val buildPlayingState = { progress: Int -> VideoLoading(progress) }
            stateStream.onNext(Preloading(BitmapHolder.get(url), title))
            disposables.add(cacheServer.prefetch(url)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete { stateStream.onNext(Playing(cacheServer.get(url), BitmapHolder.get(url), title, imageType)) }
                    .subscribe { stateStream.onNext(buildPlayingState(it)) })
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
    class VideoLoading(val progress: Int): GifRecipeViewerViewState()
    class Playing(val url: String, val image: Bitmap?, val title: String, val imageType: ImageType): GifRecipeViewerViewState()
}