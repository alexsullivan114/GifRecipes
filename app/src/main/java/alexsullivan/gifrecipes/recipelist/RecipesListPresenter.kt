package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class RecipesListPresenterImpl(category: Category) : RecipesListPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipesListViewState> by lazy {
        BehaviorSubject.create<RecipesListViewState>()
    }

    override val currentIndexObservable: BehaviorSubject<Category> = BehaviorSubject.createDefault(category)

    init {
        stateStream.onNext(RecipesListViewState.IndicatorState(category))
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun categorySelected(category: Category) {
        currentIndexObservable.onNext(category)
    }
}

interface RecipesListPresenter : Presenter<RecipesListViewState>, SelectedIndexProvider {
    companion object {
        fun create(category: Category): RecipesListPresenter {
            return RecipesListPresenterImpl(category)
        }
    }
}

sealed class RecipesListViewState : ViewState {
    // TODO: We probably don't want indicator list in its own view state - this is just to get everything
    // up and working along the same architecture we've been using.
    class IndicatorState(val selectedCategory: Category): RecipesListViewState()
}