package alexsullivan.gifrecipes;

import android.graphics.Bitmap
import com.alexsullivan.ImageType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class GifRecipeViewerPresenterImpl(val url: String,
                                   val title: String,
                                   val imageType: ImageType) : GifRecipeViewerPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<GifRecipeViewerViewState> by lazy {
        BehaviorSubject.create<GifRecipeViewerViewState>()
    }

    override fun start() {
        stateStream.onNext(GifRecipeViewerViewState.Playing(url, BitmapHolder.get(url), title, imageType))
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
        fun create(url: String, title: String, imageType: ImageType): GifRecipeViewerPresenter {
            return GifRecipeViewerPresenterImpl(url, title, imageType)
        }
    }
}

sealed class GifRecipeViewerViewState : ViewState {
    class Playing(val url: String, val image: Bitmap?, val title: String, val imageType: ImageType): GifRecipeViewerViewState()
}