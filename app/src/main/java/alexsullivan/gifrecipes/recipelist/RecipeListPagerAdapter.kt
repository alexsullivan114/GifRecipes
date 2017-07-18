package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class RecipeListPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return RecipeCategoryListFragment.build(categoryFromIndex(position))
    }

    override fun getCount() = Category.values().size
}