package alexsullivan.gifrecipes.categoryselection;

import alexsullivan.gifrecipes.BitmapHolder
import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import alexsullivan.gifrecipes.utils.firstFrame
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
        if (!stateStream.hasValue()) {
            // TODO: Make like a "top" thing in the reddit repo
            disposables.add(repository.consumeGifRecipes(15)
                    .subscribeOn(Schedulers.io())
                    // First push out our loading screen...
                    .doOnSubscribe { stateStream.onNext(CategorySelectionViewState.FetchingGifs()) }
                    .map {it.copy(url = it.url, imageType = it.imageType)}
                    .map { HotGifRecipeItem(it.firstFrame(), it.url, it.imageType, it.title) }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { stateStream.onNext(CategorySelectionViewState.GifList(it))},
                            { stateStream.onNext(CategorySelectionViewState.Error())}))
        }
    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }

    override fun recipeClicked(hotGifRecipeItem: HotGifRecipeItem) {
        BitmapHolder.put(hotGifRecipeItem.link, hotGifRecipeItem.bitmap)
    }
}

interface CategorySelectionPresenter : Presenter<CategorySelectionViewState> {
    companion object {
        fun create(gifRecipeRepository: GifRecipeRepository): CategorySelectionPresenter {
            return CategorySelectionPresenterImpl(gifRecipeRepository)
        }
    }

    fun recipeClicked(hotGifRecipeItem: HotGifRecipeItem)
}

sealed class CategorySelectionViewState : ViewState {
    class FetchingGifs: CategorySelectionViewState()
    class GifList(val gifRecipes: List<HotGifRecipeItem>): CategorySelectionViewState()
    class Error: CategorySelectionViewState()
}