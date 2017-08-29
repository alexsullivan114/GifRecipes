package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.utils.str
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class RecipeListPagerAdapter(fm: FragmentManager, val context: Context): FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val category = categoryFromIndex(position)
        return RecipeCategoryListFragment.build(context.str(category.displayName))
    }

    override fun getCount() = Category.values().size
}