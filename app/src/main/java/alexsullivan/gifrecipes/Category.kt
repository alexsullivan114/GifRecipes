package alexsullivan.gifrecipes

import android.support.annotation.DrawableRes

enum class Category(@DrawableRes val iconRes: Int) {
    DESSERT(R.drawable.big_dessert),
    VEGETARIAN(R.drawable.big_vegetarian),
    VEGAN(R.drawable.big_vegan),
    CHICKEN(R.drawable.big_chicken),
    PORK(R.drawable.big_pork),
    SALMON(R.drawable.big_salmon)
}