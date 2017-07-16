package alexsullivan.gifrecipes.recipelist

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class RecipeListPagerAdapter(fm: FragmentManager): FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return RecipeListFragment()
    }

    override fun getCount() = 6
}