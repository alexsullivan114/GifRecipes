package alexsullivan.gifrecipes.recipelist;

import alexsullivan.gifrecipes.viewarchitecture.Presenter
import alexsullivan.gifrecipes.viewarchitecture.ViewState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject


class RecipeCategoryListFragmentPresenterImpl() : RecipeCategoryListFragmentPresenter {

    val disposables = CompositeDisposable()

    override val stateStream: BehaviorSubject<RecipeCategoryListFragmentViewState> by lazy {
        BehaviorSubject.create<RecipeCategoryListFragmentViewState>()
    }

    init {

    }

    override fun destroy() {
        super.destroy()
        disposables.clear()
    }
}

interface RecipeCategoryListFragmentPresenter : Presenter<RecipeCategoryListFragmentViewState> {
    companion object {
        fun create(): RecipeCategoryListFragmentPresenter {
            return RecipeCategoryListFragmentPresenterImpl()
        }
    }
}

sealed class RecipeCategoryListFragmentViewState : ViewState {

}