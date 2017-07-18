package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.viewarchitecture.BaseFragment
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*


class RecipeCategoryListFragment : BaseFragment<RecipeCategoryListFragmentViewState, RecipeCategoryListFragmentPresenter>() {

    companion object {
        val CATEGORY_KEY = "Category Key"
        fun build(category: Category): RecipeCategoryListFragment {
            val args = Bundle()
            args.putSerializable(CATEGORY_KEY, category)
            val fragment = RecipeCategoryListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initPresenter(): RecipeCategoryListFragmentPresenter {
        return RecipeCategoryListFragmentPresenter.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_category_list, container, false)
        val rnd = Random()
        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        view.setBackgroundColor(color)
        return view
    }

    override fun accept(viewState: RecipeCategoryListFragmentViewState) {
        TODO("not implemented")
    }

    override fun acknowledge(error: Throwable) {
        TODO("not implemented")
    }
}