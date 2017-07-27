package alexsullivan.gifrecipes.search


import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.animation.CircularRevealTransition
import alexsullivan.gifrecipes.recipelist.RecipeCategoryListFragment
import alexsullivan.gifrecipes.utils.addFragment
import alexsullivan.gifrecipes.utils.textObservable
import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_category_search.*

class CategorySearchActivity : BaseActivity<CategorySearchViewState, CategorySearchPresenter>(), SearchProvider {

    companion object {
        fun buildIntent(context: Context): Intent {
            val intent = Intent(context, CategorySearchActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category_search)
        setEnterTransition()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        if (savedInstanceState == null) {
            addFragment(RecipeCategoryListFragment.build(""), R.id.container, "Testoreeno")
        }
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
    }

    override fun initPresenter(): CategorySearchPresenter {
        return CategorySearchPresenter.create()
    }

    override fun accept(viewState: CategorySearchViewState) {
        // no view states to handle.
    }

    override fun acknowledge(error: Throwable) {

    }

    override fun getObservableSource(): Observable<String> {
        return searchEditText.textObservable()
    }

    private fun setEnterTransition() {
        val circularReveal = CircularRevealTransition()
        circularReveal.duration = 500
        window.enterTransition = circularReveal
    }
}