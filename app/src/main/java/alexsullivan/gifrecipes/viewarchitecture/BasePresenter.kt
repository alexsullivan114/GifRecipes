package alexsullivan.gifrecipes.viewarchitecture

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

abstract class BasePresenter<T: ViewState> : Presenter<T> {
    private var lastValue: T? = null
    private val stateStreamSubject = BehaviorSubject.create<T>()

    override val stateStream: Observable<T>
        get() = stateStreamSubject

    protected fun pushValue(value: T) {
        val lastValue = lastValue

        if (lastValue != null) {
            val reducedValue = reduce(lastValue, value)
            reducedValue?.let {
                stateStreamSubject.onNext(reducedValue)
                this.lastValue = reducedValue
            }
        } else {
            stateStreamSubject.onNext(value)
            this.lastValue = value
        }

    }

    protected open fun reduce(old: T, new: T): T? {
        return new
    }
}