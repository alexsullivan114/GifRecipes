package alexsullivan.gifrecipes.RecipesList

import alexsullivan.gifrecipes.R
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

class GifRecipesAdapter(val recipes: List<GifRecipeListItem>): RecyclerView.Adapter<GifRecipeViewHolder>() {

    override fun onBindViewHolder(holder: GifRecipeViewHolder?, position: Int) {
        val gifRecipe = recipes[position]
        holder?.setUrl(gifRecipe.url)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifRecipeViewHolder {
        val viewHolderLayout = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return GifRecipeViewHolder(viewHolderLayout)
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

}