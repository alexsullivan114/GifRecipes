package alexsullivan.gifrecipes

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

enum class Category(@DrawableRes val iconRes: Int, @StringRes val transitionName: Int) {
    DESSERT(R.drawable.big_dessert, R.string.category_transition_image_dessert),
    VEGETARIAN(R.drawable.big_vegetarian, R.string.category_transition_image_vegetarian),
    VEGAN(R.drawable.big_vegan, R.string.category_transition_image_vegan),
    CHICKEN(R.drawable.big_chicken, R.string.category_transition_image_chicken),
    PORK(R.drawable.big_pork, R.string.category_transition_image_pork),
    SALMON(R.drawable.big_salmon, R.string.category_transition_image_salmon)
}