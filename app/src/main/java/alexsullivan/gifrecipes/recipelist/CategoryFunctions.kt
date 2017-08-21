package alexsullivan.gifrecipes.recipelist

import alexsullivan.gifrecipes.Category

fun indexFromCategory(category: Category) = when (category) {
    Category.FAVORITE -> 0
    Category.DESSERT -> 1
    Category.BREAKFAST -> 2
    Category.DINNER -> 3
    Category.VEGETARIAN -> 4
    Category.VEGAN -> 5
    Category.CHICKEN -> 6
    Category.PORK -> 7
    Category.SALMON -> 8
}

fun categoryFromIndex(position: Int) = when (position) {
    indexFromCategory(Category.FAVORITE) -> Category.FAVORITE
    indexFromCategory(Category.DESSERT) -> Category.DESSERT
    indexFromCategory(Category.BREAKFAST) -> Category.BREAKFAST
    indexFromCategory(Category.DINNER) -> Category.DINNER
    indexFromCategory(Category.VEGETARIAN) -> Category.VEGETARIAN
    indexFromCategory(Category.VEGAN) -> Category.VEGAN
    indexFromCategory(Category.CHICKEN) -> Category.CHICKEN
    indexFromCategory(Category.PORK) -> Category.PORK
    indexFromCategory(Category.SALMON) -> Category.SALMON
    else -> throw RuntimeException("Cannot find category for postion $position")
}