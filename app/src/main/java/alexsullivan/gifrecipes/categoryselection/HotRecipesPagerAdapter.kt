package alexsullivan.gifrecipes.categoryselection

import alexsullivan.gifrecipes.R
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pager_hot_recipe.view.*

class HotRecipesPagerAdapter(val gifList: List<HotGifRecipeItem>, val callback: HotRecipeAdapterCallback): PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?) = view == `object`

    override fun getCount() = gifList.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewgroup = LayoutInflater.from(container.context).inflate(R.layout.pager_hot_recipe, null)
        val recipe = gifList.get(position)
        viewgroup.image.setImageBitmap(recipe.bitmap)
        viewgroup.recipeTitle.text = recipe.title
        viewgroup.setOnClickListener { callback.recipeClicked(recipe, viewgroup.image, viewgroup.recipeTitle) }
        container.addView(viewgroup)
        return viewgroup
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }
}

interface HotRecipeAdapterCallback {
    fun recipeClicked(hotGifRecipeItem: HotGifRecipeItem, previewImage: View, titleTextView: View)
}