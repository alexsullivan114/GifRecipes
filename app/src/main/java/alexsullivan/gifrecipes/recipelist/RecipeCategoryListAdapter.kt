package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.GifRecipeUI
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.favoriting.FavoriteCache
import alexsullivan.gifrecipes.recipelist.RecipeCategoryListAdapter.GifRecipeViewHolder
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
    PagedListAdapter<GifRecipeUI, GifRecipeViewHolder>(buildConfig()) {

  override fun onBindViewHolder(holder: GifRecipeViewHolder, position: Int) {
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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifRecipeViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_category_item, parent, false)
    return GifRecipeViewHolder(view)
  }

  override fun onViewRecycled(holder: GifRecipeViewHolder) {
    super.onViewRecycled(holder)
    holder.disposeFavoriteStream()
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