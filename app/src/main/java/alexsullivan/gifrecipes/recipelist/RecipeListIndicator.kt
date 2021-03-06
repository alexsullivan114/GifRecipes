package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.show
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.adapter_recipe_indicator.view.*
import kotlin.properties.Delegates

class RecipeListIndicatorAdapter(val selectedIndexCallback: SelectedIndexCallback,
                                 private val layoutManager: RecyclerView.LayoutManager,
                                 private val showMask: Boolean): RecyclerView.Adapter<RecipeListIndicatorAdapter.RecipeListIndicatorViewHolder>() {

    var selectedCategory: Category by Delegates.observable(Category.DESSERT) {
        _, oldValue, newValue ->
            notifyItemChanged(indexFromCategory(newValue))
            notifyItemChanged(indexFromCategory(oldValue))
            layoutManager.scrollToPosition(indexFromCategory(newValue))
    }

    override fun onBindViewHolder(holder: RecipeListIndicatorViewHolder, position: Int) {
        val category = categoryFromIndex(position)
        val iconRes = category.iconRes

        holder.image.setImageResource(iconRes)
        holder.text.setText(category.displayName)
        holder.mask.show(category != selectedCategory && showMask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListIndicatorViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_indicator, parent, false)
        return RecipeListIndicatorViewHolder(rootView)
    }

    override fun getItemCount() = Category.values().size

    inner class RecipeListIndicatorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.image
        val mask: CircleImageView = itemView.mask
        val text: TextView = itemView.text

        init {
            image.setOnClickListener {
                selectedIndexCallback.categorySelected(categoryFromIndex(adapterPosition))
            }
        }

    }
}

interface SelectedIndexCallback {
    fun categorySelected(category: Category)
}