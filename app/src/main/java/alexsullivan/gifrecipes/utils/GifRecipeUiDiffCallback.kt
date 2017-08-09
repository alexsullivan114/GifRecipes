package alexsullivan.gifrecipes.utils

import alexsullivan.gifrecipes.GifRecipeUI
import android.support.v7.util.DiffUtil

class GifRecipeUiDiffCallback(val oldList: List<GifRecipeUI>, val newList: List<GifRecipeUI>): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id
    }

    override fun getOldListSize(): Int {
        return oldList.size;
    }

    override fun getNewListSize(): Int {
        return newList.size;
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition)
    }
}