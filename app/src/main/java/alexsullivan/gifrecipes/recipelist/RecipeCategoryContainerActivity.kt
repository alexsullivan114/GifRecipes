package alexsullivan.gifrecipes.recipelist


import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.components.StateAwareAppBarLayout
import alexsullivan.gifrecipes.search.CategorySearchActivity
import alexsullivan.gifrecipes.utils.animatedSetImage
import alexsullivan.gifrecipes.utils.castedAdapter
import alexsullivan.gifrecipes.utils.pageChangeListener
import alexsullivan.gifrecipes.utils.str
import alexsullivan.gifrecipes.viewarchitecture.BaseActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.layout_recipes_list.*

class RecipeCategoryContainerActivity : BaseActivity<RecipesListViewState, RecipesListPresenter>(), SelectedIndexCallback {

    lateinit var category: Category

    companion object {
        val CATEGORY_KEY = "CATEGORY_KEY"
        fun buildIntent(context: Context, category: Category): Intent {
            val intent = Intent(context, RecipeCategoryContainerActivity::class.java)
            intent.putExtra(CATEGORY_KEY, category)
            return intent
        }
    }

    override fun initPresenter() = RecipesListPresenter.create(intent.getSerializableExtra(CATEGORY_KEY) as Category)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recipes_list)
        category = intent.getSerializableExtra(CATEGORY_KEY) as Category
        presenter.categorySelected(category)
        categoryImage.transitionName = str(category.transitionName)
        categoryImage.setImageResource(category.iconRes)
        categoryTitle.setText(category.displayName)
        pager.adapter = RecipeListPagerAdapter(supportFragmentManager, applicationContext)
        pager.currentItem = indexFromCategory(category)
        pager.offscreenPageLimit = 0
        indicatorList.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false)
        indicatorList.adapter = RecipeListIndicatorAdapter(this, indicatorList.layoutManager, true)
        indicatorList.castedAdapter<RecipeListIndicatorAdapter>().selectedCategory = category
        appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalRange = appBarLayout.totalScrollRange
            val percentScrolled = -1 * (verticalOffset / totalRange.toFloat())
            dropDownArrow.rotation = 180 * (1 - percentScrolled)
        }
        toolbar.setOnClickListener {
            appBarLayout.setExpanded(appBarLayout.currentState() == StateAwareAppBarLayout.State.COLLAPSED)
        }
        // Note: I'm not sure why this works - it seems ambiguous which method this would call in the
        // page change listener...
        pager.pageChangeListener {
            presenter.categorySelected(categoryFromIndex(it))
        }
        // Counter intuitive, but we're setting this enter shared element callback with regards to
        // entering the previous activity.
        setupEnterSharedTransitionCallback()
        searchEditText.setOnClickListener({
            val rect = Rect()
            it.getGlobalVisibleRect(rect)
            startActivity(CategorySearchActivity.buildIntent(this, rect.exactCenterX(), rect.exactCenterY()), ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle())
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
                if (viewState.selectedCategory != category) {
                    category = viewState.selectedCategory
                    pager.currentItem = indexFromCategory(category)
                    categoryImage.transitionName = str(category.transitionName)
                    categoryImage.animatedSetImage(category.iconRes)
                    categoryTitle.setText(category.displayName)
                    indicatorList.castedAdapter<RecipeListIndicatorAdapter>().selectedCategory = category
                }
            }
        }
    }

    override fun categorySelected(category: Category) {
        presenter.categorySelected(category)
    }

    private fun setupEnterSharedTransitionCallback() {
        setEnterSharedElementCallback(object: SharedElementCallback(){
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                val startingCategory = intent.getSerializableExtra(CATEGORY_KEY) as Category
                names?.remove(str(startingCategory.transitionName))
                sharedElements?.remove(str(startingCategory.transitionName))
                names?.add(str(category.transitionName))
                sharedElements?.put(str(category.transitionName), categoryImage)
            }
        })
    }
}