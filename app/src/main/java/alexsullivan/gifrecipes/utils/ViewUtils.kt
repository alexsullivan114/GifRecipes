package alexsullivan.gifrecipes.utils

import android.view.View

fun View.show(value: Boolean) {
    if (value) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}