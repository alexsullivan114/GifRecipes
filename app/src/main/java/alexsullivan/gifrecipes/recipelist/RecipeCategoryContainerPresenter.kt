package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class RecipesListPresenterImpl(category: Category) : RecipesListPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipesListViewState> by lazy {
        BehaviorSubject.create<RecipesListViewState>()
    }

    init {
        stateStream.onNext(RecipesListViewState.IndicatorState(category))
    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }

    override fun categorySelected(category: Category) {
        stateStream.onNext(RecipesListViewState.IndicatorState(category))
    }
}

interface RecipesListPresenter : Presenter<RecipesListViewState>, SelectedIndexCallback {
    companion object {
        fun create(category: Category): RecipesListPresenter {
            return RecipesListPresenterImpl(category)
        }
    }
}

sealed class RecipesListViewState : ViewState {
    class IndicatorState(val selectedCategory: Category): RecipesListViewState()
}