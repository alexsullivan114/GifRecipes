package alexsullivan.gifrecipes.viewarchitecture

import alexsullivan.gifrecipes.R
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

abstract class BaseActivity<T: ViewState, P: Presenter<T>>: AppCompatActivity() {
    val TAG: String = this.javaClass.simpleName

    protected var disposables: CompositeDisposable = CompositeDisposable()
    protected lateinit var presenter: P

    abstract fun accept(viewState: T)
    @Deprecated("Acknowledge is no longer used")
    open fun acknowledge(error: Throwable) {

    }
    abstract fun initPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        val viewModel: BaseViewModel<T, P> = ViewModelProviders.of(this).get(BaseViewModel::class.java) as BaseViewModel<T, P>
        if (viewModel.presenter == null) {
            viewModel.presenter = initPresenter()
        }
        viewModel.presenter?.let { presenter = it }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        bindToolbar()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        bindToolbar()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        bindToolbar()
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
                    accept(it) }))
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "Unsubscribing from state stream")
        disposables.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishAfterTransition()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun bindToolbar() {
        val toolbar = findViewById(R.id.toolbar)
        toolbar?.let {
            try {
                setSupportActionBar(toolbar as Toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } catch (ignored: ClassCastException) {
                // Do nothing if the view with id of toolbar isnt actually a toolbar.
            }
        }
    }
}

