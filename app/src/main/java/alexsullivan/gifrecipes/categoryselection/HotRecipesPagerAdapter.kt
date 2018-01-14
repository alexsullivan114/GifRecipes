package alexsullivan.gifrecipes.categoryselection

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.previewImageUrl
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_hot_recipe.view.*

class HotRecipesPagerAdapter(val gifList: List<GifRecipeUI>, val callback: RecipeAdapterCallback): PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = gifList.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewgroup = LayoutInflater.from(container.context).inflate(R.layout.adapter_hot_recipe, null)
        val recipe = gifList.get(position)
        Glide.with(container.context).asBitmap().load(recipe.previewImageUrl()).into(viewgroup.image)
        viewgroup.recipeTitle.text = recipe.title
        viewgroup.setOnClickListener { callback.recipeClicked(recipe, viewgroup.image) }
        container.addView(viewgroup)
        return viewgroup
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}

interface RecipeAdapterCallback {
    fun recipeClicked(gifRecipe: GifRecipeUI, previewImage: View)
}