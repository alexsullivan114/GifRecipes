package alexsullivan.gifrecipes

import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity<T: ViewState>: AppCompatActivity() {
    val TAG: String = this.javaClass.simpleName

    protected var disposables: CompositeDisposable = CompositeDisposable()

    abstract val presenter: Presenter<T>
    abstract fun accept(viewState: T)
    abstract fun acknowledge(error: Throwable)

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
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        presenter.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
}

interface Presenter<T: ViewState> {

    val stateStream: Observable<T>
    fun start(){}
    fun stop(){}
    fun destroy(){}
}

interface ViewState