package alexsullivan.gifrecipes;

import android.graphics.Bitmap
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class GifRecipeViewerPresenterImpl(val url: String,
                                   val title: String) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    override fun start() {
        BitmapHolder.get(url)?.let {
            stateStream.onNext(GifRecipeViewerViewState.StaticImage(it, title))
        }
    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }
}

interface GifRecipeViewerPresenter : Presenter<GifRecipeViewerViewState> {
    companion object {
        fun create(url: String, title: String): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(url, title)
        }
    }
}

sealed class GifRecipeViewerViewState : ViewState {
    class StaticImage(val image: Bitmap, val title: String): GifRecipeViewerViewState()
    class Playing(val url: String, val title: String): GifRecipeViewerViewState()
}