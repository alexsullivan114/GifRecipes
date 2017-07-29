package alexsullivan.gifrecipes.components

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.util.AttributeSet
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

class StateAwareAppBarLayout : AppBarLayout {
    // TODO: Could be expanded at first - we can find that through the attribute set.
    private val stateSubject = BehaviorSubject.createDefault<State>(State.COLLAPSED)


    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    init {
        addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val newState = if (verticalOffset == 0) {
                State.EXPANDED
            } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                State.COLLAPSED
            } else {
                State.CHANGING
            }

            val oldState = stateSubject.value
            if (oldState != newState) {
                stateSubject.onNext(newState)
            }
        }
    }

    fun getStateObservable(): Flowable<State> {
        return stateSubject.toFlowable(BackpressureStrategy.DROP)
    }

    fun currentState(): State {
        return stateSubject.value
    }

    enum class State {
        EXPANDED, COLLAPSED, CHANGING
    }
}