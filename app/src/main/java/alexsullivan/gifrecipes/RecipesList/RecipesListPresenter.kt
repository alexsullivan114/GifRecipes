package alexsullivan.gifrecipes.RecipesList

import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class RecipesListPresenterImpl(val gifRecipeRepository: GifRecipeRepository): RecipesListPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipesListViewState> by lazy {
        BehaviorSubject.create<RecipesListViewState>()
    }

    override fun start() {
        disposables.add(gifRecipeRepository.consumeGifRecipes(25)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // First push out our loading screen...
                .doOnSubscribe { stateStream.onNext(RecipesListViewState.FullReload()) }
                .map { GifRecipeListItem(it.url) }
                .toList()
                .subscribe(
                        { stateStream.onNext(RecipesListViewState.GifList(it))},
                        { stateStream.onNext(RecipesListViewState.Error())}))
    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }
}

interface RecipesListPresenter: Presenter<RecipesListViewState> {
    companion object {
        fun create(gifRecipeRepository: GifRecipeRepository): RecipesListPresenter {
            return RecipesListPresenterImpl(gifRecipeRepository)
        }
    }
}

sealed class RecipesListViewState: ViewState {
    class FullReload: RecipesListViewState()
    class GifList(val gifRecipes: List<GifRecipeListItem>): RecipesListViewState()
    class FetchingMore(val gifRecipes: List<GifRecipe>): RecipesListViewState()
    class Error(): RecipesListViewState()
}