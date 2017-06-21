package alexsullivan.gifrecipes

import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity<T: ViewState>: AppCompatActivity() {

    protected var disposables: CompositeDisposable = CompositeDisposable()

    abstract val presenter: Presenter<T>
    abstract fun accept(viewState: T)
    abstract fun acknowledge(error: Throwable)

    override fun onStart() {
        super.onStart()
        disposables.add(presenter.stateStream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("View Architecture", "Received View State: ${it.toString()}")
                    accept(it) }, { acknowledge(it) }))
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
        presenter.stop()
    }
}

interface Presenter<T: ViewState> {

    val stateStream: Observable<T>
    fun start(){}
    fun stop(){}
}

interface ViewState