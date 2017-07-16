package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class RecipesListPresenterImpl() : RecipesListPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipesListViewState> by lazy {
        BehaviorSubject.create<RecipesListViewState>()
    }

    init {

    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }
}

interface RecipesListPresenter : Presenter<RecipesListViewState> {
    companion object {
        fun create(): RecipesListPresenter {
            return RecipesListPresenterImpl()
        }
    }
}

sealed class RecipesListViewState : ViewState {

}