package alexsullivan.gifrecipes.categoryselection;

import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import alexsullivan.utils.firstFrame
import android.media.MediaMetadataRetriever
import com.alexsullivan.GifRecipeRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class CategorySelectionPresenterImpl(val repository: GifRecipeRepository) : CategorySelectionPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<CategorySelectionViewState> by lazy {
        BehaviorSubject.create<CategorySelectionViewState>()
    }

    override fun start() {
        // TODO: Make like a "top" thing in the reddit repo
        disposables.add(repository.consumeGifRecipes(5)
                .subscribeOn(Schedulers.io())
                // First push out our loading screen...
                .doOnSubscribe { stateStream.onNext(CategorySelectionViewState.FetchingGifs()) }
                .map {
                    val metadataRetriever = MediaMetadataRetriever()
                    val bitmap = metadataRetriever.firstFrame(it.url)
                    HotGifRecipeItem(bitmap, it.url)
                }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { stateStream.onNext(CategorySelectionViewState.GifList(it))},
                        { stateStream.onNext(CategorySelectionViewState.Error())}))
    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }
}

interface CategorySelectionPresenter : Presenter<CategorySelectionViewState> {
    companion object {
        fun create(gifRecipeRepository: GifRecipeRepository): CategorySelectionPresenter {
            return CategorySelectionPresenterImpl(gifRecipeRepository)
        }
    }
}

sealed class CategorySelectionViewState : ViewState {
    class FetchingGifs: CategorySelectionViewState()
    class GifList(val gifRecipes: List<HotGifRecipeItem>): CategorySelectionViewState()
    class Error: CategorySelectionViewState()
}