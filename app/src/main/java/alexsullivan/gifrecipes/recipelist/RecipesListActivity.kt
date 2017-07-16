package alexsullivan.gifrecipes.recipelist


import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_recipes_list.*

class RecipesListActivity : BaseActivity<RecipesListViewState, RecipesListPresenter>() {

    companion object {
        val CATEGORY_KEY = "CATEGORY_KEY"
        fun buildIntent(context: Context, category: Category): Intent {
            val intent = Intent(context, RecipesListActivity::class.java)
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
        pager.adapter = RecipeListPagerAdapter(supportFragmentManager)
        indicatorList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorList.adapter = RecipeListIndicatorListAdapter(presenter, indicatorList.layoutManager)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Postpone our enter transition until the recyclerview has fully rendered.
        supportPostponeEnterTransition()
        indicatorList.post { supportStartPostponedEnterTransition() }
    }

    override fun accept(viewState: RecipesListViewState) {
        when (viewState) {
            is RecipesListViewState.IndicatorState -> {
//                indicatorList.scrollToPosition(indexFromCategory(viewState.selectedCategory))
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}