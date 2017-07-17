package alexsullivan.gifrecipes

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

enum class Category(@DrawableRes val iconRes: Int, @StringRes val transitionName: Int, @StringRes val displayName: Int) {
    DESSERT(R.drawable.big_dessert, R.string.category_transition_image_dessert, R.string.dessert),
    VEGETARIAN(R.drawable.big_vegetarian, R.string.category_transition_image_vegetarian, R.string.vegetarian),
    VEGAN(R.drawable.big_vegan, R.string.category_transition_image_vegan, R.string.vegan),
    CHICKEN(R.drawable.big_chicken, R.string.category_transition_image_chicken, R.string.chicken),
    PORK(R.drawable.big_pork, R.string.category_transition_image_pork, R.string.pork),
    SALMON(R.drawable.big_salmon, R.string.category_transition_image_salmon, R.string.salmon)
}