package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category
import alexsullivan.gifrecipes.R
import alexsullivan.gifrecipes.utils.show
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.adapter_recipe_indicator.view.*
import kotlin.properties.Delegates

class RecipeListIndicatorListAdapter(val selectedIndexCategory: SelectedIndexCallback,
                                     val layoutManager: RecyclerView.LayoutManager): RecyclerView.Adapter<RecipeListIndicatorListAdapter.RecipeListIndicatorViewHolder>() {

    private var selectedCategory: Category by Delegates.observable(Category.DESSERT) {
        _, oldValue, newValue ->
            notifyItemChanged(indexFromCategory(newValue))
            notifyItemChanged(indexFromCategory(oldValue))
            layoutManager.scrollToPosition(indexFromCategory(newValue))
    }

    init {
        selectedIndexCategory.currentIndexObservable.subscribe { selectedCategory = it }
    }

    override fun onBindViewHolder(holder: RecipeListIndicatorViewHolder, position: Int) {
        var iconRes = 0
        var transitionNameRes = 0
        val category = categoryFromIndex(position)
        when(category) {
            Category.DESSERT -> {
                iconRes = Category.DESSERT.iconRes
                transitionNameRes = R.string.category_transition_image_dessert
            }
            Category.VEGETARIAN -> {
                iconRes = Category.VEGETARIAN.iconRes
                transitionNameRes = R.string.category_transition_image_vegetarian
            }
            Category.VEGAN -> {
                iconRes = Category.VEGAN.iconRes
                transitionNameRes = R.string.category_transition_image_vegan
            }
            Category.CHICKEN -> {
                iconRes = Category.CHICKEN.iconRes
                transitionNameRes = R.string.category_transition_image_chicken
            }
            Category.PORK -> {
                iconRes = Category.PORK.iconRes
                transitionNameRes = R.string.category_transition_image_pork
            }
            Category.SALMON ->{
                iconRes = Category.SALMON.iconRes
                transitionNameRes = R.string.category_transition_image_salmon
            }
        }

        holder.image.setImageResource(iconRes)
        holder.image.transitionName = holder.image.context.getString(transitionNameRes)
        holder.mask.show(category != selectedCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListIndicatorViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_recipe_indicator, parent, false)
        return RecipeListIndicatorViewHolder(rootView)
    }

    override fun getItemCount() = Category.values().size

    inner class RecipeListIndicatorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.image
        val mask: CircleImageView = itemView.mask

        init {
            mask.setOnClickListener {
                selectedIndexCategory.categorySelected(categoryFromIndex(adapterPosition))
            }
        }

    }
}

interface SelectedIndexCallback {
    val currentIndexObservable: Observable<Category>
    fun categorySelected(category: Category)
}

fun indexFromCategory(category: Category): Int {
    return when(category) {
        Category.DESSERT -> 0
        Category.VEGETARIAN -> 1
        Category.VEGAN -> 2
        Category.CHICKEN -> 3
        Category.PORK -> 4
        Category.SALMON -> 5
    }
}

fun categoryFromIndex(position: Int): Category {
    return when(position) {
        0 -> Category.DESSERT
        1 -> Category.VEGETARIAN
        2 -> Category.VEGAN
        3 -> Category.CHICKEN
        4 -> Category.PORK
        5 -> Category.SALMON
        else -> throw RuntimeException("Couldn't find category for position $position")
    }
}