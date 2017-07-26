package alexsullivan.gifrecipes.recipelist


import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.search.CategorySearchActivity
import alexsullivan.gifrecipes.utils.pageChangeListener
import alexsullivan.gifrecipes.utils.str
import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.layout_recipes_list.*

class RecipeCategoryContainerActivity : BaseActivity<RecipesListViewState, RecipesListPresenter>() {

    lateinit var category: Category

    companion object {
        val CATEGORY_KEY = "CATEGORY_KEY"
        fun buildIntent(context: Context, category: Category): Intent {
            val intent = Intent(context, RecipeCategoryContainerActivity::class.java)
            intent.putExtra(CATEGORY_KEY, category)
            return intent
        }
    }

    override fun initPresenter(): RecipesListPresenter {
        return RecipesListPresenter.create(intent.getSerializableExtra(CATEGORY_KEY) as Category)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recipes_list)
        category = intent.getSerializableExtra(CATEGORY_KEY) as Category
        pager.adapter = RecipeListPagerAdapter(supportFragmentManager, applicationContext)
        pager.offscreenPageLimit = 1
        // Note: I'm not sure why this works - it seems ambiguous which method this would call in the
        // page change listener...
        pager.pageChangeListener {
            presenter.categorySelected(categoryFromIndex(it))
        }
        indicatorList.setHasFixedSize(true)
        indicatorList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorList.adapter = RecipeListIndicatorAdapter(presenter, indicatorList.layoutManager)
        // Postpone our enter transition until the recyclerview has fully rendered.
        supportPostponeEnterTransition()
        indicatorList.post { supportStartPostponedEnterTransition() }
        // Counter intuitive, but we're setting this enter shared element callback with regards to
        // entering the previous activity.
        setupEnterSharedTransitionCallback()
        searchEditText.setOnClickListener({
            startActivity(CategorySearchActivity.buildIntent(this), ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
        })
    }

    override fun finishAfterTransition() {
        val data = Intent()
                .putExtra(getString(R.string.category_extra_key), category)
                .putExtra(getString(R.string.category_original_extra_key), intent.getSerializableExtra(CATEGORY_KEY))
        setResult(Activity.RESULT_OK, data)
        super.finishAfterTransition()
    }

    override fun accept(viewState: RecipesListViewState) {
        when (viewState) {
            is RecipesListViewState.IndicatorState -> {
                category = viewState.selectedCategory
                (indicatorList.adapter as RecipeListIndicatorAdapter).selectedCategory = category
                if (pager.currentItem != indexFromCategory(category)) {
                    pager.currentItem = indexFromCategory(category)
                }
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }

    // Return the currently displayed viewholder if there is one, or null otherwise.
    private fun currentCategoryView(): View? {
        val holder = indicatorList.findViewHolderForLayoutPosition(indexFromCategory(category)) as? RecipeListIndicatorAdapter.RecipeListIndicatorViewHolder
        return holder?.image
    }

    private fun setupEnterSharedTransitionCallback() {
        setEnterSharedElementCallback(object: SharedElementCallback(){
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                val startingCategory = intent.getSerializableExtra(CATEGORY_KEY) as Category
                currentCategoryView()?.let {
                    names?.remove(str(startingCategory.transitionName))
                    sharedElements?.remove(str(startingCategory.transitionName))
                    names?.add(str(category.transitionName))
                    sharedElements?.put(str(category.transitionName), it)
                }
            }
        })
    }
}