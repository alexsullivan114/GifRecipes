package alexsullivan.gifrecipes.RecipesList

import alexsullivan.gifrecipes.BaseActivity
import alexsullivan.gifrecipes.R
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alexsullivan.GifRecipeRepository
import kotlinx.android.synthetic.main.layout_recipes.*


class RecipesListActivity : BaseActivity<RecipesListViewState>() {

    override val presenter by lazy {
        RecipesListPresenter.create(GifRecipeRepository.default)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_recipes)
        gifList.layoutManager = LinearLayoutManager(this)
    }

    override fun accept(viewState: RecipesListViewState) {
        when (viewState) {
            is RecipesListViewState.FullReload -> {
                gifList.visibility = View.GONE
                progress.visibility = View.VISIBLE
            }
            is RecipesListViewState.GifList -> {
                progress.visibility = View.GONE
                gifList.visibility = View.VISIBLE
                gifList.adapter = GifRecipesAdapter(viewState.gifRecipes)
            }
        }
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
