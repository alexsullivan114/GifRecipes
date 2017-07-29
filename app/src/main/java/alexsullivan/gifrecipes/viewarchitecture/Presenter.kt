package alexsullivan.gifrecipes.viewarchitecture

import io.reactivex.Observable

interface Presenter<T: ViewState> {
    val TAG: String
        get() = javaClass.simpleName
    val stateStream: Observable<T>
    fun destroy(){}
}