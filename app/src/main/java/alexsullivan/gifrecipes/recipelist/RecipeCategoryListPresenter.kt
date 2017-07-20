package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import com.alexsullivan.GifRecipe
import com.alexsullivan.GifRecipeRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class RecipeCategoryListPresenterImpl(val category: Category,
                                      val searchTerm: String,
                                      val repository: GifRecipeRepository) : RecipeCategoryListPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipeCategoryListViewState> by lazy {
        BehaviorSubject.create<RecipeCategoryListViewState>()
    }

    init {
        repository.consumeGifRecipes(25, searchTerm)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { stateStream.onNext(RecipeCategoryListViewState.Loading()) }
                .toList()
                .subscribe({ result: List<GifRecipe> ->
                    stateStream.onNext(RecipeCategoryListViewState.RecipeList(result))
                })

    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }
}

interface RecipeCategoryListPresenter : Presenter<RecipeCategoryListViewState> {
    companion object {
        fun create(category: Category, searchTerm: String, repository: GifRecipeRepository): RecipeCategoryListPresenter {
            return RecipeCategoryListPresenterImpl(category, searchTerm, repository)
        }
    }
}

sealed class RecipeCategoryListViewState : ViewState {
    class Loading : RecipeCategoryListViewState()
    class RecipeList(val recipes: List<GifRecipe>): RecipeCategoryListViewState()
}