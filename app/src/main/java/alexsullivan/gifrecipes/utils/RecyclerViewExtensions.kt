package alexsullivan.gifrecipes.utils

import android.support.v7.widget.RecyclerView

fun <T: RecyclerView.Adapter<*>> RecyclerView.castedAdapter() = adapter as T

fun <T: RecyclerView.ViewHolder> RecyclerView.castedViewHolderAtPosition(position: Int) = findViewHolderForAdapterPosition(position) as T