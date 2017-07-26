package alexsullivan.gifrecipes.search;

import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class CategorySearchPresenterImpl() : CategorySearchPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<CategorySearchViewState> by lazy {
        BehaviorSubject.create<CategorySearchViewState>()
    }

    init {

    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }
}

interface CategorySearchPresenter : Presenter<CategorySearchViewState> {
    companion object {
        fun create(): CategorySearchPresenter {
            return CategorySearchPresenterImpl()
        }
    }
}

sealed class CategorySearchViewState : ViewState {

}