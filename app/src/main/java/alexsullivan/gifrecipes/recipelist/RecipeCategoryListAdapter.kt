package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.utils.previewImageUrl
import android.arch.paging.PagedListAdapter
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.adapter_recipe_category_item.view.*

class RecipeCategoryListAdapter(private val clickCallback: ClickCallback,
                                private val favoriteCache: FavoriteCache) :
    PagedListAdapter<GifRecipeUI, RecyclerView.ViewHolder>(buildConfig()) {

  private val gifViewType = 0
  private val loadingViewType = 1

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is GifRecipeViewHolder -> {
        val recipe = getItem(position)!!
        Glide.with(holder.itemView.context).asBitmap().load(recipe.previewImageUrl()).into(holder.view.image)
        holder.view.title.text = recipe.title
        holder.view.favorite.setLiked(recipe.favorite, true)
        holder.view.setOnClickListener {
          clickCallback.recipeClicked(recipe, holder.view.image)
        }
        holder.view.favorite.setOnClickListener {
          clickCallback.recipeFavoriteToggled(recipe.copy(favorite = !holder.view.favorite.liked))
        }
        holder.view.share.setOnClickListener {
          clickCallback.recipeShareClicked(recipe)
        }
        holder.bindToFavoriteStream(recipe)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    when (viewType) {
      gifViewType -> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_item, parent, false)
        return GifRecipeViewHolder(view)
      }
      else -> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_loading, parent, false)
        return LoadingMoreViewHolder(view)
      }
    }
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
    super.onViewRecycled(holder)
    when (holder) {
      is GifRecipeViewHolder -> holder.disposeFavoriteStream()
    }
  }

  interface ClickCallback {
    fun recipeClicked(recipe: GifRecipeUI, view: View)
    fun recipeFavoriteToggled(recipe: GifRecipeUI)
    fun recipeShareClicked(recipe: GifRecipeUI)
  }

  inner class GifRecipeViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private var favoriteDisposable: Disposable? = null

    fun bindToFavoriteStream(gifRecipeUI: GifRecipeUI) {
      favoriteDisposable = favoriteCache
          .isRecipeFavorited(gifRecipeUI.id)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe { view.favorite.setLiked(it, true) }
    }

    fun disposeFavoriteStream() {
      favoriteDisposable?.dispose()
    }
  }

  class LoadingMoreViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

private fun buildConfig(): ListAdapterConfig<GifRecipeUI> {
  return ListAdapterConfig.Builder<GifRecipeUI>()
      .setDiffCallback(object : DiffCallback<GifRecipeUI>() {
        override fun areItemsTheSame(oldItem: GifRecipeUI, newItem: GifRecipeUI) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GifRecipeUI, newItem: GifRecipeUI) =
            oldItem == newItem
      })
      .build()
}