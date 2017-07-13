package alexsullivan.gifrecipes

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity<T: ViewState, P:Presenter<T>>: AppCompatActivity() {
    val TAG: String = this.javaClass.simpleName

    protected var disposables: CompositeDisposable = CompositeDisposable()
    protected lateinit var presenter: P

    abstract fun accept(viewState: T)
    abstract fun acknowledge(error: Throwable)
    abstract fun initPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        val viewModel:BaseViewModel<T,P> = ViewModelProviders.of(this).get(BaseViewModel::class.java) as BaseViewModel<T, P>
        if (viewModel.presenter == null) {
            viewModel.presenter = initPresenter()
        }
        viewModel.presenter?.let { presenter = it }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Subscribing to state stream")
        disposables.add(presenter.stateStream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    Log.d(TAG, "Successfully subscribed to state stream")
                }
                .subscribe({
                    Log.i(TAG, "Received View State: ${it.javaClass.simpleName}")
                    accept(it) }, { acknowledge(it) }))
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}

interface Presenter<T: ViewState> {

    val stateStream: Observable<T>
    fun destroy(){}
}

interface ViewState

class BaseViewModel<T: ViewState, P: Presenter<T>>(var presenter: P? = null): ViewModel() {
    override fun onCleared() {
        presenter?.destroy()
    }
}