package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.GifRecipeUiDiffCallback
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_recipe_category_item.view.*
import kotlin.properties.Delegates

class RecipeCategoryListAdapter(gifList: List<GifRecipeUI>,
                                private val clickCallback: ClickCallback): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var gifList: List<GifRecipeUI> by Delegates.observable(gifList) { _, oldValue, newValue ->
        DiffUtil.calculateDiff(GifRecipeUiDiffCallback(oldValue, newValue)).dispatchUpdatesTo(this)
    }

    var showBottomLoading: Boolean by Delegates.observable(false) {_, oldValue, newValue ->
        if (oldValue != newValue) {
            if (newValue) {
                notifyItemInserted(gifList.size)
            } else {
                notifyItemRemoved(gifList.size)
            }
        }
    }

    private val gifViewType = 0
    private val loadingViewType = 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is GifRecipeViewHolder -> {
                val recipe = gifList[position]
                Glide.with(holder.itemView.context).load(recipe.thumbnail).into(holder.view.image)
                holder.view.title.text = recipe.title
                holder.view.favorite.setLiked(recipe.favorite, false)
                holder.view.setOnClickListener {
                    clickCallback.recipeClicked(recipe, holder.view.image)
                }
                holder.view.favorite.setOnClickListener {
                    holder.view.favorite.setLiked(!holder.view.favorite.liked, true)
                    clickCallback.recipeFavoriteToggled(recipe.copy(favorite = holder.view.favorite.liked))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            gifViewType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_item, parent, false)
                val viewHolder = GifRecipeViewHolder(view)
                return viewHolder
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_loading, parent, false)
                val viewHolder = LoadingMoreViewHolder(view)
                return viewHolder
            }
        }
    }

    override fun getItemCount(): Int {
        var count = gifList.size
        if (showBottomLoading) {
            count += 1
        }

        return count
    }

    override fun getItemViewType(position: Int): Int {
        if (position == gifList.size && showBottomLoading) {
            return loadingViewType
        }

        return gifViewType
    }

    interface ClickCallback {
        fun recipeClicked(recipe: GifRecipeUI, view: View)
        fun recipeFavoriteToggled(recipe: GifRecipeUI)
    }

    class GifRecipeViewHolder(val view: View): RecyclerView.ViewHolder(view)
    class LoadingMoreViewHolder(val view: View): RecyclerView.ViewHolder(view)
}