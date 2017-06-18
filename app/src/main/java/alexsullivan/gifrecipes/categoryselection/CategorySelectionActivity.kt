package alexsullivan.gifrecipes.categoryselection;


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.R
import android.os.Bundle

class CategorySelectionActivity : BaseActivity<CategorySelectionViewState>() {
    override val presenter by lazy {
        CategorySelectionPresenter.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_category)
    }

    override fun accept(viewState: CategorySelectionViewState) {
        TODO("not implemented")
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}