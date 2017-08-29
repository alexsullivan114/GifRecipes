package alexsullivan.gifrecipes.utils

public inline fun <R> String.ifPresent(block: (String) -> R?): R? {
    if(!isNullOrEmpty()) {
        return block(this)
    } else {
        return null
    }
}