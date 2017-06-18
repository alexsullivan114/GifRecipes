package alexsullivan.gifrecipes.categoryselection;

import alexsullivan.gifrecipes.Presenter
import alexsullivan.gifrecipes.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class CategorySelectionPresenterImpl() : CategorySelectionPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<CategorySelectionViewState> by lazy {
        BehaviorSubject.create<CategorySelectionViewState>()
    }

    override fun start() {

    }

    override fun stop() {
        super.stop()
        disposables.clear()
    }
}

interface CategorySelectionPresenter : Presenter<CategorySelectionViewState> {
    companion object {
        fun create(): CategorySelectionPresenter {
            return CategorySelectionPresenterImpl()
        }
    }
}

sealed class CategorySelectionViewState : ViewState {

}