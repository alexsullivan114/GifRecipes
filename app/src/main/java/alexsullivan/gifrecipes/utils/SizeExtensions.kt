package alexsullivan.gifrecipes.utils

import android.content.Context

fun Int.toDp(context: Context) = this.toFloat().toDp(context)

fun Float.toDp(context: Context): Float {
    val density = context.resources.displayMetrics.density
    return this/density
}

fun Float.toSp(context: Context): Float {
    val density = context.resources.displayMetrics.scaledDensity
    return this/density
}

fun Float.toPx(context: Context): Float {
    val density = context.resources.displayMetrics.scaledDensity
    return this * density
}