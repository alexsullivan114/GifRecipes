package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.R
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_recipe_category_item.view.*

class RecipeCategoryListAdapter(val gifList: List<GifRecipeUI>,
                                private val clickCallback: ClickCallback): RecyclerView.Adapter<GifRecipeViewHolder>() {

    override fun onBindViewHolder(holder: GifRecipeViewHolder, position: Int) {
        val recipe = gifList[position]
        Glide.with(holder.itemView.context).load(recipe).into(holder.view.image)
        holder.view.title.text = recipe.title
        holder.view.setOnClickListener {
            clickCallback.recipeClicked(recipe, holder.view.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifRecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_item, parent, false)
        val viewHolder = GifRecipeViewHolder(view)
        return viewHolder
    }

    override fun getItemCount() = gifList.size

    interface ClickCallback {
        fun recipeClicked(recipe: GifRecipeUI, view: View)
    }
}

class GifRecipeViewHolder(val view: View): RecyclerView.ViewHolder(view)