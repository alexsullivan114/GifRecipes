package alexsullivan.gifrecipes.viewarchitecture

import android.arch.lifecycle.ViewModel

class BaseViewModel<T: ViewState, P: Presenter<T>>(var presenter: P? = null): ViewModel() {
    override fun onCleared() {
        presenter?.destroy()
    }
}