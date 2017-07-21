package alexsullivan.gifrecipes.categoryselection;

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipeRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class CategorySelectionPresenterImpl(repository: GifRecipeRepository) : CategorySelectionPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<CategorySelectionViewState> by lazy {
        BehaviorSubject.create<CategorySelectionViewState>()
    }

    init {
            // TODO: Make like a "top" thing in the reddit repo
        disposables.add(repository.consumeGifRecipes(15)
                .subscribeOn(Schedulers.io())
                // First push out our loading screen...
                .doOnSubscribe { stateStream.onNext(CategorySelectionViewState.FetchingGifs()) }
                .map {it.copy(url = it.url, imageType = it.imageType)}
                .map {GifRecipeUI(it.url, it.thumbnail, it.imageType, it.title)}
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {list: MutableList<GifRecipeUI> ->  stateStream.onNext(CategorySelectionViewState.GifList(list))}))

    }

    override fun destroy() {
        super.destroy()
        disposables.dispose()
    }

    override fun recipeClicked(gifRecipe: GifRecipeUI) {
//        BitmapHolder.put(hotGifRecipeItem.url, hotGifRecipeItem.bitmap)
    }
}

interface CategorySelectionPresenter : Presenter<CategorySelectionViewState> {
    companion object {
        fun create(gifRecipeRepository: GifRecipeRepository): CategorySelectionPresenter {
            return CategorySelectionPresenterImpl(gifRecipeRepository)
        }
    }

    fun recipeClicked(gifRecipe: GifRecipeUI)
}

sealed class CategorySelectionViewState : ViewState {
    class FetchingGifs: CategorySelectionViewState()
    class GifList(val gifRecipes: List<GifRecipeUI>): CategorySelectionViewState()
    class Error: CategorySelectionViewState()
}