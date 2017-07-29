package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category

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