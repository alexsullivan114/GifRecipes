package alexsullivan.gifrecipes.viewarchitecture

import io.reactivex.Observable

interface Presenter<T: ViewState> {
    val stateStream: Observable<T>
    fun destroy(){}
}