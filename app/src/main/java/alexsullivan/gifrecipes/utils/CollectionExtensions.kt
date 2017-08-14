package alexsullivan.gifrecipes.utils

fun <E> Collection<E>.nonEmptyLet(block: () -> Unit) {
    if (!isEmpty()) block()
}

fun <E> Collection<E>.emptyLet(block: () -> Unit) {
    if (isEmpty()) block()
}