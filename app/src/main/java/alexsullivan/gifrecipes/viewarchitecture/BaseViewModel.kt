package alexsullivan.gifrecipes.viewarchitecture

import android.arch.lifecycle.ViewModel
import android.util.Log

class BaseViewModel<T: ViewState, P: Presenter<T>>(var presenter: P? = null): ViewModel() {
    val TAG = BaseViewModel::class.java.simpleName
    override fun onCleared() {
        Log.d(TAG, "On cleared call for presenter $presenter")
        presenter?.destroy()
    }
}