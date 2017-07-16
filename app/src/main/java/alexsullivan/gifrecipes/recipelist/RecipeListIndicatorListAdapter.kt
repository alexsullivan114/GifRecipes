package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.adapter_recipe_indicator.view.*

class RecipeListIndicatorListAdapter: RecyclerView.Adapter<RecipeListIndicatorViewHolder>() {

    override fun onBindViewHolder(holder: RecipeListIndicatorViewHolder, position: Int) {
        var iconRes = 0
        var transitionNameRes = 0
        when(position) {
            0 -> {
                iconRes = Category.DESSERT.iconRes
                transitionNameRes = R.string.category_transition_image_dessert
            }
            1 -> {
                iconRes = Category.VEGETARIAN.iconRes
                transitionNameRes = R.string.category_transition_image_vegetarian
            }
            2 -> {
                iconRes = Category.VEGAN.iconRes
                transitionNameRes = R.string.category_transition_image_vegan
            }
            3 -> {
                iconRes = Category.CHICKEN.iconRes
                transitionNameRes = R.string.category_transition_image_chicken
            }
            4 -> {
                iconRes = Category.PORK.iconRes
                transitionNameRes = R.string.category_transition_image_pork
            }
            5 ->{
                iconRes = Category.SALMON.iconRes
                transitionNameRes = R.string.category_transition_image_salmon
            }
            else -> throw RuntimeException("Couldn't find icon for position $position")
        }

        holder.image.setImageResource(iconRes)
        holder.image.transitionName = holder.image.context.getString(transitionNameRes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListIndicatorViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_indicator, parent, false)
        return RecipeListIndicatorViewHolder(rootView)
    }

    override fun getItemCount() = Category.values().size
}

class RecipeListIndicatorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val image: CircleImageView = itemView.image
}