package alexsullivan.gifrecipes.viewarchitecture

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class BaseFragment<T: ViewState, P: Presenter<T>>: Fragment() {
    val TAG: String = this.javaClass.simpleName

    protected var disposables: CompositeDisposable = CompositeDisposable()
    protected lateinit var presenter: P

    abstract fun accept(viewState: T)
    abstract fun acknowledge(error: Throwable)
    abstract fun initPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST") val viewModel: BaseViewModel<T, P> = ViewModelProviders.of(this).get(BaseViewModel::class.java) as BaseViewModel<T, P>
        if (viewModel.presenter == null) {
            viewModel.presenter = initPresenter()
        }
        viewModel.presenter?.let { presenter = it }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Subscribing to state stream")
        disposables.add(presenter.stateStream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { Log.d(TAG, "Successfully subscribed to state stream") }
                .subscribe({
                    Log.i(TAG, "Received View State: ${it.javaClass.simpleName}")
                    accept(it)
                }, { acknowledge(it) }))
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}