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
        indicator_list.adapter = RecipeListIndicatorListAdapter(presenter)
        indicator_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportPostponeEnterTransition()
        indicator_list.post { supportStartPostponedEnterTransition() }
    }

    override fun accept(viewState: RecipesListViewState) {
        TODO("not implemented")
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}